package com.dev1_isitics.ligahub_be.modules.market.domain;

import com.dev1_isitics.ligahub_be.shared.money.Money;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Totals of a (filtered) history (RN17): spent in purchases (value + fee), received in sales (cash
 * change), every fee and commission, sale penalties and sale profit with its sign.
 */
public record TransactionSummary(
    BigDecimal totalSpent,
    BigDecimal totalReceived,
    BigDecimal totalFees,
    BigDecimal totalPenalties,
    BigDecimal totalProfit) {

  private static final Predicate<Transaction> PURCHASES =
      transaction -> transaction.getType() == TransactionType.COMPRA;
  private static final Predicate<Transaction> SALES =
      transaction -> transaction.getType() == TransactionType.VENDA;

  public static TransactionSummary of(List<Transaction> transactions) {
    return new TransactionSummary(
        sum(transactions, PURCHASES, transaction -> transaction.getValue().add(transaction.getFee())),
        sum(transactions, SALES, Transaction::getCashChange),
        sum(transactions, transaction -> true, Transaction::getFee),
        sum(transactions, SALES, Transaction::getPenalty),
        sum(transactions, SALES, Transaction::getProfit));
  }

  private static BigDecimal sum(
      List<Transaction> transactions,
      Predicate<Transaction> filter,
      Function<Transaction, BigDecimal> amount) {
    return Money.round(
        transactions.stream().filter(filter).map(amount).reduce(BigDecimal.ZERO, BigDecimal::add));
  }
}
