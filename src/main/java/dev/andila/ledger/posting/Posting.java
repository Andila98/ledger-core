package dev.andila.ledger.posting;

import dev.andila.ledger.account.Direction;
import dev.andila.ledger.exception.InvalidPostingException;
import dev.andila.ledger.money.Money;

/**
 * One leg of a transaction: a positive amount applied to one account on one side. A record gives us
 * an immutable carrier with equals/hashCode/toString for free; the compact constructor is where the
 * invariants are enforced.
 */
public record Posting(String accountId, Direction direction, Money amount) {

  public Posting {
    if (accountId == null || accountId.isBlank()) {
      throw new InvalidPostingException("accountId must not be blank");
    }
    if (direction == null) {
      throw new InvalidPostingException("direction must not be null");
    }
    if (amount == null) {
      throw new InvalidPostingException("amount must not be null");
    }
    if (!amount.isPositive()) {
      throw new InvalidPostingException(
          "Posting amount must be strictly positive but was "
              + amount
              + "; use the opposite direction instead of a negative amount");
    }
  }

  public static Posting debit(String accountId, Money amount) {
    return new Posting(accountId, Direction.DEBIT, amount);
  }

  public static Posting credit(String accountId, Money amount) {
    return new Posting(accountId, Direction.CREDIT, amount);
  }

  public boolean isDebit() {
    return direction == Direction.DEBIT;
  }

  public boolean isCredit() {
    return direction == Direction.CREDIT;
  }

  /** Debits count positive, credits negative, so a balanced set sums to zero. */
  public Money signedAmount() {
    return isDebit() ? amount : amount.negate();
  }
}
