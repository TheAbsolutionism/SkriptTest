package org.skriptlang.skript.parser;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.skriptlang.skript.api.*;
import org.skriptlang.skript.api.nodes.*;
import org.skriptlang.skript.api.script.ScriptSource;
import org.skriptlang.skript.api.util.ResultWithDiagnostics;
import org.skriptlang.skript.api.util.ScriptDiagnostic;
import org.skriptlang.skript.parser.tokens.Token;
import org.skriptlang.skript.parser.tokens.TokenType;
import org.skriptlang.skript.parser.tokens.Tokenizer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The JVM implementation of the Skript Parser.
 * <p>
 * This is no-man's land. I wish you luck, brave soul.
 */
public final class SkriptParserImpl implements SkriptParser {
	private final LockAccess lockAccess;

	private final List<SyntaxNodeType<?>> nodeTypes = new LinkedList<>();

	/**
	 * The tokenized syntaxes that have been generated from the node types.
	 * This is lazily computed and cached on the first parse after lock.
	 */
	private @Nullable List<TokenizedSyntax> tokenizedSyntaxes = null;

	public SkriptParserImpl(@NotNull LockAccess lockAccess) {
		Preconditions.checkNotNull(lockAccess, "lockAccess cannot be null");
		Preconditions.checkArgument(!lockAccess.isLocked(), "lockAccess must not be locked on construction of parser");
		this.lockAccess = lockAccess;
	}

	@Override
	public void submitNode(@NotNull SyntaxNodeType<?> nodeType) {
		Preconditions.checkNotNull(nodeType, "nodeType cannot be null");

		if (lockAccess.isLocked()) {
			throw new IllegalStateException("The parser is locked and cannot accept new node types.");
		}

		synchronized (nodeTypes) {
			nodeTypes.add(nodeType);
			tokenizedSyntaxes = null;
		}
	}

	@Override
	public boolean isLocked() {
		return lockAccess.isLocked();
	}

	@Contract(pure = true)
	@Override
	public @NotNull @UnmodifiableView List<SyntaxNodeType<?>> getNodeTypes() {
		return Collections.unmodifiableList(nodeTypes);
	}

	@Override
	public @NotNull ResultWithDiagnostics<SyntaxNode> parse(@NotNull ScriptSource source) {
		if (!lockAccess.isLocked()) {
			throw new IllegalStateException("The parser is not locked and cannot parse.");
		}

		if (tokenizedSyntaxes == null) {
			var computeResult = computeTokenizedSyntaxes();
			if (!computeResult.isSuccess()) {
				return ResultWithDiagnostics.failure(computeResult.getDiagnostics());
			}
		}

		var tokenizeResult = Tokenizer.tokenize(source);

		// will be joint between token diagnostics and parse diagnostics
		var diagnostics = new LinkedList<>(tokenizeResult.getDiagnostics());

		if (!tokenizeResult.isSuccess()) {
			diagnostics.add(ScriptDiagnostic.error(source, "Script could not be tokenized"));
			return ResultWithDiagnostics.failure(diagnostics);
		}

		var tokens = tokenizeResult.get();

		var fileNode = parseSection(source, diagnostics, tokens, 0, 0, 0);

		if (fileNode == null) {
			diagnostics.add(ScriptDiagnostic.error(source, "Script could not be parsed"));
			return ResultWithDiagnostics.failure(diagnostics);
		}

		return ResultWithDiagnostics.success(fileNode);
	}

