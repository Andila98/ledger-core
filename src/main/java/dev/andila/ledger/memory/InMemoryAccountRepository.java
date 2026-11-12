package dev.andila.ledger.memory;

import dev.andila.ledger.account.Account;
import dev.andila.ledger.exception.DuplicateAccountException;
import dev.andila.ledger.port.AccountRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Reference adapter for tests and demos. Thread-safe via synchronization. */
public final class InMemoryAccountRepository implements AccountRepository {

  private final Map<String, Account> byId = new LinkedHashMap<>();

  @Override
  public synchronized void save(Account account) {
    Objects.requireNonNull(account, "account");
    for (Account existing : byId.values()) {
      if (existing.code().equals(account.code()) && !existing.id().equals(account.id())) {
        throw new DuplicateAccountException(
            "Account code " + account.code() + " is already used by " + existing.id());
      }
    }
    byId.put(account.id(), account);
  }

  @Override
  public synchronized Optional<Account> findById(String id) {
    return Optional.ofNullable(byId.get(id));
  }

  @Override
  public synchronized Optional<Account> findByCode(String code) {
    return byId.values().stream().filter(a -> a.code().equals(code)).findFirst();
  }

  @Override
  public synchronized List<Account> findAll() {
    return new ArrayList<>(byId.values());
  }
}
