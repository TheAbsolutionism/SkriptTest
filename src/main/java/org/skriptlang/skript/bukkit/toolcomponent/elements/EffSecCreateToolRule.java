package org.skriptlang.skript.bukkit.toolcomponent.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.meta.components.ToolComponent.ToolRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.toolcomponent.ToolRuleWrapper;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("New Tool Rule")
@Description("Creates a new tool rule.")
@Examples({
	"create a new tool rule and store it in {_toolrule}:",
		"\tset the tool rule blocks to oak log, stone and obsidian",
		"\tset the tool rule speed to 5",
		"\tenable the tool rule drops",
	"add {_toolrule} to the tool rules of {_item}"
})
@RequiredPlugins("Minecraft 1.20.6+")
@Since("INSERT VERSION")
public class EffSecCreateToolRule extends Section {
	// TODO: Change to secpression when merged
	public static class ToolRuleEvent extends Event {

		private ToolRuleWrapper toolRuleWrapper;

		public ToolRuleEvent(ToolRuleWrapper wrapper) {
			toolRuleWrapper = wrapper;
		}

		public ToolRuleWrapper getToolRuleWrapper() {
			return toolRuleWrapper;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	static {
		Skript.registerSection(EffSecCreateToolRule.class, "create [a] [new] tool rule and store (it|the result) in %object%");
		EventValues.registerEventValue(ToolRuleEvent.class, ToolRule.class, new Getter<ToolRule, ToolRuleEvent>() {
			@Override
			public @Nullable ToolRule get(ToolRuleEvent event) {
				return event.getToolRuleWrapper();
			}
		}, EventValues.TIME_NOW);
	}

	private Expression<?> variable;
	private @Nullable Trigger trigger;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
		variable = exprs[0];

		if (sectionNode == null)
			return false;

		AtomicBoolean delayed = new AtomicBoolean(false);
		Runnable afterLoading = () -> delayed.set(!getParser().getHasDelayBefore().isFalse());
		trigger = loadCode(sectionNode, "tool rule", afterLoading, ToolRuleEvent.class);
		if (delayed.get()) {
			Skript.error("Delays can't be used within a Tool Rule Create Section.");
			return false;
		}

		return true;
	}


	@Override
	protected @Nullable TriggerItem walk(Event event) {

		ToolRuleWrapper wrapper = new ToolRuleWrapper();
		ToolRuleEvent toolRuleEvent = new ToolRuleEvent(wrapper);
		Variables.setLocalVariables(toolRuleEvent, Variables.copyLocalVariables(event));
		TriggerItem.walk(trigger, toolRuleEvent);
		Variables.setLocalVariables(event, Variables.copyLocalVariables(toolRuleEvent));
		Variables.removeLocals(toolRuleEvent);

		if (!wrapper.getBlocks().isEmpty()) {
			variable.change(event, new ToolRule[]{wrapper}, ChangeMode.SET);
		}

		return super.walk(event, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "create a new tool rule";
	}

}
