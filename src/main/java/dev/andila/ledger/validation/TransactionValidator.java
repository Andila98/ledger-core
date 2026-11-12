package dev.andila.ledger.validation;

import dev.andila.ledger.transaction.Transaction;

/**
 * Strategy: one business rule applied to a transaction before it is stored. Implementations throw a
 * LedgerException to reject. Because this is a functional interface, host apps can plug in rules as
 * lambdas, e.g. an overdraft or credit-limit check that needs the host's own data.
 */
@FunctionalInterface
public interface TransactionValidator {

  void validate(Transaction transaction);
}
