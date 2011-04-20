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

  private final ResultConverter<Object> _fudgeBasedConverter;
  private final Map<Class<?>, ResultConverter<?>> _converterMap;
  
  private final Map<String, ResultConverter<?>> _converterCache = new ConcurrentHashMap<String, ResultConverter<?>>();
  
  public ResultConverterCache(FudgeContext fudgeContext) {
    _fudgeBasedConverter = new FudgeBasedJsonGeneratorConverter(fudgeContext);
    ResultConverter<Object> primitiveConverter = new PrimitiveConverter();

    // Add standard custom converters here
    _converterMap = new ConcurrentHashMap<Class<?>, ResultConverter<?>>();
    registerConverter(Double.class, primitiveConverter);
    registerConverter(Boolean.class, primitiveConverter);
    registerConverter(String.class, primitiveConverter);
    registerConverter(YieldCurve.class, new YieldCurveConverter());
    registerConverter(VolatilitySurfaceData.class, new VolatilitySurfaceDataConverter());
    registerConverter(LabelledMatrix1D.class, new LabelledMatrix1DConverter());
    registerConverter(Tenor.class, new TenorConverter());
    //registerConverter(Collection.class, new MatrixConverter());
  }
  
  public <T> void registerConverter(Class<T> clazz, ResultConverter<? super T> converter) {
    _converterMap.put(clazz, converter);
  }
  
  /**
   * Transforms the given value into a JSON-friendly object.
   * 
   * @param <T>  the type of the value to be converted
   * @param valueRequirementName  the name of the value requirement which produced the given value, not null
   * @param value  the result to be converted, assumed to be representative of all results for the requirement name, not null
   * @param mode  the conversion mode, not null
   * @return  the converter to be used
   */
  public <T> Object convert(String valueRequirementName, T value, ConversionMode mode) {
    ResultConverter<? super T> converter = getAndCacheConverter(valueRequirementName, value);
    return converter.convert(this, value, mode);
  }
  
  @SuppressWarnings("unchecked")
  public <T> Object convert(T value, ConversionMode mode) {
    ResultConverter<? super T> converter = findConverterForType((Class<T>) value.getClass());
    return converter.convert(this, value, mode);
  }
  
  public String getKnownResultTypeName(String valueRequirementName) {
    ResultConverter<?> converter = _converterCache.get(valueRequirementName);
    return converter != null ? converter.getResultTypeName() : null;
  }
  
  public ResultConverter<Object> getFudgeConverter() {
    return _fudgeBasedConverter;
  }
  
  @SuppressWarnings("unchecked")
  private <T> ResultConverter<? super T> getAndCacheConverter(String valueRequirementName, T value) {
    ResultConverter<? super T> converter = (ResultConverter<? super T>) _converterCache.get(valueRequirementName);
    if (converter == null) {
      converter = findConverterForType((Class<T>) value.getClass());
      _converterCache.put(valueRequirementName, converter);
    }
    return converter; 
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
