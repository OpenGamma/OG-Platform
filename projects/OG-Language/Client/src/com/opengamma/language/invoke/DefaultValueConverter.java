/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.invoke;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.convert.Converters;
import com.opengamma.language.convert.ValueConversionContext;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Converts a value using some basic rules.
 */
public class DefaultValueConverter extends ValueConverter {

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
      converters = Collections.<TypeConverter>emptyList();
      _convertersByTarget.putIfAbsent(type, converters);
    } else {
      final List<TypeConverter> previous = _convertersByTarget.putIfAbsent(type, converters);
      if (previous != null) {
        converters = previous;
      }
    }
    return converters;
  }

  // TODO: This is not an efficient algorithm; the state space it explores can be pretty huge and it will insist on attempting parts of it to near completion 

  private static String s_indent = "";

  public void convertValue(ValueConversionContext conversionContext, Object value, JavaTypeInfo<?> type) {
    s_indent = s_indent + "  ";
    try {
      s_logger.debug(s_indent + "Attempting to convert {} to type {}", value, type);
      if (value == null) {
        if (type.isAllowNull()) {
          s_logger.debug(s_indent + "Type allows NULL");
          conversionContext.setResult(0, null);
        } else if (type.isDefaultValue()) {
          s_logger.debug(s_indent + "Type has default value");
          conversionContext.setResult(0, type.getDefaultValue());
        } else {
          s_logger.debug(s_indent + "Type does not allow NULL");
          conversionContext.setFail();
        }
        return;
      }
      s_logger.debug(s_indent + "Attempting class assignment from {} to {}", value.getClass(), type.getRawClass());
      if (type.getRawClass().isAssignableFrom(value.getClass())) {
        // TODO: if there are deep cast generic issues, the conversion will need to go deeper (e.g. Foo<X> to Foo<Y> where (? extends X)->Y is a well defined conversion for all values) 
        s_logger.debug(s_indent + "Raw class type is assignable");
        conversionContext.setResult(0, value);
        return;
      }
      // TODO: maintain a cache of value.getClass/type pairs that has converters that have worked in the past to avoid the expensive searching operation
      final List<TypeConverter> converters = getConvertersTo(type);
      for (TypeConverter converter : converters) {
        converter.convertValue(conversionContext, value, type);
        if (!conversionContext.isFailed()) {
          s_logger.debug(s_indent + "Used converter {}", converter);
          return;
        }
      }
      final Set<Object> visited = conversionContext.getVisited();
      visited.add(type);
      Object bestResult = null;
      int bestResultCost = 0;
      final int previousCostLimit = conversionContext.getCostLimit();
      for (TypeConverter converter : converters) {
        if (visited.add(converter)) {
          final List<JavaTypeInfo<?>> alternativeTypes = converter.getConversionsTo(type);
          if ((alternativeTypes != null) && !alternativeTypes.isEmpty()) {
            for (JavaTypeInfo<?> alternativeType : alternativeTypes) {
              if (visited.add(alternativeType)) {
                final int originalCost = conversionContext.getCost();
                s_logger.debug(s_indent + "Trying via {} for {}", alternativeType, converter);
                convertValue(conversionContext, value, alternativeType);
                final boolean tooExpensive = (conversionContext.getCost() > conversionContext.getCostLimit());
                if (!conversionContext.isFailed() && !tooExpensive) {
                  final Object alternativeValue = conversionContext.getResult();
                  s_logger.debug(s_indent + "Trying alternative {} to {}", alternativeValue, type);
                  converter.convertValue(conversionContext, alternativeValue, type);
                  final int totalCost = conversionContext.getAndSetCost(originalCost);
                  if (!conversionContext.isFailed()) {
                    s_logger.debug(s_indent + "Used converter {} converter with alternative value {}", converter, alternativeValue);
                    s_logger.debug(s_indent + "Original cost = {}, total cost = {}", originalCost, totalCost);
                    final Object result = conversionContext.getResult();
                    if ((bestResult == null) || (totalCost < bestResultCost)) {
                      bestResult = result;
                      bestResultCost = totalCost;
                      conversionContext.setCostLimit(totalCost);
                    }
                  }
                } else {
                  if (tooExpensive) {
                    s_logger.debug(s_indent + "Via {} failed at cost {}", alternativeType, conversionContext.getCost());
                    conversionContext.getResult();
                  }
                  conversionContext.setCost(originalCost);
                }
                visited.remove(alternativeType);
              }
            }
          }
          visited.remove(converter);
        }
      }
      conversionContext.setCostLimit(previousCostLimit);
      visited.remove(type);
      if (bestResult == null) {
        s_logger.debug(s_indent + "Conversion of {} to {} failed", value, type);
        conversionContext.setFail();
      } else {
        s_logger.debug(s_indent + "Best conversion result is {} at cost {}", bestResult, bestResultCost);
        conversionContext.setResult(bestResultCost, bestResult);
      }
    } finally {
      s_indent = s_indent.substring(2);
    }
  }

}
