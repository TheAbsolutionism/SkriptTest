package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.WorldUtils;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.skript.variables.Variables;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class EvtBlockDropTest extends SkriptJUnitTest {

	private Block oak;
	private Player easyPlayer;
	private Effect breakEffect;

	@Before
	public void setBlocks() {
		easyPlayer = EasyMock.niceMock(Player.class);
		oak = setBlock(Material.OAK_LOG);
		oak.setType(Material.OAK_LOG);
		breakEffect = Effect.parse("make {_player} break (block at location(10.5, -58.5, 0.5)) ", null);
	}

	@Test
	public void breakBlock() {
		if (breakEffect == null)
			Assert.fail("Break effect is null");

		//ContextlessEvent event = ContextlessEvent.get();
		//Variables.setVariable("player", getMockPlayer(), event, true);

		Skript.adminBroadcast("I will now break you!");
		easyPlayer.breakBlock(getBlock());
		//easyPlayer.breakBlock(oak);
		//EasyMock.expectLastCall();
		//EasyMock.replay(easyPlayer);
		//TriggerItem.walk(breakEffect, event);
		//EasyMock.verify(easyPlayer);
	}

	@After
	public void cleanBlock() {}

	private Player getMockPlayer() {
		InvocationHandler handler = (proxy, method, args) -> {
			if (method.getName().equals("getFoodLevel"))
				return 0;
			return method.invoke(easyPlayer, args);
		};
		return (Player) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { Player.class }, handler);
	}

}
