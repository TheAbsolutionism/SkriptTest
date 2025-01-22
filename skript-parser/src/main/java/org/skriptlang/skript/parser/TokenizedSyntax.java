package org.skriptlang.skript.parser;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.api.nodes.SyntaxNodeType;
import org.skriptlang.skript.parser.pattern.SyntaxPatternElement;
import org.skriptlang.skript.parser.tokens.Token;
import org.skriptlang.skript.parser.tokens.TokenType;

import java.util.Collections;
import java.util.List;

/**
 * A tokenized syntax is a version of a syntax that has been transformed into token representations.
 *
 * @param nodeType The node type that this syntax belongs to.
 * @param tokens   The tokens that make up this syntax.
 *                 This will be matched against tokenized scripts to determine if this syntax is present.
 */
public record TokenizedSyntax(@NotNull SyntaxNodeType<?> nodeType, @NotNull List<Token> tokens) {

	public TokenizedSyntax(@NotNull SyntaxNodeType<?> nodeType, @NotNull List<Token> tokens) {
		Preconditions.checkNotNull(nodeType);
		Preconditions.checkNotNull(tokens);

		this.nodeType = nodeType;
		this.tokens = Collections.unmodifiableList(tokens);
	}

	/**
	 * Returns whether this tokenized syntax could match against the given script tokens.
	 * @param scriptTokens The script tokens to match against.
	 */
	public boolean canMatch(@NotNull List<Token> scriptTokens) {
		int syntaxIndex = 0;
		int scriptIndex = 0;

		while (scriptIndex < scriptTokens.size()) {
			if (syntaxIndex == tokens.size()) {
				// Too many script tokens
				return false;
			}
			Token syntaxToken = tokens.get(syntaxIndex);
			Token scriptToken = scriptTokens.get(scriptIndex);

			if (syntaxToken.type() != TokenType.SYNTAX) {
				if (!syntaxToken.matches(scriptToken)) {
					// Mismatched token types
					return false;
				}

				syntaxIndex++;
				scriptIndex++;
				continue;
			}

			SyntaxPatternElement element = (SyntaxPatternElement) syntaxToken.value();
			if (element.getSyntaxType().equals("token")) {
				// special case for token syntax

				TokenType target = TokenType.fromName(element.getInputs().getFirst().type());

				if (scriptToken.type() != target) {
					// Mismatched token types
					return false;
				}

				syntaxIndex++;
				scriptIndex++;
				continue;
			}

			// Syntax token which can match many
			if (syntaxIndex == tokens.size() - 1) {
				// Last token in syntax, so it can match anything left
				return true;
			}

			if (scriptIndex == scriptTokens.size() - 1) {
				// Last token in script, so it can't match anything left
				return false;
			}

			int nextSyntaxIndex = syntaxIndex + 1;
			while (nextSyntaxIndex < tokens.size() && tokens.get(nextSyntaxIndex).type() == TokenType.SYNTAX) {
				nextSyntaxIndex++;
			}
			// checking if there isn't enough tokens left in the script to fill the syntax tokens that were skipped
			if (scriptIndex + nextSyntaxIndex - syntaxIndex > scriptTokens.size()) {
				// Not enough tokens left in script
				return false;
			}

			scriptIndex += nextSyntaxIndex - syntaxIndex;
			syntaxIndex = nextSyntaxIndex;
		}

		if (tokens.subList(syntaxIndex, tokens.size()).stream().allMatch(token -> token.type() == TokenType.SYNTAX)) {
			// All remaining tokens are syntax tokens
			return true;
		}
		return syntaxIndex != tokens.size() - 1;
	}
}
