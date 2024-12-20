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
package ch.njol.skript.lang;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.patterns.SkriptPattern;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.Checker;
import ch.njol.util.StringUtils;
import com.google.common.collect.Iterables;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.condition.Conditional;
import org.skriptlang.skript.lang.script.Script;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a trigger item, i.e. a trigger section, a condition or an effect.
 * 
 * @author Peter Güttinger
 * @see TriggerSection
 * @see Trigger
 * @see Statement
 */
public abstract class TriggerItem implements Debuggable {

	protected @Nullable TriggerSection parent = null;
	private @Nullable TriggerItem next = null;

	protected TriggerItem() {}

	protected TriggerItem(TriggerSection parent) {
		this.parent = parent;
	}

	/**
	 * Executes this item and returns the next item to run.
	 * <p>
	 * Overriding classes must call {@link #debug(Event, boolean)}. If this method is overridden, {@link #run(Event)} is not used anymore and can be ignored.
	 * 
	 * @param event The event
	 * @return The next item to run or null to stop execution
	 */
	protected @Nullable TriggerItem walk(Event event) {
		if (run(event)) {
			debug(event, true);
			return next;
		} else {
			debug(event, false);
			TriggerSection parent = this.parent;
			return parent == null ? null : parent.getNext();
		}
	}

	/**
	 * Executes this item.
	 * 
	 * @param event The event to run this item with
	 * @return True if the next item should be run, or false for the item following this item's parent.
	 */
	protected abstract boolean run(Event event);

	/**
	 * @param start The item to start at
	 * @param event The event to run the items with
	 * @return false if an exception occurred
	 */
	public static boolean walk(TriggerItem start, Event event) {
		TriggerItem triggerItem = start;
		try {
			while (triggerItem != null)
				triggerItem = triggerItem.walk(event);

			return true;
		} catch (StackOverflowError err) {
			Trigger trigger = start.getTrigger();
			String scriptName = "<unknown>";
			if (trigger != null) {
				Script script = trigger.getScript();
				if (script != null) {
					File scriptFile = script.getConfig().getFile();
					if (scriptFile != null)
						scriptName = scriptFile.getName();
				}
			}
			Skript.adminBroadcast("<red>The script '<gold>" + scriptName + "<red>' infinitely (or excessively) repeated itself!");
			if (Skript.debug())
				err.printStackTrace();
		} catch (Exception ex) {
			if (ex.getStackTrace().length != 0) // empty exceptions have already been printed
				Skript.exception(ex, triggerItem);
		} catch (Throwable throwable) {
			// not all Throwables are Exceptions, but we usually don't want to catch them (without rethrowing)
			Skript.markErrored();
			throw throwable;
		}
		return false;
	}

	/**
	 * Returns whether this item stops the execution of the current trigger or section(s).
	 * <br>
	 * If present, and there are statement(s) after this one, the parser will print a warning
	 * to the user.
	 * <p>
	 * <b>Note: This method is used purely to print warnings and doesn't affect parsing, execution or anything else.</b>
	 *
	 * @return whether this item stops the execution of the current trigger or section.
	 */
	public @Nullable ExecutionIntent executionIntent() {
		return null;
	}

	/**
	 * how much to indent each level
	 */
	private final static String INDENT = "  ";

	private @Nullable String indentation = null;

	public String getIndentation() {
		if (indentation == null) {
			int level = 0;
			TriggerItem triggerItem = this;
			while ((triggerItem = triggerItem.parent) != null)
				level++;
			indentation = StringUtils.multiply(INDENT, level);
		}
		return indentation;
	}

	protected final void debug(Event event, boolean run) {
		if (!Skript.debug())
			return;
		Skript.debug(SkriptColor.replaceColorChar(getIndentation() + (run ? "" : "-") + toString(event, true)));
	}

	@Override
	public final String toString() {
		return toString(null, false);
	}

	public TriggerItem setParent(@Nullable TriggerSection parent) {
		this.parent = parent;
		return this;
	}

	public final @Nullable TriggerSection getParent() {
		return parent;
	}

	/**
	 * @return The trigger this item belongs to, or null if this is a stand-alone item (e.g. the effect of an effect command)
	 */
	public final @Nullable Trigger getTrigger() {
		TriggerItem triggerItem = this;
		while (triggerItem != null && !(triggerItem instanceof Trigger))
			triggerItem = triggerItem.getParent();
		return (Trigger) triggerItem;
	}

	public TriggerItem setNext(@Nullable TriggerItem next) {
		this.next = next;
		return this;
	}

	public @Nullable TriggerItem getNext() {
		return next;
	}

	/**
	 * This method guarantees to return next {@link TriggerItem} after this item.
	 * This is not always the case for {@link #getNext()}, for example, {@code getNext()}
	 * of a {@link ch.njol.skript.sections.SecLoop loop section} usually returns itself.
	 * 
	 * @return The next {@link TriggerItem}.
	 */
	public @Nullable TriggerItem getActualNext() {
		return next;
	}

