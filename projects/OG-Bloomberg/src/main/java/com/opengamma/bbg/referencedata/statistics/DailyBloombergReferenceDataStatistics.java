/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.statistics;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.threeten.bp.Clock;
import org.threeten.bp.LocalDate;

/**
 * A {@link BloombergReferenceDataStatistics} which stores statistics for several days into the past.
 */
public class DailyBloombergReferenceDataStatistics implements BloombergReferenceDataStatistics {

  /**
   * The number of days that the statistics should be kept.
   */
  private static final int DAYS_TO_KEEP = 60;

  private final MapBloombergReferenceDataStatistics _allTimeStatistics = new MapBloombergReferenceDataStatistics();
  //TODO keep old snapshots, not old statistics?
  private final ReadWriteLock  _mapLock = new ReentrantReadWriteLock();
  private final Lock _mapReadLock = _mapLock.readLock();
  private final Lock _mapWriteLock = _mapLock.writeLock();
  private final TreeMap<LocalDate, MapBloombergReferenceDataStatistics> _dailyTimeStatistics = new TreeMap<LocalDate, MapBloombergReferenceDataStatistics>();

  @Override
  public void recordStatistics(Set<String> securities, Set<String> fields) {
    _allTimeStatistics.recordStatistics(securities, fields);
    MapBloombergReferenceDataStatistics todaysStats = getTodaysStats();
    todaysStats.recordStatistics(securities, fields);
  }

  public Snapshot getAllTimeSnapshot() {
    return _allTimeStatistics.getSnapshot();
  }

  public MapBloombergReferenceDataStatistics getAllTimeStats() {
    return _allTimeStatistics;
  }

  public Snapshot getTodaysSnapshot() {
    MapBloombergReferenceDataStatistics stats = getTodaysStats();
    return stats.getSnapshot();
  }

  public TreeMap<LocalDate, Snapshot> getSnapshotsMap() {
    _mapReadLock.lock();
    try {
      TreeMap<LocalDate, Snapshot> ret = new TreeMap<LocalDate, Snapshot>();
      for (Entry<LocalDate, MapBloombergReferenceDataStatistics> e : _dailyTimeStatistics.entrySet()) {
        ret.put(e.getKey(), e.getValue().getSnapshot());
      }
      return ret;
    } finally {
      _mapReadLock.unlock();
    }
  }

  public MapBloombergReferenceDataStatistics getTodaysStats() {
    LocalDate today = getToday();
    MapBloombergReferenceDataStatistics todaysStats;
    
    _mapReadLock.lock();
    try {
      todaysStats = _dailyTimeStatistics.get(today);
    } finally {
      _mapReadLock.unlock();
    }
    if (todaysStats == null) {
      //Only take the write lock when the day ticks over
      _mapWriteLock.lock();
      try {
        todaysStats = _dailyTimeStatistics.get(today);
        _dailyTimeStatistics.put(today, new MapBloombergReferenceDataStatistics());
        trim(_dailyTimeStatistics);
        todaysStats = _dailyTimeStatistics.get(today);
      } finally {
        _mapWriteLock.unlock();
      }
    }
    return todaysStats;
  }

  private void trim(TreeMap<LocalDate, MapBloombergReferenceDataStatistics> dailyTimeStatistics) {
    int toRemove = dailyTimeStatistics.size() - DAYS_TO_KEEP;
    if (toRemove <= 0) {
      return;
    }
    Iterator<Entry<LocalDate, MapBloombergReferenceDataStatistics>> iterator = dailyTimeStatistics.entrySet().iterator();
    for (int i = 0; i < toRemove; i++) {
      iterator.next();
      iterator.remove();
    }
  }

  private LocalDate getToday() {
    //I think 0000 UTC is when bloomberg tips over
    Clock clock = Clock.systemUTC();
    return LocalDate.now(clock);
  }

}
