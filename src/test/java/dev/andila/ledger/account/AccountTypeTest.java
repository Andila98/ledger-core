package dev.andila.ledger.account;

import static dev.andila.ledger.account.AccountType.*;
import static dev.andila.ledger.account.Direction.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AccountTypeTest {

  @Test
  void normalBalances() {
    assertEquals(DEBIT, ASSET.normalBalance());
    assertEquals(DEBIT, EXPENSE.normalBalance());
    assertEquals(CREDIT, LIABILITY.normalBalance());
    assertEquals(CREDIT, EQUITY.normalBalance());
    assertEquals(CREDIT, REVENUE.normalBalance());
  }

  @Test
  void increasedBy() {
    assertTrue(ASSET.isIncreasedBy(DEBIT));
    assertFalse(ASSET.isIncreasedBy(CREDIT));
    assertTrue(REVENUE.isIncreasedBy(CREDIT));
  }

  @Test
  void directionOpposite() {
    assertEquals(CREDIT, DEBIT.opposite());
    assertEquals(DEBIT, CREDIT.opposite());
  }
}
