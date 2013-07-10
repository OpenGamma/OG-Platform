/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.greeks.BucketedGreekResultCollection;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.engine.cache.MissingInput;
import com.opengamma.financial.analytics.LabelledMatrix1D;
import com.opengamma.financial.analytics.LabelledMatrix2D;
import com.opengamma.financial.analytics.LabelledMatrix3D;
import com.opengamma.financial.analytics.volatility.surface.FunctionalVolatilitySurfaceData;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ClassMap;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.Tenor;

/**
 * Manages a set of converters and provides access to the most appropriate converter for a given type.
 */
public class ResultConverterCache {

  private static final Logger s_logger = LoggerFactory.getLogger(ResultConverterCache.class);

  private final DoubleConverter _doubleConverter;
  private final ResultConverter<Object> _genericConverter;
  private final ClassMap<ResultConverter<?>> _converterMap;

  private final Map<String, ResultConverter<?>> _valueNameConverterCache = new ConcurrentHashMap<String, ResultConverter<?>>();

  public ResultConverterCache(final FudgeContext fudgeContext) {
    _genericConverter = new ToStringConverter();
    _doubleConverter = new DoubleConverter();
    final ResultConverter<Object> primitiveConverter = new PrimitiveConverter();

    // Add standard custom converters here
    _converterMap = new ClassMap<ResultConverter<?>>();
    registerConverter(Boolean.class, primitiveConverter);
    registerConverter(String.class, primitiveConverter);
    registerConverter(Double.class, _doubleConverter);
    registerConverter(BigDecimal.class, _doubleConverter);
    registerConverter(CurrencyAmount.class, _doubleConverter);
    registerConverter(YieldCurve.class, new YieldCurveConverter());
    registerConverter(VolatilityCubeData.class, new VolatilityCubeDataConverter());
    registerConverter(VolatilitySurfaceData.class, new VolatilitySurfaceDataConverter());
    registerConverter(VolatilitySurface.class, new VolatilitySurfaceConverter());
    registerConverter(LabelledMatrix1D.class, new LabelledMatrix1DConverter());
    registerConverter(LabelledMatrix2D.class, new LabelledMatrix2DConverter());
    registerConverter(LabelledMatrix3D.class, new LabelledMatrix3DConverter());
    registerConverter(Tenor.class, new TenorConverter());
    registerConverter(MultipleCurrencyAmount.class, new MultipleCurrencyAmountConverter(_doubleConverter));
    registerConverter(MissingInput.class, new StaticStringConverter("Missing market data"));
    registerConverter(ForwardCurve.class, new ForwardCurveConverter());
    registerConverter(BlackVolatilitySurfaceMoneyness.class, new BlackVolatilitySurfaceMoneynessConverter());
    registerConverter(LocalVolatilitySurfaceMoneyness.class, new LocalVolatilitySurfaceMoneynessConverter());
    registerConverter(BucketedGreekResultCollection.class, new BucketedVegaConverter());
    registerConverter(DoublesCurve.class, new CurveConverter());
    registerConverter(LocalDateDoubleTimeSeries.class, new LocalDateDoubleTimeSeriesConverter());
    registerConverter(HistoricalTimeSeries.class, new HistoricalTimeSeriesConverter());
    registerConverter(double[][].class, new DoubleArrayConverter());
    registerConverter(Double[][].class, new DoubleObjectArrayConverter());
    registerConverter(List.class, new ListDoubleArrayConverter());
    registerConverter(PresentValueForexBlackVolatilitySensitivity.class, new PresentValueVolatilitySensitivityConverter(_doubleConverter));
    registerConverter(FunctionalVolatilitySurfaceData.class, new FunctionalVolatilitySurfaceDataConverter());
  }

  private <T> void registerConverter(final Class<T> clazz, final ResultConverter<? super T> converter) {
    _converterMap.put(clazz, converter);
  }

  public <T> ResultConverter<? super T> getAndCacheConverter(final String valueName, final Class<T> valueType) {
    @SuppressWarnings("unchecked")
    ResultConverter<? super T> converter = (ResultConverter<? super T>) _valueNameConverterCache.get(valueName);
    if (converter == null) {
      converter = getConverterForType(valueType);
      _valueNameConverterCache.put(valueName, converter);
      s_logger.info("'{}' {}", valueName, valueType.getName());
    }
    return converter;
  }

  public <T> ResultConverter<? super T> getConverterForType(final Class<T> type) {
    @SuppressWarnings("unchecked")
    final ResultConverter<? super T> converter = (ResultConverter<? super T>) _converterMap.get(type);
    if (converter == null) {
      return _genericConverter;
    }
    return converter;
  }

  public <T> Object convert(final T value, final ConversionMode mode) {
    @SuppressWarnings("unchecked")
    final ResultConverter<? super T> converter = getConverterForType((Class<T>) value.getClass());
    return converter.convertForDisplay(this, null, value, mode);
  }

  public DoubleConverter getDoubleConverter() {
    return _doubleConverter;
  }

  public String getKnownResultTypeName(final String valueName) {
    final ResultConverter<?> converter = _valueNameConverterCache.get(valueName);
    return converter != null ? converter.getFormatterName() : null;
  }

  public ResultConverter<Object> getFudgeConverter() {
    return _genericConverter;
  }

}
