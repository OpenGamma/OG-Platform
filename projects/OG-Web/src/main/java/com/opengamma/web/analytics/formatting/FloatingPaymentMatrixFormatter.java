/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.cashflow.FloatingPaymentMatrix;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.Pair;

/**
 * Formatter.
 */
/* package */ class FloatingPaymentMatrixFormatter extends AbstractFormatter<FloatingPaymentMatrix> {

  /* package */ static final String X_LABELS = "xLabels";
  /* package */ static final String Y_LABELS = "yLabels";
  /* package */ static final String MATRIX = "matrix";
  private final CurrencyAmountFormatter _caFormatter;

  /* package */ FloatingPaymentMatrixFormatter(final CurrencyAmountFormatter caFormatter) {
    super(FloatingPaymentMatrix.class);
    addFormatter(new Formatter<FloatingPaymentMatrix>(Format.EXPANDED) {
      @Override
      Map<String, Object> format(FloatingPaymentMatrix value, ValueSpecification valueSpec, Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
    });
    _caFormatter = caFormatter;
  }

  @Override
  public String formatCell(FloatingPaymentMatrix value, ValueSpecification valueSpec, Object inlineKey) {
    return "FloatingPaymentMatrix (" + value.getDatesAsArray().length + ")";
  }

  private Map<String, Object> formatExpanded(FloatingPaymentMatrix value, ValueSpecification valueSpec) {
    Map<LocalDate, List<Pair<CurrencyAmount, String>>> values = value.getValues();
    int columnCount = value.getMaxEntries();
    int rowCount = values.size();
    
    Map<String, Object> results = Maps.newHashMap();
    String[] xLabels = new String[columnCount];
    String[] yLabels = new String[rowCount];
    String[][] matrix = new String[rowCount][columnCount];
    int row = 0;
    Arrays.fill(yLabels, StringUtils.EMPTY);
    for (Map.Entry<LocalDate, List<Pair<CurrencyAmount, String>>> entry : values.entrySet()) {
      yLabels[row] = entry.getKey().toString();
      List<Pair<CurrencyAmount, String>> ca = entry.getValue();
      for (int i = 0; i < columnCount; i++) {
        StringBuilder sb = new StringBuilder(_caFormatter.formatCell(ca.get(i).getFirst(), valueSpec, null));
        sb.append(" (");
        sb.append(ca.get(i).getSecond());
        sb.append(")");
        matrix[row][i] = sb.toString();
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
