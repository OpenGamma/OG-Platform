/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;

/**
 *
 */
/* package */ class PortfolioGridViewport extends MainGridViewport {

  private static final int QUANTITY_COLUMN = 1;

  /**
   * @param gridStructure Row and column structure of the grid
   * @param callbackId ID that's passed to listeners when the grid structure changes
   * @param cache Cache of calculation results used to populate the viewport's data
   */
  /* package */ PortfolioGridViewport(ViewportDefinition viewportDefinition,
                                      MainGridStructure gridStructure,
                                      String callbackId,
                                      ResultsCache cache) {
    super(viewportDefinition, gridStructure, callbackId, cache);
  }

  @Override
     protected ViewportResults.Cell getFixedColumnResult(int rowIndex, int colIndex, MainGridStructure.Row row) {
    if (colIndex == LABEL_COLUMN) {
      return ViewportResults.stringCell(row.getName(), colIndex);
    } else if (colIndex == QUANTITY_COLUMN) { // TODO this only applies to the portfolio view
      return ViewportResults.valueCell(row.getQuantity(), null, Collections.emptyList(), colIndex);
    }
    throw new IllegalArgumentException("Column " + colIndex + " is not fixed");
  }
}
