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
 * Definition of a viewport on an grid displaying analytics data. A viewport represents the visible part of a grid.
 * A viewport is defined by collections of row and column indices of the visible cells. These are non-contiguous
 * ordered sets. Row indices can be non-contiguous if the grid rows have a tree structure and parts of the
 * structure are collapsed and therefore not visible. Column indices can be non-contiguous if there is a fixed
 * set of columns and the non-fixed columns have been scrolled.
 */
public class ViewportSpecification {

  /** Zero-based indices of the rows in the viewport. */
  private final List<Integer> _rows;
  /** Zero-based indices of the columns in the viewport. */
  private final SortedSet<Integer> _columnSet;
  /** Zero-based indices of the columns in the viewport. Same as {@link #_columnSet} but allows random access. */
  private final List<Integer> _columnList;
  /**
   * Whether the viewport's data should be displayed as a summary or in full. Summary data fits in a single
   * grid cell whereas the full data might need more space. e.g. displaying matrix data in a window that pops
   * up over the main grid.
   */
  private final boolean _expanded;

  /**
   * @param rows The rows visible in the viewport
   * @param columns The columns visible in the viewport
   * @param expanded Whether the viewport's data should be displayed as a summary or in full. Summary data fits in a
   * single grid cell whereas the full data might need more space. e.g. displaying matrix data in a window that pops
   * up over the main grid.
   */
  public ViewportSpecification(List<Integer> rows, List<Integer> columns, boolean expanded) {
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
    _columnSet = ImmutableSortedSet.copyOf(sortedColumns);
    _columnList = ImmutableList.copyOf(sortedColumns);
    _expanded = expanded;
  }

  /**
   * @return Zero-based indices of the rows that are visible in the viewport
   */
  /* package */ List<Integer> getRows() {
    return _rows;
  }

  /**
   * @return Zero-based indices of the columns that are visible in the viewport
   */
  /* package */ SortedSet<Integer> getColumns() {
    return _columnSet;
  }

  /**
   * Returns the index of a column in the grid given its index in the viewport's list of columns.
   * @param viewportColumnIndex Column index in the viewport
   * @return Column index in the underlying grid
   */
  /* package */ int getGridColumnIndex(int viewportColumnIndex) {
    return _columnList.get(viewportColumnIndex);
  }

  /**
   * Checks whether all cells specified by this object are within the bounds of the grid.
   * @param grid The structure of a grid
   * @return {@code true} if the viewport defined by this object fits within the grid.
   */
  /* package */ boolean isValidFor(GridStructure grid) {
    if (!_rows.isEmpty()) {
      int maxRow = _rows.get(_rows.size() - 1);
      if (maxRow >= grid.getRowCount()) {
        return false;
      }
    }
    if (!_columnSet.isEmpty()) {
      int maxCol = _columnSet.last();
      if (maxCol >= grid.getColumnCount()) {
        return false;
      }
    }
    return true;
  }

  /**
   * @return Whether the viewport's data should be displayed as a summary or in full. Summary data fits in a single
   * grid cell whereas the full data might need more space. e.g. displaying matrix data in a window that pops
   * up over the main grid.
   */
  /* package */ boolean isExpanded() {
    return _expanded;
  }

  @Override
  public String toString() {
    return "ViewportSpecification [" +
        "_rows=" + _rows +
        ", _columnSet=" + _columnSet +
        ", _columnList=" + _columnList +
        ", _expanded=" + _expanded +
        "]";
  }
}
