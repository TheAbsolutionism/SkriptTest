package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Bukkit;
import org.bukkit.Raid;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@Name("Raid Heroes")
@Description("The players that beat a raid.")
@Examples({
	"on raid finished:",
		"broadcast the raid heroes"
})
@Since("INSERT VERSION")
public class ExprRaidHeroes extends SimplePropertyExpression<Raid, Player[]> {

	static {
		registerDefault(ExprRaidHeroes.class, Player[].class, "raid heroes", "raids");
	}

	@Override
	public Player @Nullable [] convert(Raid raid) {
		return raid.getHeroes().stream().map(Bukkit::getPlayer).toArray(Player[]::new);
	}

	@Override
	protected String getPropertyName() {
		return "raid heroes";
	}

	@Override
	public Class<Player[]> getReturnType() {
		return Player[].class;
	}

}
