package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Getter;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Consumer;


public class ExprSpawnerData extends PropertyExpression<Block, Object> {

	/*
	1.19
		Delay
		MaxSpawnDelay
		MinSpawnDelay
		MaxNearbyEntities
		RequiredPlayerRange
		SpawnCount
		SpawnedType
		SpawnRange

	1.20.4
		PotentialSpawns
		SpawnedEntity

	Paper
		MaxSpawnDelay
		MinSpawnDelay
		MaxNearbyEntities
		SpawnCount

	TODO:
		ExprTrialSpawnerData
		CreatureSpawner#isActivated -  Cond
		Add Trial Spawners into here
		SpawnerEntries Bukkit Classs + Lang
		ExprSpawnerEntry : Create + Edit

	 */

	private static final boolean RUNNING_1_20 = Skript.isRunningMinecraft(1, 20, 4);
	private static final boolean SUPPORTS_POTENTIAL = Skript.methodExists(CreatureSpawner.class, "getPotentialSpawns");
	private static final boolean SUPPORTS_MAX_DELAY = Skript.methodExists(CreatureSpawner.class, "getMaxDelay");
	private static final boolean SUPPORTS_MIN_DELAY = Skript.methodExists(CreatureSpawner.class, "getMinDelay");
	private static final boolean SUPPORTS_MAX_NEARBY = Skript.methodExists(CreatureSpawner.class, "getMaxNearbyEntities");
	private static final boolean SUPPORTS_SPAWN_COUNT = Skript.methodExists(CreatureSpawner.class, "getSpawnCount");
	private static final boolean SUPPORTS_SPAWN_ENTITY = Skript.methodExists(CreatureSpawner.class, "getSpawnedEntity");

	public enum SpawnerData {

		DELAY("[mob|creature|entity] [spawner] delay", "delay"),
		MAXDELAY("[mob|creature|entity] [spawner] max[imum] delay", SUPPORTS_MAX_DELAY, "Can only use 'max delay' on Paper", "maximum delay"),
		MINDELAY("[mob|creature|entity] [spawner] min[imum] delay", SUPPORTS_MIN_DELAY, "Can only use 'min delay' on Paper", "minimum delay"),
		MAXNEARBY("[mob|creature|entity] [spawner] max[imum] nearby entities", SUPPORTS_MAX_NEARBY, "Can only use 'max nearby entities' on Paper", "maximum nearby entities"),
		REQUIREDRANGE("[mob|creature|entity] [spawner] required [player] range", "required player range"),
		SPAWNCOUNT("[mob|creature|entity] [spawner] spawn count", SUPPORTS_SPAWN_COUNT, "Can only use 'spawn count' on Paper", "spawn count"),
		SPAWNRANGE("[mob|creature|entity] [spawner] spawn range", "spawn range"),
		SPAWNTYPE("((spawner|entity|creature)|[mob|creature|entity] [spawner]) type[s]", "entity type"),
		POTENTIALTYPE("[mob|creature|entity] [spawner] potential spawn entites", SUPPORTS_POTENTIAL, "Can only use 'potential spawn entities' on 1.20.4+", "potential entities"),
		SPAWNENTITY("[mob|creature|entity] [spawner] spawn entity", SUPPORTS_SPAWN_ENTITY, "Can only use 'spawn entity' on 1.20.4+", "entity");


		private String pattern, toString, error;
		private boolean canRun = false;

		SpawnerData(String pattern, String toString) {
			this.pattern = pattern;
			this.toString = toString;
		}

		SpawnerData(String pattern, boolean canRun, String error, String toString) {
			this.pattern = pattern;
			this.canRun = canRun;
			this.error = error;
			this.toString = toString;
		}
	}

	private static SpawnerData[] spawnerData = SpawnerData.values();

    static {
        int size = spawnerData.length;
        String[] patterns = new String[size * 2];
        for (SpawnerData data : spawnerData) {
            patterns[2 * data.ordinal()] = "%blocks%'[s] " + data.pattern;
            patterns[2 * data.ordinal() + 1] = "[the] " + data.pattern + " of %blocks%";
        }
        Skript.registerExpression(ExprSpawnerData.class, Object.class, ExpressionType.PROPERTY, patterns);
    }

