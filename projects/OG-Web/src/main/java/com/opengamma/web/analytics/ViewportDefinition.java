/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.analytics.formatting.TypeFormatter;


/**
 * Definition of a viewport on an grid displaying analytics data. A viewport represents the visible part of a grid.
 */
public abstract class ViewportDefinition implements Iterable<GridCell> {
  
  private final int _version;
  private final boolean _enableLogging;

  protected ViewportDefinition(int version, boolean enableLogging) {
    _version = version;
    _enableLogging = enableLogging;
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
   * Creates a viewport definition from row and column indices <em>or</em> a list of cells. If row and column indices
   * are specified they must both be non-empty and cells must be empty. If specifying cells the row and column indices
   * must be empty.
   * 
   * @param version  the version
   * @param rows Indices of rows in the viewport. Must be non-empty if columns is non-empty. Must be empty if cells
   * is non-empty
   * @param columns Indices of columns in the viewport. Must be non-empty if columns is non-empty. Must be empty if
   * cells is non-empty
   * @param cells Cells in the viewport. Must be non-empty if rows and columns are empty. Must be empty if rows and
   * columns are non-empty.
   * @param format The format for the viewport's data
   * @param enableLogging Whether full logging info should be collected for the viewport's cells
   * @return A new viewport definition
   */
  public static ViewportDefinition create(int version,
                                          List<Integer> rows,
                                          List<Integer> columns,
                                          List<GridCell> cells,
                                          TypeFormatter.Format format,
                                          Boolean enableLogging) {
    ArgumentChecker.notNull(cells, "cells");
    ArgumentChecker.notNull(rows, "rows");
    ArgumentChecker.notNull(columns, "columns");
    boolean logging = enableLogging == null ? false : enableLogging;
    if (cells.size() != 0) {
      if (rows.size() != 0 || columns.size() != 0) {
        throw new IllegalArgumentException("rows and columns must be empty if cells are specified");
      }
      return new ArbitraryViewportDefinition(version, cells, logging);
    } else {
      if (rows.size() == 0 || columns.size() == 0) {
        throw new IllegalArgumentException("rows and columns must not be empty if no cells are specified");
      }
      return new RectangularViewportDefinition(version, rows, columns, format, logging);
    }
  }

  public static ViewportDefinition createEmpty(int version) {
    List<Integer> emptyList = Collections.emptyList();
    return new RectangularViewportDefinition(version, emptyList, emptyList, TypeFormatter.Format.CELL, false);
  }

  /* package */ int getVersion() {
    return _version;
  }

  /* package */ boolean enableLogging() {
    return _enableLogging;
  }

  /* package */ abstract Pair<Integer, Boolean> getChangedNode(ViewportDefinition viewportDefinition);
}
