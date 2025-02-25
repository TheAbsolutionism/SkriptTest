package ch.njol.skript.hooks.regions;

import ch.njol.skript.Skript;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.util.AABB;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilID;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
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

public class WorldGuardHook extends RegionsPlugin<WorldGuardPlugin> {
	
	public WorldGuardHook() throws IOException {}
	
	@Override
	protected boolean init() {
		if (!Skript.classExists("com.sk89q.worldedit.math.BlockVector3")) {
			Skript.error("WorldEdit you're using is not compatible with Skript. Disabling WorldGuard support!");
			return false;
		}
		return super.init();
	}
	
	@Override
	public String getName() {
		return "WorldGuard";
	}
	
	@Override
	public boolean canBuild_i(Player player, Location location) {
		if (player.hasPermission("worldguard.region.bypass." + location.getWorld().getName()))
			return true; // Build access always granted by permission
		WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();
		RegionQuery query = platform.getRegionContainer().createQuery();
		return query.testBuild(BukkitAdapter.adapt(location), plugin.wrapPlayer(player));
	}
	
	static {
		Variables.yggdrasil.registerSingleClass(WorldGuardRegion.class);
	}
	
	@YggdrasilID("WorldGuardRegion")
	public final class WorldGuardRegion extends Region {
		
		final World world;
		private transient ProtectedRegion region;

		private WorldGuardRegion() {
			world = null;
		}
		
		public WorldGuardRegion(World world, ProtectedRegion region) {
			this.world = world;
			this.region = region;
		}
		
		@Override
		public boolean contains(Location location) {
			return location.getWorld().equals(world) && region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
		}
		
		@Override
		public boolean isMember(OfflinePlayer player) {
			return region.isMember(plugin.wrapOfflinePlayer(player));
		}
		
		@Override
		public Collection<OfflinePlayer> getMembers() {
			Collection<UUID> ids = region.getMembers().getUniqueIds();
			Collection<OfflinePlayer> players = new ArrayList<>(ids.size());
			for (UUID id : ids)
				players.add(Bukkit.getOfflinePlayer(id));
			return players;
		}
		
		@Override
		public boolean isOwner(OfflinePlayer player) {
			return region.isOwner(plugin.wrapOfflinePlayer(player));
		}
		
		@Override
		public Collection<OfflinePlayer> getOwners() {
			Collection<UUID> ids = region.getOwners().getUniqueIds();
			Collection<OfflinePlayer> players = new ArrayList<>(ids.size());
			for (UUID id : ids)
				players.add(Bukkit.getOfflinePlayer(id));
			return players;
		}
		
		@Override
		public Iterator<Block> getBlocks() {
			BlockVector3 min = region.getMinimumPoint(), max = region.getMaximumPoint();
			return new AABB(world, new Vector(min.getBlockX(), min.getBlockY(), min.getBlockZ()),
					new Vector(max.getBlockX(), max.getBlockY(), max.getBlockZ())).iterator();
		}
		
		@Override
		public Fields serialize() throws NotSerializableException {
			Fields fields = new Fields(this);
			fields.putObject("region", region.getId());
			return fields;
		}

		@Override
		public void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
			String stringRegion = fields.getAndRemoveObject("region", String.class);
			fields.setFields(this);
			
			WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();
			ProtectedRegion region = platform.getRegionContainer().get(BukkitAdapter.adapt(world)).getRegion(stringRegion);
			if (region == null)
				throw new StreamCorruptedException("Invalid region " + stringRegion + " in world " + world);
			this.region = region;
		}
		
		@Override
		public String toString() {
			return region.getId() + " in world " + world.getName();
		}
		
		@Override
		public RegionsPlugin<?> getPlugin() {
			return WorldGuardHook.this;
		}
		
		@Override
		public boolean equals(@Nullable Object obj) {
			if (!(obj instanceof WorldGuardRegion other))
				return false;
			if (this == other)
				return true;
			return world.equals(other.world) && region.equals(other.region);
		}
		
		@Override
		public int hashCode() {
			return world.hashCode() * 31 + region.hashCode();
		}
		
	}

	@Override
	public Collection<? extends Region> getRegionsAt_i(@Nullable Location location) {
		ArrayList<Region> regions = new ArrayList<>();
		
		if (location == null) // Working around possible cause of issue #280
			return Collections.emptyList();
		if (location.getWorld() == null)
			return Collections.emptyList();
		
		WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();
		RegionManager manager = platform.getRegionContainer().get(BukkitAdapter.adapt(location.getWorld()));
		if (manager == null)
			return regions;
		ApplicableRegionSet applicable = manager.getApplicableRegions(BukkitAdapter.asBlockVector(location));
		if (applicable == null)
			return regions;
		for (ProtectedRegion region : applicable)
			regions.add(new WorldGuardRegion(location.getWorld(), region));
		return regions;
	}
	
	@Override
	public @Nullable Region getRegion_i(World world, String name) {
		WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();
		ProtectedRegion region = platform.getRegionContainer().get(BukkitAdapter.adapt(world)).getRegion(name);
		if (region != null)
			return new WorldGuardRegion(world, region);
		return null;
	}

	@Override
	public Region @Nullable [] getRegions_i(@Nullable World world) {
		WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();
		RegionContainer container = platform.getRegionContainer();
		Map<World, RegionManager> managers = new HashMap<>();
		if (world == null) {
			for (World world1 : Bukkit.getWorlds()) {
				managers.put(world1, container.get(BukkitAdapter.adapt(world1)));
			}
		} else {
			managers.put(world, container.get(BukkitAdapter.adapt(world)));
		}
		List<WorldGuardRegion> regions = new ArrayList<>();
		for (Entry<World, RegionManager> managerEntry : managers.entrySet()) {
			for (Entry<String, ProtectedRegion> regionEntry : managerEntry.getValue().getRegions().entrySet()) {
				regions.add(new WorldGuardRegion(managerEntry.getKey(), regionEntry.getValue()));
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
		return WorldGuardRegion.class;
	}
	
}
