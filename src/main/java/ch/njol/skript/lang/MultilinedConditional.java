package ch.njol.skript.lang;

import org.bukkit.event.Event;
import org.skriptlang.skript.lang.condition.Conditional;

public interface MultilinedConditional {

	boolean isMultilined();

	Conditional<Event> getConditional();

}
