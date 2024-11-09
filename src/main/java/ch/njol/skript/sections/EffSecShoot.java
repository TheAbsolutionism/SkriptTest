package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.Getter;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Name("Shoot")
@Description("Shoots a projectile (or any other entity) from a given entity or location.")
@Examples({
	"shoot arrow from all players at speed 2",
	"shoot a pig from all players:",
		"\tadd event-entity to {_projectiles::*}"
})
@Since("INSERT VERSION")
public class EffSecShoot extends EffectSection {

	//TODO: Remove reflect method once 1.19 is no longer supported

	private enum CaseUsage {
		NOT_PROJECTILE_NO_TRIGGER,
		NOT_PROJECTILE_TRIGGER,
		PROJECTILE_NO_WORLD_NO_TRIGGER,
		PROJECTILE_NO_WORLD_TRIGGER_BUKKIT,
		PROJECTILE_NO_WORLD_TRIGGER,
		PROJECTILE_WORLD_NO_TRIGGER,
		PROJECTILE_WORLD_TRIGGER;
	}

	public static class ShootEvent extends Event {

		private Entity projectile;
		private @Nullable LivingEntity shooter;

		public ShootEvent(Entity projectile, @Nullable LivingEntity shooter) {
			this.projectile = projectile;
			this.shooter = shooter;
		}

		public Entity getProjectile() {
			return projectile;
		}

