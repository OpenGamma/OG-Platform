/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.Currency;

/**
 * Formatter for {@link FXMatrix}
 */
/* package */class FXMatrixFormatter extends AbstractFormatter<FXMatrix> {
  /** x labels */
  /* package */ static final String X_LABELS = "xLabels";
  /** y labels */
  /* package */ static final String Y_LABELS = "yLabels";
  /** matrix */
  /* package */ static final String MATRIX = "matrix";
  
  /**
   * Protected constructor.
   */
  /* package */ FXMatrixFormatter() {
    super(FXMatrix.class);
    addFormatter(new Formatter<FXMatrix>(Format.EXPANDED) {
      
      @Override
      Map<String, Object> format(final FXMatrix value, final ValueSpecification valueSpec, final Object inlineKey) {
        return formatExpanded(value);
      }
    });
  }
  
  @Override
  public String formatCell(final FXMatrix value, final ValueSpecification valueSpec, final Object inlineKey) {
    if (value.getCurrencies().isEmpty()) {
      return "FX Matrix (0 x 0)";
    }
    return "FX Matrix (" + value.getRates()[0].length + " x " + value.getRates().length + ")";
  }
  
  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_2D;    
  }
  
  /**
   * Formats the FX matrix as a 2D labelled matrix.
   * @param value The FX matrix
   * @return The formatted matrix
   */
  private static Map<String, Object> formatExpanded(final FXMatrix value) {
    final Map<String, Object> results = new HashMap<>();
    final int count = value.getCurrencies().size();
    final String[] xLabels = new String[count];
    final String[] yLabels = new String[count];
    final Object[][] matrix = new Object[count][count];
    for (final Map.Entry<Currency, Integer> entryY : value.getCurrencies().entrySet()) {
      final String code = entryY.getKey().getCode();
      final int indexY = entryY.getValue();
      xLabels[indexY] = code;
      yLabels[indexY] = code;
      for (final Map.Entry<Currency, Integer> entryX : value.getCurrencies().entrySet()) {
        final int indexX = entryX.getValue();
        matrix[indexY][indexX] = value.getFxRate(entryY.getKey(), entryX.getKey());
      }
    }
    results.put(X_LABELS, xLabels);
    results.put(Y_LABELS, yLabels);
    results.put(MATRIX, matrix);
    return results;
  }
  
}
