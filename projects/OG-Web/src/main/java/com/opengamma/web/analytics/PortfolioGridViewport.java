/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.UniqueId;

/**
 *
 */
/* package */ class PortfolioGridViewport extends MainGridViewport {

  // TODO this is a *temporary* hack
  private static final Pattern POSITION_ID_PATTERN = Pattern.compile("DbPrt-DbPos~\\d+-(\\d+)~\\d+-(\\d+)");
  private static final int QUANTITY_COLUMN = 1;

  /**
   * @param gridStructure Row and column structure of the grid
   * @param callbackId ID that's passed to listeners when the grid structure changes
   */
  /* package */ PortfolioGridViewport(final MainGridStructure gridStructure, final String callbackId) {
    super(gridStructure, callbackId);
  }

  @Override
     protected ViewportResults.Cell getFixedColumnResult(final int rowIndex, final int colIndex, final MainGridStructure.Row row) {
    if (colIndex == LABEL_COLUMN) {
      final ComputationTargetSpecification target = row.getTarget();
      final UniqueId compoundId = target.getUniqueId();
      // TODO this is a *temporary* hack
      final Matcher idMatcher = POSITION_ID_PATTERN.matcher(compoundId.toString());
      UniqueId targetId;
      if (idMatcher.matches()) {
        final String posId = idMatcher.group(1);
        final String version = idMatcher.group(2);
        targetId = UniqueId.of("DbPos", posId, version);
      } else {
        targetId = compoundId;
      }
      // TODO end hack
      final ComputationTargetType targetType = target.getType();
      if (targetType.isTargetType(ComputationTargetType.POSITION)) {
        return ViewportResults.positionCell(row.getName(), colIndex, targetId);
      } else if (targetType.isTargetType(ComputationTargetType.PORTFOLIO_NODE)) {
        return ViewportResults.nodeCell(row.getName(), colIndex, targetId);
      }
      throw new IllegalArgumentException("Unexpected target type for row: " + targetType);
    } else if (colIndex == QUANTITY_COLUMN) {
      return ViewportResults.valueCell(row.getQuantity(), null, Collections.emptyList(), colIndex);
    }
    throw new IllegalArgumentException("Column " + colIndex + " is not fixed");
  }
}
