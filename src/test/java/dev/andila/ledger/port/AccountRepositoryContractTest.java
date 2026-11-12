package dev.andila.ledger.port;

import static org.junit.jupiter.api.Assertions.*;

import dev.andila.ledger.account.Account;
import dev.andila.ledger.account.AccountType;
import dev.andila.ledger.exception.DuplicateAccountException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Every AccountRepository adapter must pass these. Subclass and supply the adapter. */
public abstract class AccountRepositoryContractTest {

  protected abstract AccountRepository createRepository();

  private AccountRepository repo;

  @BeforeEach
  void setUp() {
    repo = createRepository();
  }

  private Account cash() {
    return Account.open("acc_cash", "1000", "Cash", AccountType.ASSET, "KES");
  }

  @Test
  void savedAccountCanBeFoundByIdAndCode() {
    repo.save(cash());
    assertEquals("acc_cash", repo.findById("acc_cash").orElseThrow().id());
    assertEquals("acc_cash", repo.findByCode("1000").orElseThrow().id());
    assertTrue(repo.findById("nope").isEmpty());
  }

  @Test
  void savingSameIdReplacesTheAccount() {
    repo.save(cash());
    repo.save(cash().deactivate());
    assertEquals(1, repo.findAll().size());
    assertFalse(repo.findById("acc_cash").orElseThrow().isActive());
  }

  @Test
  void codeMustBeUniqueAcrossDifferentAccounts() {
    repo.save(cash());
    Account clash = Account.open("other", "1000", "Other", AccountType.ASSET, "KES");
    assertThrows(DuplicateAccountException.class, () -> repo.save(clash));
  }
}
