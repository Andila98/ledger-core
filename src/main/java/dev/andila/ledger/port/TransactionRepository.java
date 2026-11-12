package dev.andila.ledger.port;

import dev.andila.ledger.transaction.Transaction;
import java.util.List;
import java.util.Optional;

/**
 * Port: append-only storage of transactions. There is deliberately no update and no delete: a
 * ledger is corrected by posting new transactions (Step 8).
 *
 * <p>Contract: - append() throws DuplicateTransactionException if the id already exists. - Query
 * methods return transactions in the order they were appended.
 */
public interface TransactionRepository {

  void append(Transaction transaction);

  Optional<Transaction> findById(String id);

  List<Transaction> findByAccountId(String accountId);

  List<Transaction> findAll();
}
