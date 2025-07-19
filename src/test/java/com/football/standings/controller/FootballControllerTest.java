package com.football.standings.controller;

import com.football.standings.model.Country;
import com.football.standings.model.Standing;
import com.football.standings.model.Team;
import com.football.standings.service.FootballService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Integration tests for FootballController
 * Tests the REST API endpoints
 */
@WebMvcTest(FootballController.class)
class FootballControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private FootballService footballService;
    
    @Test
    @WithMockUser
    void getCountries_ShouldReturnCountriesWithHATEOASLinks() throws Exception {
        // Given
        List<Country> countries = List.of(
                new Country("41", "England"),
                new Country("6", "Spain")
        );
        when(footballService.getCountries()).thenReturn(countries);
        
        // When & Then
        mockMvc.perform(get("/standings/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.countryList").isArray())
                .andExpect(jsonPath("$._embedded.countryList[0].country_name").value("England"))
                .andExpect(jsonPath("$._embedded.countryList[0]._links.leagues").exists())
                .andExpect(jsonPath("$._links.self").exists());
    }
    
    @Test
    @WithMockUser
    void getTeamsByLeague_ShouldReturnTeamsWithHATEOASLinks() throws Exception {
        // Given
        List<Team> teams = List.of(
                new Team("2629", "Manchester City", "https://apiv3.apifootball.com/badges/2629_manchester-city.png"),
                new Team("2633", "Liverpool", "https://apiv3.apifootball.com/badges/2633_liverpool.png")
        );
        when(footballService.getTeamsByLeague("152")).thenReturn(teams);
        
        // When & Then
        mockMvc.perform(get("/standings/teams")
                        .param("leagueId", "152"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.teamList").isArray())
                .andExpect(jsonPath("$._embedded.teamList[0].team_name").value("Manchester City"))
                .andExpect(jsonPath("$._embedded.teamList[0].team_key").value("2629"))
                .andExpect(jsonPath("$._embedded.teamList[0]._links.self").exists())
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.standings").exists());
    }
    
    @Test
    @WithMockUser
    void getTeamStanding_ShouldReturnStandingWithHATEOASLinks() throws Exception {
        // Given
        Standing standing = createTestStanding();
        when(footballService.getTeamStanding("England", "Premier League", "Manchester City"))
                .thenReturn(Optional.of(standing));
        when(footballService.isOfflineMode()).thenReturn(false);
        
        // When & Then
        mockMvc.perform(get("/standings/team")
                        .param("countryName", "England")
                        .param("leagueName", "Premier League")
                        .param("teamName", "Manchester City"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamName").value("Manchester City"))
                .andExpect(jsonPath("$.overallLeaguePosition").value("1"))
                .andExpect(jsonPath("$.fromCache").value(false))
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.league-standings").exists())
                .andExpect(jsonPath("$._links.countries").exists());
    }
    
    @Test
    @WithMockUser
    void getTeamStanding_WithInvalidTeam_ShouldReturn404() throws Exception {
        // Given
        when(footballService.getTeamStanding(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());
        
        // When & Then
        mockMvc.perform(get("/standings/team")
                        .param("countryName", "England")
                        .param("leagueName", "Premier League")
                        .param("teamName", "Non-existent Team"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @WithMockUser
    void toggleOfflineMode_ShouldReturnSuccessMessage() throws Exception {
        // When & Then
        mockMvc.perform(post("/standings/offline-mode")
                        .param("enabled", "true")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Offline mode enabled"));
    }
    
    @Test
    @WithMockUser
    void getOfflineModeStatus_ShouldReturnCurrentStatus() throws Exception {
        // Given
        when(footballService.isOfflineMode()).thenReturn(true);
        
        // When & Then
        mockMvc.perform(get("/standings/offline-mode"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
    
    private Standing createTestStanding() {
        Standing standing = new Standing();
        standing.setCountryName("England");
        standing.setLeagueId("152");
        standing.setLeagueName("Premier League");
        standing.setTeamId("2629");
        standing.setTeamName("Manchester City");
        standing.setOverallLeaguePosition("1");
        standing.setOverallLeaguePlayed("20");
        standing.setOverallLeagueWins("15");
        standing.setOverallLeagueDraws("3");
        standing.setOverallLeagueLosses("2");
        standing.setOverallLeagueGoalsFor("55");
        standing.setOverallLeagueGoalsAgainst("25");
        standing.setOverallLeaguePoints("48");
        return standing;
    }
}