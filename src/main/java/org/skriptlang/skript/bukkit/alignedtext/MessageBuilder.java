package org.skriptlang.skript.bukkit.alignedtext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageBuilder {

	public MessageBuilder() {}

	private int pixelLength = 319;
	private Map<Integer, LineBuilder> lines = new HashMap<>();
	private int highestLine = 0;

	public LineBuilder getLine(int line) {
		return lines.get(line);
	}

	public List<LineBuilder> getLines() {
		return (List<LineBuilder>) lines.values();
	}

	public void setPixelLength(int pixelLength) {
		this.pixelLength = pixelLength;
	}

	public void setLine(int line, LineBuilder lineBuilder) {
		if (line > highestLine)
			highestLine = line;
		lines.put(line, lineBuilder);
	}

	public String build() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i <= highestLine; i++) {
			LineBuilder lineBuilder = lines.get(i);
			if (lineBuilder == null) {
				String empty = populateString("");
				result.append(empty);
			} else {
				String line = lineBuilder.build(pixelLength);
				result.append(line);
			}
			if (i < highestLine)
				result.append("\n");
		}
		return result.toString();
	}

	public String populateString(String base) {
		StringBuilder result = new StringBuilder();
		int length = pixelLength;
		int charLength = PixelUtils.getLength(' ');
		int total = 0;
		length -= 1;
		total += 1;
		total += Math.floorDiv(length, charLength + 1);
		for (int i = 0; i < total; i++)
			result.append(" ");
		return result.toString();
	}

	@Override
	public String toString() {
		return build();
	}

}
