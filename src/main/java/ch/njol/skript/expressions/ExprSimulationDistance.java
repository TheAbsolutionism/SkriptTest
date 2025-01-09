package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Name("Simulation Distance")
@Description({
	"The simulation distance of a world or a player.",
	"Simulation distance is the minimum distance for entities to tick.",
	"Simulation distance is capped to the current view distance of the world or player.",
	"The view distance is capped between 2 and 32.",
	"Paper is required to change the simulation distance for both worlds and players."
})
@Examples({
	"set simulation distance of player to 10",
	"add 50 to the simulation distance of world \"world\"",
	"reset the simulation distance of player",
	"clear the simulation distance of world \"world\""
})
@RequiredPlugins("Paper (change players), Paper 1.21+ (change worlds)")
@Since("INSERT VERSION")
public class ExprSimulationDistance extends SimplePropertyExpression<Object, Integer> {

	private static final boolean SUPPORTS_PLAYER = Skript.methodExists(Player.class, "getSimulationDistance");
	private static final boolean SUPPORTS_SETTER = Skript.methodExists(World.class, "setSimulationDistance", int.class);
	private static final boolean RUNNING_1_21 = Skript.isRunningMinecraft(1, 21, 0);

	static {
		String property = "worlds";
		if (SUPPORTS_PLAYER)
			property = "worlds/players";
		register(ExprSimulationDistance.class, Integer.class, "simulation distance", property);
	}

	@Override
	public @Nullable Integer convert(Object object) {
		if (object instanceof World world) {
			return world.getSimulationDistance();
		} else if (SUPPORTS_PLAYER && object instanceof Player player) {
			return player.getSimulationDistance();
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (SUPPORTS_SETTER) {
			return switch (mode) {
				case SET, DELETE, RESET, ADD, REMOVE -> CollectionUtils.array(Integer.class);
				default -> null;
			};
		} else {
			Skript.error("'simulation distance' requires a Paper server to change.");
			return null;
		}
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int value = 2;
		if (mode == ChangeMode.RESET) {
			value = Bukkit.getViewDistance();
		} else if (delta != null) {
			value = (int) delta[0];
		}
		int finalValue = value;
		Consumer<Player> playerConsumer;
		Consumer<World> worldConsumer;
		switch (mode) {
			case SET, DELETE, RESET -> {
				playerConsumer = player -> player.setSimulationDistance(Math2.fit(2, finalValue, 32));
				worldConsumer = world -> world.setSimulationDistance(Math2.fit(2, finalValue, 32));
			}
			case ADD -> {
				playerConsumer = player -> {
					int current = player.getSimulationDistance();
					player.setSimulationDistance(Math2.fit(2, current + finalValue, 32));
				};
				worldConsumer = world -> {
					int current = world.getSimulationDistance();
					world.setSimulationDistance(Math2.fit(2, current + finalValue, 32));
				};
			}
			case REMOVE -> {
				playerConsumer = player -> {
					int current = player.getSimulationDistance();
					player.setSimulationDistance(Math2.fit(2, current - finalValue, 32));
				};
				worldConsumer = world -> {
					int current = world.getSimulationDistance();
					world.setSimulationDistance(Math2.fit(2, current - finalValue, 32));
				};
			}
			default -> throw new IllegalStateException("Unexpected value: " + mode);
		}
		for (Object object : getExpr().getArray(event)) {
			if (object instanceof Player player) {
				playerConsumer.accept(player);
			} else if (RUNNING_1_21 && object instanceof World world) {
				worldConsumer.accept(world);
			}
		}
	}

	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "simulation distance";
	}

}
