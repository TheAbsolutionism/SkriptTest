package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

@Name("Beacon Change Effects")
@Description("Called when a player changes one of the effects of a beacon.")
@Examples({
	"on beacon change effect:",
		"\tbroadcast primary beacon effect",
		"\tbroadcast secondary beacon effect",
		"\tbroadcast event-player",
		"\tbroadcast event-block"
})
@RequiredPlugins("Paper")
@Since("INSERT VERSION")
public class ExprBeaconChangeEffects extends SimpleExpression<PotionEffectType> {

	static {
		if (Skript.classExists("io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent")) {
			Skript.registerExpression(ExprBeaconChangeEffects.class, PotionEffectType.class, ExpressionType.SIMPLE,
				"[the] primary beacon effect",
				"[the] secondary beacon effect"
			);
		}
	}

	private boolean isPrimary;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerChangeBeaconEffectEvent.class)) {
			Skript.error("The expressions 'primary beacon effect' and 'secondary beacon effect' can only be used in a beacon change effects event.");
			return false;
		}
		isPrimary = matchedPattern == 0;
		return true;
	}

	@Override
	protected PotionEffectType @Nullable [] get(Event event) {
		if (event instanceof PlayerChangeBeaconEffectEvent beaconEvent) {
			if (isPrimary) {
				return new PotionEffectType[] {beaconEvent.getPrimary()};
			} else {
				return new PotionEffectType[] {beaconEvent.getSecondary()};
			}
		}
		return null;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<PotionEffectType> getReturnType() {
		return PotionEffectType.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return isPrimary ? "primary beacon effect" : "secondary beacon effect";
	}

}
