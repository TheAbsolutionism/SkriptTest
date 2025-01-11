package org.skriptlang.skript.parser;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.api.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * All tokenization logic is done here.
 */
@ApiStatus.Internal
public class Tokenizer {
	private Tokenizer() {
		// no instance
	}

	/**
	 * Tokenizes a syntax into all possible lists of tokens.
	 *
	 * @param source A source that isn't actually a script,
	 *               but has information pertaining to a syntax node for diagnostic convenience.
	 * @return The tokenized syntax.
	 */
	public static ResultWithDiagnostics<List<TokenizedSyntax>> tokenizeSyntax(@NotNull ScriptSource source, @NotNull SkriptNodeType nodeType) {
		// syntax uses basic tokenization as a base
		ResultWithDiagnostics<List<Token>> normalResult = tokenize(source);
		if (!normalResult.isSuccess()) {
			return ResultWithDiagnostics.failure(normalResult.getDiagnostics());
		}

		var diagnostics = new LinkedList<>(normalResult.getDiagnostics());

		List<Token> tokens = normalResult.get();

		try {
			return ResultWithDiagnostics.success(nextSyntaxElement(source, tokens, nodeType, diagnostics), diagnostics);
		} catch (Exception e) {
			diagnostics.add(ScriptDiagnostic.error(source, e));
			return ResultWithDiagnostics.failure(diagnostics);
		}
	}

	private @NotNull List<TokenizedSyntax> nextSyntaxElement(
		@NotNull ScriptSource source,
		@NotNull List<Token> tokens,
		@NotNull SkriptNodeType nodeType,
		@NotNull List<ScriptDiagnostic> diagnostics
	) {

	}


	public static ResultWithDiagnostics<List<Token>> tokenize(@NotNull ScriptSource source) {
		String content = source.content();

		var tokens = new LinkedList<Token>();
		var diagnostics = new LinkedList<ScriptDiagnostic>();

		// switch that silences duplicate tokenization issues until a non-null token is found
		// (because the first token failing is probably the issue)
		boolean lastTokenWasNull = false;

		int index = 0;
		while (index < content.length()) {
			var token = nextToken(source, diagnostics, content, index);
			if (token == null) {
				if (!lastTokenWasNull)
					diagnostics.add(ScriptDiagnostic.error(source, "Unexpected character: " + content.charAt(index), index));
				lastTokenWasNull = true;
				index++;
			} else {
				lastTokenWasNull = false;
				tokens.add(token);
				index = token.end();
			}
		}

		// remove comments and whitespace that aren't newlines
		tokens.removeIf(token ->
			token.type() == TokenType.COMMENT ||
				(token.type() == TokenType.WHITESPACE
					&& !token.match().contains("\n"))
		);

		return ResultWithDiagnostics.success(Collections.unmodifiableList(tokens), diagnostics);
	}

	private static @Nullable Token nextToken(ScriptSource source, List<ScriptDiagnostic> diagnostics, @NotNull String content, int index) {
		if (index >= content.length()) return null;

		char first = content.charAt(index);

		for (String keyword : TokenType.KEYWORDS) {
			if (content.startsWith(keyword, index)) {
				return new Token(TokenType.KEYWORD, keyword, index, null);
			}
		}

		for (String symbol : TokenType.OPERATORS) {
			if (content.startsWith(symbol, index)) {
				return new Token(TokenType.OPERATOR, symbol, index, null);
			}
		}

		for (String punctuation : TokenType.PUNCTUATIONS) {
			if (content.startsWith(punctuation, index)) {
				return new Token(TokenType.PUNCTUATION, punctuation, index, null);
			}
		}

		if (first == '#') {
			int end = index + 1;
			while (end < content.length() && content.charAt(end) != '\n') {
				end++;
			}
			return new Token(TokenType.COMMENT, content.substring(index, end), index, null);
		}

		if (Character.isWhitespace(first)) {
			int end = index + 1;
			while (end < content.length() && Character.isWhitespace(content.charAt(end))) {
				end++;
			}
			return new Token(TokenType.WHITESPACE, content.substring(index, end), index, null);
		}

		if (Character.isLetter(first)) {
			int end = index + 1;
			while (end < content.length() && Character.isLetterOrDigit(content.charAt(end))) {
				end++;
			}
			return new Token(TokenType.IDENTIFIER, content.substring(index, end), index, null);
		}

		if (Character.isDigit(first)) {
			int end = index + 1;
			while (end < content.length() && (Character.isDigit(content.charAt(end)) || content.charAt(end) == '.')) {
				end++;
			}
			return new Token(TokenType.NUMBER, content.substring(index, end), index, null);
		}

		if (first == '"') {
			int end = index + 1;
			boolean escaped = false;
			List<Token> children = new LinkedList<>();

			while (end < content.length()) {
				char current = content.charAt(end);

				// same flag as in tokenize
				boolean lastTokenWasNull = false;

				if (escaped) {
					escaped = false;
				} else if (current == '\\') {
					escaped = true;
				} else if (current == '"') {
					break;
				} else if (current == '%') {
					int templateStart = end;
					end++;
					while (end < content.length() && content.charAt(end) != '%') {
						Token childToken = nextToken(source, diagnostics, content, end);
						if (childToken != null) {
							lastTokenWasNull = false;
							children.add(childToken);
							end = childToken.end();
						} else {
							if (!lastTokenWasNull)
								diagnostics.add(ScriptDiagnostic.error(
									source,
									"Unexpected character in template: " + content.charAt(end),
									templateStart
								));
							lastTokenWasNull = true;
							end++;
						}
					}
					if (end < content.length() && content.charAt(end) == '%') {
						end++;
					} else {
						break;
					}
					continue;
				}
				end++;
			}

			if (end < content.length() && content.charAt(end) == '"') {
				end++;
				return new Token(TokenType.STRING, content.substring(index, end + 1), index, children);
			}
		}

		return null;
	}
}
