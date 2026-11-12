package dev.andila.ledger.validation;

import dev.andila.ledger.exception.AccountNotFoundException;
import dev.andila.ledger.exception.InactiveAccountException;
import dev.andila.ledger.port.AccountRepository;
import dev.andila.ledger.transaction.Transaction;

public final class AccountsActiveValidator implements TransactionValidator {

  private final AccountRepository accounts;

  public AccountsActiveValidator(AccountRepository accounts) {
    this.accounts = java.util.Objects.requireNonNull(accounts, "accounts");
  }

  @Override
  public void validate(Transaction transaction) {
    transaction
        .postings()
        .forEach(
            p -> {
              var account =
                  accounts
                      .findById(p.accountId())
                      .orElseThrow(
                          () -> new AccountNotFoundException("Unknown account: " + p.accountId()));
              if (!account.isActive()) {
                throw new InactiveAccountException("Account is inactive: " + account.id());
              }
            });
  }
}
