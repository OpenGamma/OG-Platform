/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.opengamma.util.ArgumentChecker;

/**
 * Viewport containing an arbitrary collection of cells.
 */
public class ArbitraryViewportDefinition extends ViewportDefinition {

  /** Cells in the viewport, not empty, sorted by column then row (i.e. along rows) */
  private final List<GridCell> _cells;

  /**
   * @param cells Cells in the viewport, not empty
   * @param expanded Whether the cell data should show all the data (true) or be formatted to fit in a single cell (false)
   */
  /* package */ ArbitraryViewportDefinition(List<GridCell> cells, boolean expanded) {
    super(expanded);
    ArgumentChecker.notEmpty(cells, "cells");
    _cells = new ArrayList<GridCell>(cells);
    Collections.sort(_cells);
  }

  @Override
  public Iterator<GridCell> iterator() {
    return _cells.iterator();
  }

  @Override
  public boolean isValidFor(GridStructure gridStructure) {
    for (GridCell cell : _cells) {
      if (cell.getRow() >= gridStructure.getRowCount() ||
          cell.getColumn() >= gridStructure.getColumnCount()) {
        return false;
      }
    }
    return true;
  }
}
