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
	private Boolean correctForDrops;
	private Float speed;

	public ToolRuleWrapper() {}

	public ToolRuleWrapper(Collection<Material> blocks, Float speed, boolean correctForDrops) {
		this.blocks = blocks;
		this.correctForDrops = correctForDrops;
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
		return correctForDrops;
	}

	@Override
	public void setCorrectForDrops(@Nullable Boolean correctForDrops) {
		this.correctForDrops = correctForDrops;
	}

	@Override
	public @NotNull Map<String, Object> serialize() {
		return null;
	}

}
