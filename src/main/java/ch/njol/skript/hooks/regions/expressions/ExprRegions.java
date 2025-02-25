package ch.njol.skript.hooks.regions.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.hooks.regions.RegionsPlugin;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("All Regions")
@Description("Get all regions from all worlds or a specific world.")
@Examples({
	"send all regions",
	"set {_regions::*} to all of the regions in world \"world\""
})
@Since("INSERT VERSION")
@RequiredPlugins("Supported regions plugin")
public class ExprRegions extends SimpleExpression<Region> {

	static {
		Skript.registerExpression(ExprRegions.class, Region.class, ExpressionType.SIMPLE,
			"(all [[of] the]|the) regions [(in|of|from) %-worlds%]");
	}

	private @Nullable Expression<World> worlds;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (exprs[0] != null)
			//noinspection unchecked
			worlds = (Expression<World>) exprs[0];
		return true;
	}

	@Override
	protected Region @Nullable [] get(Event event) {
		if (worlds == null) {
			return RegionsPlugin.getRegions();
		} else {
			World[] worlds = this.worlds.getArray(event);
			if (worlds == null || worlds.length == 0)
				return null;
			List<Region> regions = new ArrayList<>();
			for (World world : worlds) {
				Region[] worldRegions = RegionsPlugin.getRegions(world);
				if (worldRegions == null)
					continue;
				regions.addAll(Arrays.stream(worldRegions).toList());
			}
			return regions.toArray(Region[]::new);
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<Region> getReturnType() {
		return Region.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("all of the regions");
		if (worlds != null)
			builder.append("in", worlds);
		return builder.toString();
	}

}
