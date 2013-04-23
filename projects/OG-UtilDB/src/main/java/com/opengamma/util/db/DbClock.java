/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.temporal.TemporalUnit;

import com.opengamma.util.OpenGammaClock;

/**
 * A database-backed clock.
 * <p>
 * This only queries the database once per second, using simple
 * interpolation from nanoTime() between calls.
 */
class DbClock extends Clock {

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
   * The {@link System#nanoTime} of the now instant.
   */
  private volatile long _nowNanoTime;

  /**
   * Creates the clock.
   * 
   * @param connector  the connector, not null
   */
  DbClock(DbConnector connector) {
    this(connector, OpenGammaClock.getZone());
  }

  /**
   * Creates the clock.
   * 
   * @param connector  the connector, not null
   */
  DbClock(DbConnector connector, ZoneId zone) {
    _connector = Objects.requireNonNull(connector, "connector");
    _precision = _connector.getDialect().getTimestampPrecision();
    _zone = zone;
    long now = System.nanoTime();
    long base = now - 2_000_000_000L;
    if (base > now) {  // overflow
      base = Long.MIN_VALUE;
    }
    _nowNanoTime = base;
  }

  //-------------------------------------------------------------------------
  @Override
  public Instant instant() {
    long nowNanos = System.nanoTime();
    _lock.readLock().lock();
    if (nowNanos - (_nowNanoTime + 1_000_000_000L) > 0 || _nowInstant == null) {
      _lock.readLock().unlock();  // safely upgrade to write lock
      _lock.writeLock().lock();
      try {
        // recheck, as per double checked locking
        if (nowNanos - (_nowNanoTime + 1_000_000_000L) > 0 || _nowInstant == null) {
          _nowInstant = DbDateUtils.fromSqlTimestamp(_connector.nowDb()).truncatedTo(_precision);
          _nowNanoTime = System.nanoTime();
          return _nowInstant;
        } else {
          _lock.readLock().lock();  // safely downgrade to read lock
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
