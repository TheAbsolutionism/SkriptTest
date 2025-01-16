package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.slot.EquipmentSlot.EquipSlot;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class EvtPlayerArmorChange extends SkriptEvent {

	static {
		if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerArmorChangeEvent")) {
			Skript.registerEvent("Armor Change", EvtPlayerArmorChange.class, PlayerArmorChangeEvent.class,
				"[player] armo[u]r change[d] [(from|at) %-equipmentslots%]")
				.description("Called when armor pieces of a player are changed.")
				.requiredPlugins("Paper")
				.keywords("armor", "armour")
				.examples(
					"on armor change:",
						"\tbroadcast the old armor item",
					"on player armour changed from helmet slot:",
						"\tbroadcast the new armor item"
				)
				.since("2.5, INSERT VERSION (equipment slots)");
		}
	}

	private @Nullable Literal<EquipSlot> slotLiteral;
	private @Nullable EquipSlot[] slots = null;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null) {
			//noinspection unchecked
			slotLiteral = (Literal<EquipSlot>) args[0];
			slots = slotLiteral.getArray();
			for (EquipSlot slot : slots) {
				if (slot == EquipSlot.TOOL || slot == EquipSlot.OFF_HAND || slot == EquipSlot.BODY) {
					Skript.error("You can not detect an armor change event for a '" + slot.name().replace("_", " ").toLowerCase(Locale.ENGLISH) + "'.");
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof PlayerArmorChangeEvent changeEvent))
			return false;
		if (slots == null || slots.length == 0)
			return true;
		EquipSlot changedSlot = switch (changeEvent.getSlotType()) {
			case HEAD -> EquipSlot.HELMET;
			case CHEST -> EquipSlot.CHESTPLATE;
			case LEGS -> EquipSlot.LEGGINGS;
			case FEET -> EquipSlot.BOOTS;
		};
		for (EquipSlot slot : slots) {
			if (slot == changedSlot)
				return true;
		}
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("player armor change");
		if (slotLiteral != null)
			builder.append("from", slotLiteral);
		return builder.toString();
	}

}
