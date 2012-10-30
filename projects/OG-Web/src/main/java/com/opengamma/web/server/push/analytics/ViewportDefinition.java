/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Iterator;
import java.util.List;

import com.opengamma.util.ArgumentChecker;


/**
 * Definition of a viewport on an grid displaying analytics data. A viewport represents the visible part of a grid.
 */
public abstract class ViewportDefinition implements Iterable<GridCell> {

  /**
   * Whether the viewport's data should be displayed as a summary or in full. Summary data fits in a single
   * grid cell whereas the full data might need more space. e.g. displaying matrix data in a window that pops
   * up over the main grid.
   */
  private final boolean _expanded;

  protected ViewportDefinition(boolean expanded) {
    _expanded = expanded;
  }

  /**
   * @return An iterator over the grid cells in the viewport. Cells are grouped by row, i.e. the cells for a row
   * are all returned (in column order) before the cells in the next row and so on.
   */
  @Override
  public abstract Iterator<GridCell> iterator();

  /**
   * @param gridStructure The row and column structure of a grid
   * @return true if the grid contains all cells in this viewport
   */
  /* package */ abstract boolean isValidFor(GridStructure gridStructure);

  /**
   * @return Whether the viewport's data should be displayed as a summary or in full. Summary data fits in a single
   * grid cell whereas the full data might need more space. e.g. displaying matrix data in a window that pops
   * up over the main grid.
   */
  /* package */ boolean isExpanded() {
    return _expanded;
  }

  /**
   * Creates a viewport definition from row and column indices <em>or</em> a list of cells. If row and column indices
   * are specified they must both be non-empty and cells must be empty. If specifying cells the row and column indices
   * must be empty.
   * @param rows Indices of rows in the viewport. Must be non-empty if columns is non-empty. Must be empty if cells
   * is non-empty
   * @param columns Indices of columns in the viewport. Must be non-empty if columns is non-empty. Must be empty if
   * cells is non-empty
   * @param cells Cells in the viewport. Must be non-empty if rows and columns are empty. Must be empty if rows and
   * columns are non-empty.
   * @param expanded Whether the cell data should show all the data (true) or be formatted to fit in a single cell (false)
   * @return A new viewport definition
   */
  public static ViewportDefinition create(List<Integer> rows, List<Integer> columns, List<GridCell> cells, boolean expanded) {
    ArgumentChecker.notNull(cells, "cells");
    ArgumentChecker.notNull(rows, "rows");
    ArgumentChecker.notNull(columns, "columns");
    if (cells.size() != 0) {
      if (rows.size() != 0 || columns.size() != 0) {
        throw new IllegalArgumentException("rows and columns must be empty if cells are specified");
      }
      return new ArbitraryViewportDefinition(cells, expanded);
    } else {
      if (rows.size() == 0 || columns.size() == 0) {
        throw new IllegalArgumentException("rows and columns must not be empty if no cells are specified");
      }
      return new RectangularViewportDefinition(rows, columns, expanded);
    }
  }
}
