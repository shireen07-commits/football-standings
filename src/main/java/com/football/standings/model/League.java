package com.football.standings.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Domain model representing a League
 */
@Data
public class League {
    
    @JsonProperty("league_id")
    private String leagueId;
    
    @JsonProperty("league_name")
    private String leagueName;
    
    @JsonProperty("country_id")
    private String countryId;
    
    @JsonProperty("country_name")
    private String countryName;
    
    public League() {
    }
    
    public League(String leagueId, String leagueName, String countryId, String countryName) {
        this.leagueId = leagueId;
        this.leagueName = leagueName;
        this.countryId = countryId;
        this.countryName = countryName;
    }
    
    @Override
    public String toString() {
        return "League{" +
                "leagueId='" + leagueId + '\'' +
                ", leagueName='" + leagueName + '\'' +
                ", countryId='" + countryId + '\'' +
                ", countryName='" + countryName + '\'' +
                '}';
    }
}