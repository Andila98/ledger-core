package dev.andila.ledger.period;

import static org.junit.jupiter.api.Assertions.*;

import dev.andila.ledger.Ledger;
import dev.andila.ledger.account.Account;
import dev.andila.ledger.account.AccountType;
import dev.andila.ledger.exception.InvalidPeriodException;
import dev.andila.ledger.exception.PeriodClosedException;
import dev.andila.ledger.memory.InMemoryAccountRepository;
import dev.andila.ledger.memory.InMemoryTransactionRepository;
import dev.andila.ledger.transaction.Transaction;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PeriodClosingTest {

  private static final Instant DEC_31 = Instant.parse("2026-12-31T23:59:59Z");
  private static final Instant NOW = Instant.parse("2027-01-15T00:00:00Z");

  private InMemoryPeriodRepository periods;
  private InMemoryTransactionRepository txs;
  private Ledger ledger;

  @BeforeEach
  void setUp() {
    var accounts = new InMemoryAccountRepository();
    txs = new InMemoryTransactionRepository();
    periods = new InMemoryPeriodRepository();
    accounts.save(Account.open("cash", "1000", "Cash", AccountType.ASSET, "KES"));
    accounts.save(Account.open("rev", "4000", "Revenue", AccountType.REVENUE, "KES"));
    ledger = Ledger.withStandardRules(accounts, txs, periods, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  private Transaction tx(String id, Instant at) {
    return Transaction.builder()
        .id(id)
        .at(at)
        .description("d")
        .debit("cash", "10", "KES")
        .credit("rev", "10", "KES")
        .build();
  }

  @Test
  void transactionsInsideClosedPeriodAreRejected() {
    ledger.post(tx("old", Instant.parse("2026-06-01T00:00:00Z")));
    periods.closeThrough(DEC_31);
    assertThrows(
        PeriodClosedException.class,
        () -> ledger.post(tx("late", Instant.parse("2026-12-01T00:00:00Z"))));
    assertThrows(PeriodClosedException.class, () -> ledger.post(tx("edge", DEC_31)));
    assertDoesNotThrow(() -> ledger.post(tx("new", NOW)));
  }

  @Test
  void periodCanOnlyMoveForward() {
    periods.closeThrough(DEC_31);
    assertThrows(
        InvalidPeriodException.class,
        () -> periods.closeThrough(Instant.parse("2026-06-30T00:00:00Z")));
    assertDoesNotThrow(() -> periods.closeThrough(NOW));
  }

  @Test
  void reversalOfClosedPeriodTransactionLandsInCurrentPeriod() {
    ledger.post(tx("t1", Instant.parse("2026-06-01T00:00:00Z")));
    periods.closeThrough(DEC_31);
    Transaction reversal = ledger.reverse("t1", "error found after close");
    assertEquals(NOW, reversal.timestamp());
  }
}
