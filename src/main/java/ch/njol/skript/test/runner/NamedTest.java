package ch.njol.skript.test.runner;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.util.common.AnyNamed;
import ch.njol.skript.lang.util.common.AnyProviderRegistry;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public class NamedTest implements AnyNamed {

	static {
		AnyProviderRegistry.register(AnyNamed.class, NamedTest.class, new NamedTest());
		Classes.registerClass(new ClassInfo<>(NamedTest.class, "namedtest")
			.user("named ?tests?")
		);
		//Converters.registerConverter(NamedTest.class, AnyNamed.class, named -> named::name, Converter.NO_RIGHT_CHAINING);
	}

	public NamedTest() {}

	@Override
	public @UnknownNullability String name() {
		return "test";
	}

	@Override
	public boolean supportsChange() {
		return true;
	}

	@Override
	public boolean supportsNameChange() {
		return true;
	}

	@Override
	public boolean hasCustomChanger() {
		return true;
	}

	@Override
	public void change(Object @Nullable [] delta, ChangeMode mode) {
		Skript.adminBroadcast("Change Called: " + delta[0] + " | " + mode);
	}

}
