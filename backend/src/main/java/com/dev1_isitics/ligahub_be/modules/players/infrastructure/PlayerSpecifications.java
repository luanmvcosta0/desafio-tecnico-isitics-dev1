package com.dev1_isitics.ligahub_be.modules.players.infrastructure;

import com.dev1_isitics.ligahub_be.modules.players.application.PlayerListQuery;
import com.dev1_isitics.ligahub_be.modules.players.domain.Player;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filters of the listing that are plain columns, applied by the database with bound parameters.
 * Search and market value range depend on rules computed in Java and are applied by the service.
 */
public final class PlayerSpecifications {

  private PlayerSpecifications() {}

  public static Specification<Player> matchingColumns(PlayerListQuery query) {
    return (root, criteriaQuery, builder) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(builder.isNull(root.get("deletedAt")));
      predicates.add(builder.between(root.get("age"), query.minAge(), query.maxAge()));
      if (query.position() != null) {
        predicates.add(builder.equal(root.get("position"), query.position()));
      }
      if (query.condition() != null) {
        predicates.add(builder.equal(root.get("condition"), query.condition()));
      }
      if (query.clubId() != null) {
        predicates.add(builder.equal(root.get("clubId"), query.clubId()));
      }
      if (query.free() != null) {
        predicates.add(
            query.free()
                ? builder.isNull(root.get("clubId"))
                : builder.isNotNull(root.get("clubId")));
      }
      return builder.and(predicates.toArray(Predicate[]::new));
    };
  }
}
