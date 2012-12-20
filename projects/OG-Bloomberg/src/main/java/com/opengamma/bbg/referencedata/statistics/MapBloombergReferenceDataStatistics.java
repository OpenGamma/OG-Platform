/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A {@link BloombergReferenceDataStatistics} which stores statistics in a simple map.
 */
public class MapBloombergReferenceDataStatistics implements BloombergReferenceDataStatistics {

  //TODO could trade update speed for size
  private final Map<String, Long> _getsPerSecurity = new HashMap<String, Long>();
  private final Map<String, Long> _getsPerField = new HashMap<String, Long>();

  public synchronized void recordStatistics(Set<String> securities, Set<String> fields) {
    incrementInterned(_getsPerSecurity, securities, fields.size());
    incrementInterned(_getsPerField, fields, securities.size());
  }

  private static void incrementInterned(Map<String, Long> map, Set<String> keys, long delta) {
    for (String key : keys) {
      Long previous = map.get(key);
      if (previous == null) {
        map.put(key.intern(), delta);
      } else {
        map.put(key, previous.longValue() + delta);
      }
    }
  }

  public synchronized long getTotalGetsCount() {
    long count = 0;
    //Assume there are fewer fields than securities
    for (Entry<String, Long> entry : _getsPerField.entrySet()) {
      count += entry.getValue();
    }
    return count;
  }

  public synchronized Snapshot getSnapshot() {
    return new Snapshot(_getsPerSecurity, _getsPerField);
  }

  public synchronized long getDistinctSecurityCount() {
    return _getsPerSecurity.size();
  }

  public synchronized long getDistinctFieldCount() {
    return _getsPerField.size();
  }

}
