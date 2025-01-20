package org.skriptlang.skript.parser.pattern;

import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.api.nodes.SyntaxNodeType;
import org.skriptlang.skript.parser.TokenizedSyntax;

import java.util.LinkedList;
import java.util.List;

/**
 * An element enclosing one or more other elements in a pattern.
 * This element also encapsulates optional groups.
 */
public class GroupPatternElement extends PatternElement {
	private final List<PatternElement> elements;
	private final boolean isOptional;

	public GroupPatternElement(@NotNull List<PatternElement> elements, boolean isOptional) {
		this.elements = elements.stream().toList();
		this.isOptional = isOptional;
	}

	public List<PatternElement> getElements() {
		return elements;
	}

	public boolean isOptional() {
		return isOptional;
	}

	@Override
	public List<TokenizedSyntax> createTokenizedSyntaxes(SyntaxNodeType nodeType, List<TokenizedSyntax> existingSyntaxes) {
		var newList = new LinkedList<TokenizedSyntax>();
		// preserve original if optional
		if (isOptional()) newList.addAll(existingSyntaxes);

		existingSyntaxes.forEach(existingSyntax -> {
			List<TokenizedSyntax> newSyntaxes = new LinkedList<>();
			newSyntaxes.add(existingSyntax);
			for (PatternElement element : getElements()) {
				newSyntaxes = element.createTokenizedSyntaxes(nodeType, newSyntaxes);
			}
			newList.addAll(newSyntaxes);
		});

		return newList;
	}
}
