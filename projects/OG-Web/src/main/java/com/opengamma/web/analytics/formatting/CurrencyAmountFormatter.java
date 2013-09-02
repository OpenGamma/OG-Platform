/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.math.BigDecimal;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;

/**
 *
 */
/* package */ class CurrencyAmountFormatter extends AbstractFormatter<CurrencyAmount> {

  private final BigDecimalFormatter _bigDecimalFormatter;
  private final ResultsFormatter.CurrencyDisplay _currencyDisplay;

  /* package */ CurrencyAmountFormatter(ResultsFormatter.CurrencyDisplay currencyDisplay,
                                        BigDecimalFormatter bigDecimalFormatter) {
    super(CurrencyAmount.class);
    ArgumentChecker.notNull(bigDecimalFormatter, "bigDecimalFormatter");
    ArgumentChecker.notNull(currencyDisplay, "currencyDisplay");
    _bigDecimalFormatter = bigDecimalFormatter;
    _currencyDisplay = currencyDisplay;
    addFormatter(new Formatter<CurrencyAmount>(Format.EXPANDED) {
      @Override
      Object format(CurrencyAmount value, ValueSpecification valueSpec, Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
    });
    addFormatter(new Formatter<CurrencyAmount>(Format.HISTORY) {
      @Override
      Object format(CurrencyAmount value, ValueSpecification valueSpec, Object inlineKey) {
        return formatHistory(value, valueSpec);
      }
    });
  }

  @Override
  public String formatCell(CurrencyAmount value, ValueSpecification valueSpec, Object inlineKey) {
    double amount = value.getAmount();
    BigDecimal bigDecimal = convertToBigDecimal(amount);
    return bigDecimal == null ?
        Double.toString(amount) :
        formatValue(value, valueSpec, inlineKey, bigDecimal);
  }

  private String formatValue(CurrencyAmount value,
                             ValueSpecification valueSpec,
                             Object inlineKey,
                             BigDecimal bigDecimal) {

    String prefix = _currencyDisplay == ResultsFormatter.CurrencyDisplay.DISPLAY_CURRENCY ?
        value.getCurrency().getCode() + " " :
        "";
    return prefix + _bigDecimalFormatter.formatCell(bigDecimal, valueSpec, inlineKey);
  }

  private Object formatExpanded(CurrencyAmount value, ValueSpecification valueSpec) {
    double amount = value.getAmount();
    BigDecimal bigDecimal = convertToBigDecimal(amount);
    if (bigDecimal == null) {
      return Double.toString(amount);
    } else {
      return _bigDecimalFormatter.format(bigDecimal, valueSpec, Format.EXPANDED, null);
    }
  }

  private Object formatHistory(CurrencyAmount history, ValueSpecification valueSpec) {
    double amount = history.getAmount();
    BigDecimal bigDecimal = convertToBigDecimal(amount);
    if (bigDecimal == null) {
      return null;
    } else {
      return _bigDecimalFormatter.format(bigDecimal, valueSpec, Format.HISTORY, null);
    }
  }

  @Override
  public DataType getDataType() {
    return DataType.DOUBLE;
  }

  /**
   * @param value A double value, not null
   * @return The value converted to a {@link BigDecimal}, null if the value is infinite or not a number
   */
  private static BigDecimal convertToBigDecimal(Double value) {
    if (Double.isInfinite(value) || Double.isNaN(value)) {
      return null;
    } else {
      return new BigDecimal(value.toString());
    }
  }
}
