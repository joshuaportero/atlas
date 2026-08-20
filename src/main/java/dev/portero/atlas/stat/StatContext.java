package dev.portero.atlas.stat;

import dev.portero.atlas.pipeline.PipelineContext;
import dev.portero.atlas.player.AtlasPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StatContext extends PipelineContext {

    private final AtlasPlayer player;
    private final Map<String, Double> bases = new HashMap<>();
    private final List<StatModifier> modifiers = new ArrayList<>();
    private final Map<String, Double> results = new HashMap<>();

    public StatContext(AtlasPlayer player) {
        this.player = player;
    }

    public AtlasPlayer player() {
        return this.player;
    }

    public Map<String, Double> bases() {
        return this.bases;
    }

    public List<StatModifier> modifiers() {
        return this.modifiers;
    }

    public Map<String, Double> results() {
        return this.results;
    }
}
