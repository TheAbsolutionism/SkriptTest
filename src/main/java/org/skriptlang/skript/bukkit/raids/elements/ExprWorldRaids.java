package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Raid;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

@Name("Raids")
@Description("The active raids of a world.")
@Examples("set {_raids::*} to the raids of world \"world\"")
@Since("INSERT VERSION")
public class ExprWorldRaids extends SimplePropertyExpression<World, Raid[]> {

	static {
		Skript.registerExpression(ExprWorldRaids.class, Raid[].class, ExpressionType.PROPERTY,
			"[all [of]] [the] raids (of|in) %worlds%");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends World>) exprs[0]);
		return true;
	}

	@Override
	public @Nullable Raid[] convert(World world) {
		return world.getRaids().toArray(Raid[]::new);
	}

	@Override
	protected String getPropertyName() {
		return "raids";
	}

	@Override
	public Class<Raid[]> getReturnType() {
		return Raid[].class;
	}

}
