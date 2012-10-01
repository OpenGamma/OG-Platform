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
import com.opengamma.financial.analytics.ResetScheduleMatrix;

/**
 * 
 */
public class ResetScheduleMatrixConverter implements ResultConverter<ResetScheduleMatrix> {

  @Override
  public Object convertForDisplay(final ResultConverterCache context, final ValueSpecification valueSpec, final ResetScheduleMatrix value, final ConversionMode mode) {
    final Map<String, Object> result = new HashMap<String, Object>();
    final Map<String, Object> summary = new HashMap<String, Object>();
    final int rowCount = value.getDatesAsArray().length;
    final int columnCount = value.getMaxEntries();
    summary.put("rowCount", rowCount);
    summary.put("colCount", columnCount);
    result.put("summary", summary);
    if (mode == ConversionMode.FULL) {
      final LocalDate[] dates = value.getDatesAsArray();
      final String[][] ca = value.getCurrencyAmountsAsStringArray();
      final String[] xLabels = new String[columnCount];
      final String[] yLabels = new String[rowCount];
      final Object[][] amounts = new Object[rowCount][columnCount];
      for (int i = 0; i < columnCount; i++) {
        xLabels[i] = dates[i].toString();
        for (int j = 0; j < rowCount; j++) {
          if (i == 0) {
            yLabels[j] = StringUtils.EMPTY;
          }
          amounts[j][i] = ca[j][i].toString();
        }
      }
      result.put("x", xLabels);
      result.put("y", yLabels);
      result.put("matrix", amounts);
    }
    return result;
  }

  @Override
  public Object convertForHistory(final ResultConverterCache context, final ValueSpecification valueSpec, final ResetScheduleMatrix value) {
    return null;
  }

  @Override
  public String convertToText(final ResultConverterCache context, final ValueSpecification valueSpec, final ResetScheduleMatrix value) {
    return value.getClass().getSimpleName();
  }

  @Override
  public String getFormatterName() {
    return "LABELLED_MATRIX_2D";
  }

}
