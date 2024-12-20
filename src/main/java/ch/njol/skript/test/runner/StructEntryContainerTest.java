package ch.njol.skript.test.runner;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.structure.Structure;

public class StructEntryContainerTest extends Structure {

	static {
		Skript.registerStructure(StructEntryContainerTest.class,
			EntryValidator.builder()
				.addSection("has entry", true)
				.build(),
			"test entry container");
	}

	private EntryContainer entryContainer;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, @Nullable EntryContainer entryContainer) {
		assert entryContainer != null;
		this.entryContainer = entryContainer;
		if (entryContainer.hasEntry("has entry")) {
			return true;
		}
		assert false;
		return false;
	}

	@Override
	public boolean load() {
		SectionNode section = entryContainer.get("has entry", SectionNode.class, false);
		String structureKey = ScriptLoader.replaceOptions(section.getKey());
		Structure structure = Structure.parse(structureKey, section, "");
		getParser().setCurrentStructure(structure);
		if (structure != null) {
			structure.postLoad();
		}
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "test entry container";
	}

}
