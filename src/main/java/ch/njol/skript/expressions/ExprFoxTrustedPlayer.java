package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Fox;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Name("Fox Trusted Players")
@Description("The players a fox trusts.")
@Examples({
	"if the trusted players of last spawned fox contains player:",
		"clear the trusted players of last spawned fox"
})
@Since("INSERT VERSION")
public class ExprFoxTrustedPlayer extends PropertyExpression<LivingEntity, OfflinePlayer> {

	static {
		Skript.registerExpression(ExprFoxTrustedPlayer.class, OfflinePlayer.class, ExpressionType.PROPERTY,
			"[the] trusted players of %livingentities%",
			"%livingentities%'[s] trusted players",
			"[the] first trusted player of %livingentities%",
			"%livingentities%'[s] first trusted player",
			"[the] second trusted player of %livingentities%",
			"%livingentities%'[s] second trusted player");
	}

	private boolean all;
	private boolean first;
	private boolean second;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern <= 1) {
			all = true;
		} else if (matchedPattern <= 3) {
			first = true;
		} else {
			second = true;
		}
		//noinspection unchecked
		setExpr((Expression<LivingEntity>) exprs[0]);
		return true;
	}

	@Override
	protected OfflinePlayer @Nullable [] get(Event event, LivingEntity[] source) {
		List<OfflinePlayer> players = new ArrayList<>();
		for (LivingEntity entity : getExpr().getArray(event)) {
			if (!(entity instanceof Fox fox))
				continue;
			if (all) {
				players.add((OfflinePlayer) fox.getFirstTrustedPlayer());
				players.add((OfflinePlayer) fox.getSecondTrustedPlayer());
			} else if (first) {
				players.add((OfflinePlayer) fox.getFirstTrustedPlayer());
			} else {
				players.add((OfflinePlayer) fox.getSecondTrustedPlayer());
			}
		}

		return players.toArray(new OfflinePlayer[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE) {
			if (all)
				return CollectionUtils.array(OfflinePlayer[].class);
			return CollectionUtils.array(OfflinePlayer.class);
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		OfflinePlayer firstPlayer = null;
		OfflinePlayer secondPlayer = null;
		if (mode == ChangeMode.SET) {
			assert delta != null;
			if (delta[0] != null)
				firstPlayer = (OfflinePlayer) delta[0];
			if (delta[1] != null)
				secondPlayer = (OfflinePlayer) delta[1];
		}
		OfflinePlayer finalFirst = firstPlayer;
		OfflinePlayer finalSecond = secondPlayer;
		Consumer<Fox> consumer;
		if (all) {
			consumer = fox -> {
				fox.setFirstTrustedPlayer(finalFirst);
				fox.setSecondTrustedPlayer(finalSecond);
			};
		} else if (first) {
			consumer = fox -> fox.setFirstTrustedPlayer(finalFirst);
		} else {
			consumer = fox -> fox.setSecondTrustedPlayer(finalSecond);
		}
		for (LivingEntity entity : getExpr().getArray(event)) {
			if (!(entity instanceof Fox fox))
				continue;
			consumer.accept(fox);
		}
	}

	@Override
	public Class<OfflinePlayer> getReturnType() {
		return OfflinePlayer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("the");
		if (all) {
			builder.append("trusted players");
		} else if (first) {
			builder.append("first trusted player");
		} else {
			builder.append("second trusted player");
		}
		builder.append("of", getExpr().toString(event, debug));
		return builder.toString();
	}

}
