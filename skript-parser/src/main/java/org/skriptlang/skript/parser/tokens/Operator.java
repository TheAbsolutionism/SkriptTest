package org.skriptlang.skript.parser.tokens;

public enum Operator {
	ARROW("->"),
	PLUS("+"),
	MINUS("-"),
	MULTIPLY("*"),
	DIVIDE("/"),
	MODULO("%"),
	POWER("^"),
	DOUBLE_ARROW("=>"),
	LESS_THAN("<"),
	GREATER_THAN(">"),
	LESS_THAN_OR_EQUAL("<="),
	GREATER_THAN_OR_EQUAL(">="),
	EQUALS("=="),
	NOT_EQUALS("!="),
	AND("&&"),
	OR("||"),
	NOT("!"),

	// Not necessarily operators in all use cases.
	DOUBLE_COLON("::"),
	COLON(":"),

	;

	private final String symbol;

	Operator(String symbol) {
		this.symbol = symbol;
	}

	public String getSymbol() {
		return symbol;
	}

	@Override
	public String toString() {
		return getSymbol();
	}
}
