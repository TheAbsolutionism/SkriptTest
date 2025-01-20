package org.skriptlang.skript.parser.pattern;

import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.api.nodes.SyntaxNodeType;
import org.skriptlang.skript.parser.TokenizedSyntax;
import org.skriptlang.skript.parser.tokens.Token;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A pattern element that is a list of tokens.
 * <p>
 * Effectively this is the low level pattern element
 * that all other elements eventually contain.
 */
public class TokensPatternElement extends PatternElement {
	private final List<Token> tokens;

	public TokensPatternElement(@NotNull List<Token> tokens) {
		this.tokens = tokens.stream().toList();
	}

	public List<Token> getTokens() {
		return tokens;
	}

	@Override
	public List<TokenizedSyntax> createTokenizedSyntaxes(SyntaxNodeType nodeType, List<TokenizedSyntax> existingSyntaxes) {
		return existingSyntaxes.stream().map(existingSyntax -> {
			var newList = new LinkedList<Token>();
			newList.addAll(existingSyntax.tokens());
			newList.addAll(tokens);
			return new TokenizedSyntax(nodeType, newList);
		}).toList();
	}
}
