/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import java.math.BigDecimal;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.LabelledMatrix1D;
import com.opengamma.financial.analytics.LabelledMatrix2D;
import com.opengamma.util.ClassMap;
import com.opengamma.util.money.CurrencyAmount;

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

    _formatters.put(BigDecimal.class, bigDecimalFormatter);
    _formatters.put(Double.class, doubleFormatter);
    _formatters.put(CurrencyAmount.class, currencyAmountFormatter);
    _formatters.put(LabelledMatrix1D.class, new LabelledMatrix1DFormatter());
    _formatters.put(LabelledMatrix2D.class, new LabelledMatrix2DFormatter());
  }

  private <T> Formatter getFormatter(Object value) {
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
  public String formatForDisplay(Object value, ValueSpecification valueSpec) {
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
    return getFormatterForType(type).getName();
  }
}
