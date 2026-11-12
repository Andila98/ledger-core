package dev.andila.ledger.memory;

import dev.andila.ledger.port.TransactionRepository;
import dev.andila.ledger.port.TransactionRepositoryContractTest;

class InMemoryTransactionRepositoryTest extends TransactionRepositoryContractTest {
  @Override
  protected TransactionRepository createRepository() {
    return new InMemoryTransactionRepository();
  }
}
