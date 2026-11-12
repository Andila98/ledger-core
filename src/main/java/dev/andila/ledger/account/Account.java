package dev.andila.ledger.account;

import dev.andila.ledger.exception.InvalidAccountException;
import java.util.Currency;
import java.util.Objects;
import java.util.Optional;

/**
 * A node in the chart of accounts. Immutable: "changing" an account (e.g. deactivating it) returns
 * a new instance. Identity is the id; two accounts with the same id are the same account.
 */
public final class Account {

  private final String id;
  private final String code;
  private final String name;
  private final AccountType type;
  private final Currency currency;
  private final String parentId; // nullable
  private final boolean active;

  private Account(
      String id,
      String code,
      String name,
      AccountType type,
      Currency currency,
      String parentId,
      boolean active) {
    this.id = id;
    this.code = code;
    this.name = name;
    this.type = type;
    this.currency = currency;
    this.parentId = parentId;
    this.active = active;
  }

  public static Account open(
      String id, String code, String name, AccountType type, String currencyCode) {
    return create(id, code, name, type, currencyCode, null);
  }

  public static Account openChild(
      String id, String code, String name, AccountType type, String currencyCode, Account parent) {
    Objects.requireNonNull(parent, "parent");
    if (parent.type != type) {
      throw new InvalidAccountException(
          "Child type " + type + " must match parent type " + parent.type);
    }
    if (!parent.currency.getCurrencyCode().equals(currencyCode)) {
      throw new InvalidAccountException(
          "Child currency must match parent currency " + parent.currency.getCurrencyCode());
    }
    return create(id, code, name, type, currencyCode, parent.id);
  }

  private static Account create(
      String id, String code, String name, AccountType type, String currencyCode, String parentId) {
    requireText(id, "id");
    requireText(code, "code");
    requireText(name, "name");
    if (type == null) throw new InvalidAccountException("type must not be null");
    if (currencyCode == null) throw new InvalidAccountException("currency must not be null");
    if (id.equals(parentId))
      throw new InvalidAccountException("An account cannot be its own parent");
    Currency currency;
    try {
      currency = Currency.getInstance(currencyCode);
    } catch (IllegalArgumentException e) {
      throw new InvalidAccountException("Unknown ISO 4217 currency: " + currencyCode);
    }
    return new Account(id, code, name, type, currency, parentId, true);
  }

  private static void requireText(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new InvalidAccountException(field + " must not be blank");
    }
  }

  public Account deactivate() {
    return new Account(id, code, name, type, currency, parentId, false);
  }

  public Account activate() {
    return new Account(id, code, name, type, currency, parentId, true);
  }

  public String id() {
    return id;
  }

  public String code() {
    return code;
  }

  public String name() {
    return name;
  }

  public AccountType type() {
    return type;
  }

  public Currency currency() {
    return currency;
  }

  public Optional<String> parentId() {
    return Optional.ofNullable(parentId);
  }

  public boolean isActive() {
    return active;
  }

  public Direction normalBalance() {
    return type.normalBalance();
  }

  @Override
  public boolean equals(Object o) {
    return this == o || (o instanceof Account a && id.equals(a.id));
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public String toString() {
    return code
        + " "
        + name
        + " ["
        + type
        + ", "
        + currency.getCurrencyCode()
        + (active ? "" : ", inactive")
        + "]";
  }
}
