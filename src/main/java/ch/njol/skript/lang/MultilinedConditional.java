package ch.njol.skript.lang;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.sections.SecConditional;
import org.bukkit.event.Event;
import org.skriptlang.skript.lang.condition.Conditional;

/**
 * Interface to allow {@link ScriptLoader} to debug the embedded conditions within a multilined conditional section such as {@link SecConditional}
 */
public interface MultilinedConditional {

	/**
	 * If the section is multilined and should debug the conditions by grabbing through {@link #getConditional()}
	 * @return True if multilined and has embedded conditions
	 */
	boolean isMultilined();

	/**
	 * Get the embedded conditions within the section to be properly debugged.
	 * @return
	 */
	Conditional<Event> getConditional();

}
