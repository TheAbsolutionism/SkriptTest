package org.skriptlang.skript.bukkit.customtypes.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class StructType extends Structure {

	private static final Pattern PROPERTY_PATTERN = Pattern.compile("[a-zA-Z0-9]+:[a-zA-Z]");
	private static final Pattern KEY_PATTERN = Pattern.compile("\\W");

	static {
		Skript.registerStructure(
			StructType.class,
			EntryValidator.builder()
				.addSection("properties", true)
				.addSection("create", true)
				.addSection("to string", false)
				.addSection("from string", false)
				.build(),
			"[custom] type %string%"
		);
	}

	private EntryContainer entryContainer;
	private String typeIdentifier;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, @Nullable EntryContainer entryContainer) {
		assert entryContainer != null;
		this.entryContainer = entryContainer;
		//noinspection unchecked
		typeIdentifier = ((Literal<String>) args[0]).getSingle();
		return true;
	}

	@Override
	public boolean load() {
		SectionNode propertiesNode = entryContainer.get("properties", SectionNode.class, false);
		Map<String, ClassInfo<?>> properties = new HashMap<>();
		if (propertiesNode != null) {
			propertiesNode.convertToEntries(0, ":");
			List<String> propertyErrors = new ArrayList<>();
			propertiesNode.forEach(node -> {
				String key = node.getKey();
				assert key != null;
				if (key.matches("\\W")) {
					propertyErrors.add("Invalid characters used within the key: '" + key + "'.");
				} else {
					String value = propertiesNode.getValue(key);
					assert value != null;
					assert !value.isEmpty();
					String codeName = Utils.getEnglishPlural(value).getFirst();
					ClassInfo<?> classInfo = null;
					try {
						classInfo = Classes.getClassInfo(codeName);
					} catch (SkriptAPIException ignored) {}
					if (classInfo == null) {
						propertyErrors.add("Invalid Type: " + value);
					} else {
						properties.put(key, classInfo);
					}
				}
			});
			if (!propertyErrors.isEmpty()) {
				propertyErrors.forEach(Skript::error);
				return false;
			}
		}


		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return null;
	}

}
