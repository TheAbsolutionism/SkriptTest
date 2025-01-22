package ch.njol.skript.lang.util.common;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AnyProviderRegistry<T extends AnyProvider, R extends T> {

	private static final Map<Class<?>, AnyProviderRegistry<?, ?>> registeredData = new HashMap<>();

	private final Map<Class<R>, R> typedClasses = new HashMap<>();
	private final Class<T> anyClass;

	private AnyProviderRegistry(Class<T> anyClass) {
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

	private @Nullable R fromExprClass(Class<?> exprClass) {
		for (Class<R> typedClass : getTypedClasses()) {
			if (typedClass.isAssignableFrom(exprClass))
				return getTypedReference(typedClass);
		}
		return null;
	}


	public static <T extends AnyProvider, R extends T> void register(Class<T> anyClass, Class<R> typedClass, R reference) {
		//noinspection unchecked
		AnyProviderRegistry<T, R> anyProviderRegistry = (AnyProviderRegistry<T, R>) registeredData.get(anyClass);
		if (anyProviderRegistry == null) {
			anyProviderRegistry = new AnyProviderRegistry<>(anyClass);
			registeredData.put(anyClass, anyProviderRegistry);
		}
		anyProviderRegistry.addTypedClass(typedClass, reference);
	}

	public static <T extends AnyProvider, R extends T> AnyProviderRegistry<T, R> getRegistry(Class<T> anyClass) {
		//noinspection unchecked
		return (AnyProviderRegistry<T, R>) registeredData.get(anyClass);
	}

	public static <T extends AnyProvider, R extends T> @Nullable AnyProviderRegistryData checkRestrictions(Class<T> anyClass, Expression<?> exprs) {
		AnyProviderRegistry<T, ?> anyProviderRegistry = AnyProviderRegistry.getRegistry(anyClass);
		if (exprs.isSingle() && anyClass.isAssignableFrom(exprs.getReturnType())) {
			return anyProviderRegistry.getSingleData(anyClass, exprs.getReturnType());
		}
		return anyProviderRegistry.getMultipleData(anyClass, exprs);
	}

	private @Nullable AnyProviderRegistryData getSingleData(Class<T> anyClass, Class<?> exprClass) {
		R reference = fromExprClass(exprClass);
		if (reference == null || !reference.supportsChange())
			return null;
		return new AnyProviderRegistryData(true, reference.getAcceptedModes(), reference.getAcceptedClasses());
	}

	private @Nullable AnyProviderRegistryData getMultipleData(Class<T> anyClass, Expression<?> exprs) {
		List<Class<?>> allClasses = new ArrayList<>();
		List<ChangeMode> allModes = new ArrayList<>();
		for (Class<R> typedClass : getTypedClasses()) {
			if (exprs.canReturn(typedClass)) {
				R reference = getTypedReference(typedClass);
				if (!reference.supportsChange())
					continue;
				List<Class<?>> acceptedClasses = reference.getAcceptedClasses();
				if (acceptedClasses != null)
					allClasses.addAll(acceptedClasses);
				List<ChangeMode> acceptedModes = reference.getAcceptedModes();
				if (acceptedModes != null)
					allModes.addAll(acceptedModes);
			}
		}
		return new AnyProviderRegistryData(false, allModes, allClasses);
	}

	public static class AnyProviderRegistryData {
		private final @Nullable List<ChangeMode> acceptedModes;
		private final @Nullable List<Class<?>> acceptedClasses;
		private final boolean restricted;

		public AnyProviderRegistryData(boolean restricted, @Nullable List<ChangeMode> acceptedModes, @Nullable List<Class<?>> acceptedClasses) {
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
