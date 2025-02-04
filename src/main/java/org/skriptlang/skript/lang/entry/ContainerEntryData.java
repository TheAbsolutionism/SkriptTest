package org.skriptlang.skript.lang.entry;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryValidator.EntryValidatorBuilder;
import org.skriptlang.skript.lang.structure.Structure;

/**
 * Virtually the same as {@link EntryValidator}.
 * Allows an {@link EntryValidator} used within a {@link Structure} to contain embedded {@link EntryValidator}s.
 */
public class ContainerEntryData extends EntryData<EntryContainer> {

	private final EntryValidator entryValidator;
	private EntryContainer entryContainer;

	public ContainerEntryData(String key, boolean optional, EntryValidator entryValidator) {
		super(key, null, optional);
		this.entryValidator = entryValidator;
	}

	public ContainerEntryData(String key, boolean optional, EntryValidatorBuilder validatorBuilder) {
		super(key, null, optional);
		this.entryValidator = validatorBuilder.build();
	}

	/**
	 * Since this will and should never be the main {@link EntryValidator} when used for a {@link Structure}
	 * We need to validate it when the main {@link EntryValidator} is being validated.
	 * @param sectionNode
	 * @return
	 */
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
		if (!getKey().equalsIgnoreCase(key))
			return false;
		return validate((SectionNode) node);
	}

}
