package dev.andila.ledger.transaction;

import static org.junit.jupiter.api.Assertions.*;

import dev.andila.ledger.exception.InsufficientPostingsException;
import dev.andila.ledger.exception.InvalidTransactionException;
import dev.andila.ledger.exception.UnbalancedTransactionException;
import dev.andila.ledger.money.Money;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class TransactionBuilderTest {

  private static final Instant NOW = Instant.parse("2026-11-12T08:00:00Z");
  private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

  @Test
  void buildsValidTransactionWithDefaults() {
    Transaction t =
        new TransactionBuilder(clock)
            .description("Invoice paid")
            .meta("invoiceId", "inv_1")
            .debit("cash", "1500.00", "KES")
            .credit("receivables", "1500.00", "KES")
            .build();
    assertEquals(NOW, t.timestamp());
    assertFalse(t.id().isBlank());
    assertEquals("inv_1", t.metadata().get("invoiceId"));
    assertEquals(Money.of("1500", "KES"), t.totalDebits("KES"));
  }

  @Test
  void explicitIdAndTimestampAreUsed() {
    Instant when = Instant.parse("2026-01-01T00:00:00Z");
    Transaction t =
        Transaction.builder()
            .id("tx-1")
            .at(when)
            .description("d")
            .debit("a", "1", "KES")
            .credit("b", "1", "KES")
            .build();
    assertEquals("tx-1", t.id());
    assertEquals(when, t.timestamp());
  }

  @Test
  void generatedIdsAreUnique() {
    Transaction a =
        Transaction.builder()
            .description("d")
            .debit("a", "1", "KES")
            .credit("b", "1", "KES")
            .build();
    Transaction b =
        Transaction.builder()
            .description("d")
            .debit("a", "1", "KES")
            .credit("b", "1", "KES")
            .build();
    assertNotEquals(a.id(), b.id());
  }

  @Test
  void buildEnforcesTransactionInvariants() {
    assertThrows(
        UnbalancedTransactionException.class,
        () ->
            Transaction.builder()
                .description("d")
                .debit("a", "10", "KES")
                .credit("b", "9", "KES")
                .build());
    assertThrows(
        InsufficientPostingsException.class,
        () -> Transaction.builder().description("d").debit("a", "10", "KES").build());
    assertThrows(
        InvalidTransactionException.class,
        () ->
            Transaction.builder()
                .debit("a", "10", "KES")
                .credit("b", "10", "KES")
                .build()); // no description
  }

  @Test
  void invalidMetadataIsRejected() {
    assertThrows(InvalidTransactionException.class, () -> Transaction.builder().meta(" ", "v"));
    assertThrows(InvalidTransactionException.class, () -> Transaction.builder().meta("k", null));
  }

  @Test
  void builtTransactionIsUnaffectedByLaterBuilderChanges() {
    TransactionBuilder b =
        Transaction.builder().description("d").debit("a", "1", "KES").credit("b", "1", "KES");
    Transaction first = b.build();
    b.debit("c", "5", "KES");
    assertEquals(2, first.postings().size());
  }
}
