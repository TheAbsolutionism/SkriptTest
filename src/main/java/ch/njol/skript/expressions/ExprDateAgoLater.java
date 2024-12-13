/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.WorldDate;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Date Ago/Later")
@Description("A date the specified timespan before/after another date.")
@Examples({"set {_yesterday} to 1 day ago",
			"set {_hourAfter} to 1 hour after {someOtherDate}",
			"set {_hoursBefore} to 5 hours before {someOtherDate}"})
@Since("2.2-dev33")
public class ExprDateAgoLater extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprDateAgoLater.class, Object.class, ExpressionType.COMBINED,
                "%timespan% (ago|in the past|before [the] [date] %-date/worlddate%)",
                "%timespan% (later|(from|after) [the] [date] %-date/worlddate%)");
    }

    private Expression<Timespan> timespan;
    private @Nullable Expression<?> date;
    private boolean ago;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		timespan = (Expression<Timespan>) exprs[0];
        date = exprs[1];
        ago = matchedPattern == 0;
        return true;
    }

    @Override
    protected Object @Nullable [] get(Event event) {
        Timespan timespan = this.timespan.getSingle(event);
		Object object = date != null ? date.getSingle(event) : new Date();
		if (timespan == null || object == null)
			return null;
		if (object instanceof Date date1) {
			return new Date[]{ago ? date1.minus(timespan) : date1.plus(timespan)};
		} else if (object instanceof WorldDate worldDate) {
			return new WorldDate[]{ago ? worldDate.minus(timespan) : worldDate.plus(timespan)};
		}
		return null;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<Object> getReturnType() {
        return Object.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return timespan.toString(e, debug) + " " + (ago ? (date != null ? "before " + date.toString(e, debug) : "ago")
			: (date != null ? "after " + date.toString(e, debug) : "later"));
    }

}
