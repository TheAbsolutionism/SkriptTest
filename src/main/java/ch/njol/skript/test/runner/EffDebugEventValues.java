package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.jetbrains.annotations.Nullable;

public class EffDebugEventValues extends Effect {

	static {
		Skript.registerEffect(EffDebugEventValues.class, "test event[ |-]values");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	public static Class<? extends Event> eventClass = PlayerPickupItemEvent.class;
	public static Class<?> valueClass = Entity.class;

	@Override
	protected void execute(Event event) {
		//Skript.adminBroadcast("Converters: " + EventValues.getEventValueConverter(eventClass, valueClass, 0));
		Skript.adminBroadcast("Has Multiple: " + EventValues.hasMultipleConverters(eventClass, valueClass, 0));
	}

	public static void debug(String message) {
		if (!TestMode.ENABLED)
			return;
		Skript.adminBroadcast(message);
	}

	public static void debug(Class<?> checkEvent, Class<?> checkValue, String message) {
		if (checkEvent.equals(eventClass) && checkValue.equals(valueClass))
			debug(message);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "";
	}

}
