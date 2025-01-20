package org.skriptlang.skript.parser.pattern;

import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.api.nodes.SyntaxNodeType;
import org.skriptlang.skript.parser.TokenizedSyntax;

import java.util.LinkedList;
import java.util.List;

/**
 * An element representing a choice between two patterns.
 */
public class ChoicePatternElement extends PatternElement {
	private final List<PatternElement> left;
	private final List<PatternElement> right;

	public ChoicePatternElement(@NotNull List<PatternElement> left, @NotNull List<PatternElement> right) {
		this.left = left.stream().toList();
		this.right = right.stream().toList();
	}

	public List<PatternElement> getLeft() {
		return left;
	}

	public List<PatternElement> getRight() {
		return right;
	}

	@Override
	public List<TokenizedSyntax> createTokenizedSyntaxes(SyntaxNodeType nodeType, List<TokenizedSyntax> existingSyntaxes) {
		List<TokenizedSyntax> left = new LinkedList<>(existingSyntaxes);
		List<TokenizedSyntax> right = new LinkedList<>(existingSyntaxes);

		for (PatternElement element : getLeft()) {
			left = element.createTokenizedSyntaxes(nodeType, left);
		}
		for (PatternElement element : getRight()) {
			right = element.createTokenizedSyntaxes(nodeType, right);
		}

		var combined = new LinkedList<TokenizedSyntax>();
		combined.addAll(left);
		combined.addAll(right);
		return combined.stream().distinct().toList();
	}
}
