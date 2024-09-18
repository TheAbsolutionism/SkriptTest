package org.skriptlang.skript.test.tests.syntaxes.events;

import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class EvtFurnaceTest extends SkriptJUnitTest {

	private Block furnace;
	private CookingRecipe<FurnaceRecipe> recipe;
	private Player easyMockPlayer;

	@Before
	public void setUp() {
		furnace = setBlock(Material.FURNACE);
		furnace.setType(Material.FURNACE);
		for (Recipe check : Bukkit.getRecipesFor(ItemStack.of(Material.GOLD_INGOT))) {
			if (check instanceof FurnaceRecipe) {
				recipe = (FurnaceRecipe) check;
				break;
			}
		}
		easyMockPlayer = EasyMock.niceMock(Player.class);
	}

	@Test
	public void callEvents() {
		FurnaceBurnEvent burnEvent = new FurnaceBurnEvent(furnace, ItemStack.of(Material.LAVA_BUCKET), 10);
		FurnaceSmeltEvent smeltEvent = new FurnaceSmeltEvent(furnace, ItemStack.of(Material.RAW_IRON), ItemStack.of(Material.IRON_INGOT));
		FurnaceStartSmeltEvent startEvent = new FurnaceStartSmeltEvent(furnace, ItemStack.of(Material.RAW_GOLD), recipe);
		FurnaceExtractEvent extractEvent = new FurnaceExtractEvent(easyMockPlayer, furnace, Material.COPPER_INGOT, 10, 20);

		Bukkit.getPluginManager().callEvent(burnEvent);
		Bukkit.getPluginManager().callEvent(smeltEvent);
		Bukkit.getPluginManager().callEvent(startEvent);
		Bukkit.getPluginManager().callEvent(extractEvent);
	}

}
