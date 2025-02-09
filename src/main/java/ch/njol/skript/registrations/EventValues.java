package ch.njol.skript.registrations;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.test.runner.EffDebugEventValues;
import ch.njol.skript.util.Getter;
import ch.njol.util.Kleenean;
import com.google.common.collect.ImmutableList;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventValues {

	private EventValues() {}

	/**
	 * The past value of an eventClass value. Represented by "past" or "former".
	 */
	public static final int TIME_PAST = -1;

	/**
	 * The current time of an eventClass value.
	 */
	public static final int TIME_NOW = 0;

	/**
	 * The future time of an eventClass value.
	 */
	public static final int TIME_FUTURE = 1;

	private final static List<EventValueInfo<?, ?>> defaultEventValues = new ArrayList<>(30);
	private final static List<EventValueInfo<?, ?>> futureEventValues = new ArrayList<>();
	private final static List<EventValueInfo<?, ?>> pastEventValues = new ArrayList<>();

	/**
	 * Get Event Values list for the specified time
	 * @param time The time of the eventClass values. One of
	 * {@link EventValues#TIME_PAST}, {@link EventValues#TIME_NOW} or {@link EventValues#TIME_FUTURE}.
	 * @return An immutable copy of the eventClass values list for the specified time
	 */
	public static List<EventValueInfo<?, ?>> getEventValuesListForTime(int time) {
		return ImmutableList.copyOf(getEventValuesList(time));
	}

	private static List<EventValueInfo<?, ?>> getEventValuesList(int time) {
		if (time == -1)
			return pastEventValues;
		if (time == 0)
			return defaultEventValues;
		if (time == 1)
			return futureEventValues;
		throw new IllegalArgumentException("time must be -1, 0, or 1");
	}

	/**
	 * Registers an eventClass value, specified by the provided {@link Converter}, with excluded events.
	 * Uses the default time, {@link #TIME_NOW}.
	 *
	 * @see #registerEventValue(Class, Class, Converter, int)
	 */
	public static <T, E extends Event> void registerEventValue(
		Class<E> event, Class<T> type,
		Converter<E, T> converter
	) {
		registerEventValue(event, type, converter, TIME_NOW);
	}

	/**
	 * Registers an eventClass value.
	 *
	 * @param event the eventClass type class.
	 * @param type the return type of the converter for the eventClass value.
	 * @param converter the converter to get the value with the provided eventClass.
	 * @param time value of TIME_PAST if this is the value before the eventClass, TIME_FUTURE if after, and TIME_NOW if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 */
	public static <T, E extends Event> void registerEventValue(
		Class<E> event, Class<T> type,
		Converter<E, T> converter, int time
	) {
		registerEventValue(event, type, converter, time, null, (Class<? extends E>[]) null);
	}

	/**
	 * Registers an eventClass value and with excluded events.
	 * Excluded events are events that this eventClass value can't operate in.
	 *
	 * @param event the eventClass type class.
	 * @param type the return type of the converter for the eventClass value.
	 * @param converter the converter to get the value with the provided eventClass.
	 * @param time value of TIME_PAST if this is the value before the eventClass, TIME_FUTURE if after, and TIME_NOW if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 * @param excludeErrorMessage The error message to display when used in the excluded events.
	 * @param excludes subclasses of the eventClass for which this eventClass value should not be registered for
	 */
	@SafeVarargs
	public static <T, E extends Event> void registerEventValue(
		Class<E> event, Class<T> type,
		Converter<E, T> converter, int time,
		@Nullable String excludeErrorMessage,
		@Nullable Class<? extends E>... excludes
	) {
		Skript.checkAcceptRegistrations();
		List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
		EventValueInfo<E, T> element = new EventValueInfo<>(event, type, converter, excludeErrorMessage, excludes);

		for (int i = 0; i < eventValues.size(); i++) {
			EventValueInfo<?, ?> info = eventValues.get(i);
			// We don't care for exact duplicates. Prefer Skript's over any addon.
			if (info.eventClass.equals(event) && info.valueClass.equals(type))
				return;
			// If the events don't match, we prefer the highest subclass eventClass.
			// If the events match, we prefer the highest subclass type.
			if (!info.eventClass.equals(event) ? info.eventClass.isAssignableFrom(event) : info.valueClass.isAssignableFrom(type)) {
				eventValues.add(i, element);
				return;
			}
		}
		eventValues.add(element);
	}

	/**
	 * @deprecated Use {@link #registerEventValue(Class, Class, Converter, int, String, Class[])} instead.
	 */
	@Deprecated(forRemoval = true)
	@SafeVarargs
	@SuppressWarnings({"removal"})
	public static <T, E extends Event> void registerEventValue(
		Class<E> event, Class<T> type,
		Getter<T, E> getter, int time,
		@Nullable String excludeErrorMessage,
		@Nullable Class<? extends E>... excludes
	) {
		registerEventValue(event, type, (Converter<E, T>) getter, time, excludeErrorMessage, excludes);
	}

	/**
	 * @deprecated Use {@link #registerEventValue(Class, Class, Converter, int)} instead.
	 */
	@Deprecated(forRemoval = true)
	@SuppressWarnings({"removal"})
	public static <T, E extends Event> void registerEventValue(
		Class<E> event, Class<T> type,
		Getter<T, E> getter, int time
	) {
		registerEventValue(event, type, (Converter<E, T>) getter, time);
	}

	/**
	 * Gets a specific value from an eventClass. Returns null if the eventClass doesn't have such a value (conversions are done to try and get the desired value).
	 * <p>
	 * It is recommended to use {@link EventValues#getEventValueGetter(Class, Class, int)} or {@link EventValueExpression#EventValueExpression(Class)} instead of invoking this
	 * method repeatedly.
	 *
	 * @param e eventClass
	 * @param c return type of getter
	 * @param time -1 if this is the value before the eventClass, 1 if after, and 0 if it's the default or this value doesn't have distinct states.
	 *            <b>Always register a default state!</b> You can leave out one of the other states instead, e.g. only register a default and a past state. The future state will
	 *            default to the default state in this case.
	 * @return The eventClass's value
	 * @see #registerEventValue(Class, Class, Converter, int)
	 */
	public static <T, E extends Event> @Nullable T getEventValue(E e, Class<T> c, int time) {
		//noinspection unchecked
		Converter<? super E, ? extends T> converter = getEventValueConverter((Class<E>) e.getClass(), c, time);
		if (converter == null)
			return null;
		return converter.convert(e);
	}

	/**
	 * @deprecated Use {@link #getExactEventValueConverter(Class, Class, int)} instead.
	 */
	@Nullable
	@Deprecated(forRemoval = true)
	@SuppressWarnings({"removal"})
	public static <T, E extends Event> Getter<? extends T, ? super E> getExactEventValueGetter(Class<E> event, Class<T> c, int time) {
		return toGetter(getExactEventValueConverter(event, c, time));
	}

	/**
	 * Checks that a {@link Converter} exists for the exact type. No converting or subclass checking.
	 *
	 * @param event the eventClass class the getter will be getting from
	 * @param c type of {@link Converter}
	 * @param time the eventClass-value's time
	 * @return A getter to get values for a given type of events
	 * @see #registerEventValue(Class, Class, Converter, int)
	 * @see EventValueExpression#EventValueExpression(Class)
	 */
	@Nullable
	public static <E extends Event, T> Converter<? super E, ? extends T> getExactEventValueConverter(
		Class<E> event, Class<T> c, int time
	) {
		debug(event, c, "#getExact: Checking exact value");
		List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
		// First check for exact classes matching the parameters.
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			if (!c.equals(eventValueInfo.valueClass))
				continue;
			if (!checkExcludes(eventValueInfo, event))
				return null;
			if (eventValueInfo.eventClass.isAssignableFrom(event)) {
				debug(event, c, "#getExact: Found exact");
				//noinspection unchecked
				return (Converter<? super E, ? extends T>) eventValueInfo.converter;
			}
		}
		debug(event, c,  "#getExact: No exact value");
		return null;
	}

	/**
	 * @deprecated Use {@link #hasMultipleConverters(Class, Class, int)} instead.
	 */
	@Deprecated(forRemoval = true)
	public static <T, E extends Event> Kleenean hasMultipleGetters(Class<E> event, Class<T> type, int time) {
		return hasMultipleConverters(event, type, time);
	}

	/**
	 * Checks if an eventClass has multiple {@link Converter}s, including default ones.
	 *
	 * @param event the eventClass class the {@link Converter} will be getting from.
	 * @param type type of {@link Converter}.
	 * @param time the eventClass-value's time.
	 * @return true or false if the eventClass and type have multiple {@link Converter}s.
	 */
	public static <T, E extends Event> Kleenean hasMultipleConverters(Class<E> event, Class<T> type, int time) {
		List<Converter<? super E, ? extends T>> getters = getEventValueConverters(event, type, time, true, false);
		if (getters == null)
			return Kleenean.UNKNOWN;
		return Kleenean.get(getters.size() > 1);
	}

	/**
	 * @deprecated Use {@link #getEventValueConverter(Class, Class, int)} instead.
	 */
	@Nullable
	@Deprecated(forRemoval = true)
	@SuppressWarnings({"removal"})
	public static <T, E extends Event> Getter<? extends T, ? super E> getEventValueGetter(Class<E> event, Class<T> type, int time) {
		return toGetter(getEventValueConverter(event, type, time, true));
	}

	/**
	 * Returns a {@link Converter} to get a value from in an eventClass.
	 * <p>
	 * Can print an error if the eventClass value is blocked for the given eventClass.
	 *
	 * @param event the eventClass class the {@link Converter} will be getting from.
	 * @param type type of {@link Converter}.
	 * @param time the eventClass-value's time.
	 * @return A getter to get values for a given type of events.
	 * @see #registerEventValue(Class, Class, Converter, int)
	 * @see EventValueExpression#EventValueExpression(Class)
	 */
	public static <T, E extends Event> @Nullable Converter<? super E, ? extends T> getEventValueConverter(
		Class<E> event, Class<T> type, int time
	) {
		return getEventValueConverter(event, type, time, true);
	}

	@Nullable
	private static <T, E extends Event> Converter<? super E, ? extends T> getEventValueConverter(
		Class<E> event, Class<T> type, int time, boolean allowDefault
	) {
		List<Converter<? super E, ? extends T>> list = getEventValueConverters(event, type, time, allowDefault);
		if (list == null || list.isEmpty())
			return null;
		return list.get(0);
	}

	@Nullable
	private static <T, E extends Event> List<Converter<? super E, ? extends T>> getEventValueConverters(
		Class<E> event, Class<T> type, int time, boolean allowDefault
	) {
		return getEventValueConverters(event, type, time, allowDefault, true);
	}

	/*
	 * We need to be able to collect all possible eventClass-values to a list for determining problematic collisions.
	 * Always return after the loop check if the list is not empty.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	private static <T, E extends Event> List<Converter<? super E, ? extends T>> getEventValueConverters(
		Class<E> event, Class<T> type, int time,
		boolean allowDefault, boolean allowConverting
	) {
		List<EventValueInfo<?, ?>> eventValues = getEventValuesList(time);
		List<Converter<? super E, ? extends T>> list = new ArrayList<>();
		// First check for exact classes matching the parameters.
		debug(event, type, "#getConverters: Checking exact");
		Converter<? super E, ? extends T> exact = getExactEventValueConverter(event, type, time);
		if (exact != null) {
			list.add(exact);
			debug(event, type, "#getConverters: Returning exact");
			return list;
		}
		Map<EventValueInfo<?, ?>, Converter<? super E, ? extends T>> converterMap = new HashMap<>();
		// Second check for assignable subclasses.
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			if (!type.isAssignableFrom(eventValueInfo.valueClass))
				continue;
			if (!checkExcludes(eventValueInfo, event))
				return null;
			if (eventValueInfo.eventClass.isAssignableFrom(event)) {
				debug(event, type, "#getConverters: Adding Super - " + eventValueInfo.eventClass + " - " + eventValueInfo.valueClass);
				list.add((Converter<? super E, ? extends T>) eventValueInfo.converter);
				converterMap.put(eventValueInfo, (Converter<? super E, ? extends T>) eventValueInfo.converter);
				continue;
			}
			if (!event.isAssignableFrom(eventValueInfo.eventClass))
				continue;
			debug(event, type, "#getConverters: Adding Sub - " + eventValueInfo.eventClass + " - " + eventValueInfo.valueClass);
			list.add(e -> {
				if (!eventValueInfo.eventClass.isInstance(e))
					return null;
				return ((Converter<? super E, ? extends T>) eventValueInfo.converter).convert(e);
			});
			converterMap.put(eventValueInfo, e -> {
				if (!eventValueInfo.eventClass.isInstance(e))
					return null;
				return ((Converter<? super E, ? extends T>) eventValueInfo.converter).convert(e);
			});
		}
		debug(event, type, "#getConverters: After #2");
		if (!list.isEmpty())
			return delegateConverters(event, type, converterMap, list);
		if (!allowConverting)
			return null;
		// Most checks have returned before this below is called, but Skript will attempt to convert or find an alternative.
		// Third check is if the returned object matches the class.
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			if (!eventValueInfo.valueClass.isAssignableFrom(type))
				continue;
			boolean checkInstanceOf = !eventValueInfo.eventClass.isAssignableFrom(event);
			if (checkInstanceOf && !event.isAssignableFrom(eventValueInfo.eventClass))
				continue;

			if (!checkExcludes(eventValueInfo, event))
				return null;
			debug(event, type, "#getConverters: Adding #3 - " + eventValueInfo.eventClass + " - " + eventValueInfo.valueClass);
			list.add(e -> {
				if (checkInstanceOf && !eventValueInfo.eventClass.isInstance(e))
					return null;
				T object = ((Converter<? super E, ? extends T>) eventValueInfo.converter).convert(e);
				if (type.isInstance(object))
					return object;
				return null;
			});
			converterMap.put(eventValueInfo, e -> {
				if (checkInstanceOf && !eventValueInfo.eventClass.isInstance(e))
					return null;
				T object = ((Converter<? super E, ? extends T>) eventValueInfo.converter).convert(e);
				if (type.isInstance(object))
					return object;
				return null;
			});
		}
		debug(event, type, "#getConverters: After #3");
		if (!list.isEmpty())
			return delegateConverters(event, type, converterMap, list);
		// Fourth check will attempt to convert the eventClass value to the requesting type.
		// This first for loop will check that the events are exact. See issue #5016
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			if (!event.equals(eventValueInfo.eventClass))
				continue;

			Converter<? super E, ? extends T> converter = (Converter<? super E, ? extends T>)
				getConvertedConverter(eventValueInfo, type, false);
			if (converter == null)
				continue;

			if (!checkExcludes(eventValueInfo, event))
				return null;
			debug(event, type, "#getConverters: Adding #4 - " + eventValueInfo.eventClass + " - " + eventValueInfo.valueClass);
			list.add(converter);
			continue;
		}
		debug(event, type, "#getConverters: After #4");
		if (!list.isEmpty())
			return list;
		// This loop will attempt to look for converters assignable to the class of the provided eventClass.
		for (EventValueInfo<?, ?> eventValueInfo : eventValues) {
			// The requesting eventClass must be assignable to the eventClass value's eventClass. Otherwise it'll throw an error.
			if (!event.isAssignableFrom(eventValueInfo.eventClass))
				continue;

			Converter<? super E, ? extends T> converter = (Converter<? super E, ? extends T>)
				getConvertedConverter(eventValueInfo, type, true);
			if (converter == null)
				continue;

			if (!checkExcludes(eventValueInfo, event))
				return null;
			debug(event, type, "#getConverters: Adding #5 - " + eventValueInfo.eventClass + " - " + eventValueInfo.valueClass);
			list.add(converter);
			continue;
		}
		debug(event, type, "#getConverters: After #5");
		if (!list.isEmpty())
			return list;
		// If the check should try again matching eventClass values with a 0 time (most eventClass values).
		if (allowDefault && time != 0) {
			debug(event, type, "#getConverters: Checking present state");
			return getEventValueConverters(event, type, 0, false);
		}
		debug(event, type, "#getConverters: Returning null");
		return null;
	}

	private static <E extends Event, T> List<Converter<? super E, ? extends T>> delegateConverters(
		Class<E> eventClass,
		Class<T> valueClass,
		Map<EventValueInfo<?, ?>, Converter<? super E, ? extends T>> converterMap,
		List<Converter<? super E, ? extends T>> converters
	) {
		if (converters.size() == 1)
			return converters;
		debug(eventClass, valueClass, "#delegate");
		ClassInfo<T> valueClassInfo = Classes.getExactClassInfo(valueClass);
		List<Converter<? super E, ? extends T>> delegated = new ArrayList<>();
		for (EventValueInfo<?, ?> eventValueInfo : converterMap.keySet()) {
			ClassInfo<?> thisClassInfo = Classes.getExactClassInfo(eventValueInfo.valueClass);
			debug(eventClass, valueClass, "#delegate: Checking - " + eventValueInfo.eventClass + " - " + eventValueInfo.valueClass + " - " + thisClassInfo);
			if (thisClassInfo != null && !thisClassInfo.equals(valueClassInfo))
				continue;
			debug(eventClass, valueClass, "#delegate: Adding - " + eventValueInfo.eventClass + " - " + eventValueInfo.valueClass + " - " + thisClassInfo);
			delegated.add(converterMap.get(eventValueInfo));
		}
		if (delegated.isEmpty())
			return converters;
		debug(eventClass, valueClass, "#delegate: Delegated - " + delegated);
		return delegated;
	}

	public static void debug(Class<?> eventClass, Class<?> valueClass, String message) {
		EffDebugEventValues.debug(eventClass, valueClass, message);
	}

	/**
	 * Check if the eventClass value states to exclude events.
	 * False if the current EventValueInfo cannot operate in the provided eventClass.
	 *
	 * @param info The eventClass value info that will be used to grab the value from
	 * @param event The eventClass class to check the excludes against.
	 * @return boolean if true the eventClass value passes for the events.
	 */
	private static boolean checkExcludes(EventValueInfo<?, ?> info, Class<? extends Event> event) {
		if (info.excludes == null)
			return true;
		for (Class<? extends Event> ex : (Class<? extends Event>[]) info.excludes) {
			if (ex.isAssignableFrom(event)) {
				Skript.error(info.excludeErrorMessage);
				return false;
			}
		}
		return true;
	}

	/**
	 * Return a converter wrapped in a getter that will grab the requested value by converting from the given eventClass value info.
	 *
	 * @param info The eventClass value info that will be used to grab the value from
	 * @param to The class that the converter will look for to convert the type from the eventClass value to
	 * @param checkInstanceOf If the eventClass must be an exact instance of the eventClass value info's eventClass or not.
	 * @return The found Converter wrapped in a Getter object, or null if no Converter was found.
	 */
	@Nullable
	private static <E extends Event, F, T> Converter<? super E, ? extends T> getConvertedConverter(
		EventValueInfo<E, F> info, Class<T> to, boolean checkInstanceOf
	) {
		Converter<? super F, ? extends T> converter = Converters.getConverter(info.valueClass, to);

		if (converter == null)
			return null;

		return event -> {
			if (checkInstanceOf && !info.eventClass.isInstance(event))
				return null;
			F f = info.converter.convert(event);
			if (f == null)
				return null;
			return converter.convert(f);
		};
	}

	@Deprecated(forRemoval = true)
	@SuppressWarnings({"removal"})
	private static <A, B> Getter<B, A> toGetter(Converter<A, B> converter) {
		if (converter == null)
			return null;

		return new Getter<>() {
			@Override
			public @Nullable B get(A arg) {
				return converter.convert(arg);
			}
		};
	}

	public static boolean doesExactEventValueHaveTimeStates(Class<? extends Event> event, Class<?> c) {
		return getExactEventValueConverter(event, c, TIME_PAST) != null || getExactEventValueConverter(event, c, TIME_FUTURE) != null;
	}

	public static boolean doesEventValueHaveTimeStates(Class<? extends Event> event, Class<?> c) {
		return getEventValueConverter(event, c, TIME_PAST, false) != null || getEventValueConverter(event, c, TIME_FUTURE, false) != null;
	}

	private record EventValueInfo<E extends Event, T>(
		Class<E> eventClass, Class<T> valueClass, Converter<E, T> converter,
		@Nullable String excludeErrorMessage,
		@Nullable Class<? extends E>[] excludes
	) {
		private EventValueInfo {
			assert eventClass != null;
			assert valueClass != null;
			assert converter != null;
		}

	}

}
