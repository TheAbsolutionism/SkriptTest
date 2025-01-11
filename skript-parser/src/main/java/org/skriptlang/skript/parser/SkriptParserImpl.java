package org.skriptlang.skript.parser;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.skriptlang.skript.api.*;

import java.util.ArrayList;
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

	private final List<SkriptNodeType> nodeTypes = new LinkedList<>();

	/**
	 * The tokenized syntaxes that have been generated from the node types.
	 * This is lazily computed and cached on the first parse after lock.
	 */
	private @Nullable List<TokenizedSyntax> tokenizedSyntaxes = null;

	public SkriptParserImpl(@NotNull LockAccess lockAccess) {
		Preconditions.checkNotNull(lockAccess, "lockAccess cannot be null");
		this.lockAccess = lockAccess;
	}

	@Override
	public void submitNode(@NotNull SkriptNodeType nodeType) {
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
	public @NotNull @UnmodifiableView List<SkriptNodeType> getNodeTypes() {
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

		var fileNode = parseSection(source, diagnostics, tokens, 0, 0);

		if (fileNode == null) {
			diagnostics.add(ScriptDiagnostic.error(source, "Script could not be parsed"));
			return ResultWithDiagnostics.failure(diagnostics);
		}

		return ResultWithDiagnostics.success(fileNode);
	}

	/**
	 * Parses a section.
	 * @param source The source of the script
	 * @param diagnostics The diagnostics list to add to
	 * @param tokens The tokens to parse
	 * @param start The index to start parsing at
	 * @param prevIndent The previous indent level
	 * @return The parsed section
	 */
	private @Nullable SectionNode parseSection(
		ScriptSource source,
		List<ScriptDiagnostic> diagnostics,
		List<Token> tokens,
		int start,
		int prevIndent
	) {
		// at head, we are just after a colon

		var children = new LinkedList<SyntaxNode>();
		var index = start;

		Token firstNewline = tokens.get(index);
		if (firstNewline.type() != TokenType.WHITESPACE) {
			diagnostics.add(ScriptDiagnostic.error(source, "Expected newline at start of section", firstNewline.start()));
			return null;
		}

		var indent = firstNewline.match().substring(firstNewline.match().lastIndexOf('\n')).length();
		// TODO: Validate indents follow a consistent pattern
		if (indent <= prevIndent) {
			diagnostics.add(ScriptDiagnostic.error(source, "Expected indent at start of section", firstNewline.start()));
			return null;
		}

		while (index < tokens.size()) {
			var next = parseNode(source, diagnostics, tokens, index, prevIndent, children);

			if (next == null) {
				break;
			}
			index += next.length();
		}
	}

	private @Nullable SyntaxNode parseNode(
		@NotNull ScriptSource source,
		@NotNull List<ScriptDiagnostic> diagnostics,
		@NotNull List<Token> tokens,
		int start,
		int prevIndent,
		@NotNull List<SyntaxNode> children
	) {

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

				var tokens = Tokenizer.tokenizeSyntax(source);

				if (!tokens.isSuccess()) {
					var diagnostics = new LinkedList<>(tokens.getDiagnostics());
					diagnostics.add(ScriptDiagnostic.error(source, "Syntax from " + nodeType + " could not be tokenized"));
					return ResultWithDiagnostics.failure(diagnostics);
				}

				list.add(new TokenizedSyntax(nodeType, tokens.get()));
			}

		}

		tokenizedSyntaxes = Collections.unmodifiableList(list);
		return ResultWithDiagnostics.success(new Object());
	}
}