	private SpawnerData selectedData;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		selectedData = spawnerData[(int) Math2.floor(matchedPattern / 2)];
		if (!selectedData.canRun) {
			Skript.error(selectedData.error);
			return false;
		}
		setExpr((Expression<Block>) exprs[0]);
		return true;
	}

	@Override
	protected Object[] get(Event event, Block[] source) {
		return get(source, new Getter<Object, Block>() {
			@Override
			public @Nullable Object get(Block block) {
				if (!(block.getState() instanceof CreatureSpawner creatureSpawner))
					return null;
				return switch (selectedData) {
					case DELAY -> creatureSpawner.getDelay();
					case MAXDELAY -> creatureSpawner.getMaxSpawnDelay();
					case MINDELAY -> creatureSpawner.getMinSpawnDelay();
					case MAXNEARBY -> creatureSpawner.getMaxNearbyEntities();
					case REQUIREDRANGE -> creatureSpawner.getRequiredPlayerRange();
					case SPAWNCOUNT -> creatureSpawner.getSpawnCount();
					case SPAWNRANGE -> creatureSpawner.getSpawnRange();
					case SPAWNTYPE -> EntityUtils.toSkriptEntityData(creatureSpawner.getSpawnedType());
					case POTENTIALTYPE -> creatureSpawner.getPotentialSpawns().toArray();
					case SPAWNENTITY -> creatureSpawner.getSpawnedEntity();
					default -> null;
				};
			}
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode != ChangeMode.REMOVE_ALL) {
			return switch (selectedData) {
				case DELAY, MAXDELAY, MINDELAY, MAXNEARBY, REQUIREDRANGE, SPAWNCOUNT, SPAWNRANGE ->
					switch (mode) {
						case ADD, REMOVE, DELETE, SET -> CollectionUtils.array(Integer.class);
						default -> null;
					};
				case SPAWNTYPE ->
					switch (mode) {
						case SET, DELETE, RESET -> CollectionUtils.array(EntityData.class);
						default -> null;
					};
				case POTENTIALTYPE ->
					switch (mode) {
						case ADD, REMOVE, DELETE, SET -> CollectionUtils.array(SpawnerEntry.class);
						default -> null;
					};
				case SPAWNENTITY -> {
					if (mode == ChangeMode.SET) {
						yield CollectionUtils.array(Entity.class);
					}
					yield null;
				}
				default -> null;
			};
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int providedInt = 0;
		EntityData<?> providedType = null;
		Entity providedEntity = null;
		SpawnerEntry[] providedEntries = null;
		if (delta[0] != null) {
			if (delta[0] instanceof Integer integer) {
				providedInt = integer;
			} else if (delta[0] instanceof EntityData<?> entityData) {
				providedType = entityData;
			} else if (delta[0] instanceof Entity entity) {
				providedEntity = entity;
			} else if (delta[0] instanceof SpawnerEntry) {
				providedEntries = Arrays.stream(delta).toArray(SpawnerEntry[]::new);
			}
		}
		int finalProvidedInt = providedInt;
		EntityType finalProvidedType = providedType != null ? EntityUtils.toBukkitEntityType(providedType) : null;
		Entity finalProvidedEntity = providedEntity;
		switch (selectedData) {
			case DELAY -> {
				switch (mode) {
					case ADD -> changeSpawner(event, creatureSpawner -> creatureSpawner.setDelay(Math2.fit(0, creatureSpawner.getDelay() + finalProvidedInt, Integer.MAX_VALUE)));
					case REMOVE -> changeSpawner(event, creatureSpawner -> creatureSpawner.setDelay(Math2.fit(0, creatureSpawner.getDelay() - finalProvidedInt, Integer.MAX_VALUE)));
					case SET -> changeSpawner(event, creatureSpawner -> creatureSpawner.setDelay(finalProvidedInt));
					case DELETE -> changeSpawner(event, creatureSpawner -> creatureSpawner.setDelay(0));
				}
			}
			case MAXDELAY -> {
				switch (mode) {
					case ADD -> changeSpawner(event, creatureSpawner -> creatureSpawner.setMaxSpawnDelay(Math2.fit(0, creatureSpawner.getMaxSpawnDelay() + finalProvidedInt, Integer.MAX_VALUE)));
					case REMOVE -> changeSpawner(event, creatureSpawner -> creatureSpawner.setMaxSpawnDelay(Math2.fit(0, creatureSpawner.getMaxSpawnDelay() - finalProvidedInt, Integer.MAX_VALUE)));
					case SET -> changeSpawner(event, creatureSpawner -> creatureSpawner.setMaxSpawnDelay(finalProvidedInt));
					case DELETE -> changeSpawner(event, creatureSpawner -> creatureSpawner.setMaxSpawnDelay(0));
				}
			}
			case MINDELAY -> {
				switch (mode) {
					case ADD -> changeSpawner(event, creatureSpawner -> creatureSpawner.setMinSpawnDelay(Math2.fit(0, creatureSpawner.getMinSpawnDelay() + finalProvidedInt, Integer.MAX_VALUE)));
					case REMOVE -> changeSpawner(event, creatureSpawner -> creatureSpawner.setMinSpawnDelay(Math2.fit(0, creatureSpawner.getMinSpawnDelay() - finalProvidedInt, Integer.MAX_VALUE)));
					case SET -> changeSpawner(event, creatureSpawner -> creatureSpawner.setMinSpawnDelay(finalProvidedInt));
					case DELETE -> changeSpawner(event, creatureSpawner -> creatureSpawner.setMinSpawnDelay(0));
				}
			}
			case MAXNEARBY -> {
				switch (mode) {
					case ADD -> changeSpawner(event, creatureSpawner -> creatureSpawner.setMaxNearbyEntities(Math2.fit(0, creatureSpawner.getMaxNearbyEntities() + finalProvidedInt, Integer.MAX_VALUE)));
					case REMOVE -> changeSpawner(event, creatureSpawner -> creatureSpawner.setMaxNearbyEntities(Math2.fit(0, creatureSpawner.getMaxNearbyEntities() - finalProvidedInt, Integer.MAX_VALUE)));
					case SET -> changeSpawner(event, creatureSpawner -> creatureSpawner.setMaxNearbyEntities(finalProvidedInt));
					case DELETE -> changeSpawner(event, creatureSpawner -> creatureSpawner.setMaxNearbyEntities(0));
				}
			}
			case REQUIREDRANGE -> {
				switch (mode) {
					case ADD -> changeSpawner(event, creatureSpawner -> creatureSpawner.setRequiredPlayerRange(Math2.fit(0, creatureSpawner.getRequiredPlayerRange() + finalProvidedInt, Integer.MAX_VALUE)));
					case REMOVE -> changeSpawner(event, creatureSpawner -> creatureSpawner.setRequiredPlayerRange(Math2.fit(0, creatureSpawner.getRequiredPlayerRange() - finalProvidedInt, Integer.MAX_VALUE)));
					case SET -> changeSpawner(event, creatureSpawner -> creatureSpawner.setRequiredPlayerRange(finalProvidedInt));
					case DELETE -> changeSpawner(event, creatureSpawner -> creatureSpawner.setRequiredPlayerRange(0));
				}
			}
			case SPAWNCOUNT -> {
				switch (mode) {
					case ADD -> changeSpawner(event, creatureSpawner -> creatureSpawner.setSpawnCount(Math2.fit(0, creatureSpawner.getSpawnCount() + finalProvidedInt, Integer.MAX_VALUE)));
					case REMOVE -> changeSpawner(event, creatureSpawner -> creatureSpawner.setSpawnCount(Math2.fit(0, creatureSpawner.getSpawnCount() - finalProvidedInt, Integer.MAX_VALUE)));
					case SET -> changeSpawner(event, creatureSpawner -> creatureSpawner.setSpawnCount(finalProvidedInt));
					case DELETE -> changeSpawner(event, creatureSpawner -> creatureSpawner.setSpawnCount(0));
				}
			}
			case SPAWNRANGE -> {
				switch (mode) {
					case ADD -> changeSpawner(event, creatureSpawner -> creatureSpawner.setSpawnRange(Math2.fit(0, creatureSpawner.getSpawnRange() + finalProvidedInt, Integer.MAX_VALUE)));
					case REMOVE -> changeSpawner(event, creatureSpawner -> creatureSpawner.setSpawnRange(Math2.fit(0, creatureSpawner.getSpawnRange() - finalProvidedInt, Integer.MAX_VALUE)));
					case SET -> changeSpawner(event, creatureSpawner -> creatureSpawner.setSpawnRange(finalProvidedInt));
					case DELETE -> changeSpawner(event, creatureSpawner -> creatureSpawner.setSpawnRange(0));
				}
			}
			case SPAWNTYPE -> {
				switch (mode) {
					case SET -> changeSpawner(event, creatureSpawner -> creatureSpawner.setSpawnedType(finalProvidedType));
					case DELETE -> {
						EntityType toSet = RUNNING_1_20 ? null : EntityType.PIG;
						changeSpawner(event, creatureSpawner -> creatureSpawner.setSpawnedType(toSet));
					}
				}
			}
			case POTENTIALTYPE -> {
				// COme back to
			}
			case SPAWNENTITY -> changeSpawner(event, creatureSpawner -> {
                assert finalProvidedEntity != null;
                creatureSpawner.setSpawnedEntity(finalProvidedEntity.createSnapshot());
            });
		}
	}

	private void changeSpawner(Event event, Consumer<CreatureSpawner> changer) {
		for (Block block : getExpr().getArray(event)) {
			if (block.getState() instanceof CreatureSpawner creatureSpawner) {
				changer.accept(creatureSpawner);
				creatureSpawner.update(true);
			}
		}
	}

	@Override
	public Class<?> getReturnType() {
		return switch (selectedData) {
			case DELAY, MAXDELAY, MINDELAY, MAXNEARBY, REQUIREDRANGE, SPAWNCOUNT, SPAWNRANGE -> Integer.class;
			case SPAWNTYPE -> EntityData.class;
			case POTENTIALTYPE -> SpawnerEntry.class;
			case SPAWNENTITY -> Entity.class;
			default -> null;
		};
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "spawner " + selectedData.toString + " of " + getExpr().toString(event, debug);
	}

}
