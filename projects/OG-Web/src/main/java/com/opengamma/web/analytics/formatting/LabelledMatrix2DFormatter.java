/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.LabelledMatrix2D;

/**
 * Formatter.
 */
@SuppressWarnings("rawtypes")
/* package */ class LabelledMatrix2DFormatter extends AbstractFormatter<LabelledMatrix2D> {

  /* package */ static final String X_LABELS = "xLabels";
  /* package */ static final String Y_LABELS = "yLabels";
  /* package */ static final String MATRIX = "matrix";

  /* package */ LabelledMatrix2DFormatter() {
    super(LabelledMatrix2D.class);
    addFormatter(new Formatter<LabelledMatrix2D>(Format.EXPANDED) {
      @Override
      Map<String, Object> format(LabelledMatrix2D value, ValueSpecification valueSpec, Object inlineKey) {
        return formatExpanded(value);
      }
    });
  }

  @Override
  public String formatCell(LabelledMatrix2D value, ValueSpecification valueSpec, Object inlineKey) {
    return "Matrix (" + value.getYKeys().length + " x " + value.getXKeys().length + ")";
  }

  private Map<String, Object> formatExpanded(LabelledMatrix2D value) {
    Map<String, Object> results = Maps.newHashMap();
    int rowCount = value.getYKeys().length;
    int columnCount = value.getXKeys().length;
    String[] xLabels = new String[columnCount];
    String[] yLabels = new String[rowCount];
    for (int i = 0; i < xLabels.length; i++) {
      xLabels[i] = value.getXLabels()[i].toString();
    }
    results.put(X_LABELS, xLabels);
    for (int i = 0; i < yLabels.length; i++) {
      yLabels[i] = value.getYLabels()[i].toString();
    }
    results.put(Y_LABELS, yLabels);
    results.put(MATRIX, value.getValues());
    return results;
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_2D;
  }
}
