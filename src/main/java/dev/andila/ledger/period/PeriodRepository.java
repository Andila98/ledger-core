package dev.andila.ledger.period;

import java.time.Instant;
import java.util.Optional;

/**
 * Port: remembers how far the books are closed. Everything with a timestamp at or before
 * closedThrough() is locked. Contract: closeThrough() may only move forward; moving back throws
 * InvalidPeriodException.
 */
public interface PeriodRepository {

  Optional<Instant> closedThrough();

  void closeThrough(Instant instant);
}
