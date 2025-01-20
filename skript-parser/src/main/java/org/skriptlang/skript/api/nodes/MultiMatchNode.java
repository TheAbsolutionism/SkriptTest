package org.skriptlang.skript.api.nodes;

/**
 * In the case of conflicts between multiple possible ways to parse tokens,
 * a multi-value node will be created.
 * Note that this only occurs with syntaxes that are ambiguous until a type is known.
 */
public interface MultiMatchNode {
}
