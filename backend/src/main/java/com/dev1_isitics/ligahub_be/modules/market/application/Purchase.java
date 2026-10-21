package com.dev1_isitics.ligahub_be.modules.market.application;

import com.dev1_isitics.ligahub_be.modules.market.domain.Contract;
import com.dev1_isitics.ligahub_be.modules.market.domain.Transaction;
import java.math.BigDecimal;

/** Result of a purchase: the COMPRA transaction, the new contract and the cash after it. */
public record Purchase(Transaction transaction, Contract contract, BigDecimal clubCash) {}
