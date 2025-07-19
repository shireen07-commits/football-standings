package com.football.standings.service;

import com.football.standings.model.Country;
import com.football.standings.model.League;
import com.football.standings.model.Standing;
import com.football.standings.model.Team;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Football operations
 * Implements Interface Segregation Principle (ISP)
 */
public interface FootballService {
    
    /**
     * Get all countries
     */
    List<Country> getCountries();
    
    /**
     * Get leagues by country name
     */
    List<League> getLeaguesByCountry(String countryName);
    
    /**
     * Get standing for a specific team in a league
     */
    Optional<Standing> getTeamStanding(String countryName, String leagueName, String teamName);
    
    /**
     * Get all standings for a league
     */
    List<Standing> getLeagueStandings(String leagueId);
    
    /**
     * Get all teams for a league
     */
    List<Team> getTeamsByLeague(String leagueId);
    
    /**
     * Toggle offline mode
     */
    void toggleOfflineMode(boolean offlineMode);
    
    /**
     * Check if offline mode is enabled
     */
    boolean isOfflineMode();
}