package org.skriptlang.skript.parser.tokens;

import org.skriptlang.skript.parser.pattern.SyntaxPatternElement;

import java.util.List;

public enum TokenType {
	/**
	 * 	Keywords are words reserved as having special meaning in the language.
	 * 	Their value will always be {@link Keyword}.
 	 */
	KEYWORD,

	/**
	 * Identifiers are general words used throughout the language. For example, "player" or "make".
	 * Their value will always be {@link String}.
	 */
	IDENTIFIER,

	/**
	 * Strings are sequences of characters enclosed in double quotes. For example, "Hello, world!"
	 * Their value will always be {@link String}.
	 * <p>
	 * Additionally, strings may contain children.
	 * The children have their start and end indices as absolute values, so be careful.
	 * You can relativize them using the parent's start index.
	 */
	STRING,

	/**
	 * Numbers are sequences of digits. For example, 123
	 * Their value will always be {@link String} (number is evaluated later).
	 */
	NUMBER,

	/**
	 * Operators are symbols. For example, + or -.
	 * Their value will always be {@link Operator}.
	 */
	OPERATOR,

	/**
	 * Punctuation are symbols that are not operators. For example, commas or semicolons.
	 * Their value will always be {@link Punctuation}.
	 */
	PUNCTUATION,

	/**
	 * Comments are text that are not executed. For example, # This is a comment.
	 * Their value will always be {@link String}.
	 */
	COMMENT,

	/**
	 * Whitespace is any sequence of spaces, tabs, or newlines.
	 * However, whitespace tokens that are not necessary for parsing are usually discarded at the end of tokenization.
	 * Their value will always be {@link String}.
	 */
	WHITESPACE,

	/**
	 * A token intended specifically for tokenized syntaxes that may contain any number of tokens.
	 * The value is a {@link SyntaxPatternElement} representing the syntax.
	 */
	SYNTAX,

}
