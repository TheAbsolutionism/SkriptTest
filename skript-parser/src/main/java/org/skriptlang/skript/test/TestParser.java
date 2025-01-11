package org.skriptlang.skript.test;

import org.skriptlang.skript.api.*;
import org.skriptlang.skript.parser.LockAccess;
import org.skriptlang.skript.parser.SkriptParserImpl;

import java.util.List;

public class TestParser {
	public static void main(String[] args) {
		LockAccess lockAccess = new LockAccess();
		SkriptParser parser = new SkriptParserImpl(lockAccess);
		parser.submitNode(new SkriptNodeType() {
			@Override
			public List<String> getSyntaxes() {
				return List.of("this is a test syntax");
			}

			@Override
			public Class<?> getReturnType() {
				return Object.class;
			}
		});

		ResultWithDiagnostics<SyntaxNode> result = parser.parse(new StringScriptSource(
			"test.sk",
			"this is a test syntax"
		));
		if (result.isSuccess()) {
			System.out.println("Successfully parsed script!");
		} else {
			System.out.println("Failed to parse script: ");
			for (ScriptDiagnostic diagnostic : result.getDiagnostics()) {
				System.out.println(diagnostic);
			}
		}
	}
}
