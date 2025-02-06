package org.skriptlang.skript.lang.experiment;

import ch.njol.skript.registrations.Feature;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.script.ScriptData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * A container for storing and testing experiments.
 */
public class ExperimentSet extends LinkedHashSet<Experiment> implements ScriptData, Experimented {

	public ExperimentSet(@NotNull Collection<? extends Experiment> collection) {
		super(collection);
	}

	public ExperimentSet() {
		super();
	}

	@Override
	public boolean hasExperiment(Experiment experiment) {
		return this.contains(experiment);
	}

	@Override
	public boolean hasExperiment(String featureName) {
		for (Experiment experiment : this) {
			if (experiment.matches(featureName))
				return true;
		}
		return false;
	}

	public Feature[] getExperiments() {
		List<Feature> features = new ArrayList<>();
		for (Experiment experiment : this)
			if (experiment instanceof Feature feature)
				features.add(feature);
		return features.toArray(Feature[]::new);
	}

}
