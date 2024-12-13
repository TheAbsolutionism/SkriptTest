package org.skriptlang.skript.bukkit.memory;

import ch.njol.skript.Skript;

import java.io.IOException;

public class MemoryModule {

	public static void load() throws IOException {
		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.memory", "elements");
	}

}
