package org.skriptlang.skript.parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A token represents a unit in a script before considering its execution meaning.
 * This makes it easier to work with when matching against patterns.
 * @param type The type of token.
 * @param match The text that the token matched.
 * @param start The start index of the token in the script.
 * @param children The children tokens of this token.
 *                 This is only used for special cases, as tokens are usually flat.
 *                 At the moment, it is intended for string literals which contain template expressions.
 */
public record Token(
	@NotNull TokenType type,
	@NotNull String match,
	int start,
	@Nullable List<Token> children
) {

	/**
	 * Gets the length of the token.
	 */
	public int length() {
		return match.length();
	}

	/**
	 * Gets the end index of the token in the script.
	 */
	public int end() {
		return start + length();
	}

	/**
	 * Returns whether this token is punctuation and matches the given character.
	 */
	public boolean isPunctuation(char c) {
		return type == TokenType.PUNCTUATION && match.length() == 1 && match.charAt(0) == c;
	}

	/**
	 * Returns whether this token is an operator and matches the given string.
	 */
	public boolean isOperator(String s) {
		return type == TokenType.OPERATOR && match.equals(s);
	}

}