/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

/**
 *
 */
/* package */ class PrimitivesGridViewport extends MainGridViewport {

  /**
   * @param gridStructure Row and column structure of the grid
   * @param callbackId ID that's passed to listeners when the grid structure changes
   */
  /* package */ PrimitivesGridViewport(MainGridStructure gridStructure, String callbackId) {
    super(gridStructure, callbackId);
  }

  @Override
  protected ViewportResults.Cell getFixedColumnResult(int rowIndex, int colIndex, MainGridStructure.Row row) {
    if (colIndex == LABEL_COLUMN) {
      return ViewportResults.stringCell(row.getName(), colIndex);
    }
    throw new IllegalArgumentException("Column " + colIndex + " is not fixed");
  }
}
