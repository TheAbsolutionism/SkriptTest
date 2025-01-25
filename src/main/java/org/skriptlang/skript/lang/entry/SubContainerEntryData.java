package org.skriptlang.skript.lang.entry;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryValidator.EntryValidatorBuilder;

public class SubContainerEntryData extends EntryData<EntryContainer> {

	private final EntryValidator entryValidator;
	private EntryContainer entryContainer;

	public SubContainerEntryData(String key, boolean optional, EntryValidator entryValidator) {
		super(key, null, optional);
		this.entryValidator = entryValidator;
	}

	public SubContainerEntryData(String key, boolean optional, EntryValidatorBuilder validatorBuilder) {
		super(key, null, optional);
		this.entryValidator = validatorBuilder.build();
	}

	public boolean validate(SectionNode sectionNode) {
		EntryContainer container = entryValidator.validate(sectionNode);
		entryContainer = container;
		return container != null;
	}

	@Override
	public @Nullable EntryContainer getValue(Node node) {
		return entryContainer;
	}

	@Override
	public boolean canCreateWith(Node node) {
		if (!(node instanceof SectionNode))
			return false;
		String key = node.getKey();
		if (key == null)
			return false;
		key = ScriptLoader.replaceOptions(key);
		return getKey().equalsIgnoreCase(key);
	}

}
