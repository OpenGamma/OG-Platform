/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.threeten.bp.LocalDate;

import com.opengamma.util.tuple.LongObjectPair;

/**
 * A JMX Mbean for {@link BloombergReferenceDataStatistics} 
 */
@ManagedResource(description = "Statistics collected over several days")
public class DailyBloombergReferenceDataStatisticsMBean {
  private final DailyBloombergReferenceDataStatistics _stats;

  /**
   * @param stats the  statistics to expose
   */
  public DailyBloombergReferenceDataStatisticsMBean(DailyBloombergReferenceDataStatistics stats) {
    super();
    _stats = stats;
  }
  
  @ManagedOperation(description = "The total number of snapshots done on each day.")
  public List<String> getDailyCounts() {
    TreeMap<LocalDate, Snapshot> snapshotsMap = _stats.getSnapshotsMap();
    List<String> ret = new ArrayList<String>(snapshotsMap.size());
    for (Entry<LocalDate, Snapshot> e : snapshotsMap.entrySet()) {
      ret.add("[" + e.getKey() + "," + e.getValue().getTotalLookups() + "]");
    }
    return ret;
  }
  
  //TODAY
  @ManagedAttribute(description = "The total number of gets done today.")
  public long getTodaysGetCount() {
    return getTodaysStats().getTotalGetsCount();
  }

  @ManagedAttribute(description = "The total number of securities queried today.")
  public long getTodaysSecurityCount() {
    return getTodaysStats().getDistinctSecurityCount();
  }
  
  @ManagedAttribute(description = "The total number of fields queried today.")
  public long getTodaysFieldCount() {
    return getTodaysStats().getDistinctFieldCount();
  }
  
  @ManagedOperation(description = "The total number of gets done on each security today.")
  public List<String> getTodaysGetCountsBySecurity() {
    List<LongObjectPair<String>> lookupsBySecurity = getTodaysSnapshot().getLookupsBySecurity();
    return wrap(lookupsBySecurity);
  }


  @ManagedOperation(description = "The total number of gets done on each field today.")
  public List<String> getTodaysGetCountsByField() {
    List<LongObjectPair<String>> lookupsByField = getTodaysSnapshot().getLookupsByField();
    return wrap(lookupsByField);
  }
  private Snapshot getTodaysSnapshot() {
    return _stats.getTodaysSnapshot();
  }
  
  private MapBloombergReferenceDataStatistics getTodaysStats() {
    return _stats.getTodaysStats();
  }
  
  //ALLTIME
  @ManagedAttribute(description = "The total number of gets done for all time.")
  public long getAllTimesGetCount() {
    return getAllTimesStats().getTotalGetsCount();
  }

  @ManagedAttribute(description = "The total number of securities queried for all time.")
  public long getAllTimesSecurityCount() {
    return getAllTimesStats().getDistinctSecurityCount();
  }
  
  @ManagedAttribute(description = "The total number of fields queried for all time.")
  public long getAllTimesFieldCount() {
    return getAllTimesStats().getDistinctFieldCount();
  }
  
  @ManagedOperation(description = "The total number of gets done on each security for all time.")
  public List<String> getAllTimesGetCountsBySecurity() {
    List<LongObjectPair<String>> lookupsBySecurity = getAllTimesSnapshot().getLookupsBySecurity();
    return wrap(lookupsBySecurity);
  }
  @ManagedOperation(description = "The total number of gets done on each field for all time.")
  public List<String> getAllTimesGetCountsByField() {
    List<LongObjectPair<String>> lookupsByField = getAllTimesSnapshot().getLookupsByField();
    return wrap(lookupsByField);
  }
  private Snapshot getAllTimesSnapshot() {
    return _stats.getAllTimeSnapshot();
  }
  
  private MapBloombergReferenceDataStatistics getAllTimesStats() {
    return _stats.getAllTimeStats();
  }
  
  private List<String> wrap(List<LongObjectPair<String>> lookupsBySecurity) {
    List<String> ret = new ArrayList<String>(lookupsBySecurity.size());
    for (LongObjectPair<String> p : lookupsBySecurity) {
      ret.add(p.toString());
    }
    return ret;
  }
}
