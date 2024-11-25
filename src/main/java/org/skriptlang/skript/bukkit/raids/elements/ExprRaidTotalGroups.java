package org.skriptlang.skript.bukkit.raids.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Raid;
import org.jetbrains.annotations.Nullable;

@Name("Raid Total Groups")
@Description("The total number of groups a raid can spawn.")
@Examples({
	"loop the raids in world \"world\":",
		"broadcast the total groups of loop-value"
})
@Since("INSERT VERSION")
public class ExprRaidTotalGroups extends SimplePropertyExpression<Raid, Integer> {

	static {
		registerDefault(ExprRaidTotalGroups.class, Integer.class, "total groups", "raids");
	}

	@Override
	public @Nullable Integer convert(Raid raid) {
		return raid.getTotalGroups();
	}

	@Override
	protected String getPropertyName() {
		return "total groups";
	}

	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}

}
