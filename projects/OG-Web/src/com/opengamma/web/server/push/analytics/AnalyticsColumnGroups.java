/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.util.ArgumentChecker;

public class AnalyticsColumnGroups {

  private final int _columnCount;
  private final List<AnalyticsColumnGroup> _columnGroups;

  /* package */ AnalyticsColumnGroups(List<AnalyticsColumnGroup> columnGroups) {
    ArgumentChecker.notNull(columnGroups, "columnGroups");
    ArgumentChecker.notNull(columnGroups, "columnGroups");
    int columnCount = 0;
    for (AnalyticsColumnGroup group : columnGroups) {
      columnCount += group.getColumnCount();
    }
    _columnCount = columnCount;
    _columnGroups = ImmutableList.copyOf(columnGroups);
  }

  /* package */
  static AnalyticsColumnGroups empty() {
    return new AnalyticsColumnGroups(Collections.<AnalyticsColumnGroup>emptyList());
  }

  /* package */ int getColumnCount() {
    return _columnCount;
  }

  public List<AnalyticsColumnGroup> getGroups() {
    return _columnGroups;
  }
}
