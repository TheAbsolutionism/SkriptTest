package org.skriptlang.skript.parser.tokens;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A token represents a unit in a script before considering its execution meaning.
 * This makes it easier to work with when matching against patterns.
 * @param type The type of token.
 * @param value The value of the token.
 *              This could be {@link Keyword}, {@link Punctuation}, {@link Operator}, or {@link String}
 *              depending on the type.
 * @param start The start index of the token in the script.
 * @param children The children tokens of this token.
 *                 This is only used for special cases, as tokens are usually flat.
 *                 At the moment, it is intended for string literals which contain template expressions.
 */
public record Token(
	@NotNull TokenType type,
	@NotNull Object value,
	int start,
	int length,
	@Nullable List<Token> children
) {

	/**
	 * Gets the end index of the token in the script.
	 */
	public int end() {
		return start() + length();
	}

	/**
	 * Gets the keyword if the token is a keyword.
	 */
	public @Nullable Keyword asKeyword() {
		return type == TokenType.KEYWORD ? (Keyword) value : null;
	}

	/**
	 * Gets the punctuation if the token is a punctuation.
	 */
	public @Nullable Punctuation asPunctuation() {
		return type == TokenType.PUNCTUATION ? (Punctuation) value : null;
	}

	/**
	 * Gets the operator if the token is an operator.
	 */
	public @Nullable Operator asOperator() {
		return type == TokenType.OPERATOR ? (Operator) value : null;
	}

	public @NotNull String asString() {
		return value().toString();
	}

	public boolean matches(@NotNull Token other) {
		return type == other.type() && value.equals(other.value());
	}

}