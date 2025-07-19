package com.football.standings.service;

import com.football.standings.model.Country;
import com.football.standings.model.League;
import com.football.standings.model.Standing;
import com.football.standings.model.Team;

import java.util.List;

/**
 * Service interface for external Football API calls
 * Implements Dependency Inversion Principle (DIP)
 */
public interface FootballApiService {
    
    /**
     * Fetch countries from external API
     */
    List<Country> fetchCountries();
    
    /**
     * Fetch leagues by country from external API
     */
    List<League> fetchLeaguesByCountry(String countryName);
    
    /**
     * Fetch standings for a specific league from external API
     */
    List<Standing> fetchStandings(String leagueId);
    
    /**
     * Fetch teams for a specific league from external API
     */
    List<Team> fetchTeamsByLeague(String leagueId);
}