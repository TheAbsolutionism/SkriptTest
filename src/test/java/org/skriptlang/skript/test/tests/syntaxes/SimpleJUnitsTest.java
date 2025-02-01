package org.skriptlang.skript.test.tests.syntaxes;

import ch.njol.skript.test.runner.EvtSimpleJUnit.SimpleJUnitEvent;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SimpleJUnitsTest extends SkriptJUnitTest {

	private static final List<String> tests = new ArrayList<>();

	static {
		tests.add("SimpleJUnits");

		setShutdownDelay(tests.size());
	}

	@Test
	public void runTests() {
		PluginManager pluginManager = Bukkit.getPluginManager();
		for (String currentTest : tests) {
			pluginManager.callEvent(new SimpleJUnitEvent(currentTest));
		}
	}

	@After
	public void after() {
		// Should be handled in the .sk files
	}

}
