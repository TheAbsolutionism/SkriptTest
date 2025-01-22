package ch.njol.skript.lang.util.common;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

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
	 * Used within {@link Expression#acceptChange(ChangeMode)} to check if the object can be changed.
	 *
	 * If the {@link Expression} within {@link Expression#acceptChange(ChangeMode)} is single and the return type of the
	 * expression is of this class, returning false will cause a parse error.
	 *
	 * If the {@link Expression} is not single, will check if any of the objects are instance of this class and add the supplied data
	 * from the methods {@link AnyProvider#getAcceptedClasses()} and {@link AnyProvider#getAcceptedModes()} if this method returns true.
	 *
	 * Due to already existing support change methods, ex: {@link AnyNamed#supportsNameChange()}
	 * to prevent already breaking changes, this method will stay separated.
	 * @return True if it supports being changed.
	 */
	default boolean supportsChange() {
		return false;
	}

	/**
	 * Supply classes the object can be changed to, to {@link Expression#acceptChange(ChangeMode)}.
	 *
	 * If the {@link Expression} within {@link Expression#acceptChange(ChangeMode)} is single and the return type of the
	 * expression is of this class, {@link Expression#acceptChange(ChangeMode)} will only accept classes supplied from this method,
	 * thus causing a parse error.
	 *
	 * If the {@link Expression} is not single, will check if any of the objects are instance of this class and add the supplied classes
	 * from this method to a total available list.
	 *
	 * @return A {@link List} of accepted classes that can be accepted within {@link Expression#acceptChange(ChangeMode)} and can be further used
	 * within {@link AnyProvider#change(Event, Object[], ChangeMode)}.
	 */
	default @Nullable List<Class<?>> getAcceptedClasses() {
		return new ArrayList<>();
	}

	/**
	 * Supply {@link ChangeMode}s the object can be used with, to {@link Expression#acceptChange(ChangeMode)}.
	 *
	 * If the {@link Expression} within {@link Expression#acceptChange(ChangeMode)} is single and the return type of the
	 * expression is of this class, {@link Expression#acceptChange(ChangeMode)} will only accept change modes supplied from this method,
	 * thus causing a parse error.
	 *
	 * If the {@link Expression} is not single, will check if any of the objects are instance of this class and add the supplied change modes
	 * from this method to a total available list.
	 *
	 * @return A {@link List} of accepted change modes that can be accepted within {@link Expression#acceptChange(ChangeMode)} and can be further used
	 * within {@link AnyProvider#change(Event, Object[], ChangeMode)}.
	 */
	default @Nullable List<ChangeMode> getAcceptedModes() {
		return new ArrayList<>();
	}

	/**
	 * Checks to see if the method {@link AnyProvider#change(Event, Object[], ChangeMode)} is overridden and available.
	 * @return True if the method {@link AnyProvider#change(Event, Object[], ChangeMode)} is overridden.
	 */
	default boolean hasCustomChanger() {
		return false;
	}

	/**
	 * Custom changer to be used when changing the value of an object from an expression.
	 * This method should be very strict in how it changes the object.
	 * @param event The {@link Event} of the current event the object is being changed in.
	 * @param delta The raw objects provided by the user
	 * @param mode The {@link ChangeMode} that was used
	 */
	default void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		throw new UnsupportedOperationException();
	}

}
