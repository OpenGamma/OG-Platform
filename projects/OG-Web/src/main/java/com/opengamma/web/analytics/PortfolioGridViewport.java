/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.id.UniqueId;

/**
 *
 */
/* package */ class PortfolioGridViewport extends MainGridViewport {

  private static final int QUANTITY_COLUMN = 1;

  /**
   * @param gridStructure Row and column structure of the grid
   * @param callbackId ID that's passed to listeners when the grid structure changes
   */
  /* package */ PortfolioGridViewport(final MainGridStructure gridStructure,
                                      final String callbackId,
                                      final ViewportDefinition viewportDefinition,
                                      final ViewCycle cycle,
                                      final ResultsCache cache) {
    super(gridStructure, callbackId, viewportDefinition, cycle, cache);
  }

  @Override
  protected ViewportResults.Cell getFixedColumnResult(final int rowIndex, final int colIndex, final MainGridStructure.Row row) {
    if (colIndex == LABEL_COLUMN) {
      final ComputationTargetSpecification target = row.getTarget();
      final UniqueId targetId = target.getUniqueId();
      final ComputationTargetType targetType = target.getType();
      if (targetType.isTargetType(ComputationTargetType.POSITION)) {
        return ViewportResults.objectCell(new PositionTarget(row.getName(), targetId), colIndex);
      } else if (targetType.isTargetType(ComputationTargetType.PORTFOLIO_NODE)) {
        return ViewportResults.objectCell(new NodeTarget(row.getName(), targetId), colIndex);
      }
      throw new IllegalArgumentException("Unexpected target type for row: " + targetType);
    } else if (colIndex == QUANTITY_COLUMN) {
      return ViewportResults.valueCell(row.getQuantity(), null, Collections.emptyList(), null, colIndex);
    }
    throw new IllegalArgumentException("Column " + colIndex + " is not fixed");
  }
}
