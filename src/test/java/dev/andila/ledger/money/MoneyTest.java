package dev.andila.ledger.money;

import static org.junit.jupiter.api.Assertions.*;

import dev.andila.ledger.exception.CurrencyMismatchException;
import dev.andila.ledger.exception.InvalidAmountException;
import org.junit.jupiter.api.Test;

class MoneyTest {

  @Test
  void scaleIsNormalizedSoEqualsWorks() {
    assertEquals(Money.of("10", "KES"), Money.of("10.00", "KES"));
    assertEquals(Money.of("10.0", "KES").hashCode(), Money.of("10.00", "KES").hashCode());
  }

  @Test
  void addReturnsNewInstance() {
    Money a = Money.of("10.00", "KES");
    Money sum = a.add(Money.of("5.50", "KES"));
    assertEquals(Money.of("15.50", "KES"), sum);
    assertEquals(Money.of("10.00", "KES"), a);
  }

  @Test
  void subtractAndNegate() {
    assertEquals(Money.of("-2.00", "USD"), Money.of("3", "USD").subtract(Money.of("5", "USD")));
    assertEquals(Money.of("-3.00", "USD"), Money.of("3", "USD").negate());
  }

  @Test
  void currencyMismatchIsRejected() {
    assertThrows(
        CurrencyMismatchException.class, () -> Money.of("1", "KES").add(Money.of("1", "USD")));
  }

  @Test
  void silentRoundingIsRejected() {
    assertThrows(InvalidAmountException.class, () -> Money.of("10.005", "KES"));
  }

  @Test
  void invalidInputIsRejected() {
    assertThrows(InvalidAmountException.class, () -> Money.of("abc", "KES"));
    assertThrows(InvalidAmountException.class, () -> Money.of("1", "XXXX"));
    assertThrows(InvalidAmountException.class, () -> Money.of((String) null, "KES"));
  }

  @Test
  void zeroDecimalCurrencyUsesScaleZero() {
    assertEquals("100 JPY", Money.of("100", "JPY").toString());
  }
}