	/**
	 * Parses a section.
	 * @param source The source of the script.
	 * @param diagnostics The diagnostics list to add to.
	 * @param tokens The tokens to parse.
	 * @param start The index to start parsing at.
	 * @param prevIndent The previous indent level (in spaces or tabs).
	 * @param depth The depth of the section. 0 is root. This is used with prevIndent to validate indentation increased properly.
	 * @return The parsed section, or null if failed.
	 */
	private @Nullable SectionNode parseSection(
		ScriptSource source,
		List<ScriptDiagnostic> diagnostics,
		List<Token> tokens,
		int start,
		int prevIndent,
		int depth
	) {
		// at head, we are just after a colon

		int index = start;
		int currentIndent = 0;

		// if this section isn't the root of the file, it must start with certain whitespace rules
		if (depth != 0) {
			Token mustBeWhitespace = tokens.get(index);

			// validate there is whitespace
			if (mustBeWhitespace.type() != TokenType.WHITESPACE) {
				diagnostics.add(ScriptDiagnostic.error(source, "Expected whitespace", mustBeWhitespace.start()));
				return null;
			}

			// validate the whitespace is newline
			if (!mustBeWhitespace.asString().contains("\n")) {
				diagnostics.add(ScriptDiagnostic.error(source, "Expected newline", mustBeWhitespace.start()));
				return null;
			}

			// find whitespace length
			currentIndent = mustBeWhitespace.asString().length() - mustBeWhitespace.asString().lastIndexOf('\n') - 1;

			// validate the whitespace follows the inferred indentation in this stack
			int prevInterval = depth > 1 ? prevIndent / (depth - 1) : prevIndent;
			if (prevInterval != 0 && currentIndent % prevInterval != 0) {
				diagnostics.add(ScriptDiagnostic.error(source, "Indentation must be a multiple of " + prevInterval, mustBeWhitespace.start()));
				return null;
			}

			index++;
		}

		List<StatementNode> statements = new LinkedList<>();

		Token whitespace;
		do {
			StatementNode next = parseStatement(source, diagnostics, tokens, index, depth == 0 ? StructureNodeType.class : EffectNodeType.class);
			if (next == null) {
				diagnostics.add(ScriptDiagnostic.info(source, "Fail occurred in section depth " + depth, tokens.get(index).start()));
				return null;
			}
			index += next.length();
			whitespace = tokens.get(index);
			if (whitespace.type() != TokenType.WHITESPACE || !whitespace.asString().contains("\n")) {
				diagnostics.add(ScriptDiagnostic.error(source, "Expected newline after effect", tokens.get(index).start()));
				return null;
			}
			index++;
			statements.add(next);
		} while (whitespace.asString().substring(whitespace.asString().lastIndexOf('\n') + 1).length() == currentIndent);

		return new SectionNode(statements, index - start);
	}

	/**
	 * Parses an effect. Only effects which consume an entire line will be allowed to succeed.
	 * @param source The source of the script.
	 * @param diagnostics The diagnostics list to add to.
	 * @param tokens The tokens to parse.
	 * @param start The index to start parsing at.
	 * @param superType The super type to bound candidates to.
	 * @return The parsed effect, or null if failed.
	 */
	private @Nullable StatementNode parseStatement(
		ScriptSource source,
		List<ScriptDiagnostic> diagnostics,
		List<Token> tokens,
		int start,
		Class<?> superType
	) {
		int index = start;
		int end = tokens.stream()
			.map(token -> token.type() == TokenType.WHITESPACE && token.asString().contains("\n"))
			.toList()
			.indexOf(true);

		// edge case: this is the last line of the script, and there is no newline. therefore, the effect goes to the end.
		if (end == -1) end = tokens.size();

		List<Token> effectTokens = tokens.subList(index, end);
		// This edge case shouldn't even occur
		// because the tokenizer does not output duplicate newline-containing whitespace tokens
		if (effectTokens.isEmpty()) {
			diagnostics.add(ScriptDiagnostic.error(source, "Expected effect", tokens.get(index).start()));
			return null;
		}

		// computeTokenizedSyntaxes ensures tokenizedSyntaxes is not null
		// that being said, the only way to get into this method is ultimately via parse,
		// but break-in access to the method may fail here.
		assert tokenizedSyntaxes != null;
		var candidates = tokenizedSyntaxes.stream()
			.filter(tokenizedSyntax -> superType.isInstance(tokenizedSyntax.nodeType()))
			.filter(tokenizedSyntax -> tokenizedSyntax.canMatch(effectTokens))
			.toList();

		if (candidates.isEmpty()) {
			diagnostics.add(ScriptDiagnostic.error(source, "No statement matched", tokens.get(index).start()));
			return null;
		}

		return null;
	}


	/**
	 * Lazily computes the tokenized syntaxes from the node types.
	 */
	private synchronized ResultWithDiagnostics<Object> computeTokenizedSyntaxes() {
		if (tokenizedSyntaxes != null) return ResultWithDiagnostics.success(new Object());
		if (!isLocked()) throw new IllegalStateException("Cannot compute tokenized syntaxes because the parser is not locked");

		var list = new LinkedList<TokenizedSyntax>();

		for (var nodeType : nodeTypes) {

			var syntaxes = nodeType.getSyntaxes();
			if (syntaxes == null) continue;
			for (var syntax : syntaxes) {

				var source = new ScriptSource() {
					@Override
					public @NotNull String name() {
						return nodeType.getClass().getSimpleName();
					}

					@Override
					public String content() {
						return syntax;
					}
				};

				var tokens = Tokenizer.tokenizeSyntax(source, nodeType);

				if (!tokens.isSuccess()) {
					var diagnostics = new LinkedList<>(tokens.getDiagnostics());
					diagnostics.add(ScriptDiagnostic.error(source, "Syntax from " + nodeType + " could not be tokenized"));
					return ResultWithDiagnostics.failure(diagnostics);
				}

				list.addAll(tokens.get());
			}
		}

		tokenizedSyntaxes = Collections.unmodifiableList(list);
		return ResultWithDiagnostics.success(new Object());
	}
}
