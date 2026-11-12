package dev.andila.ledger.validation;

import dev.andila.ledger.exception.PeriodClosedException;
import dev.andila.ledger.period.PeriodRepository;
import dev.andila.ledger.transaction.Transaction;

/** Rejects transactions dated inside a closed period. */
public final class OpenPeriodValidator implements TransactionValidator {

  private final PeriodRepository periods;

  public OpenPeriodValidator(PeriodRepository periods) {
    this.periods = java.util.Objects.requireNonNull(periods, "periods");
  }

  @Override
  public void validate(Transaction transaction) {
    periods
        .closedThrough()
        .ifPresent(
            closed -> {
              if (!transaction.timestamp().isAfter(closed)) {
                throw new PeriodClosedException(
                    "Transaction "
                        + transaction.id()
                        + " is dated "
                        + transaction.timestamp()
                        + " but books are closed through "
                        + closed);
              }
            });
  }
}
