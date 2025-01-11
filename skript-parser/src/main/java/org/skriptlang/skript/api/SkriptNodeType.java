package org.skriptlang.skript.api;

import java.util.List;

/**
 * Represents a type of node in the Skript syntax tree.
 * The node type stores information about a node's syntaxes.
 */
public interface SkriptNodeType {

	/**
	 * Gets the syntaxes that should result in a node of this type.
	 */
	List<String> getSyntaxes();

	/**
	 * Gets the lowest level return type that this node can return, regardless of context.
	 * This is used by the parser to perform very basic type-based filtering.
	 */
	Class<?> getReturnType();

}
