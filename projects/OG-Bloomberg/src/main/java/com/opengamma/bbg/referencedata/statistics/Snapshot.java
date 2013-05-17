/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.referencedata.statistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.common.base.Supplier;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.opengamma.util.tuple.LongObjectPair;

/**
 * A snapshot of the statistics.
 */
public class Snapshot {
  private final long _totalLookups;
  private final List<LongObjectPair<String>> _lookupsBySecurity;
  private final List<LongObjectPair<String>> _lookupsByField;

  public Snapshot(Map<String, Long> lookupsBySecurity,
      Map<String, Long> lookupsByField) {
    super();
    _lookupsByField = reverseOrder(lookupsByField);
    _lookupsBySecurity = reverseOrder(lookupsBySecurity);
    
    //Assume that fields is smaller than security
    long lookups = 0;
    for (LongObjectPair<String> point : _lookupsByField) {
      lookups += point.getFirstLong();
    }
    _totalLookups = lookups;
  }

  private static final Supplier<ArrayList<String>> ARRAY_LIST_SUPPLIER = new Supplier<ArrayList<String>>() {
    @Override
    public ArrayList<String> get() {
      return new ArrayList<String>();
    }
  };
  private static final Comparator<Long> DESCENDING_COMPARATOR = new Comparator<Long>() {
    
    @Override
    public int compare(Long o1, Long o2) {
      return o2.compareTo(o1);
    }
  };
  
  private List<LongObjectPair<String>> reverseOrder(Map<String, Long> forward) {
    ListMultimap<Long, String> index = index(forward);
    int size = forward.size();
    return flatten(index, size);
  }

  private List<LongObjectPair<String>> flatten(ListMultimap<Long, String> index, int size) {
    List<LongObjectPair<String>> reverse = new ArrayList<LongObjectPair<String>>(size);
    for (Entry<Long, String> entry : index.entries()) {
      reverse.add(LongObjectPair.of((long) entry.getKey(), entry.getValue()));
    }
    return reverse;
  }

  private ListMultimap<Long, String> index(Map<String, Long> forward) {
    ListMultimap<Long, String> index = Multimaps.newListMultimap(new TreeMap<Long, Collection<String>>(DESCENDING_COMPARATOR), ARRAY_LIST_SUPPLIER);
    for (Entry<String, Long> e : forward.entrySet()) {
      index.put(e.getValue(), e.getKey());
    }
    return index;
  }

  public long getTotalLookups() {
    return _totalLookups;
  }

  public long getDistinctSecurities() {
    return _lookupsBySecurity.size();
  }

  /**
   * Gets the distinctFields field.
   * @return the distinctFields
   */
  public long getDistinctFields() {
    return _lookupsByField.size();
  }

  public List<LongObjectPair<String>> getLookupsBySecurity() {
    return _lookupsBySecurity;
  }

  public List<LongObjectPair<String>> getLookupsByField() {
    return _lookupsByField;
  }
}
