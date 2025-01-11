package org.skriptlang.skript.api;

import java.util.List;

/**
 * The Skript Parser is responsible for parsing Skript code into a syntax tree.
 */
public interface SkriptParser {

	/**
	 * Returns whether the parser is locked.
	 * The lock state of the parser determines if
	 * new node types can be submitted via {@link SkriptParser#submitNode(SkriptNodeType) submitNode}.
	 */
	boolean isLocked();

	/**
	 * Submits a node type to the parser.
	 * Once a node is submitted, the parser will be capable of parsing that node type.
	 * @param nodeType The node type to submit
	 */
	void submitNode(SkriptNodeType nodeType);

	/**
	 * Gets an <b>immutable</b> view of all node types that have been submitted to the parser.
	 */
	List<SkriptNodeType> getNodeTypes();

	ResultWithDiagnostics<SyntaxNode> parse(ScriptSource source);
}
