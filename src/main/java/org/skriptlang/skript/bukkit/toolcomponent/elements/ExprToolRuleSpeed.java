package org.skriptlang.skript.bukkit.toolcomponent.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.components.ToolComponent.ToolRule;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.toolcomponent.ToolRuleWrapper;
import org.skriptlang.skript.bukkit.toolcomponent.elements.EffSecCreateToolRule.ToolRuleEvent;

@Name("Tool Rule - Speed")
@Description("The speed of a tool rule determining the mining speed of the blocks of the tool rule for an item.")
@Examples({
	"create a new tool rule and store it in {_toolrule}:",
		"\tset the tool rule blocks to oak log, stone and obsidian",
		"\tset the tool rule speed to 5",
		"\tenable tool rule drops",
	"add {_toolrule} to the tool rules of {_item}"
})
@RequiredPlugins("Minecraft 1.20.6+")
@Since("INSERT VERSION")
public class ExprToolRuleSpeed extends PropertyExpression<ToolRule, Number> {

	static {
		Skript.registerExpression(ExprToolRuleSpeed.class, Number.class, ExpressionType.PROPERTY,
			"[the] tool rule speed [of %toolrules%]");
	}

	private boolean isEvent = false;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (getParser().isCurrentEvent(ToolRuleEvent.class) && exprs[0].isDefault()) {
			isEvent = true;
			setExpr(new EventValueExpression<>(ToolRule.class));
		} else {
			//noinspection unchecked
			setExpr((Expression<? extends ToolRule>) exprs[0]);
		}
		return true;
	}

	@Override
	protected Number @Nullable [] get(Event event, ToolRule[] source) {
		return get(source, toolRule -> {
			return toolRule.getSpeed();
		});
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET && isEvent)
			return CollectionUtils.array(Number.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof ToolRuleEvent toolRuleEvent))
			return;

		ToolRuleWrapper wrapper = toolRuleEvent.getToolRuleWrapper();
		Number providedNumber = (Number) delta[0];
		wrapper.setSpeed(providedNumber.floatValue());
	}

	@Override
	public boolean isSingle() {
		return getExpr().isSingle();
	}

	@Override
	public Class<Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the tool rule speed of " + getExpr().toString(event, debug);
	}

}
