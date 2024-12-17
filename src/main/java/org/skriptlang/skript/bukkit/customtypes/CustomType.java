package org.skriptlang.skript.bukkit.customtypes;

import ch.njol.skript.config.SectionNode;

import java.util.HashMap;
import java.util.Map;

public class CustomType {

	private static Map<String, CustomType> registeredTypes = new HashMap<>();

	private String type;
	private SectionNode node;


	public CustomType(String type, SectionNode node) {
		this.type = type;
		this.node = node;
	}

	public String getType() {
		return type;
	}

	public SectionNode getNode() {
		return node;
	}

	public boolean build() {
		if (checkRegistry(type) != null)
			return false;
		registeredTypes.put(type, this);
		return true;
	}

	public static CustomType checkRegistry(String type) {
		return registeredTypes.get(type);
	}

}
