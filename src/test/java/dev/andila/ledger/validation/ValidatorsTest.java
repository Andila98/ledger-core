package dev.andila.ledger.validation;

import static org.junit.jupiter.api.Assertions.*;

import dev.andila.ledger.account.Account;
import dev.andila.ledger.account.AccountType;
import dev.andila.ledger.exception.*;
import dev.andila.ledger.memory.InMemoryAccountRepository;
import dev.andila.ledger.port.AccountRepository;
import dev.andila.ledger.transaction.Transaction;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValidatorsTest {

  private AccountRepository accounts;

  @BeforeEach
  void setUp() {
    accounts = new InMemoryAccountRepository();
    accounts.save(Account.open("cash", "1000", "Cash", AccountType.ASSET, "KES"));
    accounts.save(Account.open("rev", "4000", "Revenue", AccountType.REVENUE, "KES"));
  }

  private Transaction kesTx(String debit, String credit) {
    return Transaction.builder()
        .description("d")
        .debit(debit, "10", "KES")
        .credit(credit, "10", "KES")
        .build();
  }

  @Test
  void unknownAccountIsRejected() {
    assertThrows(
        AccountNotFoundException.class,
        () -> new AccountsExistValidator(accounts).validate(kesTx("cash", "ghost")));
    assertDoesNotThrow(() -> new AccountsExistValidator(accounts).validate(kesTx("cash", "rev")));
  }

  @Test
  void inactiveAccountIsRejected() {
    accounts.save(accounts.findById("rev").orElseThrow().deactivate());
    assertThrows(
        InactiveAccountException.class,
        () -> new AccountsActiveValidator(accounts).validate(kesTx("cash", "rev")));
  }

  @Test
  void postingCurrencyMustMatchAccountCurrency() {
    Transaction usd =
        Transaction.builder()
            .description("d")
            .debit("cash", "10", "USD")
            .credit("rev", "10", "USD")
            .build();
    assertThrows(
        CurrencyMismatchException.class, () -> new CurrencyMatchValidator(accounts).validate(usd));
  }

  @Test
  void chainRunsInOrderAndStopsAtFirstFailure() {
    List<String> calls = new ArrayList<>();
    ValidatorChain chain =
        ValidatorChain.of(t -> calls.add("first"))
            .then(
                t -> {
                  calls.add("second");
                  throw new InvalidTransactionException("stop");
                })
            .then(t -> calls.add("third"));
    assertThrows(InvalidTransactionException.class, () -> chain.validate(kesTx("cash", "rev")));
    assertEquals(List.of("first", "second"), calls);
  }

  @Test
  void customRuleCanBeAddedAsLambda() {
    TransactionValidator maxAmount =
        t -> {
          if (t.totalDebits("KES").amount().intValue() > 5) {
            throw new InvalidTransactionException("Over limit");
          }
        };
    assertThrows(InvalidTransactionException.class, () -> maxAmount.validate(kesTx("cash", "rev")));
  }
}
