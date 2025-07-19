package com.football.standings.controller;

import com.football.standings.dto.StandingResponseDto;
import com.football.standings.model.Country;
import com.football.standings.model.League;
import com.football.standings.model.Standing;
import com.football.standings.model.Team;
import com.football.standings.service.FootballService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * REST Controller for Football Standings with HATEOAS support
 * Implements HATEOAS (Hypermedia as the Engine of Application State) principle
 */
@RestController
@RequestMapping("/standings")
@Tag(name = "Football Standings", description = "APIs for retrieving football standings")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FootballController {
    
    private final FootballService footballService;
    
    public FootballController(FootballService footballService) {
        this.footballService = footballService;
    }
    
    @GetMapping("/countries")
    @Operation(summary = "Get all countries", description = "Retrieve all available countries")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved countries"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CollectionModel<EntityModel<Country>>> getCountries() {
        List<Country> countries = footballService.getCountries();
        
        List<EntityModel<Country>> countryModels = countries.stream()
                .map(country -> EntityModel.of(country)
                        .add(linkTo(methodOn(FootballController.class).getLeaguesByCountry(country.getCountryName())).withRel("leagues")))
                .toList();
        
        CollectionModel<EntityModel<Country>> result = CollectionModel.of(countryModels)
                .add(linkTo(methodOn(FootballController.class).getCountries()).withSelfRel());
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/leagues")
    @Operation(summary = "Get leagues by country", description = "Retrieve all leagues for a specific country")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved leagues"),
            @ApiResponse(responseCode = "400", description = "Invalid country name"),
            @ApiResponse(responseCode = "404", description = "Country not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CollectionModel<EntityModel<League>>> getLeaguesByCountry(
            @Parameter(description = "Country name", required = true)
            @RequestParam @NotBlank String countryName) {
        
        List<League> leagues = footballService.getLeaguesByCountry(countryName);
        
        if (leagues.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<EntityModel<League>> leagueModels = leagues.stream()
                .map(league -> EntityModel.of(league)
                        .add(linkTo(methodOn(FootballController.class).getLeagueStandings(league.getLeagueId())).withRel("standings")))
                .toList();
        
        CollectionModel<EntityModel<League>> result = CollectionModel.of(leagueModels)
                .add(linkTo(methodOn(FootballController.class).getLeaguesByCountry(countryName)).withSelfRel())
                .add(linkTo(methodOn(FootballController.class).getCountries()).withRel("countries"));
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/teams")
    @Operation(summary = "Get teams by league", description = "Retrieve all teams for a specific league")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved teams"),
            @ApiResponse(responseCode = "400", description = "Invalid league ID"),
            @ApiResponse(responseCode = "404", description = "League not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CollectionModel<EntityModel<Team>>> getTeamsByLeague(
            @Parameter(description = "League ID", required = true)
            @RequestParam @NotBlank String leagueId) {
        
        List<Team> teams = footballService.getTeamsByLeague(leagueId);
        
        if (teams.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<EntityModel<Team>> teamModels = teams.stream()
                .map(team -> EntityModel.of(team)
                        .add(linkTo(methodOn(FootballController.class).getTeamsByLeague(leagueId)).withSelfRel()))
                .toList();
        
        CollectionModel<EntityModel<Team>> result = CollectionModel.of(teamModels)
                .add(linkTo(methodOn(FootballController.class).getTeamsByLeague(leagueId)).withSelfRel())
                .add(linkTo(methodOn(FootballController.class).getLeagueStandings(leagueId)).withRel("standings"));
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/league/{leagueId}")
    @Operation(summary = "Get league standings", description = "Retrieve all standings for a specific league")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved standings"),
            @ApiResponse(responseCode = "404", description = "League not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CollectionModel<EntityModel<Standing>>> getLeagueStandings(
            @Parameter(description = "League ID", required = true)
            @PathVariable String leagueId) {
        
        List<Standing> standings = footballService.getLeagueStandings(leagueId);
        
        if (standings.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<EntityModel<Standing>> standingModels = standings.stream()
                .map(standing -> EntityModel.of(standing)
                        .add(linkTo(methodOn(FootballController.class).getTeamStanding(
                                standing.getCountryName(), standing.getLeagueName(), standing.getTeamName())).withSelfRel()))
                .toList();
        
        CollectionModel<EntityModel<Standing>> result = CollectionModel.of(standingModels)
                .add(linkTo(methodOn(FootballController.class).getLeagueStandings(leagueId)).withSelfRel());
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/team")
    @Operation(summary = "Get team standing", description = "Retrieve standing for a specific team in a league")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved team standing"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "404", description = "Team standing not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<StandingResponseDto> getTeamStanding(
            @Parameter(description = "Country name", required = true)
            @RequestParam @NotBlank String countryName,
            @Parameter(description = "League name", required = true)
            @RequestParam @NotBlank String leagueName,
            @Parameter(description = "Team name", required = true)
            @RequestParam @NotBlank String teamName) {
        
        Optional<Standing> standingOpt = footballService.getTeamStanding(countryName, leagueName, teamName);
        
        if (standingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Standing standing = standingOpt.get();
        StandingResponseDto responseDto = convertToDto(standing);
        responseDto.setFromCache(footballService.isOfflineMode());
        
        // Add HATEOAS links
        responseDto.add(linkTo(methodOn(FootballController.class).getTeamStanding(countryName, leagueName, teamName)).withSelfRel());
        responseDto.add(linkTo(methodOn(FootballController.class).getLeagueStandings(standing.getLeagueId())).withRel("league-standings"));
        responseDto.add(linkTo(methodOn(FootballController.class).getLeaguesByCountry(countryName)).withRel("leagues"));
        responseDto.add(linkTo(methodOn(FootballController.class).getCountries()).withRel("countries"));
        
        return ResponseEntity.ok(responseDto);
    }
    
    @PostMapping("/offline-mode")
    @Operation(summary = "Toggle offline mode", description = "Enable or disable offline mode")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Offline mode toggled successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> toggleOfflineMode(
            @Parameter(description = "Enable offline mode", required = true)
            @RequestParam boolean enabled) {
        
        footballService.toggleOfflineMode(enabled);
        String message = enabled ? "Offline mode enabled" : "Offline mode disabled";
        return ResponseEntity.ok(message);
    }
    
    @GetMapping("/offline-mode")
    @Operation(summary = "Get offline mode status", description = "Check if offline mode is enabled")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved offline mode status")
    })
    public ResponseEntity<Boolean> getOfflineModeStatus() {
        return ResponseEntity.ok(footballService.isOfflineMode());
    }
    
    /**
     * Convert Standing entity to DTO
     * Implements Builder Pattern for DTO creation
     */
    private StandingResponseDto convertToDto(Standing standing) {
        StandingResponseDto dto = new StandingResponseDto();
        dto.setCountryName(standing.getCountryName());
        dto.setLeagueId(standing.getLeagueId());
        dto.setLeagueName(standing.getLeagueName());
        dto.setTeamId(standing.getTeamId());
        dto.setTeamName(standing.getTeamName());
        dto.setOverallLeaguePosition(standing.getOverallLeaguePosition());
        dto.setOverallLeaguePlayed(standing.getOverallLeaguePlayed());
        dto.setOverallLeagueWins(standing.getOverallLeagueWins());
        dto.setOverallLeagueDraws(standing.getOverallLeagueDraws());
        dto.setOverallLeagueLosses(standing.getOverallLeagueLosses());
        dto.setOverallLeagueGoalsFor(standing.getOverallLeagueGoalsFor());
        dto.setOverallLeagueGoalsAgainst(standing.getOverallLeagueGoalsAgainst());
        dto.setOverallLeaguePoints(standing.getOverallLeaguePoints());
        dto.setTeamBadge(standing.getTeamBadge());
        return dto;
    }
}