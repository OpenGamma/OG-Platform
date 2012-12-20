/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * An aggregation of other {@link TypeConverterProvider} instances.
 */
public final class AggregatingTypeConverterProvider implements TypeConverterProvider {

  /**
   * The list of converters, in the order added, set to null when the converters are fetched for the first
   * time (after which the aggregator cannot be modified).
   */
  private List<TypeConverterProvider> _typeConverterProviders = new LinkedList<TypeConverterProvider>();
  private volatile List<TypeConverter> _typeConverters;

  public AggregatingTypeConverterProvider() {
  }

  public synchronized void addTypeConverterProvider(final TypeConverterProvider typeConverterProvider) {
    if (_typeConverterProviders == null) {
      throw new IllegalStateException();
    }
    _typeConverterProviders.add(typeConverterProvider);
  }

  @Override
  public List<TypeConverter> getTypeConverters() {
    if (_typeConverters == null) {
      synchronized (this) {
        if (_typeConverters == null) {
          final Map<String, TypeConverter> typeConverters = new LinkedHashMap<String, TypeConverter>();
          for (TypeConverterProvider typeConverterProvider : _typeConverterProviders) {
            for (TypeConverter typeConverter : typeConverterProvider.getTypeConverters()) {
              typeConverters.remove(typeConverter.getTypeConverterKey());
              typeConverters.put(typeConverter.getTypeConverterKey(), typeConverter);
            }
          }
          _typeConverterProviders = null;
          _typeConverters = Collections.unmodifiableList(new ArrayList<TypeConverter>(typeConverters.values()));
        }
      }
    }
    return _typeConverters;
  }

}
