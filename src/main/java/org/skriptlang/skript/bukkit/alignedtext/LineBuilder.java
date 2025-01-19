package org.skriptlang.skript.bukkit.alignedtext;

import ch.njol.skript.Skript;

import java.util.ArrayList;
import java.util.List;

public class LineBuilder {

	private static final int SPACE_LENGTH = PixelUtils.getLength(' ');

	private List<AlignedText> alignedTexts = new ArrayList<>();

	public LineBuilder(int line) {
		this.line = line;
	}
	public LineBuilder() {}

	private int line;
	private String fillerCharacter = " ";
	private int fillerLength = 3;
	private String leftBound = null;
	private int leftLength = 0;
	private String rightBound = null;
	private int rightLength = 0;

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

	public String getFillerCharacter() {
		return fillerCharacter;
	}

	public void setFillerCharacter(String fillerCharacter) {
		this.fillerCharacter = fillerCharacter;
		this.fillerLength = PixelUtils.getLength(fillerCharacter);
	}

	public String getLeftBound() {
		return leftBound;
	}

	public void setLeftBound(String leftBound) {
		this.leftBound = leftBound;
		this.leftLength = PixelUtils.getLength(leftBound);
	}

	public String getRightBound() {
		return rightBound;
	}

	public void setRightBound(String rightBound) {
		this.rightBound = rightBound;
		this.rightLength = PixelUtils.getLength(rightBound);
	}

	public String build(int maxPixels) {
		List<AlignedText> leftTexts = new ArrayList<>();
		List<AlignedText> centerTexts = new ArrayList<>();
		List<AlignedText> rightTexts = new ArrayList<>();
		for (AlignedText text : alignedTexts) {
			switch (text.getAlignment()) {
				case LEFT -> leftTexts.add(text);
				case CENTER -> centerTexts.add(text);
				case RIGHT -> rightTexts.add(text);
			}
		}
		int usedPixels = 0;
		StringBuilder result = new StringBuilder();
		if (leftBound != null && !leftBound.toString().isEmpty()) {
			result.append(leftBound);
			usedPixels = addContentPixels(usedPixels, leftLength);
		}
		for (AlignedText leftText: leftTexts) {
			int leftIndent = leftText.getIndentation();
			if (leftIndent > 0) {
				result.append(" ".repeat(leftIndent));
				usedPixels = addRepeatingPixels(usedPixels, SPACE_LENGTH, leftIndent);
			}
			String content = leftText.getContent();
			result.append(content);
			usedPixels = addContentPixels(usedPixels, PixelUtils.getLength(content));
		}
		for (AlignedText centerText : centerTexts) {
			String content = centerText.getContent();
			int centerPixels = PixelUtils.getLength(content) + 1;
			int beforeText = Math.floorDiv(maxPixels - centerPixels, 2);
			if (usedPixels < beforeText) {
				int difference = beforeText - usedPixels;
				int useFillers = getRepeatableAmount(usedPixels, difference, fillerLength);
				result.append(fillerCharacter.toString().repeat(useFillers));
				usedPixels = addRepeatingPixels(usedPixels, fillerLength, useFillers);
			}
			result.append(content);
			usedPixels = addContentPixels(usedPixels, centerPixels);
		}
		for (AlignedText rightText : rightTexts) {
			String content = rightText.getContent();
			int rightPixels = PixelUtils.getLength(content);
			int beforeText = maxPixels - rightPixels;
			if (usedPixels < beforeText) {
				int difference = beforeText - usedPixels;
				int useFillers = getRepeatableAmount(usedPixels, difference, fillerLength);
				result.append(fillerCharacter.toString().repeat(useFillers));
				usedPixels = addRepeatingPixels(usedPixels, fillerLength, useFillers);
			}
			result.append(content);
			usedPixels = addContentPixels(usedPixels, rightPixels);
			int rightIndent = rightText.getIndentation();
			if (rightIndent > 0) {
				result.append(" ".repeat(rightIndent));
				usedPixels = addRepeatingPixels(usedPixels, SPACE_LENGTH, rightIndent);
			}
		}

		String end = "";
		if (usedPixels < maxPixels) {
			if (rightBound != null && !rightBound.toString().isEmpty()) {
				usedPixels = addContentPixels(usedPixels, rightLength);
				end = rightBound.toString();
			}
			int difference = maxPixels - usedPixels;
			int useFillers = getRepeatableAmount(usedPixels, difference, fillerLength);
			result.append(fillerCharacter.toString().repeat(useFillers));
			usedPixels = addRepeatingPixels(usedPixels, fillerLength, useFillers);
		}
		result.append(end);
		Skript.adminBroadcast("Ending Pixels: " + usedPixels);

		return result.toString();
	}

	private static int addRepeatingPixels(int currentPixels, int length, int amount) {
		int newPixels = currentPixels;
		if (newPixels > 0) {
			newPixels += (length + 1) * amount;
		} else {
			newPixels += length + ((length + 1) * (amount - 1));
		}
		return newPixels;
	}

	private static int addContentPixels(int currentPixels, int length) {
		int newPixels = currentPixels + length;
		if (currentPixels > 0)
			newPixels += 1;
		return newPixels;
	}

	private static int getRepeatableAmount(int currentPixels, int space, int length) {
		int remaining = space;
		int amount = 0;
		if (currentPixels == 0 && space > length) {
			amount += 1;
			remaining -= length;
		}
		int followingLength = length + 1;
		if (remaining > followingLength) {
			int div = Math.floorDiv(remaining, followingLength);
			amount += div;
		}
		return amount;
	}

}
