/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class LabelRenderer implements AnalyticsColumn.CellRenderer {

  private static final Pattern POSITION_ID_PATTERN = Pattern.compile("DbPrt-DbPos~\\d+-(\\d+)~\\d+-(\\d+)");

  private final List<MainGridStructure.Row> _rows;

  /* package */ LabelRenderer(List<MainGridStructure.Row> rows) {
    ArgumentChecker.notNull(rows, "rows");
    _rows = rows;
  }

  @Override
  public ResultsCell getResults(int rowIndex, ResultsCache cache, Class<?> type) {
    MainGridStructure.Row row = _rows.get(rowIndex);
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
    if (targetType.isTargetType(ComputationTargetType.POSITION)) {
      return ViewportResults.objectCell(new PositionTarget(row.getName(), targetId), type);
    } else if (targetType.isTargetType(ComputationTargetType.PORTFOLIO_NODE)) {
      return ViewportResults.objectCell(new NodeTarget(row.getName(), targetId), type);
    }
    throw new IllegalArgumentException("Unexpected target type for row: " + targetType);
  }
}
