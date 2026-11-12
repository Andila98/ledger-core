package dev.andila.ledger.account;

import static org.junit.jupiter.api.Assertions.*;

import dev.andila.ledger.exception.InvalidAccountException;
import org.junit.jupiter.api.Test;

class AccountTest {

  private Account cash() {
    return Account.open("acc_cash", "1000", "Cash", AccountType.ASSET, "KES");
  }

  @Test
  void openCreatesActiveAccount() {
    Account a = cash();
    assertTrue(a.isActive());
    assertEquals(Direction.DEBIT, a.normalBalance());
    assertTrue(a.parentId().isEmpty());
  }

  @Test
  void deactivateReturnsNewInstance() {
    Account a = cash();
    Account b = a.deactivate();
    assertTrue(a.isActive());
    assertFalse(b.isActive());
    assertEquals(a, b); // same identity
  }

  @Test
  void childMustMatchParentTypeAndCurrency() {
    Account parent = cash();
    Account child =
        Account.openChild("acc_mpesa", "1010", "M-Pesa", AccountType.ASSET, "KES", parent);
    assertEquals("acc_cash", child.parentId().orElseThrow());
    assertThrows(
        InvalidAccountException.class,
        () -> Account.openChild("x", "2", "X", AccountType.LIABILITY, "KES", parent));
    assertThrows(
        InvalidAccountException.class,
        () -> Account.openChild("x", "2", "X", AccountType.ASSET, "USD", parent));
  }

  @Test
  void invalidInputIsRejected() {
    assertThrows(
        InvalidAccountException.class, () -> Account.open(" ", "1", "n", AccountType.ASSET, "KES"));
    assertThrows(InvalidAccountException.class, () -> Account.open("i", "1", "n", null, "KES"));
    assertThrows(
        InvalidAccountException.class,
        () -> Account.open("i", "1", "n", AccountType.ASSET, "ZZZZ"));
  }

  @Test
  void equalityIsById() {
    Account a = Account.open("same", "1", "A", AccountType.ASSET, "KES");
    Account b = Account.open("same", "2", "B", AccountType.EXPENSE, "USD");
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
  }
}
