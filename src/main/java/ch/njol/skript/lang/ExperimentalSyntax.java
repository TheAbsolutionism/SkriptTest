package ch.njol.skript.lang;

import ch.njol.skript.Skript;
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
	 * @return The required experiment feature.
	 */
	default Feature requiredExperiment() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Checks whether the required features are enabled for this syntax element.
	 * <p>
	 * By default, this method checks for the required {@link Feature} defined by {@link #requiredExperiment()}
	 * in the provided array of features. If the required feature is found, the method returns {@code true}.
	 * </p>
	 * <p>
	 * Subclasses or implementations may override this method to perform custom checks for enabled or disabled features
	 * and declare specific requirements for syntax availability.
	 * When overriding this method, it is recommended to use {@link Skript#error(String)} to inform users
	 * why the syntax element cannot be used.
	 * </p>
	 *
	 * @param features An array of {@link Feature} instances currently active in the environment.
	 * @return {@code true} if the element can be used.
	 */
	default boolean isSatisfiedBy(Feature[] features) {
		Feature required = requiredExperiment();
		for (Feature feature : features)
			if (feature.equals(required))
				return true;
		Skript.error("This syntax element is experimental. To enable this experiment, add "
			+ "'using " + required.codeName() + "' at the top of this file.");
		return false;
	}

}
