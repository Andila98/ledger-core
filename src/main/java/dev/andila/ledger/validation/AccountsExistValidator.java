package dev.andila.ledger.validation;

import dev.andila.ledger.exception.AccountNotFoundException;
import dev.andila.ledger.port.AccountRepository;
import dev.andila.ledger.transaction.Transaction;

public final class AccountsExistValidator implements TransactionValidator {

  private final AccountRepository accounts;

  public AccountsExistValidator(AccountRepository accounts) {
    this.accounts = java.util.Objects.requireNonNull(accounts, "accounts");
  }

  @Override
  public void validate(Transaction transaction) {
    transaction
        .postings()
        .forEach(
            p -> {
              if (accounts.findById(p.accountId()).isEmpty()) {
                throw new AccountNotFoundException("Unknown account: " + p.accountId());
              }
            });
  }
}
