package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Raid;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Raids")
@Description("The active raids of a world.")
@Examples("set {_raids::*} to the raids of world \"world\"")
@Since("INSERT VERSION")
public class ExprWorldRaids extends SimpleExpression<Raid> {

	static {
		Skript.registerExpression(ExprWorldRaids.class, Raid.class, ExpressionType.PROPERTY,
			"[all [of]] [the] raids (of|in) %worlds%");
	}

	private Expression<World> exprWorld;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		exprWorld = (Expression<World>) exprs[0];
		return true;
	}

	@Override
	protected Raid @Nullable [] get(Event event) {
		List<Raid> raids = new ArrayList<>();
		for (World world : exprWorld.getArray(event)) {
			raids.addAll(world.getRaids());
		}
		return raids.toArray(new Raid[0]);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<Raid> getReturnType() {
		return Raid.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return null;
	}

}
