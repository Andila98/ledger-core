package dev.andila.ledger.posting;

import static org.junit.jupiter.api.Assertions.*;

import dev.andila.ledger.account.Direction;
import dev.andila.ledger.exception.InvalidPostingException;
import dev.andila.ledger.money.Money;
import org.junit.jupiter.api.Test;

class PostingTest {

  private final Money fifty = Money.of("50", "KES");

  @Test
  void factoriesSetDirection() {
    assertEquals(Direction.DEBIT, Posting.debit("a", fifty).direction());
    assertEquals(Direction.CREDIT, Posting.credit("a", fifty).direction());
    assertTrue(Posting.debit("a", fifty).isDebit());
    assertTrue(Posting.credit("a", fifty).isCredit());
  }

  @Test
  void signedAmountIsPositiveForDebitNegativeForCredit() {
    assertEquals(Money.of("50", "KES"), Posting.debit("a", fifty).signedAmount());
    assertEquals(Money.of("-50", "KES"), Posting.credit("a", fifty).signedAmount());
  }

  @Test
  void zeroAndNegativeAmountsAreRejected() {
    assertThrows(InvalidPostingException.class, () -> Posting.debit("a", Money.zero("KES")));
    assertThrows(InvalidPostingException.class, () -> Posting.credit("a", Money.of("-1", "KES")));
  }

  @Test
  void nullAndBlankInputsAreRejected() {
    assertThrows(InvalidPostingException.class, () -> new Posting(null, Direction.DEBIT, fifty));
    assertThrows(InvalidPostingException.class, () -> new Posting(" ", Direction.DEBIT, fifty));
    assertThrows(InvalidPostingException.class, () -> new Posting("a", null, fifty));
    assertThrows(InvalidPostingException.class, () -> new Posting("a", Direction.DEBIT, null));
  }

  @Test
  void recordEquality() {
    assertEquals(Posting.debit("a", fifty), Posting.debit("a", Money.of("50.00", "KES")));
    assertNotEquals(Posting.debit("a", fifty), Posting.credit("a", fifty));
  }
}
