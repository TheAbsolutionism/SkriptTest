package ch.njol.skript.bukkitutil;

import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.EntityBlockStorage;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class EntityBlockStorageUtils {

	public enum EntityBlockStorageType {
		ENTITY_STORAGE("entity block storage"),
		BEEHIVE(Beehive.class, Bee.class, "beehive storage");

		private Class<? extends EntityBlockStorage<?>> entityStorageClass = null;
		private Class<? extends Entity> entityClass = null;
		private String codeName;
		private boolean superType = false;

		EntityBlockStorageType(Class<? extends EntityBlockStorage<?>> entityStorageClass, Class<? extends Entity> entityClass, String codeName) {
			this.entityStorageClass = entityStorageClass;
			this.entityClass = entityClass;
			this.codeName = codeName;
		}

		EntityBlockStorageType(String codeName) {
			superType = true;
			this.codeName = codeName;
		}

		public @Nullable Class<? extends EntityBlockStorage<?>> getEntityStorageClass() {
			return entityStorageClass;
		}

		public @Nullable Class<? extends Entity> getEntityClass() {
			return entityClass;
		}

		public String getCodeName() {
			return codeName;
		}

		public boolean isSuperType() {
			return superType;
		}
	}

	private static final Map<Class<? extends BlockState>, EntityBlockStorageType> blockStateToEntityStorage = new HashMap<>();

	static {
		for (EntityBlockStorageType type : EntityBlockStorageType.values()) {
			if (!type.superType)
				blockStateToEntityStorage.put(type.entityStorageClass, type);
		}
	}

	public static @Nullable EntityBlockStorageType getEnityBlockStorageType(Block block) {
		return getEntityBlockStorageType(block.getState());
	}

	public static @Nullable EntityBlockStorageType getEntityBlockStorageType(BlockState blockState) {
		return blockStateToEntityStorage.get(blockState.getClass());
	}

}
