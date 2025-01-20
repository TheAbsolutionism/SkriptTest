package org.skriptlang.skript.parser.pattern;

import org.skriptlang.skript.api.nodes.SyntaxNodeType;
import org.skriptlang.skript.parser.TokenizedSyntax;

import java.util.List;

/**
 * A class used internally by the parser to represent an element in a pattern.
 */
public abstract class PatternElement {

	/**
	 * Created a list of tokenized syntaxes from this pattern element.
	 * @param nodeType The node type to create the tokenized syntaxes for.
	 * @param existingSyntaxes The existing syntaxes that have been created so far.
	 */
	public abstract List<TokenizedSyntax> createTokenizedSyntaxes(SyntaxNodeType nodeType, List<TokenizedSyntax> existingSyntaxes);

}
