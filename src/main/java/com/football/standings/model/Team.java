package com.football.standings.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Domain model representing a Team
 */
@Data
public class Team {
    
    @JsonProperty("team_key")
    private String teamKey;
    
    @JsonProperty("team_name")
    private String teamName;
    
    @JsonProperty("team_badge")
    private String teamBadge;
    
    public Team() {
    }
    
    public Team(String teamKey, String teamName, String teamBadge) {
        this.teamKey = teamKey;
        this.teamName = teamName;
        this.teamBadge = teamBadge;
    }
    
    @Override
    public String toString() {
        return "Team{" +
                "teamKey='" + teamKey + '\'' +
                ", teamName='" + teamName + '\'' +
                ", teamBadge='" + teamBadge + '\'' +
                '}';
    }
}