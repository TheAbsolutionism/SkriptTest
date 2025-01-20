package org.skriptlang.skript.api.nodes;

/**
 * Represents a node in the syntax tree of a Skript script.
 * A syntax node is both low-level and an artifact.
 * From an addon perspective, this is not a useful interface.
 * <p>
 * This is a base interface for all nodes in the syntax tree.
 * Note that the likelihood of needing to implement this interface is low.
 * Instead, see the sub-interfaces of this.
 */
public interface SyntaxNode {

	/**
	 * The length of this node, in tokens.
	 * This includes the length of children.
	 */
	int length();

}
