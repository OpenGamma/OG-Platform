/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class DoubleArrayFormatter extends NoHistoryFormatter<double[][]> {

  @Override
  public Object formatForDisplay(double[][] value, ValueSpecification valueSpec) {
    int rowCount;
    int colCount;
    rowCount = value.length;
    if (rowCount == 0) {
      colCount = 0;
    } else {
      colCount = value[0].length;
    }
    return "Matrix (" + rowCount + " x " + colCount + ")";
  }

  @Override
  public Object formatForExpandedDisplay(double[][] value, ValueSpecification valueSpec) {
    // TODO implement formatForExpandedDisplay()
    throw new UnsupportedOperationException("formatForExpandedDisplay not implemented");
  }

  @Override
  public FormatType getFormatType() {
    return FormatType.LABELLED_MATRIX_2D;
  }
}
