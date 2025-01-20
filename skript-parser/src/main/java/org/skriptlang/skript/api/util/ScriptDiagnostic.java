package org.skriptlang.skript.api.util;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.api.script.ScriptSource;

/**
 * Represents a diagnostic message produced by a software entity in the Skript specification.
 * @param source The script source that produced the diagnostic.
 * @param severity The severity of the diagnostic.
 * @param message The diagnostic message.
 * @param line The line number of the diagnostic. 0 if not applicable.
 * @param column The column number of the diagnostic. 0 if not applicable.
 */
public record ScriptDiagnostic(
	ScriptSource source,
	Severity severity,
	String message,
	int line,
	int column
) {

	public ScriptDiagnostic {
		Preconditions.checkNotNull(source, "source cannot be null");
		Preconditions.checkNotNull(severity, "severity cannot be null");
		Preconditions.checkNotNull(message, "message cannot be null");
	}

	@Override
	public @NotNull String toString() {
		return source.name() + " " + severity + ": " + message;
	}

	@Contract("_, _, _, _ -> new")
	public static @NotNull ScriptDiagnostic info(ScriptSource source, String message, int line, int column) {
		return new ScriptDiagnostic(source, Severity.INFO, message, line, column);
	}

	@Contract("_, _, _ -> new")
	public static @NotNull ScriptDiagnostic info(ScriptSource source, String message, int index) {
		Preconditions.checkNotNull(source, "source cannot be null");
		int[] lineAndColumn = source.getLineAndColumn(index);
		return new ScriptDiagnostic(source, Severity.INFO, message, lineAndColumn[0], lineAndColumn[1]);
	}

	@Contract("_, _ -> new")
	public static @NotNull ScriptDiagnostic info(ScriptSource source, String message) {
		return new ScriptDiagnostic(source, Severity.INFO, message, 0, 0);
	}

	@Contract("_, _, _, _ -> new")
	public static @NotNull ScriptDiagnostic warning(ScriptSource source, String message, int line, int column) {
		return new ScriptDiagnostic(source, Severity.WARNING, message, line, column);
	}

	@Contract("_, _, _ -> new")
	public static @NotNull ScriptDiagnostic warning(ScriptSource source, String message, int index) {
		Preconditions.checkNotNull(source, "source cannot be null");
		int[] lineAndColumn = source.getLineAndColumn(index);
		return new ScriptDiagnostic(source, Severity.WARNING, message, lineAndColumn[0], lineAndColumn[1]);
	}

	@Contract("_, _, _, _ -> new")
	public static @NotNull ScriptDiagnostic warning(ScriptSource source, @NotNull Throwable cause, int line, int column) {
		return new ScriptDiagnostic(source, Severity.WARNING, cause.getMessage(), line, column);
	}

	@Contract("_, _, _ -> new")
	public static @NotNull ScriptDiagnostic warning(ScriptSource source, @NotNull Throwable cause, int index) {
		Preconditions.checkNotNull(source, "source cannot be null");
		int[] lineAndColumn = source.getLineAndColumn(index);
		return new ScriptDiagnostic(source, Severity.WARNING, cause.getMessage(), lineAndColumn[0], lineAndColumn[1]);
	}

	@Contract("_, _ -> new")
	public static @NotNull ScriptDiagnostic warning(ScriptSource source, String message) {
		return new ScriptDiagnostic(source, Severity.WARNING, message, 0, 0);
	}

	@Contract("_, _ -> new")
	public static @NotNull ScriptDiagnostic warning(ScriptSource source, @NotNull Throwable cause) {
		return new ScriptDiagnostic(source, Severity.WARNING, cause.getMessage(), 0, 0);
	}

	@Contract("_, _, _, _ -> new")
	public static @NotNull ScriptDiagnostic error(ScriptSource source, String message, int line, int column) {
		return new ScriptDiagnostic(source, Severity.ERROR, message, line, column);
	}

	@Contract("_, _, _ -> new")
	public static @NotNull ScriptDiagnostic error(ScriptSource source, String message, int index) {
		Preconditions.checkNotNull(source, "source cannot be null");
		int[] lineAndColumn = source.getLineAndColumn(index);
		return new ScriptDiagnostic(source, Severity.ERROR, message, lineAndColumn[0], lineAndColumn[1]);
	}

	@Contract("_, _, _, _ -> new")
	public static @NotNull ScriptDiagnostic error(ScriptSource source, @NotNull Throwable cause, int line, int column) {
		return new ScriptDiagnostic(source, Severity.ERROR, cause.getMessage(), line, column);
	}

	@Contract("_, _, _ -> new")
	public static @NotNull ScriptDiagnostic error(ScriptSource source, @NotNull Throwable cause, int index) {
		Preconditions.checkNotNull(source, "source cannot be null");
		int[] lineAndColumn = source.getLineAndColumn(index);
		return new ScriptDiagnostic(source, Severity.ERROR, cause.getMessage(), lineAndColumn[0], lineAndColumn[1]);
	}

	@Contract("_, _ -> new")
	public static @NotNull ScriptDiagnostic error(ScriptSource source, String message) {
		return new ScriptDiagnostic(source, Severity.ERROR, message, 0, 0);
	}

	@Contract("_, _ -> new")
	public static @NotNull ScriptDiagnostic error(ScriptSource source, @NotNull Throwable cause) {
		return new ScriptDiagnostic(source, Severity.ERROR, cause.getMessage(), 0, 0);
	}

}
