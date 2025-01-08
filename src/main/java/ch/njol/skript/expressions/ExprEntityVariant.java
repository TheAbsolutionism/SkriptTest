package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ExprEntityVariant extends SimplePropertyExpression<LivingEntity, Object> {

	private enum VariantStorage {
		AXOLOTL(Axolotl.class, Axolotl.Variant.class, Axolotl::getVariant, Axolotl::setVariant),
		CAT(Cat.class, Cat.Type.class, Cat::getCatType, Cat::setCatType),
		FOX(Fox.class, Fox.Type.class, Fox::getFoxType, Fox::setFoxType),
		FROG(Frog.class, Frog.Variant.class, Frog::getVariant, Frog::setVariant),
		MOOSHROOM(MushroomCow.class, MushroomCow.Variant.class, MushroomCow::getVariant, MushroomCow::setVariant),
		PARROT(Parrot.class, Parrot.Variant.class, Parrot::getVariant, Parrot::setVariant),
		RABBIT(Rabbit.class, Rabbit.Type.class, Rabbit::getRabbitType, Rabbit::setRabbitType),
		SALMON(Salmon.class, "org.bukkit.entity.Salmon$Variant", "getVariant", "setVariant"),
		WOLF(Wolf.class, "org.bukkit.entity.Wolf$Variant", "getVariant", "setVariant");


		private Class<? extends Entity> entityClass;
		private @Nullable Class<?> variantClass;
		private @Nullable Converter getVariant;
		private @Nullable BiConsumer setVariant;


		<T extends Entity, R> VariantStorage(Class<T> entityClass, String variantPath, String getter, String setter) {
			Class<?> variantClass = null;
			Converter getterFunction = null;
			BiConsumer setterFunction = null;
			try {
				variantClass = Class.forName(variantPath);
				Method getMethod = entityClass.getMethod(getter);
				getterFunction = entity -> {
					try {
						return getMethod.invoke(entity);
					} catch (IllegalAccessException | InvocationTargetException ignored) {}
					return null;
				};

				Method setMethod = entityClass.getMethod(setter, variantClass);
				setterFunction = (entity, variant) -> {
					try {
						setMethod.invoke(entity, variant);
					} catch (IllegalAccessException | InvocationTargetException ignored) {}
				};
			} catch (ClassNotFoundException | NoSuchMethodException ignored) {}
            this.entityClass = entityClass;
			this.variantClass = variantClass;
			this.getVariant = getterFunction;
			this.setVariant = setterFunction;
		}

		<T extends Entity, R> VariantStorage(Class<T> entityClass, Class<R> variantClass, Converter<T, Object> getter, @Nullable BiConsumer<T, R> setter) {
			this.entityClass = entityClass;
			this.variantClass = variantClass;
			this.getVariant = getter;
			this.setVariant = setter;
		}

	}

	private static final VariantStorage[] STORAGES = VariantStorage.values();
	private static final Class<?>[] possibleClasses;

	static {
		List<Class<?>> possibleList = new ArrayList<>();
		for (VariantStorage storage : STORAGES) {
			if (storage.entityClass != null && storage.variantClass != null)
				possibleList.add(storage.variantClass);
		}
		possibleClasses = possibleList.toArray(new Class[0]);

		registerDefault(ExprEntityVariant.class, Object.class, "entity variant[s]", "livingentities");
	}

	@Override
	public @Nullable Object convert(LivingEntity entity) {
		for (VariantStorage storage : STORAGES) {
			if (storage.entityClass != null && storage.entityClass.isInstance(entity)) {
				if (storage.variantClass != null && storage.getVariant != null)
					return storage.getVariant.convert(entity);
				return null;
			}
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return possibleClasses;
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		Object variant = delta[0];
		for (LivingEntity entity : getExpr().getArray(event)) {
			for (VariantStorage storage : STORAGES) {
				if (storage.entityClass != null && storage.entityClass.isInstance(entity)) {
					if (storage.variantClass != null && storage.setVariant != null && storage.variantClass.isInstance(variant))
						storage.setVariant.accept(entity, variant);
					break;
				}
			}
		}

	}

	@Override
	public Class<?> getReturnType() {
		return Object.class;
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		return possibleClasses;
	}

	@Override
	protected String getPropertyName() {
		return "entity variant";
	}

}
