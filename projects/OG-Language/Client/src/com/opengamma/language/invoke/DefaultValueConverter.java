/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.context.SessionContext;
import com.opengamma.language.convert.Converters;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Converts a value using some basic rules.
 */
public class DefaultValueConverter implements ValueConverter {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultValueConverter.class);

  private final List<TypeConverter> _typeConverters;
  private final ConcurrentMap<JavaTypeInfo<?>, List<TypeConverter>> _convertersByTarget = new ConcurrentHashMap<JavaTypeInfo<?>, List<TypeConverter>>();

  public DefaultValueConverter() {
    _typeConverters = new ArrayList<TypeConverter>();
    Converters.populateList(_typeConverters);
  }

  public DefaultValueConverter(final DefaultValueConverter copyFrom) {
    _typeConverters = new ArrayList<TypeConverter>(copyFrom.getTypeConverters());
  }

  /**
   * Returns a modifiable list of the type converters available.
   * 
   * @return the list of type converters
   */
  public List<TypeConverter> getTypeConverters() {
    return _typeConverters;
  }

  protected List<TypeConverter> getConvertersTo(final JavaTypeInfo<?> type) {
    List<TypeConverter> converters = _convertersByTarget.get(type);
    if (converters != null) {
      return converters;
    }
    converters = new ArrayList<TypeConverter>();
    for (TypeConverter converter : getTypeConverters()) {
      if (converter.canConvertTo(type)) {
        converters.add(converter);
      }
    }
    if (converters.isEmpty()) {
      converters = Collections.<TypeConverter> emptyList();
      _convertersByTarget.putIfAbsent(type, converters);
    } else {
      final List<TypeConverter> previous = _convertersByTarget.putIfAbsent(type, converters);
      if (previous != null) {
        converters = previous;
      }
    }
    return converters;
  }

  protected Object convertValue(final SessionContext sessionContext, final Object value, final JavaTypeInfo<?> type,
      Set<JavaTypeInfo<?>> visited) {
    s_logger.debug("Attempting to convert {} to type {}", value, type);
    if (value == null) {
      if (type.isAllowNull()) {
        s_logger.debug("Type allows NULL");
        return null;
      } else if (type.isDefaultValue()) {
        s_logger.debug("Type has default value");
        return type.getDefaultValue();
      } else {
        s_logger.debug("Type does not allow NULL");
        return new InvalidConversionException(value, type);
      }
    }
    if (type.getRawClass().isAssignableFrom(value.getClass())) {
      // TODO: if there are deep cast generic issues, the conversion will need to go deeper (e.g. Foo<X> to Foo<Y> where (? extends X)->Y is a well defined conversion for all values) 
      s_logger.debug("Raw class type is assignable");
      return value;
    }
    // TODO: maintain a cache of value.getClass/type pairs that has converters that have worked in the past to avoid the expensive searching operation
    final List<TypeConverter> converters = getConvertersTo(type);
    for (TypeConverter converter : converters) {
      if (converter.canConvert(sessionContext, value, type)) {
        s_logger.debug("Using converter {}", converter);
        return converter.convert(sessionContext, value, type);
      }
    }
    if (visited == null) {
      visited = new HashSet<JavaTypeInfo<?>>();
    }
    visited.add(type);
    for (TypeConverter converter : converters) {
      final List<JavaTypeInfo<?>> alternativeTypes = converter.getConversionsTo(type);
      if ((alternativeTypes != null) && !alternativeTypes.isEmpty()) {
        for (JavaTypeInfo<?> alternativeType : alternativeTypes) {
          if (visited.add(alternativeType)) {
            final Object alternativeValue = convertValue(sessionContext, value, alternativeType, visited);
            if (!(alternativeValue instanceof InvalidConversionException)) {
              if (converter.canConvert(sessionContext, alternativeValue, type)) {
                s_logger.debug("Using converter {} converter with alternative value {}", converter, alternativeValue);
                visited.remove(alternativeType);
                visited.remove(type);
                return converter.convert(sessionContext, alternativeValue, type);
              }
            }
            visited.remove(alternativeType);
          }
        }
      }
    }
    visited.remove(type);
    return new InvalidConversionException(value, type);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T convertValue(final SessionContext sessionContext, final Object value, final JavaTypeInfo<T> type) {
    final Object result = convertValue(sessionContext, value, type, null);
    if (result instanceof InvalidConversionException) {
      throw (InvalidConversionException) result;
    }
    return (T) result;
  }

}
