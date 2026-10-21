package com.dev1_isitics.ligahub_be.modules.league.domain;

import com.dev1_isitics.ligahub_be.modules.clubs.domain.Club;
import com.dev1_isitics.ligahub_be.shared.text.AlphabeticalComparator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Standings of every club, including those that never played (RN21): win 3, draw 1, loss 0, ordered
 * by points, goal difference, goals for, alphabetical name and id, numbered from 1.
 */
public final class StandingsCalculator {

  private static final int WIN_POINTS = 3;
  private static final int DRAW_POINTS = 1;
  private static final Comparator<Tally> ORDER =
      Comparator.comparingInt(Tally::points)
          .thenComparingInt(Tally::goalDifference)
          .thenComparingInt(tally -> tally.goalsFor)
          .reversed()
          .thenComparing(tally -> tally.club.getName(), AlphabeticalComparator.INSTANCE)
          .thenComparing(tally -> tally.club.getId());

  private StandingsCalculator() {}

  public static List<Standing> compute(List<Club> clubs, List<Match> matches) {
    Map<Integer, Tally> tallies =
        clubs.stream().collect(Collectors.toMap(Club::getId, Tally::new, (a, b) -> a));
    for (Match match : matches) {
      tallies.get(match.getHomeClubId()).add(match.getHomeGoals(), match.getAwayGoals());
      tallies.get(match.getAwayClubId()).add(match.getAwayGoals(), match.getHomeGoals());
    }
    List<Tally> ordered = tallies.values().stream().sorted(ORDER).toList();
    List<Standing> standings = new ArrayList<>();
    for (int index = 0; index < ordered.size(); index++) {
      standings.add(ordered.get(index).toStanding(index + 1));
    }
    return standings;
  }

  private static final class Tally {

    private final Club club;
    private int wins;
    private int draws;
    private int losses;
    private int goalsFor;
    private int goalsAgainst;

    private Tally(Club club) {
      this.club = club;
    }

    private void add(int scored, int conceded) {
      goalsFor += scored;
      goalsAgainst += conceded;
      if (scored > conceded) {
        wins++;
      } else if (scored == conceded) {
        draws++;
      } else {
        losses++;
      }
    }

    private int points() {
      return wins * WIN_POINTS + draws * DRAW_POINTS;
    }

    private int goalDifference() {
      return goalsFor - goalsAgainst;
    }

    private Standing toStanding(int position) {
      return new Standing(
          position,
          club,
          wins + draws + losses,
          wins,
          draws,
          losses,
          goalsFor,
          goalsAgainst,
          goalDifference(),
          points());
    }
  }
}
