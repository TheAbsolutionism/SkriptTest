package org.skriptlang.skript.bukkit.alignedtext;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;

import java.io.IOException;

public class Module {

	public static void load() throws IOException {
		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit.alignedtext", "elements");

		Classes.registerClass(new ClassInfo<>(MessageBuilder.class, "messagebuilder")
			.user("message ?builders?")
		);

		Classes.registerClass(new ClassInfo<>(LineBuilder.class, "linebuilder")
			.user("line ?builders?")
		);

		Classes.registerClass(new ClassInfo<>(AlignedText.class, "alignedtext")
			.user("aligned ?texts?")
		);

	}

}
