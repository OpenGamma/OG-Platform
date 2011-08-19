/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * An aggregation of other {@link TypeConverterProvider} instances.
 */
public final class AggregatingTypeConverterProvider implements TypeConverterProvider {

  /**
   * The list of converters, in the order added, set to {@code null} when the converters are fetched for the first
   * time (after which the aggregator cannot be modified).
   */
  private List<TypeConverterProvider> _typeConverterProviders = new LinkedList<TypeConverterProvider>();
  private volatile Set<TypeConverter> _typeConverters;

  public AggregatingTypeConverterProvider() {
  }

  public synchronized void addTypeConverterProvider(final TypeConverterProvider typeConverterProvider) {
    if (_typeConverterProviders == null) {
      throw new IllegalStateException();
    }
    _typeConverterProviders.add(typeConverterProvider);
  }

  @Override
  public Set<TypeConverter> getTypeConverters() {
    if (_typeConverters == null) {
      synchronized (this) {
        final Set<TypeConverter> typeConverters = new HashSet<TypeConverter>();
        for (TypeConverterProvider typeConverterProvider : _typeConverterProviders) {
          typeConverters.addAll(typeConverterProvider.getTypeConverters());
        }
        _typeConverterProviders = null;
        _typeConverters = Collections.unmodifiableSet(typeConverters);
      }
    }
    return _typeConverters;
  }

}
