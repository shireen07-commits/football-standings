package com.football.standings.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Domain model representing a Country
 */
@Data
public class Country {
    
    @JsonProperty("country_id")
    private String countryId;
    
    @JsonProperty("country_name")
    private String countryName;
    
    public Country() {
    }
    
    public Country(String countryId, String countryName) {
        this.countryId = countryId;
        this.countryName = countryName;
    }
    
    @Override
    public String toString() {
        return "Country{" +
                "countryId='" + countryId + '\'' +
                ", countryName='" + countryName + '\'' +
                '}';
    }
}