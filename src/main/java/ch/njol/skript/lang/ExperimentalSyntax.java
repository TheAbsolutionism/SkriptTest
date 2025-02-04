package ch.njol.skript.lang;

import ch.njol.skript.registrations.Feature;
import ch.njol.util.Kleenean;

/**
 * A syntax element that requires an experimental feature to be enabled.
 */
public interface ExperimentalSyntax extends SyntaxElement {

	/**
	 * Returns the experiment {@link Feature} required for this syntax to be used.
	 * <p>
	 * Before {@link SyntaxElement#init(Expression[], int, Kleenean, SkriptParser.ParseResult)} is called, checks
	 * to see if the experiment required has been enabled.
	 * If it is not, an error will be printed and the syntax element will not be initialised.
	 * </p>
	 *
	 * @return Required experiment feature.
	 */
	Feature requiredExperiment();

}
