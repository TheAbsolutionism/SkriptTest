package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.Skript;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.util.SkriptColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.event.Event;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class EvtFireworkTest extends SkriptJUnitTest {

	@Test
	public void callEvents() {
		List<Event> events = new ArrayList<>();
		for (SkriptColor color : SkriptColor.values()) {
			Firework firework = (Firework) getTestWorld().spawnEntity(getTestLocation(), EntityType.FIREWORK_ROCKET);
			FireworkEffect fireworkEffect = FireworkEffect.builder().withColor(color.asDyeColor().getFireworkColor()).build();
			FireworkMeta fireworkMeta = firework.getFireworkMeta();
			fireworkMeta.addEffects(fireworkEffect);
			firework.setFireworkMeta(fireworkMeta);
			events.add(new FireworkExplodeEvent(firework));
		}

		for (Event event : events) {
			Bukkit.getPluginManager().callEvent(event);
		}
	}

}
