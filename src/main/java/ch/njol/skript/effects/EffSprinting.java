package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Sprinting")
@Description("Make a player start or stop sprinting.")
@Examples({
	"make player start sprinting",
	"make last spawned camel sprint"
})
@Since("INSERT VERSION")
public class EffSprinting extends Effect {

	static {
		Skript.registerEffect(EffSprinting.class,
			"make %players% (start sprinting|sprint)",
			"make %players% (stop sprinting|not sprint)");
	}

	private Expression<Player> players;
	private boolean sprint;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		players = (Expression<Player>) exprs[0];
		sprint = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Player player : players.getArray(event)) {
			player.setSprinting(sprint);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + players.toString(event, debug) + (sprint ? " start" : " stop") + " sprinting";
	}

}
