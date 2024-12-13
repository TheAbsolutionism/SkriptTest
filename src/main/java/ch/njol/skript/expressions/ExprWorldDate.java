package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.WorldDate;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

@Name("World Tick")
@Description("Gets the number of ticks of the world that has passed since the world generated.")
@Examples({
	"send the current world date of world \"world\"",
	"send the world date of last spawned entity",
	"send 5 seconds before the world date of \"world\""
})
@Since("INSERT VERSION")
public class ExprWorldDate extends SimplePropertyExpression<Object, WorldDate> {

	static {
		registerDefault(ExprWorldDate.class, WorldDate.class, "[current] world date", "worlds/entities");
	}

	@Override
	public @Nullable WorldDate convert(Object object) {
		if (object instanceof World world)
			return new WorldDate(world);
		else if (object instanceof Entity entity)
			return new WorldDate(entity);
		return null;
	}

	@Override
	protected String getPropertyName() {
		return "current world date";
	}

	@Override
	public Class<WorldDate> getReturnType() {
		return WorldDate.class;
	}

}
