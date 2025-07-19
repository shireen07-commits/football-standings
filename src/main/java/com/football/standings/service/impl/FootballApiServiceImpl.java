package com.football.standings.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.football.standings.model.Country;
import com.football.standings.model.League;
import com.football.standings.model.Standing;
import com.football.standings.model.Team;
import com.football.standings.service.FootballApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Implementation of FootballApiService for external API calls
 * Implements Single Responsibility Principle (SRP)
 */
@Service
public class FootballApiServiceImpl implements FootballApiService {
    
    private static final Logger LOGGER = Logger.getLogger(FootballApiServiceImpl.class.getName());
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Value("${football.api.key}")
    private String apiKey;
    
    @Value("${football.api.timeout}")
    private int timeout;
    
    public FootballApiServiceImpl(WebClient webClient) {
        this.webClient = webClient;
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public List<Country> fetchCountries() {
        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/")
                            .queryParam("action", "get_countries")
                            .queryParam("APIkey", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeout))
                    .onErrorReturn("[]")
                    .block();
            
            if (response != null && !response.equals("[]")) {
                return objectMapper.readValue(response, new TypeReference<List<Country>>() {});
            }
        } catch (Exception e) {
            LOGGER.warning("Failed to fetch countries from API: " + e.getMessage());
        }
        
        return getFallbackCountries();
    }
    
    @Override
    public List<League> fetchLeaguesByCountry(String countryName) {
        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/")
                            .queryParam("action", "get_leagues")
                            .queryParam("country_id", getCountryIdByName(countryName))
                            .queryParam("APIkey", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeout))
                    .onErrorReturn("[]")
                    .block();
            
            if (response != null && !response.equals("[]")) {
                return objectMapper.readValue(response, new TypeReference<List<League>>() {});
            }
        } catch (Exception e) {
            LOGGER.warning("Failed to fetch leagues for country " + countryName + ": " + e.getMessage());
        }
        
        return getFallbackLeagues(countryName);
    }
    
    @Override
    public List<Standing> fetchStandings(String leagueId) {
        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/")
                            .queryParam("action", "get_standings")
                            .queryParam("league_id", leagueId)
                            .queryParam("APIkey", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeout))
                    .onErrorReturn("[]")
                    .block();
            
            if (response != null && !response.equals("[]")) {
                return objectMapper.readValue(response, new TypeReference<List<Standing>>() {});
            }
        } catch (Exception e) {
            LOGGER.warning("Failed to fetch standings for league " + leagueId + ": " + e.getMessage());
        }
        
        return getFallbackStandings(leagueId);
    }
    
    @Override
    public List<Team> fetchTeamsByLeague(String leagueId) {
        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/")
                            .queryParam("action", "get_teams")
                            .queryParam("league_id", leagueId)
                            .queryParam("APIkey", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeout))
                    .onErrorReturn("[]")
                    .block();
            
            if (response != null && !response.equals("[]")) {
                return objectMapper.readValue(response, new TypeReference<List<Team>>() {});
            }
        } catch (Exception e) {
            LOGGER.warning("Failed to fetch teams for league " + leagueId + ": " + e.getMessage());
        }
        
        return getFallbackTeams(leagueId);
    }
    
    /**
     * Fallback method to provide sample data when API is unavailable
     * Implements Resilience Pattern
     */
    private List<Country> getFallbackCountries() {
        List<Country> countries = new ArrayList<>();
        countries.add(new Country("41", "England"));
        countries.add(new Country("6", "Spain"));
        countries.add(new Country("5", "Germany"));
        countries.add(new Country("3", "France"));
        countries.add(new Country("4", "Italy"));
        return countries;
    }
    
    private List<League> getFallbackLeagues(String countryName) {
        List<League> leagues = new ArrayList<>();
        if ("England".equalsIgnoreCase(countryName)) {
            leagues.add(new League("152", "Premier League", "41", "England"));
            leagues.add(new League("153", "Championship", "41", "England"));
        } else if ("Spain".equalsIgnoreCase(countryName)) {
            leagues.add(new League("302", "La Liga", "6", "Spain"));
        } else if ("Germany".equalsIgnoreCase(countryName)) {
            leagues.add(new League("175", "Bundesliga", "5", "Germany"));
        }
        return leagues;
    }
    
    private List<Standing> getFallbackStandings(String leagueId) {
        List<Standing> standings = new ArrayList<>();
        
        // Sample Premier League standings
        if ("152".equals(leagueId)) {
            Standing standing1 = new Standing();
            standing1.setCountryName("England");
            standing1.setLeagueId("152");
            standing1.setLeagueName("Premier League");
            standing1.setTeamId("2629");
            standing1.setTeamName("Manchester City");
            standing1.setOverallLeaguePosition("1");
            standing1.setOverallLeaguePlayed("20");
            standing1.setOverallLeagueWins("15");
            standing1.setOverallLeagueDraws("3");
            standing1.setOverallLeagueLosses("2");
            standing1.setOverallLeagueGoalsFor("55");
            standing1.setOverallLeagueGoalsAgainst("25");
            standing1.setOverallLeaguePoints("48");
            
            Standing standing2 = new Standing();
            standing2.setCountryName("England");
            standing2.setLeagueId("152");
            standing2.setLeagueName("Premier League");
            standing2.setTeamId("2633");
            standing2.setTeamName("Liverpool");
            standing2.setOverallLeaguePosition("2");
            standing2.setOverallLeaguePlayed("20");
            standing2.setOverallLeagueWins("14");
            standing2.setOverallLeagueDraws("4");
            standing2.setOverallLeagueLosses("2");
            standing2.setOverallLeagueGoalsFor("52");
            standing2.setOverallLeagueGoalsAgainst("28");
            standing2.setOverallLeaguePoints("46");
            
            standings.add(standing1);
            standings.add(standing2);
        }
        
        return standings;
    }
    
    private List<Team> getFallbackTeams(String leagueId) {
        List<Team> teams = new ArrayList<>();
        
        // Sample Premier League teams
        if ("152".equals(leagueId)) {
            teams.add(new Team("2629", "Manchester City", "https://apiv3.apifootball.com/badges/2629_manchester-city.png"));
            teams.add(new Team("2633", "Liverpool", "https://apiv3.apifootball.com/badges/2633_liverpool.png"));
            teams.add(new Team("2634", "Arsenal", "https://apiv3.apifootball.com/badges/2634_arsenal.png"));
            teams.add(new Team("2635", "Chelsea", "https://apiv3.apifootball.com/badges/2635_chelsea.png"));
            teams.add(new Team("2631", "Manchester United", "https://apiv3.apifootball.com/badges/2631_manchester-united.png"));
            teams.add(new Team("2636", "Tottenham", "https://apiv3.apifootball.com/badges/2636_tottenham.png"));
        }
        
        return teams;
    }
    
    private String getCountryIdByName(String countryName) {
        // Simple mapping for demo purposes
        switch (countryName.toLowerCase()) {
            case "england": return "41";
            case "spain": return "6";
            case "germany": return "5";
            case "france": return "3";
            case "italy": return "4";
            default: return "41";
        }
    }
}