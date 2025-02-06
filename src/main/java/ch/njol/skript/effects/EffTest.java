package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.jetbrains.annotations.Nullable;

public class EffTest extends Effect {

	static {
		Skript.registerEffect(EffTest.class, "test values");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	public static Class<? extends Event> eventClass = PlayerEggThrowEvent.class;
	public static Class<?> valueClass = Projectile.class;

	@Override
	protected void execute(Event event) {
		Skript.adminBroadcast("Converters: " + EventValues.getEventValueConverter(eventClass, valueClass, 0));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "";
	}

}