		public @Nullable LivingEntity getShooter() {
			return shooter;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	private static final boolean RUNNING_PAPER;
	private static Method launchWithBukkitConsumer;

	static {
		Skript.registerSection(EffSecShoot.class,
			"shoot %entitydatas% [from %livingentities/locations%] [(at|with) (speed|velocity) %-number%] [%-direction%]",
			"(make|let) %livingentities/locations% shoot %entitydatas% [(at|with) (speed|velocity) %-number%] [%-direction%]"
		);
		EventValues.registerEventValue(ShootEvent.class, Entity.class, new Getter<Entity, ShootEvent>() {
			@Override
			public @Nullable Entity get(ShootEvent shootEvent) {
				return shootEvent.getProjectile();
			}
		}, EventValues.TIME_NOW);
		EventValues.registerEventValue(ShootEvent.class, Projectile.class, new Getter<Projectile, ShootEvent>() {
			@Override
			public @Nullable Projectile get(ShootEvent shootEvent) {
				return shootEvent.getProjectile() instanceof Projectile projectile ? projectile : null;
			}
		}, EventValues.TIME_NOW);

		if (!Skript.isRunningMinecraft(1, 20, 3)) {
			try {
				launchWithBukkitConsumer = LivingEntity.class.getMethod("launchProjectile", Class.class, Vector.class, org.bukkit.util.Consumer.class);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}
		boolean launchHasJavaConsumer = Skript.methodExists(LivingEntity.class, "launchProjectile", Class.class, Vector.class, Consumer.class);
		RUNNING_PAPER = launchWithBukkitConsumer != null || launchHasJavaConsumer;
	}

	private final static Double DEFAULT_SPEED = 5.;
	private Expression<EntityData<?>> types;
	private Expression<?> shooters;
	private @Nullable Expression<Number> velocity;
	private @Nullable Expression<Direction> direction;
	public static Entity lastSpawned = null;
	private @Nullable Trigger trigger;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> triggerItems) {
		//noinspection unchecked
		types = (Expression<EntityData<?>>) exprs[matchedPattern];
		shooters = exprs[1 - matchedPattern];
		//noinspection unchecked
		velocity = (Expression<Number>) exprs[2];
		//noinspection unchecked
		direction = (Expression<Direction>) exprs[3];

		if (sectionNode != null) {
			AtomicBoolean delayed = new AtomicBoolean(false);
			Runnable afterLoading = () -> delayed.set(!getParser().getHasDelayBefore().isFalse());
			trigger = loadCode(sectionNode, "shoot", afterLoading, ShootEvent.class);
			if (delayed.get()) {
				Skript.error("Delays cannot be used within a 'shoot' effect section");
				return false;
			}
		}
		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		lastSpawned = null;
		Number finalVelocity = velocity != null ? velocity.getSingle(event) : DEFAULT_SPEED;
		Direction finalDirection = direction != null ? direction.getSingle(event) : Direction.IDENTITY;
		if (finalVelocity == null || finalDirection == null)
			return null;
		EntityData<?>[] data = types.getArray(event);

		for (Object shooter : shooters.getArray(event)) {
			for (EntityData<?> entityData : data) {
				Entity finalProjectile = null;
				Vector vector;
				if (shooter instanceof LivingEntity livingShooter) {
					vector = finalDirection.getDirection(livingShooter.getLocation()).multiply(finalVelocity.doubleValue());
					//noinspection rawtypes
					Consumer afterSpawn = afterSpawn(event, entityData, livingShooter);
					Class<? extends Entity> type = entityData.getType();
					Location shooterLoc = livingShooter.getLocation();
					shooterLoc.setY(shooterLoc.getY() + livingShooter.getEyeHeight() / 2);
					boolean isProjectile = false, useWorldSpawn = false;
					if (Fireball.class.isAssignableFrom(type)) {
						shooterLoc = livingShooter.getEyeLocation().add(vector.clone().normalize().multiply(0.5));
						isProjectile = true;
						useWorldSpawn = true;
					} else if (Projectile.class.isAssignableFrom(type)) {
						isProjectile = true;
						if (trigger != null && !RUNNING_PAPER) {
							useWorldSpawn = true;
						}
					}

					CaseUsage caseUsage = getCaseUsage(isProjectile, useWorldSpawn, trigger != null);

					switch (caseUsage) {
						case NOT_PROJECTILE_NO_TRIGGER -> {
							finalProjectile = entityData.spawn(shooterLoc);
						}
						case NOT_PROJECTILE_TRIGGER -> {
							//noinspection unchecked
							entityData.spawn(shooterLoc, afterSpawn);
						}
						case PROJECTILE_NO_WORLD_NO_TRIGGER -> {
							//noinspection unchecked
							finalProjectile = livingShooter.launchProjectile((Class<? extends Projectile>) type);
							set(finalProjectile, entityData);
						}
						case PROJECTILE_NO_WORLD_TRIGGER_BUKKIT -> {
							try {
								launchWithBukkitConsumer.invoke(livingShooter, type, vector, afterSpawnBukkit(event, entityData, livingShooter));
							} catch (Exception ignored) {};
						}
						case PROJECTILE_NO_WORLD_TRIGGER -> {
							//noinspection unchecked
							livingShooter.launchProjectile((Class<? extends Projectile>) type, vector, afterSpawn);
						}
						case PROJECTILE_WORLD_NO_TRIGGER -> {
							Projectile projectile = (Projectile) livingShooter.getWorld().spawn(shooterLoc, type);
							projectile.setShooter(livingShooter);
							finalProjectile = projectile;
						}
						case PROJECTILE_WORLD_TRIGGER -> {
							//noinspection unchecked
							livingShooter.getWorld().spawn(shooterLoc, type, afterSpawn);
						}
						default -> throw new IllegalStateException("Unexpected value: " + caseUsage);
					}
				} else {
					vector = finalDirection.getDirection((Location) shooter).multiply(finalVelocity.doubleValue());
					if (trigger != null) {
						//noinspection unchecked,rawtypes
						entityData.spawn((Location) shooter, (Consumer) afterSpawn(event, entityData, null));
					} else {
						finalProjectile = entityData.spawn((Location) shooter);
					}
				}
				if (finalProjectile != null) {
					finalProjectile.setVelocity(vector);
					lastSpawned = finalProjectile;
				}
			}
		}

		return super.walk(event, false);
	}

	private static <E extends Entity> void set(Entity entity, EntityData<E> entityData) {
		//noinspection unchecked
		entityData.set((E) entity);
	}

	private CaseUsage getCaseUsage(Boolean isProjectile, Boolean useWorldSpawn, Boolean hasTrigger) {
		if (!isProjectile) {
			if (!hasTrigger)
				return CaseUsage.NOT_PROJECTILE_NO_TRIGGER;
			return CaseUsage.NOT_PROJECTILE_TRIGGER;
		}
		if (!useWorldSpawn) {
			if (!hasTrigger)
				return CaseUsage.PROJECTILE_NO_WORLD_NO_TRIGGER;
			if (launchWithBukkitConsumer != null)
				return CaseUsage.PROJECTILE_NO_WORLD_TRIGGER_BUKKIT;
			return CaseUsage.PROJECTILE_NO_WORLD_TRIGGER;
		}
		if (!hasTrigger)
			return CaseUsage.PROJECTILE_WORLD_NO_TRIGGER;
		return CaseUsage.PROJECTILE_WORLD_TRIGGER;
	}

	private Consumer<? extends Entity> afterSpawn(Event event, EntityData<?> entityData, @Nullable LivingEntity shooter) {
		return entity -> {
			if (entity instanceof Fireball fireball)
				fireball.setShooter(shooter);
			else if (entity instanceof Projectile projectile && shooter != null) {
				projectile.setShooter(shooter);
				set(projectile, entityData);
			}
			ShootEvent shootEvent = new ShootEvent(entity, shooter);
			lastSpawned = entity;
			Variables.setLocalVariables(shootEvent, Variables.copyLocalVariables(event));
			TriggerItem.walk(trigger, shootEvent);
			Variables.setLocalVariables(event, Variables.copyLocalVariables(shootEvent));
			Variables.removeLocals(shootEvent);
		};
	}

	/**
	 * MC 1.19 uses Bukkit Consumer for LivingEntity#launchProjectile instead of Java Consumer
	 */
	@SuppressWarnings("deprecation")
	private org.bukkit.util.Consumer<? extends Entity> afterSpawnBukkit(Event event, EntityData<?> entityData, @Nullable LivingEntity shooter) {
		return entity -> {
			if (entity instanceof Fireball fireball)
				fireball.setShooter(shooter);
			else if (entity instanceof Projectile projectile && shooter != null) {
				projectile.setShooter(shooter);
				set(projectile, entityData);
			}
			ShootEvent shootEvent = new ShootEvent(entity, shooter);
			lastSpawned = entity;
			Variables.setLocalVariables(shootEvent, Variables.copyLocalVariables(event));
			TriggerItem.walk(trigger, shootEvent);
			Variables.setLocalVariables(event, Variables.copyLocalVariables(shootEvent));
			Variables.removeLocals(shootEvent);
		};
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "shoot " + types.toString(event, debug) + " from " + shooters.toString(event, debug) +
			(velocity != null ? " at speed " + velocity.toString(event, debug) : "") +
			(direction != null ? " " + direction.toString(event, debug) : "");
	}

}
