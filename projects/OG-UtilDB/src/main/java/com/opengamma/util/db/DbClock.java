/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.temporal.TemporalUnit;

/**
 * A database-backed clock.
 * <p>
 * This only queries the database once per second, using simple interpolation from nanoTime() between calls.
 */
class DbClock extends Clock {

  private static final Logger s_logger = LoggerFactory.getLogger(DbClock.class);

  /**
   * The connector.
   */
  private final DbConnector _connector;
  /**
   * The timestamp precision.
   */
  private final TemporalUnit _precision;
  /**
   * The zone.
   */
  private final ZoneId _zone;
  /**
   * The lock.
   */
  private final ReadWriteLock _lock = new ReentrantReadWriteLock(true);
  /**
   * The base database instant.
   */
  private volatile Instant _nowInstant;
  /**
   * The last "now" instant returned. Any "now"s subsequently returned must not be before this.
   */
  private final AtomicReference<Instant> _previousNow = new AtomicReference<Instant>();
  /**
   * The {@link System#nanoTime} of the now instant.
   */
  private volatile long _nowNanoTime;

  /**
   * Creates the clock.
   * 
   * @param connector the connector, not null
   */
  DbClock(DbConnector connector) {
    this(connector, ZoneOffset.UTC);
  }

  /**
   * Creates the clock.
   * 
   * @param connector the connector, not null
   */
  DbClock(DbConnector connector, ZoneId zone) {
    _connector = Objects.requireNonNull(connector, "connector");
    _precision = _connector.getDialect().getTimestampPrecision();
    _zone = zone;
    long now = System.nanoTime();
    long base = now - 2_000_000_000L;
    if (base > now) { // overflow
      base = Long.MIN_VALUE;
    }
    _nowNanoTime = base;
  }

  //-------------------------------------------------------------------------
  private Instant instantImpl() {
    long nowNanos = System.nanoTime();
    _lock.readLock().lock();
    if (nowNanos - (_nowNanoTime + 1_000_000_000L) > 0 || _nowInstant == null) {
      _lock.readLock().unlock(); // safely upgrade to write lock
      _lock.writeLock().lock();
      try {
        // recheck, as per double checked locking
        if (nowNanos - (_nowNanoTime + 1_000_000_000L) > 0 || _nowInstant == null) {
          _nowInstant = DbDateUtils.fromSqlTimestamp(_connector.nowDb()).truncatedTo(_precision);
          _nowNanoTime = System.nanoTime();
          return _nowInstant;
        } else {
          _lock.readLock().lock(); // safely downgrade to read lock
        }
      } finally {
        _lock.writeLock().unlock();
      }
    }
    // calculate interpolated time
    Instant result;
    try {
      nowNanos = System.nanoTime();
      long interpolate = Math.max(nowNanos - _nowNanoTime, 0);
      int precisionNano = _precision.getDuration().getNano();
      interpolate = (interpolate / precisionNano) * precisionNano;
      result = _nowInstant.plusNanos(interpolate);
    } finally {
      _lock.readLock().unlock();
    }
    return result;
  }

  @Override
  public Instant instant() {
    final Instant instant = instantImpl();
    Instant previous = _previousNow.get();
    if (previous == null) {
      if (_previousNow.compareAndSet(null, instant)) {
        // This is the first result
        return instant;
      } else {
        // Another thread did the first result; check against that
        previous = _previousNow.get();
        // [PLAT-3965] This might not be necessary. I think the problem is more to do with two successive calls from the same thread getting invalid times
      }
    }
    do {
      if (previous.isAfter(instant)) {
        // Can't have time going backwards; have it stand still instead
        s_logger.debug("Returning previous time instant {} instead of {}", previous, instant);
        return previous;
      }
      // Time has progressed; update the reference
      if (_previousNow.compareAndSet(previous, instant)) {
        return instant;
      }
      // Another thread has returned a time; check against that
      previous = _previousNow.get();
      // [PLAT-3965] This might not be necessary. I think the problem is more to do with two successive calls from the same thread getting invalid times
    } while (true);
  }

  //-------------------------------------------------------------------------
  @Override
  public ZoneId getZone() {
    return _zone;
  }

  @Override
  public Clock withZone(final ZoneId zone) {
    Objects.requireNonNull(zone, "zone");
    return new DbClock(_connector, zone);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "DbClock";
  }

}
