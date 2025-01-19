package org.skriptlang.skript.bukkit.alignedtext;

import org.bukkit.map.MinecraftFont;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

public class PixelUtils {

	private static final String NORMAL_CHARACTERS = "abcdefghijklmnopqrstuvwxyz"
		+ "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
		+ "1234567890"
		+ "~!@#$%^&*()-_=+[{]}\\|;:'\",<.>/? ";

	private static final Font FONT = new Font("Arial", Font.PLAIN, 4);

	private static final Map<Character, Integer> loggedCharacters = new HashMap<>();

	static {
		char[] chars = NORMAL_CHARACTERS.toCharArray();
		for (Character c : chars)
			loggedCharacters.put(c, MinecraftFont.Font.getWidth(c.toString()));
	}

	public static int getLength(Character c) {
		if (loggedCharacters.containsKey(c))
			return loggedCharacters.get(c);
		if (FONT.canDisplay(c)) {
			Rectangle2D rectangle2D = FONT.getStringBounds(
				c.toString(),
				new FontRenderContext(null, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT)
			);
			int length = (int) rectangle2D.getWidth();
			loggedCharacters.put(c, length);
			return length;
		}
		throw new IllegalArgumentException("Could not get pixel length of '" + c + "'");
	}

	public static int getLength(String string) {
		int totalLength = -1;
		for (Character c : string.toCharArray())
			totalLength += getLength(c) + 1;
		return totalLength;
	}

}
