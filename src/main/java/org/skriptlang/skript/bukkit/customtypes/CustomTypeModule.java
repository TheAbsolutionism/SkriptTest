package org.skriptlang.skript.bukkit.customtypes;

import ch.njol.skript.Skript;

import java.io.IOException;

public class CustomTypeModule {

	public static void load() throws IOException {
		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.customtypes", "elements");
	}

}
