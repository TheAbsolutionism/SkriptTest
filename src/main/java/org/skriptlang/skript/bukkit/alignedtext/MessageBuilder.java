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

	public void setLine(int line, LineBuilder lineBuilder) {
		if (line > highestLine)
			highestLine = line;
		lines.put(line, lineBuilder);
	}

}
