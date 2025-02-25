package ch.njol.skript.hooks.regions;

import ch.njol.skript.hooks.regions.WorldGuardHook.WorldGuardRegion;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilID;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import com.google.common.base.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.*;
import java.util.Map.Entry;

/**
 * Hook for Residence protection plugin. Currently supports
 * only basic operations.
 * @author bensku
 */
public class ResidenceHook extends RegionsPlugin<Residence> {
	
	public ResidenceHook() throws IOException {}
	
	@Override
	protected boolean init() {
		return super.init();
	}
	
	@Override
	public String getName() {
		return "Residence";
	}
	
	@Override
	public boolean canBuild_i(Player player, Location location) {
		ClaimedResidence claimedResidence = Residence.getInstance().getResidenceManager().getByLoc(location);
		if (claimedResidence == null)
			return true; // No claim here
		ResidencePermissions permissions = claimedResidence.getPermissions();
		return permissions.playerHas(player, Flags.build, true);
	}
	
	@SuppressWarnings("null")
	@Override
	public Collection<? extends Region> getRegionsAt_i(Location location) {
		List<ResidenceRegion> residences = new ArrayList<>();
		ClaimedResidence claimedResidence = Residence.getInstance().getResidenceManager().getByLoc(location);
		if (claimedResidence == null)
			return Collections.emptyList();
		residences.add(new ResidenceRegion(location.getWorld(), claimedResidence));
		return residences;
	}
	
	@Override
	public @Nullable Region getRegion_i(World world, String name) {
		ClaimedResidence claimedResidence = Residence.getInstance().getResidenceManager().getByName(name);
		if (claimedResidence == null)
			return null;
		return new ResidenceRegion(world, claimedResidence);
	}

	@Override
	public Region @Nullable [] getRegions_i(@Nullable World world) {
		ResidenceManager manager = Residence.getInstance().getResidenceManager();
		String[] residenceIDs = manager.getResidenceList();
		Map<World, ClaimedResidence> residences = new HashMap<>();
		for (String id : residenceIDs) {
			ClaimedResidence claimedResidence = manager.getByName(id);
			if (claimedResidence == null)
				continue;
			CuboidArea[] cuboids = claimedResidence.getAreaArray();
			if (cuboids != null && cuboids.length >= 1) {
				CuboidArea cuboidArea = cuboids[0];
				residences.put(cuboidArea.getWorld(), claimedResidence);
			} else {
				String worldName = claimedResidence.getWorld();
				World world1 = Bukkit.getWorld(worldName);
				if (world1 == null)
					continue;
				residences.put(world1, claimedResidence);
			}
		}
		List<Region> regions = new ArrayList<>();
		for (Entry<World, ClaimedResidence> residenceEntry : residences.entrySet()) {
			if (world != null && !residenceEntry.getKey().equals(world))
				continue;
			regions.add(new ResidenceRegion(residenceEntry.getKey(), residenceEntry.getValue()));
		}
		return regions.toArray(Region[]::new);
	}
	
	@Override
	public boolean hasMultipleOwners_i() {
		return true;
	}
	
	@Override
	protected Class<? extends Region> getRegionClass() {
		return WorldGuardRegion.class;
	}
	
	static {
		Variables.yggdrasil.registerSingleClass(ResidenceRegion.class);
	}
	
	@YggdrasilID("ResidenceRegion")
	public class ResidenceRegion extends Region {
		
		private transient ClaimedResidence residence;
		final World world;

		private ResidenceRegion() {
			world = null;
		}
		
		public ResidenceRegion(World world, ClaimedResidence residence) {
			this.residence = residence;
			this.world = world;
		}
		
		@Override
		public Fields serialize() throws NotSerializableException {
			Fields fields = new Fields(this);
			fields.putObject("region", residence.getName());
			return fields;
		}

		@Override
		public void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
			Object region = fields.getObject("region");
			if (!(region instanceof String stringRegion))
				throw new StreamCorruptedException("Tried to deserialize Residence region with no valid name!");
			fields.setFields(this);
			ClaimedResidence claimedResidence = Residence.getInstance().getResidenceManager().getByName(stringRegion);
			if (claimedResidence == null)
				throw new StreamCorruptedException("Invalid region " + region + " in world " + world);
			this.residence = claimedResidence;
		}

		@Override
		public boolean contains(Location location) {
			return residence.containsLoc(location);
		}

		@Override
		public boolean isMember(OfflinePlayer player) {
			return residence.getPermissions().playerHas(player.getName(), Flags.build, false);
		}

		@Override
		public Collection<OfflinePlayer> getMembers() {
			return Collections.emptyList();
		}

		@Override
		public boolean isOwner(OfflinePlayer player) {
			return Objects.equal(residence.getPermissions().getOwnerUUID(), player.getUniqueId());
		}

		@Override
		public Collection<OfflinePlayer> getOwners() {
			return Collections.singleton(Residence.getInstance().getOfflinePlayer(residence.getPermissions().getOwner()));
		}

		@Override
		public Iterator<Block> getBlocks() {
			return Collections.emptyIterator();
		}

		@Override
		public String toString() {
			return residence.getName() + " in world " + world.getName();
		}

		@Override
		public RegionsPlugin<?> getPlugin() {
			return ResidenceHook.this;
		}

		@Override
		public boolean equals(@Nullable Object obj) {
			if (!(obj instanceof ResidenceRegion other))
				return false;
			if (this == other || this.hashCode() == other.hashCode())
				return true;
			return false;
		}

		@Override
		public int hashCode() {
			return residence.getName().hashCode();
		}
		
	}

}
