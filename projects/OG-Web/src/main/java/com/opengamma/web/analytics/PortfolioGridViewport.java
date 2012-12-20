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
import com.opengamma.engine.ComputationTargetType;
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
  /* package */ PortfolioGridViewport(MainGridStructure gridStructure, String callbackId) {
    super(gridStructure, callbackId);
  }

  @Override
     protected ViewportResults.Cell getFixedColumnResult(int rowIndex, int colIndex, MainGridStructure.Row row) {
    if (colIndex == LABEL_COLUMN) {
      ComputationTargetSpecification target = row.getTarget();
      UniqueId compoundId = target.getUniqueId();
      // TODO this is a *temporary* hack
      Matcher idMatcher = POSITION_ID_PATTERN.matcher(compoundId.toString());
      UniqueId targetId;
      if (idMatcher.matches()) {
        String posId = idMatcher.group(1);
        String version = idMatcher.group(2);
        targetId = UniqueId.of("DbPos", posId, version);
      } else {
        targetId = compoundId;
      }
      // TODO end hack
      ComputationTargetType targetType = target.getType();
      if (targetType == ComputationTargetType.POSITION) {
        return ViewportResults.positionCell(row.getName(), colIndex, targetId);
      } else if (targetType == ComputationTargetType.PORTFOLIO_NODE) {
        return ViewportResults.nodeCell(row.getName(), colIndex, targetId);
      }
      throw new IllegalArgumentException("Unexpected target type for row: " + targetType);
    } else if (colIndex == QUANTITY_COLUMN) {
      return ViewportResults.valueCell(row.getQuantity(), null, Collections.emptyList(), colIndex);
    }
    throw new IllegalArgumentException("Column " + colIndex + " is not fixed");
  }
}
