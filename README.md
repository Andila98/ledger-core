# ledger-core

A small, framework-agnostic **double-entry ledger library for Java**. It enforces the accounting rules
(balanced transactions, immutable history, closed periods) and leaves storage to your application.

- Java 21, **zero runtime dependencies** (JUnit 5 for tests only)
- No ORM, no schema, no database driver: you implement a few small interfaces ("ports")
- Immutable domain objects, exact decimal arithmetic, corrections as new entries, never edits

> **Status:** version `0.1.0-SNAPSHOT`, not yet published to a public repository.

## Features

| Area | What you get |
| --- | --- |
| Money | Immutable `BigDecimal` amount plus currency. Scale follows the currency's minor units. Silent rounding is rejected. |
| Accounts | Five account types with normal balances (debit or credit), parent/child accounts, activate and deactivate. |
| Transactions | A `Transaction` cannot exist unless it has at least two postings, one debit and one credit, and debits equal credits in every currency. |
| Builder | Fluent `Transaction.builder()` API. |
| Validation | A pluggable chain of rules. Built in: accounts exist, accounts are active, posting currency matches the account, period is open. Add your own as a lambda. |
| Balances | Derived by replaying postings: current balance, balance as of a date, trial balance. |
| Reversals | `ledger.reverse(id, reason)` posts an offsetting transaction linked to the original. |
| Period closing | Lock everything up to a date. The lock can only move forward. |
| Persistence | Ports for accounts, transactions and periods, with in-memory reference adapters. |

## Quick start

```java
var accounts = new InMemoryAccountRepository();
var transactions = new InMemoryTransactionRepository();

accounts.save(Account.open("cash", "1000", "Cash", AccountType.ASSET, "KES"));
accounts.save(Account.open("sales", "4000", "Sales", AccountType.REVENUE, "KES"));

Ledger ledger = Ledger.withStandardRules(accounts, transactions);

ledger.post(
    Transaction.builder()
        .description("Invoice 4092 paid")
        .meta("invoiceId", "inv_9921")
        .debit("cash", "1500.00", "KES")
        .credit("sales", "1500.00", "KES")
        .build());

var balances = new BalanceCalculator(accounts, transactions);
balances.balanceOf("cash"); // 1500.00 KES
```

An unbalanced, empty or invalid transaction throws a subclass of `LedgerException` and is never stored.

### Reversing a transaction

```java
ledger.reverse("<transaction-id>", "customer refund");
```

The original stays in the history. The reversal flips every direction, carries `reversalOf` in its
metadata, and a transaction can only be reversed once.

### Closing a period

```java
var periods = new InMemoryPeriodRepository();
Ledger ledger = Ledger.withStandardRules(accounts, transactions, periods, Clock.systemUTC());

periods.closeThrough(Instant.parse("2026-12-31T23:59:59Z"));
// Anything dated on or before that instant is now rejected with PeriodClosedException.
// A reversal is dated "now", so corrections land in the open period.
```

### Adding your own rule

`TransactionValidator` is a functional interface, so a host-specific rule is one lambda:

```java
TransactionValidator maxAmount = tx -> {
  if (tx.totalDebits("KES").amount().intValue() > 1_000_000) {
    throw new InvalidTransactionException("Over limit");
  }
};

Ledger ledger =
    new Ledger(
        ValidatorChain.of(
            new AccountsExistValidator(accounts),
            new AccountsActiveValidator(accounts),
            new CurrencyMatchValidator(accounts),
            maxAmount),
        transactions);
```

## Bring your own storage

The library never touches a database. To persist, implement the ports with whatever your
application uses (JPA, JDBC, Mongo, files):

| Port | Contract |
| --- | --- |
| `AccountRepository` | `save` inserts or replaces by id and rejects a code already used by another account. |
| `TransactionRepository` | Append-only. A duplicate id is rejected. Queries return transactions in append order. |
| `PeriodRepository` | Remembers the instant the books are closed through. It may only move forward. |

The abstract tests `AccountRepositoryContractTest` and `TransactionRepositoryContractTest` describe
the behaviour every adapter must satisfy. Extend them in your own test suite once they are
published as a test artifact.

## Package layout

```
dev.andila.ledger
├── Ledger                entry point: validate, then store; reverse
├── money/                Money
├── account/              Account, AccountType, Direction
├── posting/              Posting
├── transaction/          Transaction, TransactionBuilder
├── validation/           TransactionValidator, ValidatorChain, built-in rules
├── balance/              BalanceCalculator, AccountBalance
├── period/               PeriodRepository, InMemoryPeriodRepository
├── port/                 AccountRepository, TransactionRepository
├── memory/               in-memory adapters
└── exception/            LedgerException and its subclasses
```

## Design principles

1. **Invalid states cannot exist.** Validation lives in constructors, so holding a `Transaction`
   means its invariants hold.
2. **History is immutable.** Objects are immutable, the transaction port is append-only, and
   corrections are new transactions.
3. **Exact money.** `BigDecimal` throughout. The library never rounds for you; rounding policy
   (tax, exchange rates) belongs to your application.
4. **Ports and adapters.** The core depends on interfaces only. You choose the storage.
5. **Ids are strings.** Use UUIDs, database keys or readable codes, whatever your system already has.

## Build and test

Requires JDK 21 and Maven.

```
mvn test
```

## Not included (yet)

- Roll-up of child account balances into their parent
- Multi-currency conversion (each currency must balance on its own)
- Database adapters and a Spring Boot starter
- Publishing to a public repository (JitPack or Maven Central)

## License

No license has been chosen yet. Until one is added, all rights are reserved.
