/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.util.ArgumentChecker;

public class AnalyticsColumnGroups {

  private final List<AnalyticsColumn> _columns = Lists.newArrayList();
  private final List<AnalyticsColumnGroup> _columnGroups;

  /* package */ AnalyticsColumnGroups(List<AnalyticsColumnGroup> columnGroups) {
    ArgumentChecker.notNull(columnGroups, "columnGroups");
    ArgumentChecker.notNull(columnGroups, "columnGroups");
    for (AnalyticsColumnGroup group : columnGroups) {
      _columns.addAll(group.getColumns());
    }
    _columnGroups = ImmutableList.copyOf(columnGroups);
  }

  /* package */
  static AnalyticsColumnGroups empty() {
    return new AnalyticsColumnGroups(Collections.<AnalyticsColumnGroup>emptyList());
  }

  /* package */ int getColumnCount() {
    return _columns.size();
  }

  /* package */ AnalyticsColumn getColumn(int index) {
    return _columns.get(index);
  }

  public List<AnalyticsColumnGroup> getGroups() {
    return _columnGroups;
  }
}
