package org.skriptlang.skript.bukkit.raids;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import org.bukkit.Raid;
import org.bukkit.Raid.RaidStatus;
import org.bukkit.entity.Player;
import org.bukkit.event.raid.RaidEvent;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidStopEvent;
import org.bukkit.event.raid.RaidStopEvent.Reason;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class RaidModule {

	public static void load() throws IOException {
		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.raids", "elements");

		//EVENT VALUES//

		EventValues.registerEventValue(RaidEvent.class, Raid.class, new Getter<Raid, RaidEvent>() {
			@Override
			public Raid get(RaidEvent event) {
				return event.getRaid();
			}
		}, EventValues.TIME_NOW);

		EventValues.registerEventValue(RaidStopEvent.class, RaidStopEvent.Reason.class, new Getter<Reason, RaidStopEvent>() {
			@Override
			public Reason get(RaidStopEvent event) {
				return event.getReason();
			}
		}, EventValues.TIME_NOW);

		EventValues.registerEventValue(RaidFinishEvent.class, Player[].class, new Getter<Player[], RaidFinishEvent>() {
			@Override
			public Player @Nullable [] get(RaidFinishEvent event) {
				return event.getWinners().toArray(new Player[0]);
			}
		}, EventValues.TIME_NOW);

		EventValues.registerEventValue(RaidTriggerEvent.class, Player.class, new Getter<Player, RaidTriggerEvent>() {
			@Override
			public Player get(RaidTriggerEvent event) {
				return event.getPlayer();
			}
		}, EventValues.TIME_NOW);

		//CLASSES//

		Classes.registerClass(new ClassInfo<>(Raid.class, "raid")
			.user("raids?")
			.name("Raid")
			.description("Represents a raid.")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(Raid.class))
		);

		Classes.registerClass(new EnumClassInfo<>(RaidStopEvent.Reason.class, "raidstopreason", "raid stop reasons")
			.user("raid ?stop ?reasons?")
			.name("Raid Stop Reason")
			.description("Represents a reason why a raid stopped.")
			.since("INSERT VERSION")
		);

		Classes.registerClass(new EnumClassInfo<>(RaidStatus.class, "raidstatus", "raid statuses")
			.user("raid ?status(es)?")
			.name("Raid Status")
			.description("Represents the status of a raid.")
			.since("INSERT VERSION")
		);

	}

}
