package edu.univ.erp.api.types;

import java.util.HashMap;
import java.util.Map;


public class SectionStatsDto {
    private Map<String, Double> stats;

    public SectionStatsDto() {
        this.stats = new HashMap<>();
    }

    public SectionStatsDto(Map<String, Double> stats) {
        this.stats = stats != null ? new HashMap<>(stats) : new HashMap<>();
    }

    public Map<String, Double> getStats() {
        return stats;
    }

    public void setStats(Map<String, Double> stats) {
        this.stats = stats != null ? new HashMap<>(stats) : new HashMap<>();
    }
}


