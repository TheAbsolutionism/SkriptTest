package org.skriptlang.skript.bukkit.alignedtext;

import java.util.ArrayList;
import java.util.List;

public class LineBuilder {

	private List<AlignedText> alignedTexts = new ArrayList<>();

	public LineBuilder(int line) {
		this.line = line;
	}
	public LineBuilder() {}

	private int line;
	private Character fillerCharacter = ' ';
	private Character leftBound = null;
	private Character rightBound = null;

	public List<AlignedText> getAlignedTexts() {
		return alignedTexts;
	}

	public void setAlignedTexts(List<AlignedText> alignedTexts) {
		this.alignedTexts = alignedTexts;
	}

	public void addAlignedText(AlignedText alignedText) {
		this.alignedTexts.add(alignedText);
	}

	public void clearAlignedTexts() {
		alignedTexts.clear();
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public Character getFillerCharacter() {
		return fillerCharacter;
	}

	public void setFillerCharacter(Character fillerCharacter) {
		this.fillerCharacter = fillerCharacter;
	}

	public Character getLeftBound() {
		return leftBound;
	}

	public void setLeftBound(Character leftBound) {
		this.leftBound = leftBound;
	}

	public Character getRightBound() {
		return rightBound;
	}

	public void setRightBound(Character rightBound) {
		this.rightBound = rightBound;
	}

}
