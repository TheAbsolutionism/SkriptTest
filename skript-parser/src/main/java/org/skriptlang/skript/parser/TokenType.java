package org.skriptlang.skript.parser;

import java.util.List;

public enum TokenType {
	/**
	 * 	Keywords are words reserved as having special meaning in the language
	 * 	At the moment, the keywords are:
	 * 	- if
	 * 	- else
	 * 	- on
 	 */
	KEYWORD,

	/**
	 * Identifiers are general words used throughout the language. For example, "player" or "make"
	 */
	IDENTIFIER,

	/**
	 * Strings are sequences of characters enclosed in double quotes. For example, "Hello, world!"
	 */
	STRING,

	/**
	 * Numbers are sequences of digits. For example, 123
	 */
	NUMBER,

	/**
	 * Operators are symbols. For example, + or -
	 */
	OPERATOR,

	/**
	 * Punctuation are symbols that are not operators. For example, commas or semicolons
	 */
	PUNCTUATION,

	/**
	 * Comments are text that are not executed. For example, # This is a comment
	 */
	COMMENT,

	/**
	 * Whitespace is any sequence of spaces, tabs, or newlines.
	 * However, whitespace tokens that are not necessary for parsing are usually discarded at the end of tokenization.
	 */
	WHITESPACE,

	/**
	 * A token intended specifically for tokenized syntaxes that may contain any number of tokens.
	 * The value is a registered type (either singular or plural name) which will hint the parser to look for syntaxes capable of returning that type.
	 */
	SYNTAX;

	public static final List<String> KEYWORDS = List.of(
		"if",
		"else",
		"on"
	);

	public static final List<String> PUNCTUATIONS = List.of(
		",",
		";",
		".",
		"(",
		")",
		"[",
		"]",
		"{",
		"}",
		"|"
	);

	public static final List<String> OPERATORS = List.of(
		"+",
		"-",
		"*",
		"/",
		"%",
		"==",
		"!=",
		"<",
		">",
		"<=",
		">=",
		"&&",
		"||",
		"!",
		"++",
		"--",
		"=",
		"+=",
		"-=",
		"*=",
		"/=",
		"%=",
		"~",
		"&",
		"|",
		"^",
		"<<",
		">>",
		">>>",
		"~=",
		"&=",
		"|=",
		"^=",
		"<<=",
		">>=",
		">>>=",
		"?",
		":",
		"->",
		"::",
		"::="
	);


}
