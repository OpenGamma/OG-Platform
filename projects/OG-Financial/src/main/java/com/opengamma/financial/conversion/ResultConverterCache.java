/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 */
public class ResultConverterCache {
  
  private final Map<Class<?>, ResultConverter<?>> _converterMap = new ConcurrentHashMap<Class<?>, ResultConverter<?>>();
  
  public ResultConverterCache() {
    // Add standard converters here
    registerConverter(new BigDecimalConverter());
    registerConverter(new DoubleConverter());
    registerConverter(new DoubleMatrix1DConverter());
    registerConverter(new DoubleMatrix2DConverter());
    registerConverter(new TimeSeriesConverter());
    registerConverter(new YieldAndDiscountCurveConverter());
    registerConverter(new LabelledMatrix1DConverter());
    registerConverter(new LabelledMatrix2DConverter());
    registerConverter(new CurrencyAmountConverter());
    registerConverter(new MultipleCurrencyAmountConverter());
    registerConverter(new MultipleCurrencyInterestRateCurveSensitivityConverter());
    registerConverter(new VolatilitySurfaceDataConverter());
  }
  
  public <T> void registerConverter(ResultConverter<?> converter) {
    _converterMap.put(converter.getConvertedClass(), converter);
  }
  
  /**
   * Gets, the converter to be used for a given result, in order to transform the result into a 
   * a String/Double map suitable for writing into a database.
   * 
   * @param <T>  the type of the value to be converted
   * @param value  the result to be converted, assumed to be representative of all results for the requirement name
   * @return  the converter to be used
   * @throws IllegalArgumentException If no converter was found
   */
  @SuppressWarnings("unchecked")
  public <T> ResultConverter<? super T> getConverter(T value) {
    ResultConverter<?> converter = findConverterForType(value.getClass());
    if (converter == null) {
      throw new IllegalArgumentException("No converter found for " + value.getClass());
    }
    return (ResultConverter<? super T>) converter; 
  }
  
  private ResultConverter<?> findConverterForType(Class<?> type) {
    ResultConverter<?> converter = _converterMap.get(type);
    if (converter != null) {
      return converter;
    }

    if (type.getSuperclass() != null) {
      converter = findConverterForType(type.getSuperclass());
      if (converter != null) {
        return converter;
      }
    }
    
    for (Class<?> intface : type.getInterfaces()) {
      converter = findConverterForType(intface);
      if (converter != null) {
        return converter;
      }
    }
    
    return null;
  }

}
