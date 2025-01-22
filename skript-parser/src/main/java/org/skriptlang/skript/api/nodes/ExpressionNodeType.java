package org.skriptlang.skript.api.nodes;

public interface ExpressionNodeType<T extends ExpressionNode> extends SyntaxNodeType<T> {

	/**
	 * Gets the lowest level return type that this node can return, regardless of context.
	 * This is used by the parser to perform very basic type-based filtering.
	 */
	Class<?> getReturnType();

}
