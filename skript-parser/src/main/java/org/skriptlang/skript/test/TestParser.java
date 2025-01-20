package org.skriptlang.skript.test;

import org.skriptlang.skript.api.*;
import org.skriptlang.skript.api.nodes.SyntaxNode;
import org.skriptlang.skript.api.nodes.SyntaxNodeType;
import org.skriptlang.skript.api.script.StringScriptSource;
import org.skriptlang.skript.api.util.ResultWithDiagnostics;
import org.skriptlang.skript.api.util.ScriptDiagnostic;
import org.skriptlang.skript.parser.LockAccess;
import org.skriptlang.skript.parser.SkriptParserImpl;

import java.util.List;

public class TestParser {
	public static void main(String[] args) {
		LockAccess lockAccess = new LockAccess();
		SkriptParser parser = new SkriptParserImpl(lockAccess);

		parser.submitNode(new SyntaxNodeType() {
			@Override
			public List<String> getSyntaxes() {
				return List.of("this is a test syntax [test [test2]] b <expr> a");
			}

			@Override
			public Class<?> getReturnType() {
				return Object.class;
			}
		});

		lockAccess.lock();

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
