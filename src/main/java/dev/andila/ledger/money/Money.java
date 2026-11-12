package dev.andila.ledger.money;

import dev.andila.ledger.exception.CurrencyMismatchException;
import dev.andila.ledger.exception.InvalidAmountException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Immutable value object: an exact amount in a single currency. Scale is normalized to the
 * currency's minor units, so equals() is reliable.
 */
public final class Money implements Comparable<Money> {

  private final BigDecimal amount;
  private final Currency currency;

  private Money(BigDecimal amount, Currency currency) {
    this.amount = amount;
    this.currency = currency;
  }

  public static Money of(String amount, String currencyCode) {
    if (amount == null) throw new InvalidAmountException("Amount must not be null");
    return of(parse(amount), currencyCode);
  }

  public static Money of(BigDecimal amount, String currencyCode) {
    if (amount == null) throw new InvalidAmountException("Amount must not be null");
    if (currencyCode == null) throw new InvalidAmountException("Currency must not be null");
    Currency currency;
    try {
      currency = Currency.getInstance(currencyCode);
    } catch (IllegalArgumentException e) {
      throw new InvalidAmountException("Unknown ISO 4217 currency: " + currencyCode);
    }
    return new Money(normalize(amount, currency), currency);
  }

  public static Money zero(String currencyCode) {
    return of(BigDecimal.ZERO, currencyCode);
  }

  private static BigDecimal parse(String raw) {
    try {
      return new BigDecimal(raw);
    } catch (NumberFormatException e) {
      throw new InvalidAmountException("Not a valid number: " + raw);
    }
  }

  private static BigDecimal normalize(BigDecimal value, Currency currency) {
    int digits = Math.max(currency.getDefaultFractionDigits(), 0);
    try {
      return value.setScale(digits, RoundingMode.UNNECESSARY);
    } catch (ArithmeticException e) {
      throw new InvalidAmountException(
          "Amount "
              + value.toPlainString()
              + " has more than "
              + digits
              + " decimal places for "
              + currency.getCurrencyCode()
              + "; rounding is the caller's responsibility");
    }
  }

  public Money add(Money other) {
    requireSameCurrency(other);
    return new Money(amount.add(other.amount), currency);
  }

  public Money subtract(Money other) {
    requireSameCurrency(other);
    return new Money(amount.subtract(other.amount), currency);
  }

  public Money negate() {
    return new Money(amount.negate(), currency);
  }

  public boolean isZero() {
    return amount.signum() == 0;
  }

  public boolean isPositive() {
    return amount.signum() > 0;
  }

  public boolean isNegative() {
    return amount.signum() < 0;
  }

  public BigDecimal amount() {
    return amount;
  }

  public Currency currency() {
    return currency;
  }

  private void requireSameCurrency(Money other) {
    Objects.requireNonNull(other, "other");
    if (!currency.equals(other.currency)) {
      throw new CurrencyMismatchException(
          "Cannot combine "
              + currency.getCurrencyCode()
              + " with "
              + other.currency.getCurrencyCode());
    }
  }

  @Override
  public int compareTo(Money other) {
    requireSameCurrency(other);
    return amount.compareTo(other.amount);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Money m)) return false;
    return amount.equals(m.amount) && currency.equals(m.currency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount, currency);
  }

  @Override
  public String toString() {
    return amount.toPlainString() + " " + currency.getCurrencyCode();
  }
}
