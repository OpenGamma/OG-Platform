/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.opengamma.util.ArgumentChecker;

/**
 * TODO should there be subclasses for portfolio, depgraph, primitives? and associated visitors
 * TODO expanded cells that need full data (matrices, vectors, curves etc)
 */
public class ViewportSpecification {

  private final List<Integer> _rows;
  private final SortedSet<Integer> _columns;

  public ViewportSpecification(List<Integer> rows, List<Integer> columns) {
    ArgumentChecker.notNull(rows, "rows");
    ArgumentChecker.notNull(columns, "columns");
    SortedSet<Integer> sortedColumns = new TreeSet<Integer>(columns);
    List<Integer> sortedRows = new ArrayList<Integer>(rows);
    Collections.sort(sortedRows);
    if (!sortedRows.isEmpty()) {
      int minRow = sortedRows.get(0);
      if (minRow < 0) {
        throw new IllegalArgumentException("All row indices must be non-negative: " + rows);
      }
    }
    if (!sortedColumns.isEmpty()) {
      if (sortedColumns.first() < 0) {
        throw new IllegalArgumentException("All column indices must be non-negative: " + sortedColumns);
      }
    }
    _rows = ImmutableList.copyOf(sortedRows);
    _columns = ImmutableSortedSet.copyOf(sortedColumns);
  }

  public static ViewportSpecification empty() {
    return new ViewportSpecification(Collections.<Integer>emptyList(), Collections.<Integer>emptyList());
  }

  public List<Integer> getRows() {
    return _rows;
  }

  public SortedSet<Integer> getColumns() {
    return _columns;
  }

  /**
   * Checks whether all cells specified by this object are within the bounds of the grid.
   * @param grid The structure of a grid
   * @return {@code true} if the viewport defined by this object fits within the grid.
   */
  public boolean isValidFor(GridBounds grid) {
    if (!_rows.isEmpty()) {
      int maxRow = _rows.get(_rows.size() - 1);
      if (maxRow >= grid.getRowCount()) {
        return false;
      }
    }
    if (!_columns.isEmpty()) {
      int maxCol = _columns.last();
      if (maxCol >= grid.getColumnCount()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    return "ViewportSpecification [_rows=" + _rows + ", _columns=" + _columns + "]";
  }
}
