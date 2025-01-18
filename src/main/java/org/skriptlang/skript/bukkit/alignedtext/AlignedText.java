package org.skriptlang.skript.bukkit.alignedtext;

import java.util.Locale;

public class AlignedText {

	public enum Alignment {
		LEFT, CENTER, RIGHT
	}

	private final String content;
	private final Alignment alignment;
	private final int indentation;

	public AlignedText(String content) {
		this(content, Alignment.LEFT, 0);
	}

	public AlignedText(String content, Alignment alignment) {
		this(content, alignment, 0);
	}

	public AlignedText(String content, Alignment alignment, int indentation) {
		this.content = content;
		this.alignment = alignment;
		this.indentation = indentation;
	}

	public String getContent() {
		return content;
	}

	public Alignment getAlignment() {
		return alignment;
	}

	public int getIndentation() {
		return indentation;
	}

	@Override
	public String toString() {
		return alignment.name().toLowerCase(Locale.ENGLISH) + " aligned text " + content;
	}

}
