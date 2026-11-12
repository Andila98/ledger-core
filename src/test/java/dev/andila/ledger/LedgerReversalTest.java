package dev.andila.ledger;

import static org.junit.jupiter.api.Assertions.*;

import dev.andila.ledger.account.Account;
import dev.andila.ledger.account.AccountType;
import dev.andila.ledger.balance.BalanceCalculator;
import dev.andila.ledger.exception.AlreadyReversedException;
import dev.andila.ledger.exception.TransactionNotFoundException;
import dev.andila.ledger.memory.InMemoryAccountRepository;
import dev.andila.ledger.memory.InMemoryTransactionRepository;
import dev.andila.ledger.money.Money;
import dev.andila.ledger.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LedgerReversalTest {

  private InMemoryTransactionRepository txs;
  private Ledger ledger;
  private BalanceCalculator calc;

  @BeforeEach
  void setUp() {
    var accounts = new InMemoryAccountRepository();
    txs = new InMemoryTransactionRepository();
    accounts.save(Account.open("cash", "1000", "Cash", AccountType.ASSET, "KES"));
    accounts.save(Account.open("rev", "4000", "Revenue", AccountType.REVENUE, "KES"));
    ledger = Ledger.withStandardRules(accounts, txs);
    calc = new BalanceCalculator(accounts, txs);
    ledger.post(
        Transaction.builder()
            .id("t1")
            .description("sale")
            .debit("cash", "100", "KES")
            .credit("rev", "100", "KES")
            .build());
  }

  @Test
  void reversalRestoresBalancesAndKeepsHistory() {
    Transaction reversal = ledger.reverse("t1", "customer refund");
    assertEquals(Money.zero("KES"), calc.balanceOf("cash"));
    assertEquals(Money.zero("KES"), calc.balanceOf("rev"));
    assertEquals(2, txs.findAll().size()); // original is still there
    assertEquals("t1", reversal.metadata().get(Ledger.REVERSAL_OF));
    assertEquals("customer refund", reversal.metadata().get(Ledger.REVERSAL_REASON));
  }

  @Test
  void cannotReverseTwiceOrReverseUnknown() {
    ledger.reverse("t1", null);
    assertThrows(AlreadyReversedException.class, () -> ledger.reverse("t1", null));
    assertThrows(TransactionNotFoundException.class, () -> ledger.reverse("nope", null));
  }
}
