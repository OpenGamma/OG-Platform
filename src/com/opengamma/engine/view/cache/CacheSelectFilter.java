/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.view.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * A filter to determine whether given values are to go into a private or shared cache. 
 */
public final class CacheSelectFilter {

  private static final String VALUES_FIELD_NAME = "cacheValues";
  private static final String PRIVATE_FIELD_NAME = "cacheValuesPrivate";

  private final Set<ValueSpecification> _valueSpecifications;
  private long[] _valueIdentifiers;
  private final boolean _isPrivate;

  private CacheSelectFilter(final Collection<ValueSpecification> valueSpecifications, final long[] valueIdentifiers, final boolean isPrivate) {
    if (valueSpecifications != null) {
      _valueSpecifications = new HashSet<ValueSpecification>(valueSpecifications);
    } else {
      _valueSpecifications = new HashSet<ValueSpecification>();
    }
    _valueIdentifiers = valueIdentifiers;
    _isPrivate = isPrivate;
  }

  public static CacheSelectFilter privateValues(final Collection<ValueSpecification> privateValues) {
    ArgumentChecker.notNull(privateValues, "privateValues");
    return new CacheSelectFilter(privateValues, null, true);
  }

  public static CacheSelectFilter sharedValues(final Collection<ValueSpecification> sharedValues) {
    ArgumentChecker.notNull(sharedValues, "sharedValues");
    return new CacheSelectFilter(sharedValues, null, false);
  }

  public static CacheSelectFilter allShared() {
    return new CacheSelectFilter(null, null, true);
  }

  public static CacheSelectFilter allPrivate() {
    return new CacheSelectFilter(null, null, false);
  }

  /**
   * Converts the full {@link ValueSpecification} objects to numeric identifiers for Fudge message encoding.
   * 
   * @param identifierMap the identifier map to use
   */
  public void convertSpecifications(final IdentifierMap identifierMap) {
    if (_valueIdentifiers == null) {
      final Collection<Long> identifiers = identifierMap.getIdentifiers(_valueSpecifications).values();
      _valueIdentifiers = new long[identifiers.size()];
      int i = 0;
      for (Long identifier : identifiers) {
        _valueIdentifiers[i++] = identifier;
      }
    }
  }

  /**
   * Converts numeric identifiers to full {@link ValueSpecification} objects.
   * 
   * @param identifierMap the identifier map to use
   */
  public void resolveSpecifications(final IdentifierMap identifierMap) {
    if (_valueSpecifications.isEmpty()) {
      final Collection<Long> identifiers = new ArrayList<Long>(_valueIdentifiers.length);
      for (long identifier : _valueIdentifiers) {
        identifiers.add(identifier);
      }
      _valueSpecifications.addAll(identifierMap.getValueSpecifications(identifiers).values());
    }
  }

  public boolean isPrivateValue(final ValueSpecification valueSpecification) {
    if (_isPrivate) {
      return _valueSpecifications.contains(valueSpecification);
    } else {
      return !_valueSpecifications.contains(valueSpecification);
    }
  }

  public void toFudgeMsg(final MutableFudgeFieldContainer msg) {
    msg.add(VALUES_FIELD_NAME, _valueIdentifiers);
    msg.add(PRIVATE_FIELD_NAME, _isPrivate);
  }

  public static CacheSelectFilter fromFudgeMsg(final FudgeFieldContainer msg) {
    final long[] valueIdentifiers = (long[]) msg.getByName(VALUES_FIELD_NAME).getValue();
    final boolean isPrivate = msg.getBoolean(PRIVATE_FIELD_NAME);
    return new CacheSelectFilter(null, valueIdentifiers, isPrivate);
  }

}
