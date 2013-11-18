/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.math.BigDecimal;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.web.analytics.formatting.ResultsFormatter.CurrencyDisplay;

/**
 * Formats rates into percentage form.
 */
public class RateFormatter extends AbstractFormatter<Double> {
  /** Percentage symbol */
  private static final String PERCENT = "%";  
  /** The underlying double formatter */
  private final AbstractFormatter<BigDecimal> _bigDecimalFormatter;
  
  /**
   * @param bigDecimalFormatter A BigDecimal formatter, not null
   */
  /* package */ RateFormatter() {
    super(Double.class);
    _bigDecimalFormatter = new BigDecimalFormatter(CurrencyDisplay.SUPPRESS_CURRENCY);
    addFormatter(new Formatter<Double>(Format.HISTORY) {

      @Override
      Object format(final Double value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatHistory(value, valueSpec);
      }
    });
    addFormatter(new Formatter<Double>(Format.EXPANDED) {

      @Override
      Object format(final Double value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
      
    });
  }

  @Override
  public Object formatCell(final Double value, final ValueSpecification valueSpec, final Object inlineKey) {
    final BigDecimal bd = convertToBigDecimal(value);
    final Object result = _bigDecimalFormatter.formatCell(bd, null, null);
    if (result == null) {
      return Double.toString(value);
    }
    return result.toString() + PERCENT;
  }

  /**
   * Formats the history.
   * @param history The history
   * @param valueSpec The value specification
   * @return A value formatted for a history
   */
  /* package */ Object formatHistory(final Double history, final ValueSpecification valueSpec) {
    final BigDecimal bd = convertToBigDecimal(history);
    final Object result = _bigDecimalFormatter.formatCell(bd, null, null);
    if (result == null) {
      return Double.toString(history);
    }
    return result.toString() + PERCENT;
  }
  
  /**
   * Expands the format.
   * @param value The value
   * @param valueSpec The value specification
   * @return An expanded format
   */
  /* package */ Object formatExpanded(final Double value, final ValueSpecification valueSpec) {
    final BigDecimal bd = convertToBigDecimal(value);
    final Object result = _bigDecimalFormatter.formatCell(bd, null, null);
    if (result == null) {
      return Double.toString(value);
    }
    return result.toString() + PERCENT;
  }
  
  @Override
  public DataType getDataType() {
    return DataType.STRING;
  }
  
  /**
   * Converts a Double to a BigDecimal
   * @param value The value
   * @return A BigDecimal
   */
  private static BigDecimal convertToBigDecimal(Double value) {
    if (Double.isInfinite(value) || Double.isNaN(value)) {
      return null;
    } 
    return new BigDecimal(Double.valueOf(100. * value).toString());
  }
}
