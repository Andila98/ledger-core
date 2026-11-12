package dev.andila.ledger;

import static org.junit.jupiter.api.Assertions.*;

import dev.andila.ledger.account.Account;
import dev.andila.ledger.account.AccountType;
import dev.andila.ledger.exception.AccountNotFoundException;
import dev.andila.ledger.memory.InMemoryAccountRepository;
import dev.andila.ledger.memory.InMemoryTransactionRepository;
import dev.andila.ledger.transaction.Transaction;
import org.junit.jupiter.api.Test;

class LedgerTest {

  @Test
  void validTransactionIsStoredAndInvalidOneIsNot() {
    var accounts = new InMemoryAccountRepository();
    var txs = new InMemoryTransactionRepository();
    accounts.save(Account.open("cash", "1000", "Cash", AccountType.ASSET, "KES"));
    accounts.save(Account.open("rev", "4000", "Revenue", AccountType.REVENUE, "KES"));
    Ledger ledger = Ledger.withStandardRules(accounts, txs);

    ledger.post(
        Transaction.builder()
            .id("t1")
            .description("sale")
            .debit("cash", "10", "KES")
            .credit("rev", "10", "KES")
            .build());
    assertEquals(1, txs.findAll().size());

    assertThrows(
        AccountNotFoundException.class,
        () ->
            ledger.post(
                Transaction.builder()
                    .id("t2")
                    .description("bad")
                    .debit("cash", "10", "KES")
                    .credit("ghost", "10", "KES")
                    .build()));
    assertEquals(1, txs.findAll().size());
  }
}
