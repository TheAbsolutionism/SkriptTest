package org.skriptlang.skript.parser.tokens;

public enum Keyword {
	IF("if"),
	ELSE("else"),
	ON("on"),

	;

	private final String keyword;

	Keyword(String keyword) {
		this.keyword = keyword;
	}

	public String getKeyword() {
		return keyword;
	}

	@Override
	public String toString() {
		return getKeyword();
	}
}
