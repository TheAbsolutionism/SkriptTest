package org.skriptlang.skript.test.tests.syntaxes.effects;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.event.world.WorldLoadEvent;
import org.junit.Test;

public class EffGoatHornsTest extends SkriptJUnitTest {

	static {
		setShutdownDelay(10);
	}

	@Test
	public void test() {
		Bukkit.getPluginManager().callEvent(new WorldLoadEvent(getTestWorld()));
	}

}
