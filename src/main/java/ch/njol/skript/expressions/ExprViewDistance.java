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
	"The view distance of a player is the distance sent by the server to the player. "
		+ "This has nothing to do with client side view distance settings.",
	"View distance is capped between 2 to 32.",
	"Requires Paper server to change the view distance for both worlds and players.",
	"Due to unstable behavior on older versions, the changing of view distance for worlds is restricted to Paper 1.21+."
})
@Examples({
	"set view distance of player to 10",
	"add 50 to the view distance of world \"world\"",
	"reset the view distance of player",
	"clear the view distance of world \"world\""
})
@RequiredPlugins("Paper (change players), Paper 1.21+ (change worlds)")
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
		int provided = mode == ChangeMode.RESET ? Bukkit.getViewDistance() : delta == null ? 2 : (Integer) delta[0];
		Consumer<Player> playerConsumer;
		Consumer<World> worldConsumer;
		switch (mode) {
			case SET, DELETE, RESET -> {
				playerConsumer = player -> player.setViewDistance(Math2.fit(2, provided, 32));
				worldConsumer = world -> world.setViewDistance(Math2.fit(2, provided, 32));
			}
			case ADD -> {
				playerConsumer = player -> {
					int current = player.getViewDistance();
					player.setViewDistance(Math2.fit(2, current + provided, 32));
				};
				worldConsumer = world -> {
					int current = world.getViewDistance();
					world.setViewDistance(Math2.fit(2, current + provided, 32));
				};
			}
			case REMOVE -> {
				playerConsumer = player -> {
					int current = player.getViewDistance();
					player.setViewDistance(Math2.fit(2, current - provided, 32));
				};
				worldConsumer = world -> {
					int current = world.getViewDistance();
					world.setViewDistance(Math2.fit(2, current - provided, 32));
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
		return "view distance";
	}

}
