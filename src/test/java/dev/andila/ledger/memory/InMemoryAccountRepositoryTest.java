package dev.andila.ledger.memory;

import dev.andila.ledger.port.AccountRepository;
import dev.andila.ledger.port.AccountRepositoryContractTest;

class InMemoryAccountRepositoryTest extends AccountRepositoryContractTest {
  @Override
  protected AccountRepository createRepository() {
    return new InMemoryAccountRepository();
  }
}
