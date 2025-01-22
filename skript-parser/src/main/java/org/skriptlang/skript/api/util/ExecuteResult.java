package org.skriptlang.skript.api.util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * The result of executing a statement.
 * <p>
 * A successful execution should return a {@link Success} using {@link #success()},
 * which indicates this statement did not critically fail,
 * and execution should continue.
 * <p>
 * A failed execution should return a {@link Failure} using {@link #failure(Throwable)},
 * which should contain a reason for the failure.
 * <p>
 * TODO:
 * In the future, this throwable will likely be replaced
 * with a Skript error value, which is usable in the Skript runtime.
 */
public sealed interface ExecuteResult permits ExecuteResult.Success, ExecuteResult.Failure {
	/**
	 * The singleton instance of {@link Success}.
	 * API should use {@link #success()} to get this singleton.
	 */
	@ApiStatus.Internal
	Success SUCCESS = new Success();


	/**
	 * Returns a successful execution.
	 */
	static @NotNull Success success() {
		return SUCCESS;
	}

	/**
	 * Returns a failed execution with the given reason.
	 * @param reason The reason for the failure.
	 */
	static @NotNull Failure failure(@NotNull Throwable reason) {
		return new Failure(reason);
	}

	/**
	 * Represents a successful execution. Success utilizes a singleton pattern and should not be instantiated.
	 */
	final class Success implements ExecuteResult {
		private Success() {}
	}
	record Failure(Throwable throwable) implements ExecuteResult {}



}
