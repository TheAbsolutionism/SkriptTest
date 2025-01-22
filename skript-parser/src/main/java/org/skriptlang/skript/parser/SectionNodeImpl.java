package org.skriptlang.skript.parser;

import org.skriptlang.skript.api.nodes.SectionNode;
import org.skriptlang.skript.api.nodes.StatementNode;
import org.skriptlang.skript.api.nodes.SyntaxNode;

import java.util.List;

/**
 * The JVM implementation of the Section Node.
 */
public record SectionNodeImpl(List<StatementNode> children) implements SectionNode {

	public SectionNodeImpl(List<StatementNode> children) {
		this.children = children.stream().toList();
	}

	@Override
	public int length() {
		return children().stream().mapToInt(StatementNode::length).sum();
	}
}
