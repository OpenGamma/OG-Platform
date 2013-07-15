/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class DoubleObjectArrayFormatter extends AbstractFormatter<Double[][]> {

  /* package */ DoubleObjectArrayFormatter() {
    super(Double[][].class);
    addFormatter(new Formatter<Double[][]>(Format.EXPANDED) {
      @Override
      Object format(Double[][] value, ValueSpecification valueSpec, Object inlineKey) {
        return value;
      }
    });
  }

  @Override
  public Object formatCell(Double[][] value, ValueSpecification valueSpec, Object inlineKey) {
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
  public DataType getDataType() {
    return DataType.MATRIX_2D;
  }
}
