package org.skriptlang.skript.bukkit.toolcomponent.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.components.ToolComponent.ToolRule;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.toolcomponent.elements.EffSecCreateToolRule.ToolRuleEvent;

@Name("Tool Rule - Drops")
@Description("If the tool with this tool rule should drop items of a block defined from the blocks of this tool rule.")
@Examples({
	"create a new tool rule and store it in {_toolrule}:",
		"\tset the tool rule blocks to oak log, stone and obsidian",
		"\tset the tool rule speed to 5",
		"\tenable tool rule drops",
	"add {_toolrule} to the tool rules of {_item}"
})
@RequiredPlugins("Minecraft 1.20.6+")
@Since("INSERT VERSION")
public class EffToolRuleDrops extends Effect {

	static {
		Skript.registerEffect(EffToolRuleDrops.class,
			"enable [the] tool rule drops [(of|for) %toolrules%]",
			"disable [the] tool rule drops [(of|for) %toolrules%]");
	}

	private boolean isEvent = false;
	private boolean enable;
	private Expression<? extends ToolRule> exprToolRule;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (getParser().isCurrentEvent(ToolRuleEvent.class) && exprs[0].isDefault())
			isEvent = true;
		//noinspection unchecked
		exprToolRule = (Expression<? extends ToolRule>) exprs[0];
		enable = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (event instanceof ToolRuleEvent toolRuleEvent && isEvent) {
			toolRuleEvent.getToolRuleWrapper().setCorrectForDrops(enable);
		} else {
			for (ToolRule rule : exprToolRule.getArray(event)) {
				rule.setCorrectForDrops(enable);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (enable ? "enable" : "disable") + " tool rule drops for " + exprToolRule.toString(event, debug);
	}

}
