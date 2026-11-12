package dev.andila.ledger.balance;

import static org.junit.jupiter.api.Assertions.*;

import dev.andila.ledger.Ledger;
import dev.andila.ledger.account.Account;
import dev.andila.ledger.account.AccountType;
import dev.andila.ledger.exception.AccountNotFoundException;
import dev.andila.ledger.memory.InMemoryAccountRepository;
import dev.andila.ledger.memory.InMemoryTransactionRepository;
import dev.andila.ledger.money.Money;
import dev.andila.ledger.transaction.Transaction;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BalanceCalculatorTest {

  private static final Instant JAN = Instant.parse("2026-01-10T00:00:00Z");
  private static final Instant FEB = Instant.parse("2026-02-10T00:00:00Z");

  private InMemoryAccountRepository accounts;
  private BalanceCalculator calc;

  @BeforeEach
  void setUp() {
    accounts = new InMemoryAccountRepository();
    var txs = new InMemoryTransactionRepository();
    accounts.save(Account.open("cash", "1000", "Cash", AccountType.ASSET, "KES"));
    accounts.save(Account.open("rev", "4000", "Revenue", AccountType.REVENUE, "KES"));
    accounts.save(Account.open("exp", "5000", "Rent", AccountType.EXPENSE, "KES"));
    Ledger ledger = Ledger.withStandardRules(accounts, txs);
    ledger.post(
        Transaction.builder()
            .id("t1")
            .at(JAN)
            .description("sale")
            .debit("cash", "100", "KES")
            .credit("rev", "100", "KES")
            .build());
    ledger.post(
        Transaction.builder()
            .id("t2")
            .at(FEB)
            .description("rent")
            .debit("exp", "30", "KES")
            .credit("cash", "30", "KES")
            .build());
    calc = new BalanceCalculator(accounts, txs);
  }

  @Test
  void balancesFollowNormalSide() {
    assertEquals(Money.of("70", "KES"), calc.balanceOf("cash")); // asset: +100 -30
    assertEquals(Money.of("100", "KES"), calc.balanceOf("rev")); // revenue: credit increases
    assertEquals(Money.of("30", "KES"), calc.balanceOf("exp"));
  }

  @Test
  void balanceAsOfIgnoresLaterTransactions() {
    assertEquals(Money.of("100", "KES"), calc.balanceAsOf("cash", JAN));
    assertEquals(Money.of("70", "KES"), calc.balanceAsOf("cash", FEB));
  }

  @Test
  void trialBalanceSatisfiesAccountingEquation() {
    var tb = calc.trialBalance(Instant.MAX);
    Money debitSide = Money.zero("KES"), creditSide = Money.zero("KES");
    for (AccountBalance b : tb) {
      if (b.account().normalBalance() == dev.andila.ledger.account.Direction.DEBIT)
        debitSide = debitSide.add(b.balance());
      else creditSide = creditSide.add(b.balance());
    }
    assertEquals(debitSide, creditSide);
  }

  @Test
  void unknownAccountIsRejected() {
    assertThrows(AccountNotFoundException.class, () -> calc.balanceOf("ghost"));
  }
}
