package org.skriptlang.skript.api.nodes;

/**
 * An effect node is a statement with effect, executed unconditionally if it is reached.
 */
public interface EffectNode {

	void execute();

}
