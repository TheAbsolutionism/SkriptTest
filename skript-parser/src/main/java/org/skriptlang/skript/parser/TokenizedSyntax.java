package org.skriptlang.skript.parser;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.api.SkriptNodeType;

import java.util.Collections;
import java.util.List;

/**
 * A tokenized syntax is a version of a syntax that has been transformed into token representations.
 *
 * @param nodeType The node type that this syntax belongs to.
 * @param tokens   The tokens that make up this syntax.
 *                 This will be matched against tokenized scripts to determine if this syntax is present.
 */
public record TokenizedSyntax(@NotNull SkriptNodeType nodeType, @NotNull List<Token> tokens) {

	public TokenizedSyntax(@NotNull SkriptNodeType nodeType, @NotNull List<Token> tokens) {
		Preconditions.checkNotNull(nodeType);
		Preconditions.checkNotNull(tokens);
		Preconditions.checkArgument(!tokens.isEmpty(), "tokens cannot be empty");

		this.nodeType = nodeType;
		this.tokens = Collections.unmodifiableList(tokens);
	}

	public boolean canMatch(@NotNull List<Token> scriptTokens) {
		if (scriptTokens.size() < tokens.size()) return false;

		int scriptIndex = 0;
		for (int i = 0; i < tokens.size(); i++) {
			Token syntaxToken = tokens.get(i);
			Token scriptToken = scriptTokens.get(scriptIndex);

			if (syntaxToken.type() == TokenType.SYNTAX) {
				// a special case that can match with multiple tokens

				if (i == tokens.size() - 1) {
					// if this is the last token, it can match
					return true;
				}

				if (i == tokens.size() - 1) {
					// there are more tokens in the syntax, but we have reached the end of the script.
					return false;
				}

				// the syntax token must consume at least one token
				scriptToken = scriptTokens.get(scriptIndex + 1);

				// we need to locate the next token that isn't a syntax
				// so we can figure out how many tokens to skip in the script
				int searchIndex = i + 1;
				Token nextSyntaxToken = tokens.get(searchIndex);
				while (nextSyntaxToken.type() == TokenType.SYNTAX && searchIndex < tokens.size() - 1) {
					nextSyntaxToken = tokens.get(++searchIndex);
				}

				if (nextSyntaxToken.type() == TokenType.SYNTAX) {
					// all next tokens were also syntax, so we can match the rest of the script
					return true;
				}

				// we have found a non-syntax token, so we need to skip tokens in the script
				// until we find a token that matches the next syntax token
				while (scriptToken.type() != nextSyntaxToken.type() && scriptIndex < scriptTokens.size() - 1) {
					scriptToken = scriptTokens.get(++scriptIndex);
				}

				if (scriptToken.type() != nextSyntaxToken.type()) {
					// we have reached the end of the script without finding the next syntax token
					return false;
				}

				continue;
			}

			if (!syntaxToken.equals(scriptToken)) {
				// the next syntax cannot match with the next token in the script.
				// mismatch
				return false;
			}

			// successful match
			scriptIndex++;
		}

		// all tokens matched
		return true;
	}
}
