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

/**
*
*/
/* package */ class AnalyticsColumnGroup {

  private final String _name;
  private final List<AnalyticsColumn> _columns;

  /* package */ AnalyticsColumnGroup(String name, List<AnalyticsColumn> columns) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(columns, "cols");
    _name = name;
    _columns = ImmutableList.copyOf(columns);
  }

  /* package */ String getName() {
    return _name;
  }

  /* package */ List<AnalyticsColumn> getColumns() {
    return _columns;
  }

  /* package */ int getColumnCount() {
    return _columns.size();
  }

  @Override
  public String toString() {
    return "AnalyticsColumnGroup [_name='" + _name + '\'' + ", _columns=" + _columns + "]";
  }
}

/* package */ class AnalyticsColumnGroups {

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

  /* package */ List<AnalyticsColumnGroup> getGroups() {
    return _columnGroups;
  }
}