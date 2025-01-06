package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Frog;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Frog Tongue Target")
@Description("The target of a frog that it is about to launch its tongue at.")
@Examples({
	"set the tongue target of {_frog} to last spawned bee",
	"clear the tongue target of {_frog}"
})
@Since("INSERT VERSION")
public class ExprTongueTarget extends SimplePropertyExpression<LivingEntity, Entity> {

	static {
		registerDefault(ExprTongueTarget.class, Entity.class, "tongue target", "livingentities");
	}

	@Override
	public @Nullable Entity convert(LivingEntity entity) {
		if (entity instanceof Frog frog)
			return frog.getTongueTarget();
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(Entity.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Entity target = delta != null ? (Entity) delta[0] : null;
		for (LivingEntity entity : getExpr().getArray(event)) {
			if (entity instanceof Frog frog)
				frog.setTongueTarget(target);
		}
	}

	@Override
	public Class<Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	protected String getPropertyName() {
		return "tongue target";
	}

}
