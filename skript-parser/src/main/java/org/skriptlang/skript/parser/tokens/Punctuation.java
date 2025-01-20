package org.skriptlang.skript.parser.tokens;

public enum Punctuation {
	COMMA(','),
	SEMICOLON(';'),
	DOT('.'),
	OPEN_PARENTHESIS('('),
	CLOSE_PARENTHESIS(')'),
	OPEN_BRACKET('['),
	CLOSE_BRACKET(']'),
	OPEN_BRACE('{'),
	CLOSE_BRACE('}'),
	PIPE('|')
	;

	private final char character;

	Punctuation(char character) {
		this.character = character;
	}

	public char getCharacter() {
		return character;
	}

	@Override
	public String toString() {
		return Character.toString(character);
	}
}
