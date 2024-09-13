package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.Skript;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EvtBlockDropTest extends SkriptJUnitTest {

	private Block oak;
	private final Player easyPlayer = EasyMock.createMock(Player.class);

	@Before
	public void setBlocks() {
		oak = setBlock(Material.OAK_LOG);
		oak.setType(Material.OAK_LOG);
	}

	@Test
	public void breakBlock() {
		Skript.adminBroadcast("I will now break you!");
		//easyPlayer.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_AXE));
		easyPlayer.breakBlock(oak);
	}

	@After
	public void cleanBlock() {}

}
