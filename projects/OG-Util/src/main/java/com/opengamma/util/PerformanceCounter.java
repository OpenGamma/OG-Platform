/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.util.Arrays;

import com.opengamma.OpenGammaRuntimeException;

/**
 * A counter that keeps track of how many times something has happened
 * in the recent past.
 * <p>
 * The implementation uses {@link System#currentTimeMillis()} so
 * the figures will only be approximate.
 * <p>
 * The implementation is thread-safe.   
 */
public class PerformanceCounter {

  /**
   * The length of history to keep.
   */
  private final int _secondsOfHistoryToKeep;
  /**
   * The number of hits.
   */
  private long _hits;
  /**
   * The history of hits.
   */
  private final long[] _hitsHistory;
  /**
   * The timestamp representing zero.
   */
  private long _zeroTimestamp; 
  /**
   * The timestamp that the last hit occurred.
   */
  private long _lastHitTimestamp;

  /**
   * Creates the counter with a number of seconds to keep history for.
   * @param secondsOfHistoryToKeep  the seconds to keep history for
   */
  public PerformanceCounter(int secondsOfHistoryToKeep) {
    this(secondsOfHistoryToKeep, System.currentTimeMillis());
  }

  /**
   * Creates the counter with a number of seconds to keep history for and a timestamp.
   * @param secondsOfHistoryToKeep  the seconds to keep history for
   * @param zeroTimestamp  the zero epoch millisecond timestamp
   */
  PerformanceCounter(int secondsOfHistoryToKeep, long zeroTimestamp) {
    if (secondsOfHistoryToKeep <= 0) {
      throw new IllegalArgumentException("secondsOfHistoryToKeep must be positive");
    }
    if (zeroTimestamp < 0) {
      throw new IllegalArgumentException("zeroTimestamp must be non-negative");
    }
    _secondsOfHistoryToKeep = secondsOfHistoryToKeep;
    _hitsHistory = new long[_secondsOfHistoryToKeep];
    reset(zeroTimestamp);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the number of seconds to keep history for,
   * @return the history storage length in seconds
   */
  public int getSecondsOfHistoryToKeep() {
    return _secondsOfHistoryToKeep;
  }

  //-------------------------------------------------------------------------
  /**
   * Resets the counter.
   */
  public final void reset() {
    // final, as this is called in the constructor
    long timestamp = System.currentTimeMillis();
    reset(timestamp);
  }

  /**
   * Resets the counter.
   * @param zeroTimestamp  the zero epoch millisecond timestamp
   */
  synchronized void reset(long zeroTimestamp) {
    _hits = 0;
    Arrays.fill(_hitsHistory, 0);
    _zeroTimestamp = zeroTimestamp;
    _lastHitTimestamp = _zeroTimestamp;
  }

  private long getSecondsSinceInception(long timestamp) {
    return (timestamp - _zeroTimestamp) / 1000;   
  }
  
  private long getEarliestAvailableTime() {
    long lastHitTimestampRoundedDownToSeconds = _lastHitTimestamp / 1000 * 1000;
    return lastHitTimestampRoundedDownToSeconds - 1000 * _secondsOfHistoryToKeep; 
  }
  
  private int getIndex(long timestamp) {
    if (timestamp < getEarliestAvailableTime()) {
      throw new IllegalArgumentException("Earliest available is " + getEarliestAvailableTime() + ", tried to request " + timestamp);
    }
    return (int) (getSecondsSinceInception(timestamp) % _secondsOfHistoryToKeep);   
  }

  private synchronized void hitMultiple(long timestamp, long count) {
    if (timestamp < _lastHitTimestamp) { // could happen if the system clock is played with 
      reset(timestamp);      
    }
    long secondsSinceLastHit = getSecondsSinceInception(timestamp) - getSecondsSinceInception(_lastHitTimestamp);
    if (secondsSinceLastHit < 0) {
      throw new OpenGammaRuntimeException("Seconds since last hit should never be negative" + secondsSinceLastHit);
    }
    
    if (secondsSinceLastHit >= _secondsOfHistoryToKeep) {
      Arrays.fill(_hitsHistory, _hits);
    } else {
      int lastIndex = getIndex(_lastHitTimestamp);
      int index = getIndex(timestamp);

      if (index > lastIndex) {
        Arrays.fill(_hitsHistory, lastIndex, index, _hits);
      } else if (index < lastIndex) {
        Arrays.fill(_hitsHistory, lastIndex, _hitsHistory.length, _hits);
        Arrays.fill(_hitsHistory, 0, index, _hits);
      }
    }
    _hits += count;
    _lastHitTimestamp = timestamp;
  }
  
  void hit(long timestamp) {
    hitMultiple(timestamp, 1);
  }

  /**
   * Stores a performance counter hit.
   */
  public void hit() {
    hitMultiple(1);
  }
  
  /**
   * Stores multiple performance counter hits.
   * @param count The number of hits to register
   */
  public synchronized void hitMultiple(long count) {
    long timestamp = System.currentTimeMillis();
    hitMultiple(timestamp, count);
  }

  /**
   * Gets the count of hits per second.
   * @return the hit-rate
   */
  public double getHitsPerSecond() {
    return getHitsPerSecond(_secondsOfHistoryToKeep);    
  }

  /**
   * Gets the count of hits per second.
   * @param secsOfHistory  the history to calculate over
   * @return the hit-rate
   */
  public double getHitsPerSecond(int secsOfHistory) {
    long timestamp = System.currentTimeMillis();
    return getHitsPerSecond(secsOfHistory, timestamp);
  }

  double getHitsPerSecondAsOfLastHit(int secsOfHistory) {
    long timestamp = _lastHitTimestamp;
    return getHitsPerSecond(secsOfHistory, timestamp);
  }

  synchronized double getHitsPerSecond(int secsOfHistory, long timestamp) {
    if (secsOfHistory <= 0) {
      throw new IllegalArgumentException("Please give positive secs of history: " + secsOfHistory);
    }
    if (secsOfHistory > _secondsOfHistoryToKeep) {
      throw new IllegalArgumentException("Max secs of history is " + _secondsOfHistoryToKeep + ", was given " + secsOfHistory);
    }
    
    long historicalTime = timestamp - 1000 * secsOfHistory;
    if (historicalTime < getEarliestAvailableTime()) { // could happen if the system clock is played with
      reset(timestamp); 
    }
    
    long currentHitCount;
    if (timestamp < _zeroTimestamp) {
      currentHitCount = 0;      
    } else if (timestamp >= _lastHitTimestamp) {
      currentHitCount = _hits;      
    } else {
      int currentIndex = getIndex(timestamp);
      currentHitCount = _hitsHistory[currentIndex];
    }
    
    long historicalHitCount;
    if (historicalTime < _zeroTimestamp) {
      historicalHitCount = 0;
    } else if (historicalTime >= _lastHitTimestamp) {
      historicalHitCount = _hits;
    } else {
      int historicalIndex = getIndex(historicalTime);
      historicalHitCount = _hitsHistory[historicalIndex];
    }
    
    long hits = currentHitCount - historicalHitCount;
    double hitsPerSecond = ((double) hits) / secsOfHistory;
    return hitsPerSecond;
  }

}
