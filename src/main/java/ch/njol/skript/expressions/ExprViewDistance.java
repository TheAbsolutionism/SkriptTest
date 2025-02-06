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

@Name("View Distance")
@Description({
	"The view distance of a world or a player.",
	"The view distance of a player is the distance in chunks sent by the server to the player. "
		+ "This has nothing to do with client side view distance settings.",
	"View distance is capped between 2 to 32 chunks.",
	"Paper is required to change the view distance for both worlds and players."
})
@Examples({
	"set view distance of player to 10",
	"add 50 to the view distance of world \"world\"",
	"reset the view distance of player",
	"clear the view distance of world \"world\""
})
@RequiredPlugins("Paper (change for players), Paper 1.21+ (change for worlds)")
@Since("2.4, INSERT VERSION (worlds)")
public class ExprViewDistance extends SimplePropertyExpression<Object, Integer> {

	private static final boolean SUPPORTS_SETTER = Skript.methodExists(Player.class, "setViewDistance", int.class);
	private static final boolean RUNNING_1_21 = Skript.isRunningMinecraft(1, 21, 0);

	static {
		register(ExprViewDistance.class, Integer.class, "view distance[s]", "players/worlds");
	}

	@Override
	public @Nullable Integer convert(Object object) {
		if (object instanceof Player player) {
			return player.getViewDistance();
		} else if (object instanceof World world) {
			return world.getViewDistance();
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
			Skript.error("'view distance' requires a Paper server to change.");
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
		Consumer<Player> playerConsumer = getPlayerConsumer(mode, value);
		Consumer<World> worldConsumer = getWorldConsumer(mode, value);
		for (Object object : getExpr().getArray(event)) {
			if (object instanceof Player player) {
				playerConsumer.accept(player);
			} else if (RUNNING_1_21 && object instanceof World world) {
				worldConsumer.accept(world);
			}
		}
	}

	public Consumer<Player> getPlayerConsumer(ChangeMode mode, int value) {
		return switch (mode) {
			case SET, DELETE, RESET -> player -> player.setViewDistance(Math2.fit(2, value, 32));
			case ADD -> player -> {
				int current = player.getViewDistance();
				player.setViewDistance(Math2.fit(2, current + value, 32));
			};
			case REMOVE -> player -> {
				int current = player.getViewDistance();
				player.setViewDistance(Math2.fit(2, current - value, 32));
			};
			default -> null;
		};
	}

	public Consumer<World> getWorldConsumer(ChangeMode mode, int value) {
		return switch (mode) {
			case SET, DELETE, RESET -> world -> world.setViewDistance(Math2.fit(2, value, 32));
			case ADD -> world -> {
				int current = world.getViewDistance();
				world.setViewDistance(Math2.fit(2, current + value, 32));
			};
			case REMOVE -> world -> {
				int current = world.getViewDistance();
				world.setViewDistance(Math2.fit(2, current - value, 32));
			};
			default -> null;
		};
	}

	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "view distance";
	}

}
