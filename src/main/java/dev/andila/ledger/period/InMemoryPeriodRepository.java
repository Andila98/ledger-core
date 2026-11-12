package dev.andila.ledger.period;

import dev.andila.ledger.exception.InvalidPeriodException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class InMemoryPeriodRepository implements PeriodRepository {

  private Instant closedThrough;

  @Override
  public synchronized Optional<Instant> closedThrough() {
    return Optional.ofNullable(closedThrough);
  }

  @Override
  public synchronized void closeThrough(Instant instant) {
    Objects.requireNonNull(instant, "instant");
    if (closedThrough != null && instant.isBefore(closedThrough)) {
      throw new InvalidPeriodException(
          "Cannot reopen: books are already closed through " + closedThrough);
    }
    closedThrough = instant;
  }
}
