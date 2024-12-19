package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PlayerElytraBoostEventTest extends SkriptJUnitTest {

	private Player player;
	private Firework firework;

	@Before
	public void setUp() {
		player = EasyMock.niceMock(Player.class);
		firework = (Firework) getTestWorld().spawnEntity(getTestLocation(), EntityType.FIREWORK_ROCKET);
		firework.setTicksToDetonate(9999999);
	}

	@Test
	public void test() {
		Bukkit.getPluginManager().callEvent(new PlayerElytraBoostEvent(player, new ItemStack(Material.FIREWORK_ROCKET), firework, EquipmentSlot.HAND));
	}

	@After
	public void cleanUp() {
		firework.remove();
	}

}
