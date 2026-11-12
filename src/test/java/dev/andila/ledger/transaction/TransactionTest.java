package dev.andila.ledger.transaction;

import static org.junit.jupiter.api.Assertions.*;

import dev.andila.ledger.exception.InsufficientPostingsException;
import dev.andila.ledger.exception.InvalidTransactionException;
import dev.andila.ledger.exception.UnbalancedTransactionException;
import dev.andila.ledger.money.Money;
import dev.andila.ledger.posting.Posting;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TransactionTest {

  private static Money kes(String v) {
    return Money.of(v, "KES");
  }

  private static Money usd(String v) {
    return Money.of(v, "USD");
  }

  private Transaction tx(List<Posting> postings) {
    return new Transaction("t1", Instant.parse("2026-11-12T00:00:00Z"), "test", postings, Map.of());
  }

  @Test
  void balancedTransactionIsAccepted() {
    Transaction t =
        tx(List.of(Posting.debit("cash", kes("100")), Posting.credit("revenue", kes("100"))));
    assertEquals(kes("100"), t.totalDebits("KES"));
    assertEquals(kes("100"), t.totalCredits("KES"));
  }

  @Test
  void splitTransactionBalances() {
    assertDoesNotThrow(
        () ->
            tx(
                List.of(
                    Posting.debit("cash", kes("116")),
                    Posting.credit("revenue", kes("100")),
                    Posting.credit("vat", kes("16")))));
  }

  @Test
  void unbalancedIsRejected() {
    assertThrows(
        UnbalancedTransactionException.class,
        () -> tx(List.of(Posting.debit("a", kes("100")), Posting.credit("b", kes("99")))));
  }

  @Test
  void eachCurrencyMustBalanceOnItsOwn() {
    assertDoesNotThrow(
        () ->
            tx(
                List.of(
                    Posting.debit("a", kes("100")), Posting.credit("b", kes("100")),
                    Posting.debit("c", usd("1")), Posting.credit("d", usd("1")))));
    assertThrows(
        UnbalancedTransactionException.class,
        () -> tx(List.of(Posting.debit("a", kes("100")), Posting.credit("b", usd("100")))));
  }

  @Test
  void tooFewPostingsOrOneSidedIsRejected() {
    assertThrows(
        InsufficientPostingsException.class, () -> tx(List.of(Posting.debit("a", kes("1")))));
    assertThrows(
        InsufficientPostingsException.class,
        () -> tx(List.of(Posting.debit("a", kes("1")), Posting.debit("b", kes("1")))));
  }

  @Test
  void invalidHeaderFieldsAreRejected() {
    List<Posting> ok = List.of(Posting.debit("a", kes("1")), Posting.credit("b", kes("1")));
    assertThrows(
        InvalidTransactionException.class,
        () -> new Transaction(" ", Instant.now(), "d", ok, Map.of()));
    assertThrows(
        InvalidTransactionException.class, () -> new Transaction("t", null, "d", ok, Map.of()));
    assertThrows(
        InvalidTransactionException.class,
        () -> new Transaction("t", Instant.now(), "", ok, Map.of()));
  }

  @Test
  void postingsAreDefensivelyCopiedAndImmutable() {
    List<Posting> source =
        new ArrayList<>(List.of(Posting.debit("a", kes("1")), Posting.credit("b", kes("1"))));
    Transaction t = tx(source);
    source.clear();
    assertEquals(2, t.postings().size());
    assertThrows(
        UnsupportedOperationException.class, () -> t.postings().add(Posting.debit("x", kes("1"))));
  }
}
