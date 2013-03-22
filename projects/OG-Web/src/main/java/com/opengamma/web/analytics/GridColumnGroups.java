/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.util.ArgumentChecker;

/**
 * Collection of {@link GridColumnGroup}s that make up the columns in a grid.
 */
public class GridColumnGroups {

  /** The columns from all the groups. */
  private final List<GridColumn> _columns;
  /** The column groups. */
  private final List<GridColumnGroup> _columnGroups;

  /* package */ GridColumnGroups(List<GridColumnGroup> columnGroups) {
    ArgumentChecker.notNull(columnGroups, "columnGroups");
    List<GridColumn> columns = Lists.newArrayList();
    for (GridColumnGroup group : columnGroups) {
      columns.addAll(group.getColumns());
    }
    _columns = Collections.unmodifiableList(columns);
    _columnGroups = ImmutableList.copyOf(columnGroups);
  }

  /* package */ GridColumnGroups(GridColumnGroup columnGroup) {
    this(Lists.newArrayList(columnGroup));
  }

  /**
   * @return A instance containing no column groups
   */
  /* package */ static GridColumnGroups empty() {
    return new GridColumnGroups(Collections.<GridColumnGroup>emptyList());
  }

  /**
   * @return Total number of columns in all column groups
   */
  public int getColumnCount() {
    return _columns.size();
  }

  /**
   * Returns the column at an index
   * @param index The column index, zero based
   * @return The column at the specified index
   */
  public GridColumn getColumn(int index) {
    return _columns.get(index);
  }

  /**
   * @return The column groups in the order they should be displayed
   */
  public List<GridColumnGroup> getGroups() {
    return _columnGroups;
  }

  /* package */ List<GridColumn> getColumns() {
    return _columns;
  }

  @Override
  public String toString() {
    return "AnalyticsColumnGroups [_columns=" + _columns + ", _columnGroups=" + _columnGroups + "]";
  }
}
