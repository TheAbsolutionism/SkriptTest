package ch.njol.skript.hooks.regions;

import ch.njol.skript.Skript;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.util.AABB;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.iterator.EmptyIterator;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilID;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class GriefPreventionHook extends RegionsPlugin<GriefPrevention> {
	
	public GriefPreventionHook() throws IOException {}
	
	boolean supportsUUIDs;
	@Nullable Method getClaim;
	@Nullable Field claimsField;

	@Override
	protected boolean init() {
		// ownerID is a public field
		supportsUUIDs = Skript.fieldExists(Claim.class, "ownerID");
		try {
			getClaim = DataStore.class.getDeclaredMethod("getClaim", long.class);
			getClaim.setAccessible(true);
			if (!Claim.class.isAssignableFrom(getClaim.getReturnType()))
				getClaim = null;
		} catch (final NoSuchMethodException e) {} catch (final SecurityException e) {}
		try {
			claimsField = DataStore.class.getDeclaredField("claims");
			claimsField.setAccessible(true);
			if (!List.class.isAssignableFrom(claimsField.getType()))
				claimsField = null;
		} catch (final NoSuchFieldException e) {} catch (final SecurityException e) {}
		if (getClaim == null && claimsField == null) {
			Skript.error("Skript " + Skript.getVersion() + " is not compatible with GriefPrevention " + plugin.getDescription().getVersion() + "."
					+ " Please report this at https://github.com/SkriptLang/Skript/issues/ if this error occurred after you updated GriefPrevention.");
			return false;
		}
		return super.init();
	}

	@Nullable Claim getClaim(long id) {
		if (getClaim != null) {
			try {
				return (Claim) getClaim.invoke(plugin.dataStore, id);
			} catch (IllegalAccessException | IllegalArgumentException e) {
				assert false : e;
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e.getCause());
			}
		} else {
			assert claimsField != null;
			try {
				List<?> claims = (List<?>) claimsField.get(plugin.dataStore);
				for (Object claim : claims) {
					if (!(claim instanceof Claim claimObject))
						continue;
					if (claimObject.getID() == id)
						return claimObject;
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				assert false : e;
			}
		}
		return null;
	}
	
	@Override
	public String getName() {
		return "GriefPrevention";
	}
	
	@Override
	public boolean canBuild_i(Player player, Location location) {
		return plugin.allowBuild(player, location) == null; // returns reason string if not allowed to build
	}
	
	static {
		Variables.yggdrasil.registerSingleClass(GriefPreventionRegion.class);
	}
	
	@YggdrasilID("GriefPreventionRegion")
	public final class GriefPreventionRegion extends Region {
		
		private transient Claim claim;

		private GriefPreventionRegion() {}
		
		public GriefPreventionRegion(Claim claim) {
			this.claim = claim;
		}
		
		@Override
		public boolean contains(Location location) {
			return claim.contains(location, false, false);
		}
		
		@Override
		public boolean isMember(OfflinePlayer player) {
			return isOwner(player);
		}
		
		@Override
		public Collection<OfflinePlayer> getMembers() {
			return getOwners();
		}
		
		@Override
		public boolean isOwner(OfflinePlayer player) {
			String name = player.getName();
			if (name != null)
				return name.equalsIgnoreCase(claim.getOwnerName());
			return false; // Assume no ownership when player has never visited server
		}

		@Override
		public Collection<OfflinePlayer> getOwners() {
			if (claim.isAdminClaim() || (supportsUUIDs && claim.ownerID == null)) { // Not all claims have owners!
				return Collections.emptyList();
			} else if (supportsUUIDs) {
				return List.of(Bukkit.getOfflinePlayer(claim.ownerID));
			} else {
				return List.of(Bukkit.getOfflinePlayer(claim.getOwnerName()));
			}
		}
		
		@Override
		public Iterator<Block> getBlocks() {
			Location lower = claim.getLesserBoundaryCorner(), upper = claim.getGreaterBoundaryCorner();
			if (lower == null || upper == null || lower.getWorld() == null || upper.getWorld() == null || lower.getWorld() != upper.getWorld())
				return EmptyIterator.get();
			upper.setY(upper.getWorld().getMaxHeight() - 1);
			upper.setX(upper.getBlockX());
			upper.setZ(upper.getBlockZ());
			return new AABB(lower, upper).iterator();
		}
		
		@Override
		public String toString() {
			return "Claim #" + claim.getID();
		}

		@Override
		public Fields serialize() {
			Fields fields = new Fields();
			fields.putPrimitive("id", claim.getID());
			return fields;
		}
		
		@Override
		public void deserialize(Fields fields) throws StreamCorruptedException {
			long id = fields.getPrimitive("id", long.class);
			Claim claim1 = getClaim(id);
			if (claim1 == null)
				throw new StreamCorruptedException("Invalid claim " + id);
			claim = claim1;
		}
		
		@Override
		public RegionsPlugin<?> getPlugin() {
			return GriefPreventionHook.this;
		}
		
		@Override
		public boolean equals(@Nullable Object obj) {
			if (obj == this)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof GriefPreventionRegion other))
				return false;
			return claim.equals(other.claim);
		}
		
		@Override
		public int hashCode() {
			return claim.hashCode();
		}
		
	}

	@Override
	public Collection<? extends Region> getRegionsAt_i(Location location) {
		Claim claim = plugin.dataStore.getClaimAt(location, false, null);
		if (claim != null)
			return List.of(new GriefPreventionRegion(claim));
		return Collections.emptySet();
	}
	
	@Override
	public @Nullable Region getRegion_i(World world, String name) {
		try {
			Claim claim = getClaim(Long.parseLong(name));
			if (claim != null && world.equals(claim.getLesserBoundaryCorner().getWorld()))
				return new GriefPreventionRegion(claim);
			return null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public Region @Nullable [] getRegions_i(@Nullable World world) {
		if (claimsField == null)
			return null;
		List<Claim> claims = new ArrayList<>();
		try {
			//noinspection unchecked
			claims = (List<Claim>) claimsField.get(plugin.dataStore);
		} catch (IllegalAccessException ignored) {}
		if (claims == null || claims.isEmpty())
			return null;
		List<GriefPreventionRegion> regions = new ArrayList<>();
		for (Claim claim : claims) {
			if (world != null && !claim.getLesserBoundaryCorner().getWorld().equals(world))
				continue;
			regions.add(new GriefPreventionRegion(claim));
		}
		return regions.toArray(Region[]::new);
	}

	@Override
	public boolean hasMultipleOwners_i() {
		return false;
	}
	
	@Override
	protected Class<? extends Region> getRegionClass() {
		return GriefPreventionRegion.class;
	}

}
