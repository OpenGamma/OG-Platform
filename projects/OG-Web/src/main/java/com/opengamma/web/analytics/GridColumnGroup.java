/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.util.ArgumentChecker;

/**
 * Named group of columns in a grid displaying analytics data.
 */
public class GridColumnGroup {

  /** Name of the group. */
  private final String _name;
  /** The columns in the group. */
  private final List<GridColumn> _columns;
  /** Whether the values in this group's cells were calculated by the engine and have dependency graphs available. */
  private final boolean  _dependencyGraphsAvailable;

  /**
   * @param name The name of the group
   * @param columns The columns in the group
   * @param dependencyGraphsAvailable Whether the values in this group's cells were calculated by the engine and
   * have dependency graphs available
   */
  /* package */ GridColumnGroup(String name, List<GridColumn> columns, boolean dependencyGraphsAvailable) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(columns, "cols");
    _dependencyGraphsAvailable = dependencyGraphsAvailable;
    _name = name;
    _columns = ImmutableList.copyOf(columns);
  }

  /**
   * @return The name of the group
   */
  public String getName() {
    return _name;
  }

  /**
   * @return The columns in the group
   */
  public List<GridColumn> getColumns() {
    return _columns;
  }

  /**
   * @return Whether the values in this group's cells were calculated by the engine and have dependency graphs available
   */
  public boolean isDependencyGraphsAvailable() {
    return _dependencyGraphsAvailable;
  }

  @Override
  public String toString() {
    return "AnalyticsColumnGroup [_name='" + _name + '\'' + ", _columns=" + _columns + "]";
  }

  /* package */ static GridColumnGroup empty() {
    return new GridColumnGroup("", Collections.<GridColumn>emptyList(), false);
  }
}

