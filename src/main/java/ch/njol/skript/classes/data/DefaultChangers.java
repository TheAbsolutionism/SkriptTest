/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.classes.data;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.util.Experience;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
public class DefaultChangers {
	
	public DefaultChangers() {}
	
	public final static Changer<Entity> entityChanger = new Changer<Entity>() {
		@Override
		@Nullable
		public Class<? extends Object>[] acceptChange(final ChangeMode mode) {
			switch (mode) {
				case ADD:
					return CollectionUtils.array(ItemType[].class, Inventory.class, Experience[].class);
				case DELETE:
					return CollectionUtils.array();
				case REMOVE:
					return CollectionUtils.array(PotionEffectType[].class, ItemType[].class, Inventory.class);
				case REMOVE_ALL:
					return CollectionUtils.array(PotionEffectType[].class, ItemType[].class);
				case SET:
				case RESET: // REMIND reset entity? (unshear, remove held item, reset weapon/armour, ...)
					return null;
			}
			assert false;
			return null;
		}
		
		@Override
		public void change(final Entity[] entities, final @Nullable Object[] delta, final ChangeMode mode) {
			if (delta == null) {
				for (final Entity e : entities) {
					if (!(e instanceof Player))
						e.remove();
				}
				return;
			}
			boolean hasItem = false;
			for (final Entity e : entities) {
				for (final Object d : delta) {
					if (d instanceof PotionEffectType potionEffectType) {
						assert mode == ChangeMode.REMOVE || mode == ChangeMode.REMOVE_ALL;
						if (!(e instanceof LivingEntity livingEntity))
							continue;
						livingEntity.removePotionEffect(potionEffectType);
					} else {
						if (e instanceof Player player) {
							if (d instanceof Experience experience) {
								player.giveExp(experience.getXP());
							} else if (d instanceof Inventory inv) {
								PlayerInventory inventory = player.getInventory();
								for (ItemStack itemStack : inv) {
									if (itemStack == null)
										continue;
									if (mode == ChangeMode.ADD) {
										inventory.addItem(itemStack);
									} else {
										inventory.remove(itemStack);
									}
								}
							} else if (d instanceof ItemType itemType) {
								hasItem = true;
								final PlayerInventory invi = player.getInventory();
								if (mode == ChangeMode.ADD)
									itemType.addTo(invi);
								else if (mode == ChangeMode.REMOVE)
									itemType.removeFrom(invi);
								else
									itemType.removeAll(invi);
							}
						}
					}
				}
				if (e instanceof Player player && hasItem)
					PlayerUtils.updateInventory(player);
			}
		}
	};
	
