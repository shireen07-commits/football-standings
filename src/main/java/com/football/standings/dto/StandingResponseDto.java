package com.football.standings.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

/**
 * DTO for Standing Response with HATEOAS support
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StandingResponseDto extends RepresentationModel<StandingResponseDto> {
    
    private String countryId;
    private String countryName;
    private String leagueId;
    private String leagueName;
    private String teamId;
    private String teamName;
    private String overallLeaguePosition;
    private String overallLeaguePlayed;
    private String overallLeagueWins;
    private String overallLeagueDraws;
    private String overallLeagueLosses;
    private String overallLeagueGoalsFor;
    private String overallLeagueGoalsAgainst;
    private String overallLeaguePoints;
    private String teamBadge;
    private boolean fromCache;
    
    public StandingResponseDto() {
    }
}