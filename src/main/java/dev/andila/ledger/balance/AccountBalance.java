package dev.andila.ledger.balance;

import dev.andila.ledger.account.Account;
import dev.andila.ledger.money.Money;

/** An account with its balance, expressed on the account's normal side (positive = normal). */
public record AccountBalance(Account account, Money balance) {}
