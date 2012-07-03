/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class AnalyticsGridStructure {

  private final int _columnCount;
  private final List<AnalyticsColumnGroup> _columnGroups;
  private final AnalyticsNode _root;
  // TODO persistent row IDs that can be tracked when the structure changes (for dynamic reaggregation)

  /* package */ AnalyticsGridStructure(AnalyticsNode root, List<AnalyticsColumnGroup> columnGroups) {
    ArgumentChecker.notNull(root, "root");
    ArgumentChecker.notNull(columnGroups, "columnGroups");
    _root = root;
    int columnCount = 0;
    for (AnalyticsColumnGroup group : columnGroups) {
      columnCount += group.getColumnCount();
    }
    _columnCount = columnCount;
    _columnGroups = ImmutableList.copyOf(columnGroups);
  }

  public AnalyticsNode getRoot() {
    return _root;
  }

  public int getColumnCount() {
    return _columnCount;
  }

  public List<AnalyticsColumnGroup> getColumnGroups() {
    return _columnGroups;
  }

  @Override
  public String toString() {
    return "AnalyticsGridStructure [" +
        "_columnCount=" + _columnCount +
        ", _columnGroups=" + _columnGroups +
        ", _root=" + _root +
        "]";
  }
}
