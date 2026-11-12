package dev.andila.ledger.validation;

import dev.andila.ledger.transaction.Transaction;
import java.util.ArrayList;
import java.util.List;

/**
 * Chain of responsibility: runs validators in order and stops at the first rejection. Immutable:
 * then() returns a new chain.
 */
public final class ValidatorChain implements TransactionValidator {

  private final List<TransactionValidator> validators;

  private ValidatorChain(List<TransactionValidator> validators) {
    this.validators = List.copyOf(validators);
  }

  public static ValidatorChain of(TransactionValidator... validators) {
    return new ValidatorChain(List.of(validators));
  }

  public ValidatorChain then(TransactionValidator next) {
    List<TransactionValidator> copy = new ArrayList<>(validators);
    copy.add(next);
    return new ValidatorChain(copy);
  }

  @Override
  public void validate(Transaction transaction) {
    for (TransactionValidator v : validators) {
      v.validate(transaction);
    }
  }
}
