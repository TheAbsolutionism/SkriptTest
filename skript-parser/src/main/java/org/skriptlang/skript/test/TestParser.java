package org.skriptlang.skript.test;

import org.skriptlang.skript.api.*;
import org.skriptlang.skript.api.nodes.*;
import org.skriptlang.skript.api.script.StringScriptSource;
import org.skriptlang.skript.api.util.ExecuteResult;
import org.skriptlang.skript.api.util.ResultWithDiagnostics;
import org.skriptlang.skript.api.util.ScriptDiagnostic;
import org.skriptlang.skript.parser.LockAccess;
import org.skriptlang.skript.parser.SkriptParserImpl;

import java.util.List;

public class TestParser {
	public static void main(String[] args) {
		LockAccess lockAccess = new LockAccess();
		SkriptParser parser = new SkriptParserImpl(lockAccess);

		parser.submitNode(new StructureNodeType<>() {
			@Override
			public List<String> getSyntaxes() {
				return List.of("on script load:<section>");
			}

			@Override
			public StructureNode create(List<SyntaxNode> children) {
				SectionNode section = (SectionNode) children.getFirst();

				return new StructureNode(section) {


					@Override
					public ExecuteResult execute() {
						return ExecuteResult.success();
					}

					@Override
					public int length() {
						return 0;
					}
				};
			}
		});

		parser.submitNode(new EffectNodeType<>() {
			@Override
			public List<String> getSyntaxes() {
				return List.of("this is a [conflicting] test syntax");
			}
		});

		lockAccess.lock();

		ResultWithDiagnostics<SyntaxNode> result = parser.parse(new StringScriptSource(
			"test.sk",
			"""
				on script load:
					this is a test syntax
				"""
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
