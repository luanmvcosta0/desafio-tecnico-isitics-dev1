package com.dev1_isitics.ligahub_be.modules.players.application;

import com.dev1_isitics.ligahub_be.modules.clubs.infrastructure.ClubRepository;
import com.dev1_isitics.ligahub_be.modules.players.domain.Player;
import com.dev1_isitics.ligahub_be.modules.players.infrastructure.PlayerRepository;
import com.dev1_isitics.ligahub_be.modules.players.infrastructure.PlayerSpecifications;
import com.dev1_isitics.ligahub_be.shared.exception.ResourceNotFoundException;
import com.dev1_isitics.ligahub_be.shared.text.AlphabeticalComparator;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listing of players (RN07): column filters in the database; search, market value range, sorting
 * and pagination in Java, because they depend on the market value and on the alphabetical rule.
 */
@Service
@RequiredArgsConstructor
public class PlayerQueryService {

  private static final String CLUB_NOT_FOUND_MESSAGE = "Clube %d não encontrado.";

  private final PlayerRepository playerRepository;
  private final ClubRepository clubRepository;

  @Transactional(readOnly = true)
  public PlayerPage list(PlayerListQuery query) {
    if (query.clubId() != null && !clubRepository.existsById(query.clubId())) {
      throw new ResourceNotFoundException(CLUB_NOT_FOUND_MESSAGE.formatted(query.clubId()));
    }
    List<Player> matching =
        playerRepository.findAll(PlayerSpecifications.matchingColumns(query)).stream()
            .filter(matchesSearch(query.search()))
            .filter(matchesValueRange(query.minValue(), query.maxValue()))
            .sorted(comparatorOf(query.sorts()))
            .toList();
    return paginate(matching, query.page(), query.limit());
  }

  private static Predicate<Player> matchesSearch(String search) {
    if (search == null) {
      return player -> true;
    }
    String key = AlphabeticalComparator.toComparisonKey(search);
    return player -> AlphabeticalComparator.toComparisonKey(player.getName()).contains(key);
  }

  private static Predicate<Player> matchesValueRange(BigDecimal min, BigDecimal max) {
    return player -> {
      BigDecimal value = player.marketValue();
      return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    };
  }

  private static Comparator<Player> comparatorOf(List<PlayerSort> sorts) {
    return sorts.stream()
        .map(PlayerSort::comparator)
        .reduce(Comparator::thenComparing)
        .orElseThrow()
        .thenComparing(PlayerSort.TIE_BREAK);
  }

  private static PlayerPage paginate(List<Player> players, int page, int limit) {
    long total = players.size();
    long totalPages = (total + limit - 1) / limit;
    long offset = (long) (page - 1) * limit;
    List<Player> items =
        offset >= total ? List.of() : players.subList((int) offset, (int) Math.min(total, offset + limit));
    return new PlayerPage(items, page, limit, total, totalPages);
  }
}
