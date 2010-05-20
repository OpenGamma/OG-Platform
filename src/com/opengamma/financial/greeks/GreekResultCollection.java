/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

public class GreekResultCollection implements Iterable<Pair<Greek, GreekResult<?>>> {
  // REVIEW kirk 2010-05-20 -- Ideas for speeding up:
  // - Just store SingleGreekResult instances as a double and convert on result
  // - Change .put() to allow just providing a double, and avoiding constructing a SingleGreekResult
  //   in the calling code.
  // - For common cases, store SingleGreekResult in a double[], where the indices are ordinals
  //   for the greek in the enumeration. Super-fast lookup and small objects, but wasted
  //   space for the common case of one greek in a result collection.

  // TODO kirk 2010-05-20 -- Needs a set of fudge converters.

  // REVIEW kirk 2010-05-20 -- Is this the best backing map?
  private final Map<Greek, GreekResult<?>> _backingMap = new TreeMap<Greek, GreekResult<?>>();

  public GreekResult<?> get(final Greek greek) {
    if (greek == null) {
      return null;
    }
    return _backingMap.get(greek);
  }

  public void put(final Greek greek, final GreekResult<?> result) {
    ArgumentChecker.notNull(greek, "Greek");
    // REVIEW kirk 2010-05-20 -- This totally seems wrong that we're not checking it,
    // but there is test code that relies on us allowing null results.
    //ArgumentChecker.notNull(result, "Greek result");
    _backingMap.put(greek, result);
  }

  public boolean isEmpty() {
    return _backingMap.isEmpty();
  }

  public boolean contains(final Greek greek) {
    return _backingMap.containsKey(greek);
  }

  @Deprecated
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
    return Collections.unmodifiableCollection(_backingMap.values());
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("GreekResultCollection[");
    final List<String> elements = new LinkedList<String>();
    for (final Map.Entry<Greek, GreekResult<?>> entry : _backingMap.entrySet()) {
      final StringBuilder elementSb = new StringBuilder();
      sb.append(entry.getKey()).append("=").append(entry.getValue());
      elements.add(elementSb.toString());
    }
    sb.append(StringUtils.join(elements, ", "));
    sb.append("]");
    return sb.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    for (final Map.Entry<Greek, GreekResult<?>> entry : _backingMap.entrySet()) {
      result = prime * result + entry.getKey().hashCode();
      result = prime * result + entry.getValue().hashCode();
    }
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof GreekResultCollection)) {
      return false;
    }
    final GreekResultCollection other = (GreekResultCollection) obj;
    // This is really bad and we'll want to change it when we're less reliant on just the backing map.
    return ObjectUtils.equals(_backingMap, other._backingMap);
  }

  @Override
  public Iterator<Pair<Greek, GreekResult<?>>> iterator() {
    // TODO kirk 2010-05-20 -- This can be dramatically improved if we change the backing map.
    return new BackingMapGreekIterator(_backingMap.entrySet().iterator());
  }

  protected static class BackingMapGreekIterator implements Iterator<Pair<Greek, GreekResult<?>>> {
    private final Iterator<Map.Entry<Greek, GreekResult<?>>> _backingIterator;

    public BackingMapGreekIterator(final Iterator<Map.Entry<Greek, GreekResult<?>>> backingIterator) {
      _backingIterator = backingIterator;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
      return _backingIterator.hasNext();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    @Override
    public Pair<Greek, GreekResult<?>> next() {
      final Map.Entry<Greek, GreekResult<?>> nextEntry = _backingIterator.next();
      return Pair.<Greek, GreekResult<?>> of(nextEntry.getKey(), nextEntry.getValue());
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    @Override
    public void remove() {
      throw new UnsupportedOperationException("Cannot remove from this iterator.");
    }

  }

}
