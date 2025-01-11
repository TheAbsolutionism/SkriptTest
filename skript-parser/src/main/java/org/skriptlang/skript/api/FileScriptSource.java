package org.skriptlang.skript.api;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A script sourced from a file.
 */
public class FileScriptSource implements ScriptSource {
	private final Path path;
	private volatile @UnknownNullability String content = null;

	public FileScriptSource(@NotNull Path path) {
		Preconditions.checkNotNull(path, "path cannot be null");
		Preconditions.checkArgument(Files.isRegularFile(path), "path must be a regular file");
		Preconditions.checkArgument(Files.isReadable(path), "path must be readable (check permissions)");
		this.path = path;
	}

	@Override
	public String name() {
		return path.getFileName().toString();
	}

	@Override
	public String content() {
		if (content == null) synchronized (this) {
			if (content == null) {
				try {
					content = Files.readString(path);
				} catch (IOException e) {
					throw new RuntimeException("Unable to read script '" + name() + "'", e);
				}
			}
		}
		return content;
	}
}
