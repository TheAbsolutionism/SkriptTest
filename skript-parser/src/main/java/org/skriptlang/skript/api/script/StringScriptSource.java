package org.skriptlang.skript.api.script;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

public record StringScriptSource(@NotNull String name, @NotNull String content) implements ScriptSource {

	public StringScriptSource {
		Preconditions.checkNotNull(name, "name cannot be null");
		Preconditions.checkNotNull(content, "content cannot be null");
	}

}
