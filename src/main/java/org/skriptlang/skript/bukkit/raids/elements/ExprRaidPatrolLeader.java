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
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import org.jetbrains.annotations.Nullable;

@Name("Raider Patrol Leader")
@Description("The raider type entity patrol leader of a new wave of a raid.")
@Examples({
	"on raid wave spawn:",
		"\tbroadcast event-patrol leader"
})
@Since("INSERT VERSION")
public class ExprRaidPatrolLeader extends SimpleExpression<LivingEntity> {

	static {
		Skript.registerExpression(ExprRaidPatrolLeader.class, LivingEntity.class, ExpressionType.SIMPLE,
			"[the] [event-]patrol leader");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(RaidSpawnWaveEvent.class)) {
			Skript.error("This expression can only be used in a 'Raid Wave Spawn' event.");
			return false;
		}
		return true;
	}

	@Override
	protected LivingEntity @Nullable [] get(Event event) {
		if (!(event instanceof RaidSpawnWaveEvent spawnWaveEvent))
			return null;
		return new LivingEntity[]{spawnWaveEvent.getPatrolLeader()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<LivingEntity> getReturnType() {
		return LivingEntity.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "patrol leader";
	}

}
