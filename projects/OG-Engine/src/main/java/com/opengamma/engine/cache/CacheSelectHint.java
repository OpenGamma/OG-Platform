/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.cache;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2LongMap;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * A filter to determine whether given values are to go into a private or shared cache.
 */
public final class CacheSelectHint implements IdentifierEncodedValueSpecifications, Serializable {

  private static final long serialVersionUID = 1L;

  private static final CacheSelectHint ALL_SHARED_INSTANCE = new CacheSelectHint(null, null, true);
  private static final CacheSelectHint ALL_PRIVATE_INSTANCE = new CacheSelectHint(null, null, false);

  private final Set<ValueSpecification> _valueSpecifications;
  private long[] _valueIdentifiers;
  private final boolean _isPrivate;

  private CacheSelectHint(final Collection<ValueSpecification> valueSpecifications, final long[] valueIdentifiers, final boolean isPrivate) {
    if (valueSpecifications != null) {
      _valueSpecifications = new HashSet<ValueSpecification>(valueSpecifications);
    } else {
      _valueSpecifications = new HashSet<ValueSpecification>();
    }
    _valueIdentifiers = valueIdentifiers;
    _isPrivate = isPrivate;
  }

  public static CacheSelectHint privateValues(final Collection<ValueSpecification> privateValues) {
    ArgumentChecker.notNull(privateValues, "privateValues");
    return new CacheSelectHint(privateValues, null, true);
  }

  public static CacheSelectHint sharedValues(final Collection<ValueSpecification> sharedValues) {
    ArgumentChecker.notNull(sharedValues, "sharedValues");
    return new CacheSelectHint(sharedValues, null, false);
  }

  public static CacheSelectHint allShared() {
    return ALL_SHARED_INSTANCE;
  }

  public static CacheSelectHint allPrivate() {
    return ALL_PRIVATE_INSTANCE;
  }

  @Override
  public void convertValueSpecifications(final Object2LongMap<ValueSpecification> valueSpecifications) {
    if (_valueIdentifiers == null) {
      _valueIdentifiers = new long[_valueSpecifications.size()];
      int i = 0;
      for (final ValueSpecification specification : _valueSpecifications) {
        _valueIdentifiers[i++] = valueSpecifications.getLong(specification);
      }
    }
  }

  @Override
  public void collectValueSpecifications(final Set<ValueSpecification> valueSpecifications) {
    valueSpecifications.addAll(_valueSpecifications);
  }

  @Override
  public void convertIdentifiers(final Long2ObjectMap<ValueSpecification> identifiers) {
    if (_valueSpecifications.isEmpty()) {
      for (final long identifier : _valueIdentifiers) {
        _valueSpecifications.add(identifiers.get(identifier));
      }
    }
  }

  @Override
  public void collectIdentifiers(final LongSet identifiers) {
    for (final long identifier : _valueIdentifiers) {
      identifiers.add(identifier);
    }
  }

  public boolean isPrivateValue(final ValueSpecification valueSpecification) {
    if (_isPrivate) {
      return _valueSpecifications.contains(valueSpecification);
    } else {
      return !_valueSpecifications.contains(valueSpecification);
    }
  }

  /**
   * Gets the valueIdentifiers field.
   * 
   * @return the valueIdentifiers
   */
  public long[] getValueIdentifiers() {
    return _valueIdentifiers;
  }

  /**
   * Gets the isPrivate field.
   * 
   * @return the isPrivate
   */
  public boolean isPrivate() {
    return _isPrivate;
  }

  public static CacheSelectHint create(final long[] valueIdentifiers, final boolean isPrivate) {
    return new CacheSelectHint(null, valueIdentifiers, isPrivate);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if (isPrivate()) {
      sb.append("All SHARED");
    } else {
      sb.append("All PRIVATE");
    }
    if (!_valueSpecifications.isEmpty()) {
      sb.append(" except for [");
      boolean comma = false;
      for (final ValueSpecification v : _valueSpecifications) {
        if (comma) {
          sb.append(", ");
        } else {
          comma = true;
        }
        sb.append(v);
      }
      sb.append("]");
    }
    return sb.toString();
  }
}
