/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.util.Arrays;

/**
 * A counter that keeps track of how many times something has happened
 * in the recent past.
 * <p>
 * The implementation uses {@link System#currentTimeMillis()} so
 * the figures will only be approximate.
 * <p>
 * The implementation is thread-safe.   
 *
 * @author pietari
 */
public class PerformanceCounter {
  
  private final int _secondsOfHistoryToKeep;
  private long _hits;
  private final long[] _hitsHistory;
  private long _zeroTimestamp; 
  private long _lastHitTimestamp;
  
  public PerformanceCounter(int secondsOfHistoryToKeep) {
    this(secondsOfHistoryToKeep, System.currentTimeMillis());
  }
  
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
  
  public void reset() {
    long timestamp = System.currentTimeMillis();
    reset(timestamp);
  }
  
  synchronized void reset(long timestamp) {
    _hits = 0;
    Arrays.fill(_hitsHistory, 0);
    _zeroTimestamp = timestamp;
    _lastHitTimestamp = _zeroTimestamp;
  }
  
  public int getSecondsOfHistoryToKeep() {
    return _secondsOfHistoryToKeep;
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
  
  synchronized void hit(long timestamp) {
    if (timestamp < _lastHitTimestamp) { // could happen if the system clock is played with 
      reset(timestamp);      
    }

    long secondsSinceLastHit = getSecondsSinceInception(timestamp) - getSecondsSinceInception(_lastHitTimestamp);
    if (secondsSinceLastHit < 0) {
      throw new RuntimeException("Seconds since last hit should never be negative" + secondsSinceLastHit);
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
    
    _hits++;
    _lastHitTimestamp = timestamp;
  }
  
  public void hit() {
    long timestamp = System.currentTimeMillis();
    hit(timestamp);
  }
  
  public double getHitsPerSecond() {
    return getHitsPerSecond(_secondsOfHistoryToKeep);    
  }
  
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
