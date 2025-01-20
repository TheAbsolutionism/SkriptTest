package org.skriptlang.skript.parser.tokens;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.api.nodes.SyntaxNodeType;
import org.skriptlang.skript.api.script.ScriptSource;
import org.skriptlang.skript.api.util.ResultWithDiagnostics;
import org.skriptlang.skript.api.util.ScriptDiagnostic;
import org.skriptlang.skript.parser.TokenizedSyntax;
import org.skriptlang.skript.parser.pattern.*;

import java.util.*;

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
	public static ResultWithDiagnostics<List<TokenizedSyntax>> tokenizeSyntax(@NotNull ScriptSource source, @NotNull SyntaxNodeType nodeType) {
		// syntax uses basic tokenization as a base
		ResultWithDiagnostics<List<Token>> normalResult = tokenize(source);
		if (!normalResult.isSuccess()) {
			return ResultWithDiagnostics.failure(normalResult.getDiagnostics());
		}

		var diagnostics = new LinkedList<>(normalResult.getDiagnostics());

		List<Token> tokens = normalResult.get();
		List<PatternElement> patternElements;

		try {
			patternElements = toPatternElements(tokens);
		} catch (Exception e) {
			diagnostics.add(ScriptDiagnostic.error(source, e));
			return ResultWithDiagnostics.failure(diagnostics);
		}

		List<TokenizedSyntax> tokenizedSyntaxes = new LinkedList<>();
		// initial empty syntax for elements to spin off of
		tokenizedSyntaxes.add(new TokenizedSyntax(nodeType, Collections.emptyList()));
		for (PatternElement patternElement : patternElements) {
			tokenizedSyntaxes = patternElement.createTokenizedSyntaxes(nodeType, tokenizedSyntaxes);
		}

		try {
			return ResultWithDiagnostics.success(tokenizedSyntaxes, diagnostics);
		} catch (Exception e) {
			diagnostics.add(ScriptDiagnostic.error(source, e));
			return ResultWithDiagnostics.failure(diagnostics);
		}
	}

	private static List<PatternElement> toPatternElements(List<Token> tokens) {
		List<PatternElement> elements = new LinkedList<>();

		int index = -1;
		int lastClosing = -1;
		while (index < tokens.size()) {
			index = findNextInstructingElement(tokens, index + 1);
			if (index == -1) {
				if (lastClosing + 1 != tokens.size()) {
					elements.add(new TokensPatternElement(tokens.subList(lastClosing + 1, tokens.size())));
				}
				break;
			}
			Token token = tokens.get(index);

			if (lastClosing + 1 != index && token.asPunctuation() != Punctuation.PIPE) {
				elements.add(new TokensPatternElement(tokens.subList(lastClosing + 1, index)));
			}

			if (token.type() == TokenType.PUNCTUATION) {
				switch (Objects.requireNonNull(token.asPunctuation())) {
					case OPEN_PARENTHESIS -> {
						int closeParenthesis = findClosingPunctuation(tokens, index);
						if (closeParenthesis == -1) {
							throw new IllegalArgumentException("No closing parenthesis found");
						}

						List<Token> innerTokens = tokens.subList(index + 1, closeParenthesis);
						List<PatternElement> innerElements = toPatternElements(innerTokens);
						index = closeParenthesis;
						lastClosing = closeParenthesis;
						elements.add(new GroupPatternElement(innerElements, false));
					}
					case OPEN_BRACKET -> {
						int closeBracket = findClosingPunctuation(tokens, index);
						if (closeBracket == -1) {
							throw new IllegalArgumentException("No closing bracket found");
						}

						List<Token> innerTokens = tokens.subList(index + 1, closeBracket);
						List<PatternElement> innerElements = toPatternElements(innerTokens);
						index = closeBracket;
						lastClosing = closeBracket;
						elements.add(new GroupPatternElement(innerElements, true));
					}
					case Punctuation.PIPE -> {
						List<Token> leftTokens = tokens.subList(0, index);
						List<PatternElement> leftElements = toPatternElements(leftTokens);

						List<Token> rightTokens = tokens.subList(index + 1, tokens.size());
						List<PatternElement> rightElements = toPatternElements(rightTokens);

						elements.add(new ChoicePatternElement(leftElements, rightElements));
						// return because right consumes the rest of the tokens
						return elements;
					}
					default -> throw new IllegalArgumentException("Unexpected punctuation token");
				}
			} else if (token.type() == TokenType.OPERATOR) {
				switch (Objects.requireNonNull(token.asOperator())) {
					case LESS_THAN -> {
						int closeArrow = findNext(tokens, index + 1, Operator.GREATER_THAN);
						if (closeArrow == -1) {
							throw new IllegalArgumentException("No closing arrow found");
						}

						List<Token> innerTokens = tokens.subList(index + 1, closeArrow + 1);
						index = closeArrow;
						lastClosing = closeArrow;
						elements.add(tokenizeSyntaxPatternElement(innerTokens));
					}
					default -> throw new IllegalArgumentException("Unexpected operator token");
				}
			} else {
				throw new IllegalArgumentException("Unexpected token");
			}
		};

		return elements.stream().toList();
	}

	private static @NotNull SyntaxPatternElement tokenizeSyntaxPatternElement(@NotNull List<Token> tokens) {
		Token syntaxTypeToken = tokens.getFirst();
		if (syntaxTypeToken.type() != TokenType.IDENTIFIER) {
			throw new IllegalArgumentException("Syntax type must be an identifier");
		}

		Token closeArrowOrDelimiter = tokens.get(1);
		if (closeArrowOrDelimiter.asOperator() == Operator.DOUBLE_COLON) {
			// inputs or return type are present

			List<SyntaxPatternElement.Input> inputs = new LinkedList<>();

			int index = 2;
			while (index < tokens.size()) {
				Token token = tokens.get(index);
				if (token.asOperator() == Operator.ARROW || token.asOperator() == Operator.GREATER_THAN) {
					break;
				}

				if (token.type() != TokenType.IDENTIFIER) {
					throw new IllegalArgumentException("Expected identifier");
				}

				String inputName = token.asString();
				String inputType = null;

				Token nextToken = tokens.get(index + 1);
				if (nextToken.asOperator() == Operator.COLON) {
					Token inputTypeToken = tokens.get(index + 2);

					if (inputTypeToken.type() != TokenType.IDENTIFIER) {
						throw new IllegalArgumentException("Expected identifier");
					}

					inputType = inputTypeToken.asString();
					index += 2;
					nextToken = tokens.get(index + 1);
				}

				inputs.add(new SyntaxPatternElement.Input(inputName, inputType));

				if (nextToken.asPunctuation() == Punctuation.COMMA) {
					index++;
				} else {
					if (nextToken.asOperator() == Operator.ARROW) index++;
					break;
				}
			}

			Token arrow = tokens.get(index);
			if (arrow.asOperator() == Operator.GREATER_THAN) {
				return new SyntaxPatternElement(syntaxTypeToken.asString(), inputs, null);
			}

			if (arrow.asOperator() != Operator.ARROW) {
				throw new IllegalArgumentException("Expected arrow");
			}

			Token returnTypeToken = tokens.get(index + 1);
			if (returnTypeToken.type() != TokenType.IDENTIFIER) {
				throw new IllegalArgumentException("Expected identifier");
			}

			if (tokens.get(index + 2).asOperator() != Operator.GREATER_THAN) {
				throw new IllegalArgumentException("Expected closing arrow");
			}

			return new SyntaxPatternElement(syntaxTypeToken.asString(), inputs, returnTypeToken.asString());

		} else {
			if (closeArrowOrDelimiter.asOperator() != Operator.GREATER_THAN) {
				throw new IllegalArgumentException("Expected closing arrow or double colon");
			}
		}

		return new SyntaxPatternElement(syntaxTypeToken.asString(), List.of(), null);
	}

	/**
	 * Finds the next occurrence of a punctuation token.
	 * @param tokens The tokens to search through.
	 * @param start The index to start searching from.
	 * @param punctuation The punctuation to search for.
	 * @return The index of the next occurrence of the punctuation, or -1 if none is found.
	 */
	private static int findNext(List<Token> tokens, int start, Punctuation punctuation) {
		for (int i = start; i < tokens.size(); i++) {
			Token token = tokens.get(i);
			if (token.type() == TokenType.PUNCTUATION && token.asPunctuation() == punctuation) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Finds the next occurrence of an operator token.
	 * @param tokens The tokens to search through.
	 * @param start The index to start searching from.
	 * @param operator The operator to search for.
	 * @return The index of the next occurrence of the operator, or -1 if none is found.
	 */
	private static int findNext(List<Token> tokens, int start, Operator operator) {
		for (int i = start; i < tokens.size(); i++) {
			Token token = tokens.get(i);
			if (token.type() == TokenType.OPERATOR && token.asOperator() == operator) {
				return i;
			}
		}
		return -1;
	}

	private static int findClosingPunctuation(List<Token> tokens, int start) {
		Punctuation opening = tokens.get(start).asPunctuation();
		if (opening == null) throw new IllegalArgumentException("Token at start index is not a punctuation token");

		Punctuation closing = switch (opening) {
			case OPEN_PARENTHESIS -> Punctuation.CLOSE_PARENTHESIS;
			case OPEN_BRACKET -> Punctuation.CLOSE_BRACKET;
			case OPEN_BRACE -> Punctuation.CLOSE_BRACE;
			default -> throw new IllegalArgumentException("Punctuation token is not an opening punctuation");
		};

		Stack<Punctuation> stack = new Stack<>();

		for (int i = start; i < tokens.size(); i++) {
			Token token = tokens.get(i);
			if (token.type() == TokenType.PUNCTUATION) {
				Punctuation tokenPunctuation = token.asPunctuation();
				if (tokenPunctuation == opening) {
					stack.push(tokenPunctuation);
				} else if (tokenPunctuation == closing) {
					stack.pop();
					if (stack.isEmpty()) {
						return i;
					}
				}
			}
		}

		return -1;
	}

	/**
	 * Finds the next element that instructs a different pattern to be created.
	 * @param tokens The tokens to search through.
	 * @param start The index to start searching from.
	 * @return The index of the next instructing element, or -1 if none is found.
	 */
	@Contract(pure = true)
	private static int findNextInstructingElement(List<Token> tokens, int start) { // TODO: name this
		for (int i = start; i < tokens.size(); i++) {
			Token token = tokens.get(i);
			if (token.type() == TokenType.PUNCTUATION) {
				switch (Objects.requireNonNull(token.asPunctuation())) {
					case OPEN_PARENTHESIS, OPEN_BRACKET, PIPE -> {
						return i;
					}
					default -> {
						return -1;
					}
				}
			} else if (token.type() == TokenType.OPERATOR) {
				switch (Objects.requireNonNull(token.asOperator())) {
					case LESS_THAN -> {
						return i;
					}
					default -> {
						return -1;
					}
				}
			}
		}
		return -1;
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
					&& !token.asString().contains("\n"))
		);

		return ResultWithDiagnostics.success(Collections.unmodifiableList(tokens), diagnostics);
	}

	private static @Nullable Token nextToken(ScriptSource source, List<ScriptDiagnostic> diagnostics, @NotNull String content, int index) {
		if (index >= content.length()) return null;

		char first = content.charAt(index);

		for (Keyword keyword : Keyword.values()) {
			if (content.startsWith(keyword.getKeyword(), index)) {
				return new Token(TokenType.KEYWORD, keyword, index, keyword.getKeyword().length(), null);
			}
		}

		for (Operator operator : Operator.values()) {
			if (content.startsWith(operator.getSymbol(), index)) {
				return new Token(TokenType.OPERATOR, operator, index, operator.getSymbol().length(), null);
			}
		}

		for (Punctuation punctuation : Punctuation.values()) {
			if (content.charAt(index) == punctuation.getCharacter()) {
				return new Token(TokenType.PUNCTUATION, punctuation, index, 1, null);
			}
		}

		if (first == '#') {
			int end = index + 1;
			while (end < content.length() && content.charAt(end) != '\n') {
				end++;
			}
			return new Token(TokenType.COMMENT, content.substring(index, end), index, end - index, null);
		}

		if (Character.isWhitespace(first)) {
			int end = index + 1;
			while (end < content.length() && Character.isWhitespace(content.charAt(end))) {
				end++;
			}
			return new Token(TokenType.WHITESPACE, content.substring(index, end), index, end - index, null);
		}

		if (Character.isLetter(first)) {
			int end = index + 1;
			while (end < content.length() && Character.isLetterOrDigit(content.charAt(end))) {
				end++;
			}
			return new Token(TokenType.IDENTIFIER, content.substring(index, end), index, end - index, null);
		}

		if (Character.isDigit(first)) {
			int end = index + 1;
			while (end < content.length() && (Character.isDigit(content.charAt(end)) || content.charAt(end) == '.')) {
				end++;
			}
			return new Token(TokenType.NUMBER, content.substring(index, end), index, end - index, null);
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
				return new Token(TokenType.STRING, content.substring(index, end + 1), index, end - index + 1, children);
			}
		}

		return null;
	}
}
