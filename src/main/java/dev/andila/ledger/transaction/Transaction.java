package dev.andila.ledger.transaction;

import dev.andila.ledger.exception.InsufficientPostingsException;
import dev.andila.ledger.exception.InvalidTransactionException;
import dev.andila.ledger.exception.UnbalancedTransactionException;
import dev.andila.ledger.money.Money;
import dev.andila.ledger.posting.Posting;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The aggregate root of the ledger: a balanced, immutable set of postings. If a Transaction object
 * exists, its invariants hold. There is no way to build an invalid one, which is the point of
 * validating in the constructor.
 *
 * <p>Invariants: 1. non-blank id and description, non-null timestamp 2. at least two postings, with
 * at least one debit and one credit 3. for every currency present, total debits equal total credits
 */
public record Transaction(
    String id,
    Instant timestamp,
    String description,
    List<Posting> postings,
    Map<String, String> metadata) {

  public Transaction {
    if (id == null || id.isBlank()) {
      throw new InvalidTransactionException("id must not be blank");
    }
    if (timestamp == null) {
      throw new InvalidTransactionException("timestamp must not be null");
    }
    if (description == null || description.isBlank()) {
      throw new InvalidTransactionException("description must not be blank");
    }
    if (postings == null || metadata == null) {
      throw new InvalidTransactionException("postings and metadata must not be null");
    }
    // Defensive copies: the caller keeps their list, but cannot mutate ours.
    postings = List.copyOf(postings);
    metadata = Map.copyOf(metadata);

    if (postings.size() < 2) {
      throw new InsufficientPostingsException(
          "A transaction needs at least 2 postings but had " + postings.size());
    }
    boolean hasDebit = postings.stream().anyMatch(Posting::isDebit);
    boolean hasCredit = postings.stream().anyMatch(Posting::isCredit);
    if (!hasDebit || !hasCredit) {
      throw new InsufficientPostingsException(
          "A transaction needs at least one debit and one credit");
    }
    requireBalanced(postings);
  }

  public static TransactionBuilder builder() {
    return new TransactionBuilder();
  }

  private static void requireBalanced(List<Posting> postings) {
    Map<Currency, BigDecimal> net = new LinkedHashMap<>();
    for (Posting p : postings) {
      net.merge(p.amount().currency(), p.signedAmount().amount(), BigDecimal::add);
    }
    net.forEach(
        (currency, sum) -> {
          if (sum.signum() != 0) {
            throw new UnbalancedTransactionException(
                "Debits and credits differ by "
                    + sum.abs().toPlainString()
                    + " "
                    + currency.getCurrencyCode());
          }
        });
  }

  public Money totalDebits(String currencyCode) {
    return total(currencyCode, true);
  }

  public Money totalCredits(String currencyCode) {
    return total(currencyCode, false);
  }

  private Money total(String currencyCode, boolean debits) {
    return postings.stream()
        .filter(p -> p.isDebit() == debits)
        .filter(p -> p.amount().currency().getCurrencyCode().equals(currencyCode))
        .map(Posting::amount)
        .reduce(Money.zero(currencyCode), Money::add);
  }
}
