package com.football.standings.service;

import com.football.standings.model.Country;
import com.football.standings.model.League;
import com.football.standings.model.Standing;
import com.football.standings.service.impl.FootballServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FootballService
 * Follows TDD (Test-Driven Development) approach
 */
@ExtendWith(MockitoExtension.class)
class FootballServiceTest {
    
    @Mock
    private FootballApiService footballApiService;
    
    private FootballService footballService;
    
    @BeforeEach
    void setUp() {
        footballService = new FootballServiceImpl(footballApiService);
    }
    
    @Test
    void getCountries_ShouldReturnCountriesList() {
        // Given
        List<Country> expectedCountries = List.of(
                new Country("41", "England"),
                new Country("6", "Spain")
        );
        when(footballApiService.fetchCountries()).thenReturn(expectedCountries);
        
        // When
        List<Country> result = footballService.getCountries();
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCountryName()).isEqualTo("England");
        assertThat(result.get(1).getCountryName()).isEqualTo("Spain");
    }
    
    @Test
    void getLeaguesByCountry_ShouldReturnLeaguesForValidCountry() {
        // Given
        List<League> expectedLeagues = List.of(
                new League("152", "Premier League", "41", "England")
        );
        when(footballApiService.fetchLeaguesByCountry("England")).thenReturn(expectedLeagues);
        
        // When
        List<League> result = footballService.getLeaguesByCountry("England");
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLeagueName()).isEqualTo("Premier League");
    }
    
    @Test
    void getTeamStanding_ShouldReturnStandingForValidTeam() {
        // Given
        List<League> leagues = List.of(
                new League("152", "Premier League", "41", "England")
        );
        List<Standing> standings = List.of(
                createTestStanding("152", "Premier League", "2629", "Manchester City", "1")
        );
        
        when(footballApiService.fetchLeaguesByCountry("England")).thenReturn(leagues);
        when(footballApiService.fetchStandings("152")).thenReturn(standings);
        
        // When
        Optional<Standing> result = footballService.getTeamStanding("England", "Premier League", "Manchester City");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTeamName()).isEqualTo("Manchester City");
        assertThat(result.get().getOverallLeaguePosition()).isEqualTo("1");
    }
    
    @Test
    void getTeamStanding_ShouldReturnEmptyForNonExistentTeam() {
        // Given
        List<League> leagues = List.of(
                new League("152", "Premier League", "41", "England")
        );
        List<Standing> standings = List.of(
                createTestStanding("152", "Premier League", "2629", "Manchester City", "1")
        );
        
        when(footballApiService.fetchLeaguesByCountry("England")).thenReturn(leagues);
        when(footballApiService.fetchStandings("152")).thenReturn(standings);
        
        // When
        Optional<Standing> result = footballService.getTeamStanding("England", "Premier League", "Non-existent Team");
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    void toggleOfflineMode_ShouldUpdateOfflineStatus() {
        // When
        footballService.toggleOfflineMode(true);
        
        // Then
        assertThat(footballService.isOfflineMode()).isTrue();
        
        // When
        footballService.toggleOfflineMode(false);
        
        // Then
        assertThat(footballService.isOfflineMode()).isFalse();
    }
    
    @Test
    void getCountries_InOfflineMode_ShouldReturnFallbackData() {
        // Given
        footballService.toggleOfflineMode(true);
        
        // When
        List<Country> result = footballService.getCountries();
        
        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).anyMatch(country -> "England".equals(country.getCountryName()));
    }
    
    private Standing createTestStanding(String leagueId, String leagueName, String teamId, String teamName, String position) {
        Standing standing = new Standing();
        standing.setLeagueId(leagueId);
        standing.setLeagueName(leagueName);
        standing.setTeamId(teamId);
        standing.setTeamName(teamName);
        standing.setOverallLeaguePosition(position);
        standing.setCountryName("England");
        return standing;
    }
}