package org.skriptlang.skript.api.nodes;

import java.util.List;

/**
 * Represents a type of node in the Skript syntax tree.
 * The node type stores information about a node's syntaxes.
 * @param <T> The type of node that this node type creates.
 */
public interface SyntaxNodeType<T extends SyntaxNode> {

	/**
	 * Gets the syntaxes that should result in a node of this type.
	 */
	List<String> getSyntaxes();

	T create(List<SyntaxNode> children);

}
