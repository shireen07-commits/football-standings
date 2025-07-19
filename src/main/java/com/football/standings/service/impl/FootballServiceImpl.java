package com.football.standings.service.impl;

import com.football.standings.model.Country;
import com.football.standings.model.League;
import com.football.standings.model.Standing;
import com.football.standings.model.Team;
import com.football.standings.service.FootballApiService;
import com.football.standings.service.FootballService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main business service implementing FootballService
 * Implements Single Responsibility and Open/Closed Principles
 */
@Service
public class FootballServiceImpl implements FootballService {
    
    private final FootballApiService footballApiService;
    private final AtomicBoolean offlineMode;
    
    @Value("${football.offline.enabled}")
    private boolean defaultOfflineMode;
    
    public FootballServiceImpl(FootballApiService footballApiService) {
        this.footballApiService = footballApiService;
        this.offlineMode = new AtomicBoolean(false);
    }
    
    @Override
    @Cacheable(value = "countries", unless = "#result.isEmpty()")
    public List<Country> getCountries() {
        if (offlineMode.get()) {
            return getFallbackCountries();
        }
        return footballApiService.fetchCountries();
    }
    
    @Override
    @Cacheable(value = "leagues", key = "#countryName", unless = "#result.isEmpty()")
    public List<League> getLeaguesByCountry(String countryName) {
        if (offlineMode.get()) {
            return getFallbackLeagues(countryName);
        }
        return footballApiService.fetchLeaguesByCountry(countryName);
    }
    
    @Override
    @Cacheable(value = "standings", key = "#leagueId", unless = "#result.isEmpty()")
    public List<Standing> getLeagueStandings(String leagueId) {
        if (offlineMode.get()) {
            return getFallbackStandings(leagueId);
        }
        return footballApiService.fetchStandings(leagueId);
    }
    
    @Override
    @Cacheable(value = "teams", key = "#leagueId", unless = "#result.isEmpty()")
    public List<Team> getTeamsByLeague(String leagueId) {
        if (offlineMode.get()) {
            return getFallbackTeams(leagueId);
        }
        return footballApiService.fetchTeamsByLeague(leagueId);
    }
    
    @Override
    public Optional<Standing> getTeamStanding(String countryName, String leagueName, String teamName) {
        // First get leagues for the country
        List<League> leagues = getLeaguesByCountry(countryName);
        
        // Find the league by name
        Optional<League> targetLeague = leagues.stream()
                .filter(league -> league.getLeagueName().equalsIgnoreCase(leagueName))
                .findFirst();
        
        if (targetLeague.isEmpty()) {
            return Optional.empty();
        }
        
        // Get standings for the league
        List<Standing> standings = getLeagueStandings(targetLeague.get().getLeagueId());
        
        // Find the team in standings
        return standings.stream()
                .filter(standing -> standing.getTeamName().equalsIgnoreCase(teamName))
                .findFirst();
    }
    
    @Override
    public void toggleOfflineMode(boolean offlineMode) {
        this.offlineMode.set(offlineMode);
    }
    
    @Override
    public boolean isOfflineMode() {
        return offlineMode.get();
    }
    
    /**
     * Fallback data when in offline mode or API unavailable
     * Implements Strategy Pattern for data retrieval
     */
    private List<Country> getFallbackCountries() {
        return List.of(
                new Country("41", "England"),
                new Country("6", "Spain"),
                new Country("5", "Germany"),
                new Country("3", "France"),
                new Country("4", "Italy")
        );
    }
    
    private List<League> getFallbackLeagues(String countryName) {
        return switch (countryName.toLowerCase()) {
            case "england" -> List.of(
                    new League("152", "Premier League", "41", "England"),
                    new League("153", "Championship", "41", "England")
            );
            case "spain" -> List.of(
                    new League("302", "La Liga", "6", "Spain")
            );
            case "germany" -> List.of(
                    new League("175", "Bundesliga", "5", "Germany")
            );
            default -> List.of();
        };
    }
    
    private List<Standing> getFallbackStandings(String leagueId) {
        if ("152".equals(leagueId)) {
            // Premier League sample data
            Standing city = createSampleStanding("152", "Premier League", "2629", "Manchester City", "1", "20", "15", "3", "2", "55", "25", "48");
            Standing liverpool = createSampleStanding("152", "Premier League", "2633", "Liverpool", "2", "20", "14", "4", "2", "52", "28", "46");
            Standing arsenal = createSampleStanding("152", "Premier League", "2628", "Arsenal", "3", "20", "13", "4", "3", "48", "30", "43");
            return List.of(city, liverpool, arsenal);
        }
        return List.of();
    }
    
    private Standing createSampleStanding(String leagueId, String leagueName, String teamId, String teamName,
                                        String position, String played, String wins, String draws, String losses,
                                        String goalsFor, String goalsAgainst, String points) {
        Standing standing = new Standing();
        standing.setCountryName("England");
        standing.setLeagueId(leagueId);
        standing.setLeagueName(leagueName);
        standing.setTeamId(teamId);
        standing.setTeamName(teamName);
        standing.setOverallLeaguePosition(position);
        standing.setOverallLeaguePlayed(played);
        standing.setOverallLeagueWins(wins);
        standing.setOverallLeagueDraws(draws);
        standing.setOverallLeagueLosses(losses);
        standing.setOverallLeagueGoalsFor(goalsFor);
        standing.setOverallLeagueGoalsAgainst(goalsAgainst);
        standing.setOverallLeaguePoints(points);
        return standing;
    }
    
    private List<Team> getFallbackTeams(String leagueId) {
        if ("152".equals(leagueId)) {
            // Premier League sample teams
            return List.of(
                    new Team("2629", "Manchester City", "https://apiv3.apifootball.com/badges/2629_manchester-city.png"),
                    new Team("2633", "Liverpool", "https://apiv3.apifootball.com/badges/2633_liverpool.png"),
                    new Team("2634", "Arsenal", "https://apiv3.apifootball.com/badges/2634_arsenal.png"),
                    new Team("2635", "Chelsea", "https://apiv3.apifootball.com/badges/2635_chelsea.png"),
                    new Team("2631", "Manchester United", "https://apiv3.apifootball.com/badges/2631_manchester-united.png"),
                    new Team("2636", "Tottenham", "https://apiv3.apifootball.com/badges/2636_tottenham.png")
            );
        }
        return List.of();
    }
}