package dev.andila.ledger.port;

import dev.andila.ledger.account.Account;
import java.util.List;
import java.util.Optional;

/**
 * Port: how the ledger reads and stores accounts. The host application implements this with its own
 * database; the library never knows which.
 *
 * <p>Contract: - save() inserts a new account or replaces the one with the same id (accounts are
 * immutable objects, so "update" means saving a new version). - save() throws
 * DuplicateAccountException if a DIFFERENT account already uses the same code.
 */
public interface AccountRepository {

  void save(Account account);

  Optional<Account> findById(String id);

  Optional<Account> findByCode(String code);

  List<Account> findAll();
}
