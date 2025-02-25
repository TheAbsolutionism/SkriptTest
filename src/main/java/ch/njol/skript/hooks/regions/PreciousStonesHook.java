package ch.njol.skript.hooks.regions;

import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.util.AABB;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilID;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.ForceFieldManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PreciousStonesHook extends RegionsPlugin<PreciousStones> {

	public PreciousStonesHook() throws IOException {}

	private static java.lang.reflect.Field FIELDS_BY_OWNER;

	@Override
	protected boolean init() {
		try {
			FIELDS_BY_OWNER = ForceFieldManager.class.getDeclaredField("fieldsByOwner");
			FIELDS_BY_OWNER.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
		return super.init();
	}

	@Override
	public String getName() {
		return "PreciousStones";
	}

	@Override
	public boolean canBuild_i(Player player, Location location) {
		return PreciousStones.API().canBreak(player, location) && PreciousStones.API().canPlace(player, location);
	}

	@Override
	public Collection<? extends Region> getRegionsAt_i(Location location) {
		Set<PreciousStonesRegion> collect = PreciousStones.API().getFieldsProtectingArea(FieldFlag.ALL, location).stream()
				.map(PreciousStonesRegion::new)
				.collect(Collectors.toSet());
		assert collect != null;
		return collect;
	}

	@Override
	public @Nullable Region getRegion_i(World world, String name) {
		return null;
	}

	@Override
	public Region @Nullable [] getRegions_i(@Nullable World world) {
		if (FIELDS_BY_OWNER == null)
			return null;
		Map<String, List<Field>> fieldMap = null;
		try {
			//noinspection unchecked
			fieldMap = (Map<String, List<Field>>) FIELDS_BY_OWNER.get(PreciousStones.getInstance());
		} catch (IllegalAccessException ignored) {}
		if (fieldMap == null || fieldMap.isEmpty())
			return null;
		if (world != null && !fieldMap.containsKey(world.getName()))
			return null;
		List<PreciousStonesRegion> regions = new ArrayList<>();
		for (Entry<String, List<Field>> fieldEntry : fieldMap.entrySet()) {
			if (world != null && !fieldEntry.getKey().equalsIgnoreCase(world.getName()))
				continue;
			for (Field field : fieldEntry.getValue()) {
				regions.add(new PreciousStonesRegion(field));
			}
		}
		return regions.toArray(Region[]::new);
	}

	@Override
	public boolean hasMultipleOwners_i() {
		return true;
	}

	@Override
	protected Class<? extends Region> getRegionClass() {
		return PreciousStonesRegion.class;
	}

	@YggdrasilID("PreciousStonesRegion")
	public final class PreciousStonesRegion extends Region {

		private transient Field field;

		public PreciousStonesRegion(Field field) {
			this.field = field;
		}

		@Override
		public boolean contains(Location location) {
			return field.envelops(location);
		}

		@Override
		public boolean isMember(OfflinePlayer player) {
			return field.isInAllowedList(player.getName());
		}

		@Override
		public Collection<OfflinePlayer> getMembers() {
			Set<OfflinePlayer> collect = field.getAllAllowed().stream()
					.map(Bukkit::getOfflinePlayer)
					.collect(Collectors.toSet());
			assert collect != null;
			return collect;
		}

		@Override
		public boolean isOwner(OfflinePlayer player) {
			return field.isOwner(player.getName());
		}

		@Override
		public Collection<OfflinePlayer> getOwners() {
			Set<OfflinePlayer> collect = Stream.of(Bukkit.getOfflinePlayer(field.getOwner()))
					.collect(Collectors.toSet());
			assert collect != null;
			return collect;
		}

		@Override
		public Iterator<Block> getBlocks() {
			List<Vector> vectors = field.getCorners();
			return new AABB(Bukkit.getWorld(field.getWorld()), vectors.get(0), vectors.get(7)).iterator();
		}

		@Override
		public String toString() {
			return field.getName() + " in world " + field.getWorld();
		}

		@Override
		public RegionsPlugin<?> getPlugin() {
			return PreciousStonesHook.this;
		}

		@Override
		public boolean equals(@Nullable Object obj) {
			if (!(obj instanceof PreciousStonesRegion other))
				return false;
			if (this == other)
				return true;
			return Objects.equals(field, other.field);
		}

		@Override
		public int hashCode() {
			return Objects.hash(field);
		}

		@Override
		public Fields serialize() throws NotSerializableException {
			return new Fields(this);
		}

		@Override
		public void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
			new Fields(fields).setFields(this);
		}

	}

}
