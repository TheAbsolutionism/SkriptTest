package org.skriptlang.skript.parser.pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.api.nodes.SyntaxNodeType;
import org.skriptlang.skript.parser.TokenizedSyntax;
import org.skriptlang.skript.parser.tokens.Token;
import org.skriptlang.skript.parser.tokens.TokenType;

import java.util.List;

/**
 * A pattern element representing a syntax element.
 */
public class SyntaxPatternElement extends PatternElement {
	private final @NotNull String syntaxType;
	private final @NotNull List<Input> inputs;
	private final @Nullable String output;

	public SyntaxPatternElement(@NotNull String syntaxType, @NotNull List<Input> inputs, @Nullable String output) {
		this.syntaxType = syntaxType;
		this.inputs = inputs.stream().toList();
		this.output = output;
	}

	public @NotNull String getSyntaxType() {
		return syntaxType;
	}

	public @NotNull List<Input> getInputs() {
		return inputs;
	}

	public @Nullable String getOutput() {
		return output;
	}

	public record Input(String name, String type) {}

	@Override
	public List<TokenizedSyntax> createTokenizedSyntaxes(SyntaxNodeType nodeType, List<TokenizedSyntax> existingSyntaxes) {
		return existingSyntaxes.stream().map(existingSyntax -> {
			var newList = new java.util.ArrayList<>(existingSyntax.tokens().stream().toList());
			newList.add(new Token(TokenType.SYNTAX, this, -1, 0, null));
			return new TokenizedSyntax(nodeType, newList);
		}).toList();
	}
}
