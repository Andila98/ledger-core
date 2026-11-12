package dev.andila.ledger.port;

import static org.junit.jupiter.api.Assertions.*;

import dev.andila.ledger.exception.DuplicateTransactionException;
import dev.andila.ledger.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Every TransactionRepository adapter must pass these. */
public abstract class TransactionRepositoryContractTest {

  protected abstract TransactionRepository createRepository();

  private TransactionRepository repo;

  @BeforeEach
  void setUp() {
    repo = createRepository();
  }

  private Transaction tx(String id, String debitAcc, String creditAcc) {
    return Transaction.builder()
        .id(id)
        .description("d")
        .debit(debitAcc, "10", "KES")
        .credit(creditAcc, "10", "KES")
        .build();
  }

  @Test
  void appendedTransactionCanBeFoundById() {
    repo.append(tx("t1", "cash", "rev"));
    assertEquals("t1", repo.findById("t1").orElseThrow().id());
    assertTrue(repo.findById("missing").isEmpty());
  }

  @Test
  void duplicateIdIsRejected() {
    repo.append(tx("t1", "cash", "rev"));
    assertThrows(DuplicateTransactionException.class, () -> repo.append(tx("t1", "a", "b")));
  }

  @Test
  void findByAccountReturnsOnlyTouchingTransactionsInOrder() {
    repo.append(tx("t1", "cash", "rev"));
    repo.append(tx("t2", "bank", "rev"));
    repo.append(tx("t3", "cash", "loan"));
    var result = repo.findByAccountId("cash");
    assertEquals(2, result.size());
    assertEquals("t1", result.get(0).id());
    assertEquals("t3", result.get(1).id());
  }

  @Test
  void findAllKeepsAppendOrder() {
    repo.append(tx("b", "x", "y"));
    repo.append(tx("a", "x", "y"));
    assertEquals("b", repo.findAll().get(0).id());
  }
}
