package dev.andila.ledger.balance;

import dev.andila.ledger.account.Account;
import dev.andila.ledger.exception.AccountNotFoundException;
import dev.andila.ledger.money.Money;
import dev.andila.ledger.port.AccountRepository;
import dev.andila.ledger.port.TransactionRepository;
import dev.andila.ledger.posting.Posting;
import dev.andila.ledger.transaction.Transaction;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Balances are never stored: they are derived by replaying postings. A posting on the account's
 * normal side increases the balance; the opposite side decreases it.
 */
public final class BalanceCalculator {

  private final AccountRepository accounts;
  private final TransactionRepository transactions;

  public BalanceCalculator(AccountRepository accounts, TransactionRepository transactions) {
    this.accounts = Objects.requireNonNull(accounts, "accounts");
    this.transactions = Objects.requireNonNull(transactions, "transactions");
  }

  public Money balanceOf(String accountId) {
    return balanceAsOf(accountId, Instant.MAX);
  }

  /** Balance including every transaction with timestamp at or before {@code asOf}. */
  public Money balanceAsOf(String accountId, Instant asOf) {
    Account account =
        accounts
            .findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Unknown account: " + accountId));
    Money balance = Money.zero(account.currency().getCurrencyCode());
    for (Transaction t : transactions.findByAccountId(accountId)) {
      if (t.timestamp().isAfter(asOf)) continue;
      for (Posting p : t.postings()) {
        if (!p.accountId().equals(accountId)) continue;
        balance =
            account.type().isIncreasedBy(p.direction())
                ? balance.add(p.amount())
                : balance.subtract(p.amount());
      }
    }
    return balance;
  }

  public List<AccountBalance> trialBalance(Instant asOf) {
    return accounts.findAll().stream()
        .map(a -> new AccountBalance(a, balanceAsOf(a.id(), asOf)))
        .toList();
  }
}
