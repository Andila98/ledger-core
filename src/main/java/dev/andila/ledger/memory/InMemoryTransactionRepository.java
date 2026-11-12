package dev.andila.ledger.memory;

import dev.andila.ledger.exception.DuplicateTransactionException;
import dev.andila.ledger.port.TransactionRepository;
import dev.andila.ledger.transaction.Transaction;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Reference adapter for tests and demos. Append-only, keeps insertion order. */
public final class InMemoryTransactionRepository implements TransactionRepository {

  private final Map<String, Transaction> byId = new LinkedHashMap<>();

  @Override
  public synchronized void append(Transaction transaction) {
    Objects.requireNonNull(transaction, "transaction");
    if (byId.containsKey(transaction.id())) {
      throw new DuplicateTransactionException("Transaction already exists: " + transaction.id());
    }
    byId.put(transaction.id(), transaction);
  }

  @Override
  public synchronized Optional<Transaction> findById(String id) {
    return Optional.ofNullable(byId.get(id));
  }

  @Override
  public synchronized List<Transaction> findByAccountId(String accountId) {
    return byId.values().stream()
        .filter(t -> t.postings().stream().anyMatch(p -> p.accountId().equals(accountId)))
        .toList();
  }

  @Override
  public synchronized List<Transaction> findAll() {
    return new ArrayList<>(byId.values());
  }
}
