package com.dev1_isitics.ligahub_be.modules.market.infrastructure;

import com.dev1_isitics.ligahub_be.modules.market.application.TransactionFilter;
import com.dev1_isitics.ligahub_be.modules.market.domain.Transaction;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

/** History of a club with the optional filters, applied by the database with bound parameters. */
public final class TransactionSpecifications {

  /** Most recent first; same date-time, higher id first (RN17). */
  public static final Sort HISTORY_ORDER =
      Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

  private TransactionSpecifications() {}

  public static Specification<Transaction> ofClub(Integer clubId, TransactionFilter filter) {
    return (root, query, builder) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(builder.equal(root.get("clubId"), clubId));
      if (!filter.types().isEmpty()) {
        predicates.add(root.get("type").in(filter.types()));
      }
      if (filter.playerId() != null) {
        predicates.add(builder.equal(root.get("playerId"), filter.playerId()));
      }
      return builder.and(predicates.toArray(Predicate[]::new));
    };
  }
}
