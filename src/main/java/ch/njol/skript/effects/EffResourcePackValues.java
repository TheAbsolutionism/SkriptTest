package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.sections.EffSecSendResourcePack;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Resource Pack Values")
@Description({"Optional values to be used when sending a resource pack",
	"Can only be used in a Resource Pack Effect Section",
	"",
	"The uuid is used to store the resource pack with that as the identifier.",
	"Allowing you to remove the resource pack using the same uuid.",
	"You can allow up to 32 alphanumeric characters in the uuid.",
	"",
	"The hash is used for caching, the player won't have to re-download the resource pack that way. ",
	"The hash must be SHA-1, you can get SHA-1 hash of your resource pack using ",
	"[**this online tool**](https://emn178.github.io/online-tools/sha1_checksum.html)",
	"",
	"The prompt will be the displayed message a player sees when prompted",
	"to download the sent resource pack",
	"",
	"Using with force, forces the player to accept the request",
})
@Examples({
	"on join:",
	"   send the resource pack from \"URL\" to the player using:",
	"      set the resource pack uuid to \"1\"",
	"      set the resource pack hash to \"Hash\"",
	"      set the resource pack prompt to \"Please Download\"",
	"      force the player to accept"
})
@Since("INSERT VERSION")
public class EffResourcePackValues extends Effect {

	static {
		Skript.registerEffect(EffResourcePackValues.class,
			"set [the] [resource][ ][pack] [uu]id to %string%",
			"set [the] [resource][ ][pack] hash to %string%",
			"set [the] [resource][ ][pack] prompt [message] to %string%",
			"force [the] player[s] to accept"
		);
	}

	@Nullable
	private Expression<String> value;
	@Nullable
	private int pattern;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		boolean inResourceEvent = getParser().isCurrentEvent(EffSecSendResourcePack.ResourcePackEvent.class);
		if (!inResourceEvent) {
			Skript.error("This can only be used in the Resource Pack Effect Section");
			return false;
		}
		pattern = matchedPattern;
		if (exprs.length == 1 || exprs[0] != null) {
			value = (Expression<String>) exprs[0];
		}
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (pattern == 0) {
			((EffSecSendResourcePack.ResourcePackEvent) event).setId(value.getSingle(event));
		} else if (pattern == 1) {
			((EffSecSendResourcePack.ResourcePackEvent) event).setHash(value.getSingle(event));
		} else if (pattern == 2) {
			((EffSecSendResourcePack.ResourcePackEvent) event).setPrompt(value.getSingle(event));
		} else if (pattern == 3) {
			((EffSecSendResourcePack.ResourcePackEvent) event).setForce(true);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (pattern == 0) {
			return "set the resource pack id to " + value.getSingle(event);
		} else if (pattern == 1) {
			return "set the resource pack hash to " + value.getSingle(event);
		} else if (pattern == 3) {
			return "set the resource pack prompt to " + value.getSingle(event);
		} else if (pattern == 4) {
			return "force the player to accept";
		}
		return null;
	}


}
