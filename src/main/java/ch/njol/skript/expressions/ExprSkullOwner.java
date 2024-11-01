package ch.njol.skript.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

@Name("Skull Owner")
@Description("The skull owner of a player skull.")
@Examples({
	"set {_owner} to the skull owner of event-block",
	"set skull owner of {_block} to \"Njol\" parsed as offlineplayer",
	"set skull owner of player's tool to \"Njol\" parsed as offlineplayer"
})
@Since("2.9.0, INSERT VERSION (items)")
public class ExprSkullOwner extends SimplePropertyExpression<Object, OfflinePlayer> {

	static {
		register(ExprSkullOwner.class, OfflinePlayer.class, "(head|skull) owner", "slots/itemtypes/itemstacks/blocks");
	}

	@Override
	public @Nullable OfflinePlayer convert(Object object) {
		if (object instanceof Block block && block.getState() instanceof Skull skull) {
			return skull.getOwningPlayer();
		} else {
			ItemStack skullItem = ItemUtils.asItemStack(object);
			if (skullItem == null || !(skullItem.getItemMeta() instanceof SkullMeta skullMeta))
				return null;
			return skullMeta.getOwningPlayer();
		}
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(OfflinePlayer.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		OfflinePlayer offlinePlayer = (OfflinePlayer) delta[0];
		for (Object object : getExpr().getArray(event)) {
			if (object instanceof Block block && block.getState() instanceof Skull skull) {
				if (offlinePlayer.getName() != null) {
					skull.setOwningPlayer(offlinePlayer);
				} else if (ItemUtils.CAN_CREATE_PLAYER_PROFILE) {
					//noinspection deprecation
					skull.setOwnerProfile(Bukkit.createPlayerProfile(offlinePlayer.getUniqueId(), ""));
				} else {
					//noinspection deprecation
					skull.setOwner("");
				}
				skull.update(true, false);
			} else {
				ItemStack skullItem = ItemUtils.asItemStack(object);
				if (skullItem == null || !(skullItem.getItemMeta() instanceof SkullMeta skullMeta))
					continue;
				if (offlinePlayer.getName() != null) {
					skullMeta.setOwningPlayer(offlinePlayer);
				} else if (ItemUtils.CAN_CREATE_PLAYER_PROFILE) {
					//noinspection deprecation
					skullMeta.setOwnerProfile(Bukkit.createPlayerProfile(offlinePlayer.getUniqueId(), ""));
				} else {
					//noinspection deprecation
					skullMeta.setOwner("");
				}
				skullItem.setItemMeta(skullMeta);
				if (object instanceof Slot slot) {
					slot.setItem(skullItem);
				} else if (object instanceof ItemType itemType) {
					itemType.setItemMeta(skullMeta);
				} else if (object instanceof ItemStack itemStack) {
					itemStack.setItemMeta(skullMeta);
				}
			}
		}
	}

	@Override
	public Class<? extends OfflinePlayer> getReturnType() {
		return OfflinePlayer.class;
	}

	@Override
	protected String getPropertyName() {
		return "skull owner";
	}

}
