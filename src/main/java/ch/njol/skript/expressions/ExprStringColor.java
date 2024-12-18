package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("String Colors")
@Description({
	"Retrieve the first, the last, or all of the colors of a string.",
	"NOTE: The retrieved colors of the string will be formatted with the color symbol."
})
@Examples("set {_colors::*} to the string colors of \"<red>hey<blue>yo\"")
@Since("2.6")
public class ExprStringColor extends PropertyExpression<String, String> {

	private enum StringColor {
		ALL("[all]", "all"),
		FIRST("first", "first"),
		LAST("last", "last");

		private String pattern, toString;

		StringColor(String pattern, String toString) {
			this.pattern = pattern;
			this.toString = toString;
		}
	}

	private static final StringColor[] STRING_COLORS = StringColor.values();

	static {
		String[] patterns = new String[STRING_COLORS.length];
		for (StringColor color : STRING_COLORS) {
			patterns[color.ordinal()] = "[the] " + color.pattern + " string color[s] of %strings%";
		}
		Skript.registerExpression(ExprStringColor.class, String.class, ExpressionType.PROPERTY, patterns);
	}

	private StringColor selectedState;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		selectedState = STRING_COLORS[matchedPattern];
		//noinspection unchecked
		setExpr((Expression<String>) exprs[0]);
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event, String[] source) {
		List<String> colors = new ArrayList<>();
		for (String string : getExpr().getArray(event)) {
			List<String> stringColors = getColors(string);
			if (stringColors.isEmpty())
				continue;
			if (selectedState == StringColor.ALL) {
				colors.addAll(stringColors);
			} else if (selectedState == StringColor.FIRST) {
				colors.add(stringColors.get(0));
			} else if (selectedState == StringColor.LAST) {
				colors.add(stringColors.get(stringColors.size() - 1));
			}
		}
		if (colors.isEmpty())
			return null;
		return colors.toArray(new String[0]);
	}

	@Override
	public Class<String> getReturnType() {
		return String.class;
	}

	@Override
	public boolean isSingle() {
		if (selectedState != StringColor.ALL && getExpr().isSingle())
			return true;
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + selectedState.toString + " string colors of " + getExpr().toString(event, debug);
	}

	private List<String> getColors(String string) {
		List<String> colors = new ArrayList<>();
		int length = string.length();
		for (int index = 0; index < length; index++) {
			char section = string.charAt(index);
			if (section == '§') {
				boolean checkHex = checkHex(string, index);
				boolean checkChar = SkriptColor.checkChar(string.charAt(index + 1));
				if (checkHex) {
					String result = string.substring(index, index + 14);
					colors.add(result);
					index += 13;
				} else if (checkChar) {
					String result = string.substring(index, index + 2);
					colors.add(result);
					index += 1;
				}
			}
		}
		return colors;
	}

	private boolean checkHex(String string, int index) {
		int length = string.length();
		if (length < index + 12)
			return false;
		if (string.charAt(index + 1) != 'x')
			return false;

		for (int i = index + 2; i <= index; i += 2) {
			if (string.charAt(i) != '§')
				return false;
		}

		for (int i = index + 3; i <= index; i += 2) {
			char toCheck = string.charAt(i);
			if (toCheck < '0'  || toCheck > 'f')
				return false;
			if (toCheck > '9' && toCheck < 'A')
				return false;
			if (toCheck > 'F' && toCheck < 'a')
				return false;
		}

		return true;
	}

}
