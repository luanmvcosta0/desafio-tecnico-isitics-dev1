package com.dev1_isitics.ligahub_be.modules.market.application;

import com.dev1_isitics.ligahub_be.modules.market.domain.Contract;
import com.dev1_isitics.ligahub_be.modules.market.domain.Transaction;
import java.math.BigDecimal;

/** Result of a sale: the VENDA transaction, the ended contract and the penalty details. */
public record Sale(
    Transaction transaction,
    Contract contract,
    long remainingDays,
    int penaltyRate,
    BigDecimal clubCash) {}
