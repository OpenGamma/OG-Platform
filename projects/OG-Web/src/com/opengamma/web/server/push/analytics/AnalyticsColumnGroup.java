/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.util.ArgumentChecker;

/**
*
*/
public class AnalyticsColumnGroup {

  private final String _name;
  private final List<AnalyticsColumn<?>> _columns;

  /* package */ AnalyticsColumnGroup(String name, List<AnalyticsColumn<?>> columns) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(columns, "cols");
    _name = name;
    _columns = ImmutableList.copyOf(columns);
  }

  /* package */ List<AnalyticsColumn<?>> getColumns() {
    return _columns;
  }
}
