package dev.andila.ledger;

import dev.andila.ledger.exception.AlreadyReversedException;
import dev.andila.ledger.exception.TransactionNotFoundException;
import dev.andila.ledger.period.PeriodRepository;
import dev.andila.ledger.port.AccountRepository;
import dev.andila.ledger.port.TransactionRepository;
import dev.andila.ledger.posting.Posting;
import dev.andila.ledger.transaction.Transaction;
import dev.andila.ledger.transaction.TransactionBuilder;
import dev.andila.ledger.validation.AccountsActiveValidator;
import dev.andila.ledger.validation.AccountsExistValidator;
import dev.andila.ledger.validation.CurrencyMatchValidator;
import dev.andila.ledger.validation.OpenPeriodValidator;
import dev.andila.ledger.validation.TransactionValidator;
import dev.andila.ledger.validation.ValidatorChain;
import java.time.Clock;
import java.util.Objects;

/**
 * Entry point for hosts: validate, then store. A transaction is only appended if every validator
 * accepts it. Corrections are new transactions (reverse), never edits.
 */
public final class Ledger {

  /** Metadata key linking a reversal to the transaction it undoes. */
  public static final String REVERSAL_OF = "reversalOf";

  public static final String REVERSAL_REASON = "reversalReason";

  private final TransactionValidator validator;
  private final TransactionRepository transactions;
  private final Clock clock;

  public Ledger(TransactionValidator validator, TransactionRepository transactions) {
    this(validator, transactions, Clock.systemUTC());
  }

  public Ledger(TransactionValidator validator, TransactionRepository transactions, Clock clock) {
    this.validator = Objects.requireNonNull(validator, "validator");
    this.transactions = Objects.requireNonNull(transactions, "transactions");
    this.clock = Objects.requireNonNull(clock, "clock");
  }

  /** Standard rules: accounts exist, are active, and match posting currencies. */
  public static Ledger withStandardRules(
      AccountRepository accounts, TransactionRepository transactions) {
    return new Ledger(
        ValidatorChain.of(
            new AccountsExistValidator(accounts),
            new AccountsActiveValidator(accounts),
            new CurrencyMatchValidator(accounts)),
        transactions);
  }

  /** Standard rules plus period locking. */
  public static Ledger withStandardRules(
      AccountRepository accounts,
      TransactionRepository transactions,
      PeriodRepository periods,
      Clock clock) {
    return new Ledger(
        ValidatorChain.of(
            new OpenPeriodValidator(periods),
            new AccountsExistValidator(accounts),
            new AccountsActiveValidator(accounts),
            new CurrencyMatchValidator(accounts)),
        transactions,
        clock);
  }

  public void post(Transaction transaction) {
    validator.validate(transaction);
    transactions.append(transaction);
  }

  /**
   * Undoes a transaction by posting a new one with every direction flipped. The original is
   * untouched. The reversal is dated now, so it lands in the current (open) period even when the
   * original is in a closed one.
   */
  public Transaction reverse(String transactionId, String reason) {
    Transaction original =
        transactions
            .findById(transactionId)
            .orElseThrow(
                () -> new TransactionNotFoundException("Unknown transaction: " + transactionId));
    boolean alreadyReversed =
        transactions.findAll().stream()
            .anyMatch(t -> transactionId.equals(t.metadata().get(REVERSAL_OF)));
    if (alreadyReversed) {
      throw new AlreadyReversedException(
          "Transaction " + transactionId + " has already been reversed");
    }
    TransactionBuilder builder =
        new TransactionBuilder(clock)
            .description("Reversal of " + transactionId)
            .meta(REVERSAL_OF, transactionId);
    if (reason != null && !reason.isBlank()) {
      builder.meta(REVERSAL_REASON, reason);
    }
    for (Posting p : original.postings()) {
      builder.posting(new Posting(p.accountId(), p.direction().opposite(), p.amount()));
    }
    Transaction reversal = builder.build();
    post(reversal);
    return reversal;
  }
}
