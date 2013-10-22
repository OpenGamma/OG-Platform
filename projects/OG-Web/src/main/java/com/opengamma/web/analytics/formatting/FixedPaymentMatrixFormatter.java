/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.cashflow.FixedPaymentMatrix;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Formatter.
 */
/* package */ class FixedPaymentMatrixFormatter extends AbstractFormatter<FixedPaymentMatrix> {

  /* package */ static final String X_LABELS = "xLabels";
  /* package */ static final String Y_LABELS = "yLabels";
  /* package */ static final String MATRIX = "matrix";
  private final CurrencyAmountFormatter _caFormatter;

  /* package */ FixedPaymentMatrixFormatter(final CurrencyAmountFormatter caFormatter) {
    super(FixedPaymentMatrix.class);
    addFormatter(new Formatter<FixedPaymentMatrix>(Format.EXPANDED) {
      @Override
      Map<String, Object> format(FixedPaymentMatrix value, ValueSpecification valueSpec, Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
    });
    _caFormatter = caFormatter;
  }

  @Override
  public String formatCell(FixedPaymentMatrix value, ValueSpecification valueSpec, Object inlineKey) {
    return "FixedPaymentMatrix (" + value.getDatesAsArray().length + ")";
  }

  private Map<String, Object> formatExpanded(FixedPaymentMatrix value, ValueSpecification valueSpec) {
    Map<LocalDate, MultipleCurrencyAmount> values = value.getValues();
    int columnCount = value.getMaxCurrencyAmounts();
    int rowCount = values.size();
    
    Map<String, Object> results = Maps.newHashMap();
    String[] xLabels = new String[columnCount];
    String[] yLabels = new String[rowCount];
    String[][] matrix = new String[rowCount][columnCount];
    int row = 0;
    Arrays.fill(yLabels, StringUtils.EMPTY);
    for (Map.Entry<LocalDate, MultipleCurrencyAmount> entry : values.entrySet()) {
      yLabels[row] = entry.getKey().toString();
      CurrencyAmount[] ca = entry.getValue().getCurrencyAmounts();
      for (int i = 0; i < columnCount; i++) {
        matrix[row][i] = _caFormatter.formatCell(ca[i], valueSpec, null);
      }
      row++;
    }
    results.put(X_LABELS, xLabels);
    results.put(Y_LABELS, yLabels);
    results.put(MATRIX, matrix);
    return results;
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_2D;
  }
}
