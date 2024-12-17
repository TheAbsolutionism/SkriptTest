package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.Raid;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Raid Heroes")
@Description("The players that beat a raid.")
@Examples({
	"on raid finished:",
		"broadcast the raid heroes"
})
@Since("INSERT VERSION")
public class ExprRaidHeroes extends PropertyExpression<Raid, Player> {

	static {
		registerDefault(ExprRaidHeroes.class, Player.class, "raid heroes", "raids");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends Raid>) exprs[0]);
		return true;
	}

	@Override
	protected Player @Nullable [] get(Event event, Raid[] source) {
		List<Player> players = new ArrayList<>();
		for (Raid raid : getExpr().getArray(event)) {
			players.addAll(raid.getHeroes().stream().map(Bukkit::getPlayer).toList());
		}
		return players.toArray(new Player[0]);
	}

	@Override
	public Class<Player> getReturnType() {
		return Player.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return null;
	}

}