	/**
	 * Get the first element instance of {@code tClass}
	 * @param tClass
	 * @param triggerItems
	 * @return
	 * @param <T>
	 */
	public static <T extends SyntaxElement> @Nullable T getPrecedingElement(Class<T> tClass, List<TriggerItem> triggerItems) {
		return getPrecedingElement(tClass, triggerItems, null, null);
	}

	/**
	 * Get the first element instance of {@code tClass} that passes {@code checker}
	 * If an element does not pass {@code checker} will return null
	 * @param tClass
	 * @param triggerItems
	 * @param checker
	 * @return
	 * @param <T>
	 */
	public static <T extends SyntaxElement> @Nullable T getPrecedingElement(Class<T> tClass, List<TriggerItem> triggerItems, Checker<T> checker) {
		return getPrecedingElement(tClass, triggerItems, checker, o -> !checker.check(o));
	}

	/**
	 * Get the first element instance of {@code tClass} that passes {@code checker} and does not pass {@code forceStop}
	 * @param tClass
	 * @param triggerItems
	 * @param checker
	 * @param forceStop
	 * @return
	 * @param <T>
	 */
	public static <T extends SyntaxElement> @Nullable T getPrecedingElement(Class<T> tClass, List<TriggerItem> triggerItems, @Nullable Checker<T> checker, @Nullable Checker<T> forceStop) {
		for (int i = triggerItems.size() - 1; i >= 0; i--) {
			TriggerItem triggerItem = triggerItems.get(i);
			if (!tClass.isInstance(triggerItem))
				return null;
			//noinspection unchecked
			T element = (T) triggerItem;
			if (forceStop != null && forceStop.check(element))
				return null;
			if (checker == null || checker.check(element))
				return element;
		}
		return null;
	}

	public static <T extends SyntaxElement> List<T> getPrecedingElements(Class<T> tclass, List<TriggerItem> triggerItems) {
		return getPrecedingElements(tclass, triggerItems, null);
	}

	public static <T extends SyntaxElement> List<T> getPrecedingElements(Class<T> tClass, List<TriggerItem> triggerItems, @Nullable Checker<T> checker)  {
		List<T> elementList = new ArrayList<>();
		for (int i = triggerItems.size() - 1; i >= 0; i--) {
			TriggerItem triggerItem = triggerItems.get(i);
			if (!tClass.isInstance(triggerItem))
				break;
			//noinspection unchecked
			T element = (T) triggerItem;
			if (checker != null && !checker.check(element))
				break;
			elementList.add(element);
		}
		return elementList;
	}

	public static boolean checkSubsidingElement(SectionNode sectionNode, ParserInstance parser, SkriptPattern pattern) {
		return checkSubsidingElement(sectionNode, parser, node -> {
			if (!(node instanceof SectionNode) || node.getKey() == null)
				return false;
			String nextKey = ScriptLoader.replaceOptions(node.getKey());
			if (pattern.match(nextKey) == null)
				return false;
			return true;
		});
	}

	public static boolean checkSubsidingElement(SectionNode sectionNode, ParserInstance parser, Checker<Node> checker) {
		Node originalNode = parser.getNode();
		SectionNode parentNode = sectionNode.getParent();
		if (parentNode == null)
			return false;
		Iterator<Node> nodeIterator = parentNode.iterator();
		Node nextNode = null;
		while (nodeIterator.hasNext()) {
			Node current = nodeIterator.next();
			if (current == sectionNode) {
				nextNode = nodeIterator.hasNext() ? nodeIterator.next() : null;
				break;
			}
		}
		parser.setNode(originalNode);
		if (nextNode == null)
			return false;
		return checker.check(nextNode);
	}

	public static @Nullable List<Conditional<Event>> parseMultiline(SectionNode sectionNode, ParserInstance parser, String sectionType)  {
		List<Conditional<Event>> conditionals = new ArrayList<>();
		int nodeCount = Iterables.size(sectionNode);
		// we have to get the size of the iterator here as SectionNode#size includes empty/void nodes
		if (nodeCount < 2) {
			Skript.error(sectionType + " sections must contain atleast two conditions.");
			return null;
		}
		for (Node childNode : sectionNode) {
			if (childNode instanceof SectionNode) {
				Skript.error(sectionType + " section may not contain other sections.");
				return null;
			}
			String childKey = childNode.getKey();
			if (childKey == null)
				continue;
			childKey = ScriptLoader.replaceOptions(childKey);
			parser.setNode(childNode);
			Condition condition = Condition.parse(childKey, "Can't understand this condition: '" + childKey + "'");
			// if this condition was invalid, don't bother parsing the rest
			if (condition == null)
				return null;
			conditionals.add(condition);
		}
		parser.setNode(sectionNode);
		return conditionals;
	}

}
