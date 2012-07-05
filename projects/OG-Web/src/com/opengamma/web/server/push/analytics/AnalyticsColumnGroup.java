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

