/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.LocalDate;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.PaymentScheduleMatrix;
import com.opengamma.util.money.CurrencyAmount;

/**
 * 
 */
public class PaymentScheduleMatrixConverter implements ResultConverter<PaymentScheduleMatrix> {

  @Override
  public Object convertForDisplay(final ResultConverterCache context, final ValueSpecification valueSpec, final PaymentScheduleMatrix value, final ConversionMode mode) {
    final Map<String, Object> result = new HashMap<String, Object>();
    final Map<String, Object> summary = new HashMap<String, Object>();
    final int xValues = value.getDatesAsArray().length;
    summary.put("rowCount", xValues);
    summary.put("colCount", value.getMaxCurrencyAmounts());
    result.put("summary", summary);
    if (mode == ConversionMode.FULL) {
      final LocalDate[] dates = value.getDatesAsArray();
      final CurrencyAmount[][] ca = value.getCurrencyAmountsAsArray();
      final String[] xLabels = new String[dates.length];
      final String[] yLabels = new String[value.getMaxCurrencyAmounts()];
      final Object[][] amounts = new Object[dates.length][value.getMaxCurrencyAmounts()];
      for (int i = 0; i < dates.length; i++) {
        xLabels[i] = dates[i].toString();
        for (int j = 0; j < value.getMaxCurrencyAmounts(); j++) {
          if (j == 0) {
            yLabels[j] = "";
          }
          if (ca[i][j] != null) {
            amounts[i][j] = ca[i][j].toString();
          } else {
            amounts[i][j] = "";
          }
        }
      }
      result.put("x", xLabels);
      result.put("y", yLabels);
      result.put("matrix", amounts);
    }
    return result;
  }

  @Override
  public Object convertForHistory(final ResultConverterCache context, final ValueSpecification valueSpec, final PaymentScheduleMatrix value) {
    return null;
  }

  @Override
  public String convertToText(final ResultConverterCache context, final ValueSpecification valueSpec, final PaymentScheduleMatrix value) {
    return value.getClass().getSimpleName();
  }

  @Override
  public String getFormatterName() {
    return "LABELLED_MATRIX_2D";
  }

}
