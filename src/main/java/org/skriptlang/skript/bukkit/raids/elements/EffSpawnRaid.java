package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.raids.MutableRaid;

public class EffSpawnRaid extends Effect {

	static {
		Skript.registerEffect(EffSpawnRaid.class,
			"spawn [a] [new] raid %direction% %location% by %player%");
	}

	private Expression<Location> exprLocation;
	private Expression<Player> exprPlayer;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		exprLocation = Direction.combine((Expression<Direction>) exprs[0], (Expression<Location>) exprs[1]);
		//noinspection unchecked
		exprPlayer = (Expression<Player>) exprs[2];
		return true;
	}

	@Override
	protected void execute(Event event) {
		Location location = exprLocation.getSingle(event);
		if (location == null)
			return;
		World world = location.getWorld();
		if (world == null)
			return;
		Player player = exprPlayer.getSingle(event);
		if (player == null)
			return;
		MutableRaid mutableRaid = new MutableRaid(location);
		Bukkit.getPluginManager().callEvent(new RaidTriggerEvent(mutableRaid, world, player));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return null;
	}

}
