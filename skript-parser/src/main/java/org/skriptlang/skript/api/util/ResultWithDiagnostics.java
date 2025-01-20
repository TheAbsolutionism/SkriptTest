package org.skriptlang.skript.api.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Represents the result of an action that may have produced diagnostics alongside a successful or unsuccessful result.
 * @param <T> The type of the result.
 */
public final class ResultWithDiagnostics<T> {
	private final @Nullable T result;
	private final @NotNull List<ScriptDiagnostic> diagnostics;

	private ResultWithDiagnostics(@Nullable T result, @Nullable List<ScriptDiagnostic> diagnostics) {
		this.result = result;
		this.diagnostics = diagnostics != null ? diagnostics.stream().toList() : Collections.emptyList();
	}

	/**
	 * Returns whether the action was successful.
	 */
	public boolean isSuccess() {
		return result != null;
	}

	/**
	 * Gets the result of the action.
	 */
	public @NotNull T get() {
		if (result == null) {
			// TODO: Helpful to find most severe diagnostic and incorporate into exception message
			throw new IllegalStateException("Action was not successful");
		}
		return result;
	}

	/**
	 * Gets a list of diagnostics that may .
	 * This may be non-empty even if the result is successful,
	 * in which it may contain various warnings.
	 */
	public @NotNull List<ScriptDiagnostic> getDiagnostics() {
		return diagnostics;
	}

	@Contract("_ -> new")
	public static <T> @NotNull ResultWithDiagnostics<T> success(@NotNull T result) {
		return new ResultWithDiagnostics<>(result, null);
	}

	@Contract("_, _ -> new")
	public static <T> @NotNull ResultWithDiagnostics<T> success(@NotNull T result, @NotNull List<ScriptDiagnostic> diagnostics) {
		return new ResultWithDiagnostics<>(result, diagnostics);
	}

	@Contract("_ -> new")
	public static <T> @NotNull ResultWithDiagnostics<T> failure(@NotNull List<ScriptDiagnostic> diagnostics) {
		return new ResultWithDiagnostics<>(null, diagnostics);
	}

	@Contract("_ -> new")
	public static <T> @NotNull ResultWithDiagnostics<T> failure(@NotNull ScriptDiagnostic diagnostic) {
		return new ResultWithDiagnostics<>(null, List.of(diagnostic));
	}

}
