package ch.njol.skript.lang.util.common;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AnyRegistry<T extends AnyProvider, R extends T> {

	private static final Map<Class<?>, AnyRegistry<?, ?>> registeredData = new HashMap<>();

	private final Map<Class<R>, R> typedClasses = new HashMap<>();
	private final Class<T> anyClass;

	private AnyRegistry(Class<T> anyClass) {
		this.anyClass = anyClass;
	}

	private void addTypedClass(Class<R> typedClass, R reference) {
		if (!anyClass.isAssignableFrom(typedClass)) {
			throw new IllegalArgumentException("Provided class does not extend '" + anyClass + "'.");
		}
		typedClasses.put(typedClass, reference);
	}

	public Set<Class<R>> getTypedClasses() {
		return typedClasses.keySet();
	}

	public R getTypedReference(Class<? extends T> typedClass) {
		return typedClasses.get(typedClass);
	}


	public static <T extends AnyProvider, R extends T> void register(Class<T> anyClass, Class<R> typedClass, R reference) {
		//noinspection unchecked
		AnyRegistry<T, R> anyRegistry = (AnyRegistry<T, R>) registeredData.get(anyClass);
		if (anyRegistry == null) {
			anyRegistry = new AnyRegistry<>(anyClass);
			registeredData.put(anyClass, anyRegistry);
		}
		anyRegistry.addTypedClass(typedClass, reference);
	}

	public static <T extends AnyProvider, R extends T> AnyRegistry<T, R> getRegistry(Class<T> anyClass) {
		//noinspection unchecked
		return (AnyRegistry<T, R>) registeredData.get(anyClass);
	}

	public static <T extends AnyProvider, R extends T> @Nullable AnyRegistryData checkRestrictions(Class<T> anyClass, boolean single, Class<?> exprClass) {
		AnyRegistry<T, ?> anyRegistry = AnyRegistry.getRegistry(anyClass);
		List<ChangeMode> acceptedModes = new ArrayList<>();
		List<Class<?>> acceptedClasses = new ArrayList<>();
		if (single && AnyNamed.class.isAssignableFrom(exprClass)) {
			for (Class<? extends T> typedClass : anyRegistry.getTypedClasses()) {
				if (typedClass.isAssignableFrom(exprClass)) {
					T anyTyped = anyRegistry.getTypedReference(typedClass);
					if (!anyTyped.supportsChange()) {
						Skript.error("You cant touch my no no square");
						return null;
					} else {
						acceptedModes = anyTyped.getAcceptedModes();
						acceptedClasses = anyTyped.getAcceptedClasses();
					}
					break;
				}
			}
		} else {
			for (Class<? extends T> typedClass : anyRegistry.getTypedClasses()) {
				T anyTyped = anyRegistry.getTypedReference(typedClass);
				if (anyTyped.supportsChange()) {
					acceptedModes.addAll(anyTyped.getAcceptedModes());
					acceptedClasses.addAll(anyTyped.getAcceptedClasses());
				}
			}
		}
		return new AnyRegistryData(single, acceptedModes, acceptedClasses);
	}

	public static class AnyRegistryData {
		private final @Nullable List<ChangeMode> acceptedModes;
		private final @Nullable List<Class<?>> acceptedClasses;
		private final boolean restricted;

		public AnyRegistryData(boolean restricted, @Nullable List<ChangeMode> acceptedModes, @Nullable List<Class<?>> acceptedClasses) {
			this.restricted = restricted;
			this.acceptedModes = acceptedModes;
			this.acceptedClasses = acceptedClasses;
		}

		public boolean isRestricted() {
			return restricted;
		}

		public @Nullable List<ChangeMode> getAcceptedModes() {
			return acceptedModes;
		}

		public @Nullable List<Class<?>> getAcceptedClasses() {
			return acceptedClasses;
		}
	}

}
