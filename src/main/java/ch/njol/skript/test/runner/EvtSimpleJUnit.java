package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NoDoc
public class EvtSimpleJUnit extends SkriptEvent {

	public static class SimpleJUnitEvent extends Event {

		private final static HandlerList handlers = new HandlerList();
		private final String testName;

		public SimpleJUnitEvent(String testName) {
			this.testName = testName;
		}

		public String getTestName() {
			return testName;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			return handlers;
		}

		public static HandlerList getHandlerList() {
			return handlers;
		}

	}

	static {
		if (TestMode.ENABLED)
			Skript.registerEvent("Simple JUnit Load", EvtSimpleJUnit.class, SimpleJUnitEvent.class,
				"simple junit of %string%");
	}

	private Literal<String> literal;
	private String testName;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		//noinspection unchecked
		literal = (Literal<String>) args[0];
		testName = literal.getSingle();
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof SimpleJUnitEvent simpleJUnitEvent))
			return false;

		if (simpleJUnitEvent.getTestName().equalsIgnoreCase(testName))
			return true;

		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "simple junit of " + literal.toString(event, debug);
	}

}
