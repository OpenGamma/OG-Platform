/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class GreekResultCollection {
  private final Map<Greek, GreekResult<?>> _backingMap = new TreeMap<Greek, GreekResult<?>>();

  public GreekResult<?> get(final Greek greek) {
    return _backingMap.get(greek);
  }

  public void put(final Greek greek, final GreekResult<?> result) {
    _backingMap.put(greek, result);
  }

  public boolean isEmpty() {
    return _backingMap.isEmpty();
  }

  public boolean containsKey(final Greek greek) {
    return _backingMap.containsKey(greek);
  }

  public Set<Map.Entry<Greek, GreekResult<?>>> entrySet() {
    return _backingMap.entrySet();
  }

  public int size() {
    return _backingMap.size();
  }

  public Set<Greek> keySet() {
    return _backingMap.keySet();
  }

  public Collection<GreekResult<?>> values() {
    return _backingMap.values();
  }

  @Override
  public String toString() {
    // TODO kirk 2010-05-20 -- Fix this.
    return _backingMap.toString();
  }

}
