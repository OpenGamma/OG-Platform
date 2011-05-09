/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fudgemsg.FudgeContext;

import com.opengamma.financial.analytics.LabelledMatrix1D;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceData;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.util.time.Tenor;

/**
 * Manages a set of converters and provides access to the most appropriate converter for a given type.
 */
public class ResultConverterCache {

  private final DoubleConverter _doubleConverter;
  private final ResultConverter<Object> _fudgeBasedConverter;
  private final Map<Class<?>, ResultConverter<?>> _converterMap;
  
  private final Map<String, ResultConverter<?>> _converterCache = new ConcurrentHashMap<String, ResultConverter<?>>();
  
  public ResultConverterCache(FudgeContext fudgeContext) {
    _fudgeBasedConverter = new FudgeBasedJsonGeneratorConverter(fudgeContext);
    _doubleConverter = new DoubleConverter();
    ResultConverter<Object> primitiveConverter = new PrimitiveConverter();

    // Add standard custom converters here
    _converterMap = new ConcurrentHashMap<Class<?>, ResultConverter<?>>();
    registerConverter(Boolean.class, primitiveConverter);
    registerConverter(String.class, primitiveConverter);
    registerConverter(Double.class, _doubleConverter);
    registerConverter(YieldCurve.class, new YieldCurveConverter());
    registerConverter(VolatilitySurfaceData.class, new VolatilitySurfaceDataConverter());
    registerConverter(LabelledMatrix1D.class, new LabelledMatrix1DConverter());
    registerConverter(Tenor.class, new TenorConverter());
  }
  
  private <T> void registerConverter(Class<T> clazz, ResultConverter<? super T> converter) {
    _converterMap.put(clazz, converter);
  }
  
  @SuppressWarnings("unchecked")
  public <T> ResultConverter<? super T> getAndCacheConverter(String valueName, Class<T> valueType) {
    ResultConverter<? super T> converter = (ResultConverter<? super T>) _converterCache.get(valueName);
    if (converter == null) {
      converter = findConverterForType((Class<T>) valueType);
      _converterCache.put(valueName, converter);
    }
    return converter; 
  }
  
  @SuppressWarnings("unchecked")
  public <T> Object convert(T value, ConversionMode mode) {
    ResultConverter<? super T> converter = findConverterForType((Class<T>) value.getClass());
    return converter.convertForDisplay(this, null, value, mode);
  }
  
  public DoubleConverter getDoubleConverter() {
    return _doubleConverter;
  }
  
  public String getKnownResultTypeName(String valueRequirementName) {
    ResultConverter<?> converter = _converterCache.get(valueRequirementName);
    return converter != null ? converter.getFormatterName() : null;
  }
  
  public ResultConverter<Object> getFudgeConverter() {
    return _fudgeBasedConverter;
  }
  
  @SuppressWarnings("unchecked")
  private <T> ResultConverter<? super T> findConverterForType(Class<T> type) {
    Class<?> searchType = type;
    while (searchType != null) {
      ResultConverter<?> converter = _converterMap.get(searchType);
      if (converter != null) {
        return (ResultConverter<? super T>) converter;
      }
      searchType = searchType.getSuperclass();
    }
    return _fudgeBasedConverter;
  }
  
}
