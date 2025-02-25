package ch.njol.skript.hooks.regions;

import ch.njol.skript.Skript;
import ch.njol.skript.hooks.Hook;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.ClassResolver;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public abstract class RegionsPlugin<P extends Plugin> extends Hook<P> {
	
	public RegionsPlugin() throws IOException {}
	
	public static Collection<RegionsPlugin<?>> plugins = new ArrayList<>(2);
	
	static {
		Variables.yggdrasil.registerClassResolver(new ClassResolver() {
			@Override
			public @Nullable String getID(Class<?> clazz) {
				for (RegionsPlugin<?> regionsPlugin : plugins)
					if (regionsPlugin.getRegionClass() == clazz)
						return clazz.getClass().getSimpleName();
				return null;
			}
			
			@Override
			public @Nullable Class<?> getClass(String id) {
				for (RegionsPlugin<?> regionsPlugin : plugins)
					if (id.equals(regionsPlugin.getRegionClass().getSimpleName()))
						return regionsPlugin.getRegionClass();
				return null;
			}
		});
	}
	
	@Override
	protected boolean init() {
		plugins.add(this);
		return true;
	}
	
	public abstract boolean canBuild_i(Player player, Location location);
	
	public static boolean canBuild(Player player, Location location) {
		for (RegionsPlugin<?> regionsPlugin : plugins) {
			if (!regionsPlugin.canBuild_i(player, location))
				return false;
		}
		return true;
	}
	
	public abstract Collection<? extends Region> getRegionsAt_i(Location location);
	
	public static Set<? extends Region> getRegionsAt(Location location) {
		Set<Region> regions = new HashSet<>();
		Iterator<RegionsPlugin<?>> iterator = plugins.iterator();
		while (iterator.hasNext()) {
			RegionsPlugin<?> regionsPlugin = iterator.next();
			try {
				regions.addAll(regionsPlugin.getRegionsAt_i(location));
			} catch (Throwable e) { // Unstable WorldGuard API
				Skript.error(regionsPlugin.getName() + " hook crashed and was removed to prevent future errors.");
				e.printStackTrace();
				iterator.remove();
			}
		}
		return regions;
	}

	public abstract @Nullable Region getRegion_i(World world, String name);

	public static @Nullable Region getRegion(World world, String name) {
		for (RegionsPlugin<?> regionsPlugin : plugins) {
			return regionsPlugin.getRegion_i(world, name);
		}
		return null;
	}

	public abstract Region @Nullable [] getRegions_i(@Nullable World world);

	public static Region @Nullable [] getRegions() {
		return getRegions(null);
	}

	public static Region @Nullable [] getRegions(@Nullable World world) {
		for (RegionsPlugin<?> regionsPlugin : plugins) {
			return regionsPlugin.getRegions_i(world);
		}
		return null;
	}

	
	public abstract boolean hasMultipleOwners_i();
	
	public static boolean hasMultipleOwners() {
		for (final RegionsPlugin<?> pl : plugins) {
			if (pl.hasMultipleOwners_i())
				return true;
		}
		return false;
	}
	
	protected abstract Class<? extends Region> getRegionClass();

	public static @Nullable RegionsPlugin<?> getPlugin(String name) {
		for (RegionsPlugin<?> regionsPlugin : plugins) {
			if (regionsPlugin.getName().equalsIgnoreCase(name))
				return regionsPlugin;
		}
		return null;
	}
	
}
