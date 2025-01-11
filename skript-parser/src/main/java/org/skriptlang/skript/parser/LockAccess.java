package org.skriptlang.skript.parser;

/**
 * A class that allows delegating lock control to an orchestrator.
 */
public final class LockAccess {
	private volatile boolean locked = false;

	public void lock() {
		locked = true;
	}

	public void unlock() {
		locked = false;
	}

	public boolean isLocked() {
		return locked;
	}
}
