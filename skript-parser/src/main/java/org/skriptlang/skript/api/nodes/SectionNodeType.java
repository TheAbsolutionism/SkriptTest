package org.skriptlang.skript.api.nodes;

import java.util.List;

/**
 *
 * @see SectionNode
 */
public final class SectionNodeType implements SyntaxNodeType<SectionNode> {

	/**
	 * Gets the syntaxes that should result in a node of this type.
	 */
	@Override
	public List<String> getSyntaxes() {
		return null;
	}
}
