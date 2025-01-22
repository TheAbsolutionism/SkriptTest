package org.skriptlang.skript.api.nodes;

import org.skriptlang.skript.api.util.ExecuteResult;

public interface StatementNode extends SyntaxNode {

	/**
	 * Executes the statement.
	 */
	ExecuteResult execute();

}
