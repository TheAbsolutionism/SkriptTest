package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Raid;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Raid Bad Omen Level")
@Description({
	"The bad omen level of a raid.",
	"Level is capped between 0 and 5."
})
@Examples({
	"set {_raid} to nearest raid from location(0, 0, 0) in radius 5",
	"set the bad omen level of {_raid} to 5"
})
@Since("INSERT VERSION")
public class ExprRaidLevel extends SimplePropertyExpression<Raid, Integer> {

	static {
		registerDefault(ExprRaidLevel.class, Integer.class, "bad omen level", "raids");
	}

	@Override
	public @Nullable Integer convert(Raid raid) {
		return raid.getBadOmenLevel();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Integer.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int level = (int) delta[0];
		level = Math2.fit(0, level, 5);
		for (Raid raid : getExpr().getArray(event)) {
			raid.setBadOmenLevel(level);
		}
	}

	@Override
	protected String getPropertyName() {
		return "bad omen level";
	}

	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}

}
