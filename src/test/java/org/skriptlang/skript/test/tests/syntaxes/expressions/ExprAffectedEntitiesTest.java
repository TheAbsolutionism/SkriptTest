package org.skriptlang.skript.test.tests.syntaxes.expressions;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.entity.Pig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ExprAffectedEntitiesTest extends SkriptJUnitTest {

	private Effect spawn, spawn2, shoot;
	private Pig piggy = spawnTestPig();

	@Before
	public void setUp() {
		spawn = Effect.parse("spawn a thrown lingering potion of weakness at (spawn of world \"world\" ~ vector(0,5,0))", null);
		spawn2 = Effect.parse("spawn a thrown lingering potion of weakness at (all living entities)", null);
		shoot = Effect.parse("make (all living entities) shoot (a thrown lingering potion of weakness) at speed 0 vector(0,1,0)", null);
	}

	@Test
	public void callEvent() {
		if (spawn == null)
			Assert.fail("Spawn effect is null");
		if (spawn2 == null)
			Assert.fail("Spawn-2 effect is null");
		if (shoot == null)
			Assert.fail("Shoot effect is null");
		ContextlessEvent event = ContextlessEvent.get();
		TriggerItem.walk(spawn, event);
		TriggerItem.walk(spawn2, event);
	}

	@After
	public void cleanup() {
		if (piggy != null)
			piggy.remove();
	}

}
