package dev.andila.ledger.account;

/** The two sides of a double-entry posting. */
public enum Direction {
  DEBIT,
  CREDIT;

  public Direction opposite() {
    return this == DEBIT ? CREDIT : DEBIT;
  }
}
