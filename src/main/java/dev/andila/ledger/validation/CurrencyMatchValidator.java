package dev.andila.ledger.validation;

import dev.andila.ledger.exception.AccountNotFoundException;
import dev.andila.ledger.exception.CurrencyMismatchException;
import dev.andila.ledger.port.AccountRepository;
import dev.andila.ledger.transaction.Transaction;

/** Each posting must be in its account's currency. */
public final class CurrencyMatchValidator implements TransactionValidator {

  private final AccountRepository accounts;

  public CurrencyMatchValidator(AccountRepository accounts) {
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
              if (!account.currency().equals(p.amount().currency())) {
                throw new CurrencyMismatchException(
                    "Account "
                        + account.id()
                        + " is in "
                        + account.currency().getCurrencyCode()
                        + " but posting is in "
                        + p.amount().currency().getCurrencyCode());
              }
            });
  }
}
