package dev.andila.ledger.account;

/**
 * The five fundamental account classes. Each knows its "normal balance": the side on which the
 * account increases.
 *
 * <p>Accounting equation: ASSET + EXPENSE = LIABILITY + EQUITY + REVENUE
 */
public enum AccountType {
  ASSET(Direction.DEBIT),
  EXPENSE(Direction.DEBIT),
  LIABILITY(Direction.CREDIT),
  EQUITY(Direction.CREDIT),
  REVENUE(Direction.CREDIT);

  private final Direction normalBalance;

  AccountType(Direction normalBalance) {
    this.normalBalance = normalBalance;
  }

  public Direction normalBalance() {
    return normalBalance;
  }

  /** True if a posting in this direction increases an account of this type. */
  public boolean isIncreasedBy(Direction direction) {
    return direction == normalBalance;
  }
}
