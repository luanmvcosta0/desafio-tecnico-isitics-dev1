package com.dev1_isitics.ligahub_be.modules.market.application;

import com.dev1_isitics.ligahub_be.modules.market.domain.Transaction;
import com.dev1_isitics.ligahub_be.modules.market.domain.TransactionSummary;
import java.util.List;

public record TransactionHistory(List<Transaction> transactions, TransactionSummary summary) {}
