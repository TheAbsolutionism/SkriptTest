package org.skriptlang.skript.api.nodes;

/**
 * Structure nodes are nodes that are permitted on the top-level of a script.
 */
public interface StructureNode extends EffectNode {
	public static final int DEFAULT_PRIORITY = 0;

	default int priority() {
		return DEFAULT_PRIORITY;
	}
}
