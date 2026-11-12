package dev.andila.ledger.transaction;

import dev.andila.ledger.account.Direction;
import dev.andila.ledger.exception.InvalidTransactionException;
import dev.andila.ledger.money.Money;
import dev.andila.ledger.posting.Posting;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Fluent builder for {@link Transaction}. It only collects input; every rule is enforced by the
 * Transaction constructor when {@link #build()} is called.
 *
 * <pre>
 * Transaction t = Transaction.builder()
 *     .description("Invoice 4092 paid")
 *     .meta("invoiceId", "inv_9921")
 *     .debit("cash", "1500.00", "KES")
 *     .credit("receivables", "1500.00", "KES")
 *     .build();
 * </pre>
 */
public final class TransactionBuilder {

  private final Clock clock;
  private String id;
  private Instant timestamp;
  private String description;
  private final List<Posting> postings = new ArrayList<>();
  private final Map<String, String> metadata = new LinkedHashMap<>();

  public TransactionBuilder() {
    this(Clock.systemUTC());
  }

  /** The clock is injectable so tests can control "now". */
  public TransactionBuilder(Clock clock) {
    this.clock = java.util.Objects.requireNonNull(clock, "clock");
  }

  public TransactionBuilder id(String id) {
    this.id = id;
    return this;
  }

  public TransactionBuilder at(Instant timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  public TransactionBuilder description(String description) {
    this.description = description;
    return this;
  }

  public TransactionBuilder meta(String key, String value) {
    if (key == null || key.isBlank() || value == null) {
      throw new InvalidTransactionException(
          "metadata key must not be blank and value must not be null");
    }
    metadata.put(key, value);
    return this;
  }

  public TransactionBuilder posting(Posting posting) {
    postings.add(java.util.Objects.requireNonNull(posting, "posting"));
    return this;
  }

  public TransactionBuilder debit(String accountId, Money amount) {
    postings.add(new Posting(accountId, Direction.DEBIT, amount));
    return this;
  }

  public TransactionBuilder credit(String accountId, Money amount) {
    postings.add(new Posting(accountId, Direction.CREDIT, amount));
    return this;
  }

  public TransactionBuilder debit(String accountId, String amount, String currencyCode) {
    return debit(accountId, Money.of(amount, currencyCode));
  }

  public TransactionBuilder credit(String accountId, String amount, String currencyCode) {
    return credit(accountId, Money.of(amount, currencyCode));
  }

  /** The only gate: validation happens in the Transaction constructor. */
  public Transaction build() {
    String finalId = id != null ? id : UUID.randomUUID().toString();
    Instant finalTimestamp = timestamp != null ? timestamp : clock.instant();
    return new Transaction(finalId, finalTimestamp, description, postings, metadata);
  }
}