	public final static Changer<Player> playerChanger = new Changer<Player>() {
		@Override
		@Nullable
		public Class<? extends Object>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.DELETE)
				return null;
			return entityChanger.acceptChange(mode);
		}
		
		@Override
		public void change(final Player[] players, final @Nullable Object[] delta, final ChangeMode mode) {
			entityChanger.change(players, delta, mode);
		}
	};
	
	public final static Changer<Entity> nonLivingEntityChanger = new Changer<Entity>() {
		@Override
		@Nullable
		public Class<Object>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.DELETE)
				return CollectionUtils.array();
			return null;
		}
		
		@Override
		public void change(final Entity[] entities, final @Nullable Object[] delta, final ChangeMode mode) {
			assert mode == ChangeMode.DELETE;
			for (final Entity e : entities) {
				if (e instanceof Player)
					continue;
				e.remove();
			}
		}
	};
	
	public final static Changer<Item> itemChanger = new Changer<Item>() {
		@Override
		@Nullable
		public Class<?>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.SET)
				return CollectionUtils.array(ItemStack.class);
			return nonLivingEntityChanger.acceptChange(mode);
		}
		
		@Override
		public void change(final Item[] what, final @Nullable Object[] delta, final ChangeMode mode) {
			if (mode == ChangeMode.SET) {
				assert delta != null;
				for (final Item i : what)
					i.setItemStack((ItemStack) delta[0]);
			} else {
				nonLivingEntityChanger.change(what, delta, mode);
			}
		}
	};
	
	public final static Changer<Inventory> inventoryChanger = new Changer<Inventory>() {
		
		private Material[] cachedMaterials = Material.values();
		
		@Override
		@Nullable
		public Class<? extends Object>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.RESET)
				return null;
			if (mode == ChangeMode.REMOVE_ALL)
				return CollectionUtils.array(ItemType[].class);
			if (mode == ChangeMode.SET)
				return CollectionUtils.array(ItemType[].class, Inventory.class);
			return CollectionUtils.array(ItemType[].class, Inventory[].class);
		}
		
		@Override
		public void change(final Inventory[] invis, final @Nullable Object[] delta, final ChangeMode mode) {
			for (final Inventory invi : invis) {
				assert invi != null;
				switch (mode) {
					case DELETE:
						invi.clear();
						break;
					case SET:
						invi.clear();
						//$FALL-THROUGH$
					case ADD:
						assert delta != null;
						
						if(delta instanceof ItemStack[] items) { // Old behavior - legacy code (is it used? no idea)
							if(items.length > 36) {
								return;
							}
							for (final Object d : delta) {
								if (d instanceof Inventory inventory) {
									for (final ItemStack i : inventory) {
										if (i != null)
											invi.addItem(i);
									}
								} else {
									((ItemType) d).addTo(invi);
								}
							}
						} else {
							for (final Object d : delta) {
								if (d instanceof ItemStack itemStack) {
									new ItemType(itemStack).addTo(invi); // Can't imagine why would be ItemStack, but just in case...
								} else if (d instanceof ItemType itemType) {
									itemType.addTo(invi);
								} else if (d instanceof Block block) {
									new ItemType(block).addTo(invi);
								} else {
									Skript.error("Can't " + d.toString() + " to an inventory!");
								}
							}
						}
						
						break;
					case REMOVE:
					case REMOVE_ALL:
						assert delta != null;
						if (delta.length == cachedMaterials.length) {
							// Potential fast path: remove all items -> clear inventory
							boolean equal = true;
							for (int i = 0; i < delta.length; i++) {
								if (!(delta[i] instanceof ItemType itemType)) {
									equal = false;
									break; // Not an item, take slow path
								}
								if (itemType.getMaterial() != cachedMaterials[i]) {
									equal = false;
									break;
								}
							}
							if (equal) { // Take fast path, break out before slow one
								invi.clear();
								break;
							}
						}
						
						// Slow path
						for (final Object d : delta) {
							if (d instanceof Inventory inventory) {
								assert mode == ChangeMode.REMOVE;
								for (ItemStack itemStack : inventory) {
									if (itemStack != null)
										invi.removeItem(itemStack);
								}
							} else {
								if (mode == ChangeMode.REMOVE)
									((ItemType) d).removeFrom(invi);
								else
									((ItemType) d).removeAll(invi);
							}
						}
						break;
					case RESET:
						assert false;
				}
				InventoryHolder holder = invi.getHolder();
				if (holder instanceof Player player) {
					player.updateInventory();
				}
			}
		}
	};
	
	public final static Changer<Block> blockChanger = new Changer<Block>() {
		@Override
		@Nullable
		public Class<?>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.RESET)
				return null; // REMIND regenerate?
			if (mode == ChangeMode.SET)
				return CollectionUtils.array(ItemType.class, BlockData.class);
			return CollectionUtils.array(ItemType[].class, Inventory[].class);
		}
		
		@Override
		public void change(final Block[] blocks, final @Nullable Object[] delta, final ChangeMode mode) {
			for (Block block : blocks) {
				assert block != null;
				switch (mode) {
					case SET:
						assert delta != null;
						Object object = delta[0];
						if (object instanceof ItemType itemType) {
							itemType.getBlock().setBlock(block, true);
						} else if (object instanceof BlockData blockData) {
							block.setBlockData(blockData);
						}
						break;
					case DELETE:
						block.setType(Material.AIR, true);
						break;
					case ADD:
					case REMOVE:
					case REMOVE_ALL:
						assert delta != null;
						BlockState state = block.getState();
						if (!(state instanceof InventoryHolder inventoryHolder))
							break;
						Inventory invi = inventoryHolder.getInventory();
						if (mode == ChangeMode.ADD) {
							for (Object obj : delta) {
								if (obj instanceof Inventory inventory) {
									for (ItemStack i : inventory) {
										if (i != null)
											invi.addItem(i);
									}
								} else {
									((ItemType) obj).addTo(invi);
								}
							}
						} else {
							for (Object obj : delta) {
								if (obj instanceof Inventory inventory) {
									invi.removeItem(inventory.getContents());
								} else {
									if (mode == ChangeMode.REMOVE)
										((ItemType) obj).removeFrom(invi);
									else
										((ItemType) obj).removeAll(invi);
								}
							}
						}
						state.update();
						break;
					case RESET:
						assert false;
				}
			}
		}
	};
	
}
