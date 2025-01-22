package ch.njol.skript.lang.util.common;

import ch.njol.skript.classes.Changer.ChangeMode;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.Expression;

import java.util.ArrayList;
import java.util.List;

/**
 * 'AnyProvider' types are holders for common properties (e.g. name, size) where
 * it is highly likely that things other than Skript may wish to register
 * exponents of the property.
 * <br/>
 * <br/>
 * If possible, types should implement an {@link AnyProvider} subtype directly for
 * the best possible parsing efficiency.
 * However, implementing the interface may not be possible if:
 * <ul>
 *     <li>registering an existing class from a third-party library</li>
 *     <li>the subtype getter method conflicts with the type's own methods
 *     or erasure</li>
 *     <li>the presence of the supertype might confuse the class's design</li>
 * </ul>
 * In these cases, a converter from the class to the AnyX type can be registered.
 * The converter should not permit right-chaining or unsafe casts.
 * <br/>
 * <br/>
 * The root provider supertype cannot include its own common methods, since these
 * may conflict between things that provide two values (e.g. something declaring
 * both a name and a size)
 */
public interface AnyProvider {

	/**
	 * Checks to see if the object can be changed.
	 * This is checked within {@link ch.njol.skript.lang.Expression#acceptChange(ChangeMode)}.
	 *
	 * Due to already existing support change methods, ex: {@link AnyNamed#supportsNameChange()}
	 * to prevent already breaking changes, this method will stay separated.
	 * @return True if it supports being changed.
	 */
	default boolean supportsChange() {
		return false;
	}

	/**
	 * Provide classes users can use when setting the value of through {@link Expression#acceptChange(ChangeMode)}.
	 * If the expression is single and the return type of the expression is of the class, will only accept classes provided from this method.
	 * If the expression is not single, the provided classes from this method will be combined with the default classes from
	 * the {@link Expression#acceptChange(ChangeMode)} and provided classes from other {@link AnyProvider}s.
	 * @return A {@link List} of accepted classes that can be accepted allowing use of {@link AnyProvider#change(Object[], ChangeMode)}.
	 */
	default @Nullable List<Class<?>> getAcceptedClasses() {
		return new ArrayList<>();
	}

	/**
	 * Provide {@link ChangeMode}s users can use when changing the value of this object through {@link Expression#acceptChange(ChangeMode)}.
	 * If the expression is single and the return type of the expression is of the class, will only accept modes provided from this method.
	 * If the expression is not single, the provided modes from this method will be combined with the default modes from
	 * the {@link Expression#acceptChange(ChangeMode)} and provided modes from other {@link AnyProvider}s.
	 * @return A {@link List} of accepted modes that can be accepted allowing use of {@link AnyProvider#change(Object[], ChangeMode)}.
	 */
	default @Nullable List<ChangeMode> getAcceptedModes() {
		return new ArrayList<>();
	}

	/**
	 * Checks to see if the method {@link AnyProvider#change(Object[], ChangeMode)} is overridden and available.
	 * @return True if the method {@link AnyProvider#change(Object[], ChangeMode)} is overridden.
	 */
	default boolean hasCustomChanger() {
		return false;
	}

	/**
	 * Custom changer to be used when changing the value of an object from an expression.
	 * @param delta The raw objects provided by the user
	 * @param mode The {@link ChangeMode} that was used
	 */
	default void change(Object @Nullable [] delta, ChangeMode mode) {
		throw new UnsupportedOperationException();
	}

}
