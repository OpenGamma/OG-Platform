/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import java.math.BigDecimal;
import java.util.List;

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
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.MissingMarketDataSentinel;
import com.opengamma.financial.analytics.LabelledMatrix1D;
import com.opengamma.financial.analytics.LabelledMatrix2D;
import com.opengamma.financial.analytics.LabelledMatrix3D;
import com.opengamma.util.ClassMap;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 *
 */
public class ResultsFormatter {

  private final Formatter _nullFormatter = new NullFormatter();
  private final Formatter _defaultFormatter = new DefaultFormatter();
  private final ClassMap<Formatter<?>> _formatters = new ClassMap<Formatter<?>>();

  public ResultsFormatter() {
    BigDecimalFormatter bigDecimalFormatter = new BigDecimalFormatter();
    DoubleFormatter doubleFormatter = new DoubleFormatter(bigDecimalFormatter);
    CurrencyAmountFormatter currencyAmountFormatter = new CurrencyAmountFormatter(bigDecimalFormatter);

    _formatters.put(Boolean.class, _defaultFormatter);
    _formatters.put(String.class, _defaultFormatter);
    _formatters.put(Double.class, doubleFormatter);
    _formatters.put(BigDecimal.class, bigDecimalFormatter);
    _formatters.put(CurrencyAmount.class, currencyAmountFormatter);
    _formatters.put(YieldCurve.class, new YieldCurveFormatter());
    _formatters.put(VolatilityCubeData.class, new VolatilityCubeDataFormatter());
    _formatters.put(VolatilitySurfaceData.class, new VolatilitySurfaceDataFormatter());
    _formatters.put(VolatilitySurface.class, new VolatilitySurfaceFormatter());
    _formatters.put(LabelledMatrix1D.class, new LabelledMatrix1DFormatter(doubleFormatter));
    _formatters.put(LabelledMatrix2D.class, new LabelledMatrix2DFormatter());
    _formatters.put(LabelledMatrix3D.class, new LabelledMatrix3DFormatter());
    _formatters.put(Tenor.class, new TenorFormatter());
    _formatters.put(MultipleCurrencyAmount.class, new MultipleCurrencyAmountFormatter());
    _formatters.put(MissingMarketDataSentinel.class, new FixedStringFormatter("Missing market data"));
    _formatters.put(ForwardCurve.class, new ForwardCurveFormatter());
    _formatters.put(BlackVolatilitySurfaceMoneyness.class, new BlackVolatilitySurfaceMoneynessFormatter());
    _formatters.put(LocalVolatilitySurfaceMoneyness.class, new LocalVolatilitySurfaceMoneynessFormatter());
    _formatters.put(BucketedGreekResultCollection.class, new BucketedGreekResultCollectionFormatter());
    _formatters.put(DoublesCurve.class, new DoublesCurveFormatter());
    _formatters.put(LocalDateDoubleTimeSeries.class, new LocalDateDoubleTimeSeriesFormatter());
    _formatters.put(HistoricalTimeSeries.class, new HistoricalTimeSeriesFormatter());
    _formatters.put(double[][].class, new DoubleArrayFormatter());
    _formatters.put(Double[][].class, new DoubleObjectArrayFormatter());
    _formatters.put(List.class, new ListDoubleArrayFormatter());
    _formatters.put(PresentValueForexBlackVolatilitySensitivity.class, new PresentValueForexBlackVolatilitySensitivityFormatter());
  }

  private Formatter getFormatter(Object value) {
    if (value == null) {
      return _nullFormatter;
    } else {
      return getFormatterForType(value.getClass());
    }
  }

  private Formatter getFormatterForType(Class<?> type) {
    Formatter formatter = _formatters.get(type);
    if (formatter == null) {
      return _defaultFormatter;
    } else {
      return formatter;
    }
  }

  /**
   * Returns a formatted version of a value suitable for display in the UI.
   * @param value The value
   * @param valueSpec The value's specification
   * @return {@code null} if the value is {@code null}, otherwise a formatted version of a value suitable
   * for display in the UI.
   */
  @SuppressWarnings("unchecked")
  public Object formatForDisplay(Object value, ValueSpecification valueSpec) {
    return getFormatter(value).formatForDisplay(value, valueSpec);
  }

  @SuppressWarnings("unchecked")
  public Object formatForExpandedDisplay(Object value, ValueSpecification valueSpec) {
    return getFormatter(value).formatForExpandedDisplay(value, valueSpec);
  }

  @SuppressWarnings("unchecked")
  public Object formatForHistory(Object value, ValueSpecification valueSpec) {
    return getFormatter(value).formatForHistory(value, valueSpec);
  }

  public String getFormatName(Class<?> type) {
    return getFormatterForType(type).getFormatType().name();
  }
}
