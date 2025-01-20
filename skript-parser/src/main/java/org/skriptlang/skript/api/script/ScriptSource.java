package org.skriptlang.skript.api.script;

import com.google.common.base.Preconditions;

/**
 * The source of a script. Implementations of this generally represent various ways the script can be sourced.
 */
public interface ScriptSource {

	/**
	 * The name of the script.
	 */
	String name();

	/**
	 * The content of the script. It is recommended that this is lazily loaded.
	 */
	String content();

	/**
	 * Gets the line number of the given index.
	 * @return An array containing the line number and column number, respectively. The size of the array is always 2.
	 */
	default int[] getLineAndColumn(int index) {
		String content = content();
		Preconditions.checkElementIndex(index, content.length());

		int line = 1;
		int column = 1;
		for (int i = 0; i < index; i++) {
			char c = content.charAt(i);
			if (c == '\n') {
				line++;
				column = 1;
			} else {
				column++;
			}
		}

		return new int[] { line, column };
	}

}
