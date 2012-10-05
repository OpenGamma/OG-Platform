/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.StringUtils;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.PaymentScheduleMatrix;

/**
 * 
 */
public class PaymentScheduleMatrixConverter implements ResultConverter<PaymentScheduleMatrix> {

  @Override
  public Object convertForDisplay(final ResultConverterCache context, final ValueSpecification valueSpec, final PaymentScheduleMatrix value, final ConversionMode mode) {
    final Map<String, Object> result = new HashMap<String, Object>();
    final Map<String, Object> summary = new HashMap<String, Object>();
    final int rowCount = value.getDatesAsArray().length;
    final int columnCount = value.getMaxCurrencyAmounts();
    summary.put("rowCount", rowCount);
    summary.put("colCount", columnCount);
    result.put("summary", summary);
    if (mode == ConversionMode.FULL) {
      final LocalDate[] dates = value.getDatesAsArray();
      final String[][] ca = value.getCurrencyAmountsAsStringArray();
      final String[] xLabels = new String[rowCount];
      final String[] yLabels = new String[columnCount];
      final Object[][] amounts = new Object[columnCount][rowCount];
      for (int i = 0; i < rowCount; i++) {
        xLabels[i] = dates[i].toString();
        for (int j = 0; j < columnCount; j++) {
          if (i == 0) {
            yLabels[j] = StringUtils.EMPTY;
          }
          amounts[j][i] = ca[i][j].toString();
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
