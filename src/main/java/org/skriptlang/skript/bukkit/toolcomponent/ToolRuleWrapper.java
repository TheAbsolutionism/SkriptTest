package org.skriptlang.skript.bukkit.toolcomponent;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.meta.components.ToolComponent.ToolRule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class ToolRuleWrapper implements ToolRule {

	private Collection<Material> blocks = new ArrayList<>();
	private boolean correct = false;
	private Float speed = 5.0F;

	public ToolRuleWrapper() {}
	public ToolRuleWrapper(Collection<Material> blocks, Float speed, boolean correct) {
		this.blocks = blocks;
		this.correct = correct;
		this.speed = speed;
	}

	@Override
	public @NotNull Collection<Material> getBlocks() {
		return blocks;
	}

	@Override
	public void setBlocks(@NotNull Material material) {
		blocks = Arrays.stream(new Material[]{material}).toList();
	}

	@Override
	public void setBlocks(@NotNull Collection<Material> collection) {
		blocks = collection;
	}

	@Override
	public void setBlocks(@NotNull Tag<Material> tag) {
		blocks = tag.getValues().stream().toList();
	}

	@Override
	public @Nullable Float getSpeed() {
		return speed;
	}

	@Override
	public void setSpeed(@Nullable Float speed) {
		this.speed = speed;
	}

	@Override
	public @Nullable Boolean isCorrectForDrops() {
		return correct;
	}

	@Override
	public void setCorrectForDrops(@Nullable Boolean correct) {
		this.correct = correct;
	}

	@Override
	public @NotNull Map<String, Object> serialize() {
		return null;
	}

}
