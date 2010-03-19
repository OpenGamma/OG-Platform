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
 * The implementation is not thread safe.   
 *
 * @author pietari
 */
public class PerformanceCounter {
  
  private final int _secondsOfHistoryToKeep;
  private final int[] _hits;
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
    _hits = new int[secondsOfHistoryToKeep];
    
    reset(zeroTimestamp);
  }
  
  public void reset() {
    long timestamp = System.currentTimeMillis();
    reset(timestamp);
  }
  
  void reset(long timestamp) {
    Arrays.fill(_hits, 0);
    _zeroTimestamp = timestamp;
    _lastHitTimestamp = _zeroTimestamp;
  }
  
  public int getSecondsOfHistoryToKeep() {
    return _secondsOfHistoryToKeep;
  }
  
  private long getSecondsSinceInception(long timestamp) {
    if (timestamp < _zeroTimestamp) { // could happen if the system clock is played with 
      reset(timestamp);      
    }
    return (timestamp - _zeroTimestamp) / 1000;   
  }
  
  private int getIndex(long timestamp) {
    return (int) (getSecondsSinceInception(timestamp) % _secondsOfHistoryToKeep);   
  }
  
  void hit(long timestamp) {
    int lastIndex = getIndex(_lastHitTimestamp);
    int index = getIndex(timestamp);
    
    long secondsSinceLastHit = getSecondsSinceInception(timestamp) - getSecondsSinceInception(_lastHitTimestamp);
    
    if (secondsSinceLastHit >= _secondsOfHistoryToKeep) {
      Arrays.fill(_hits, 0);
    } else {
      if (index > lastIndex) {
        Arrays.fill(_hits, lastIndex + 1, index + 1, 0);
      } else if (index < lastIndex) {
        if (lastIndex < _hits.length) {
          Arrays.fill(_hits, lastIndex + 1, _hits.length, 0);
        }
        Arrays.fill(_hits, 0, index + 1, 0);
      }
    }
    
    _hits[index]++;
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
  
  double getHitsPerSecond(int secsOfHistory, long timestamp) {
    if (secsOfHistory <= 0) {
      throw new IllegalArgumentException("Please give positive secs of history: " + secsOfHistory);
    }
    if (secsOfHistory > _secondsOfHistoryToKeep) {
      throw new IllegalArgumentException("Max secs of history is " + _secondsOfHistoryToKeep + ", was given " + secsOfHistory);
    }
    
    if (timestamp < _lastHitTimestamp) {
      reset(timestamp); // could happen if the system clock is played with
    }
    
    int count = 0;
    for (int i = 0; i < secsOfHistory; i++) {
      long historicalTime = timestamp - 1000 * i;
      long historicalTimeRoundedDownToSeconds = historicalTime / 1000 * 1000;
      if (historicalTimeRoundedDownToSeconds <= _lastHitTimestamp 
          && historicalTimeRoundedDownToSeconds >= _zeroTimestamp) {
        int index = getIndex(historicalTimeRoundedDownToSeconds);
        count += _hits[index];
      } 
      // else { count += 0; }
    }
    
    double hitsPerSecond = ((double) count) / secsOfHistory;
    return hitsPerSecond;
  }

}
