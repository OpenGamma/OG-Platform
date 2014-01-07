/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;
import com.opengamma.web.analytics.formatting.TypeFormatter;

/**
 * Represents a rectangular set of cells visible in a grid. The viewport is defined by collections of row and
 * column indices of the visible cells. These are non-contiguous ordered sets. Row indices can be non-contiguous if
 * the grid rows have a tree structure and parts of the structure are collapsed and therefore not visible. Column
 * indices can be non-contiguous if columns are hidden or there is a fixed set of columns and the non-fixed columns
 * have been scrolled.
 */
public class RectangularViewportDefinition extends ViewportDefinition {

  private static final Logger s_logger = LoggerFactory.getLogger(RectangularViewportDefinition.class);

  /** Indices of rows in the viewport, not empty, sorted in ascending order. */
  private final List<Integer> _rows;
  /** Indices of columns in the viewport, not empty, sorted in ascending order. */
  private final List<Integer> _columns;
  /** Format of all cells in the viewport */
  private final TypeFormatter.Format _format;

  /**
   * @param version
   * @param rows Indices of rows in the viewport, not empty
   * @param columns Indices of columns in the viewport, not empty
   * @param format
   * @param enableLogging
   */
  /* package */ RectangularViewportDefinition(int version,
                                              List<Integer> rows,
                                              List<Integer> columns,
                                              TypeFormatter.Format format,
                                              Boolean enableLogging) {
    super(version, enableLogging);
    ArgumentChecker.notNull(format, "format");
    _format = format;
    // TODO bounds checking
    _rows = ImmutableList.copyOf(rows);
    _columns = ImmutableList.copyOf(columns);
  }

  @Override
  public Iterator<GridCell> iterator() {
    return new CellIterator();
  }

  @Override
  public boolean isValidFor(GridStructure grid) {
    if (!_rows.isEmpty()) {
      int maxRow = _rows.get(_rows.size() - 1);
      if (maxRow >= grid.getRowCount()) {
        return false;
      }
    }
    if (!_columns.isEmpty()) {
      int maxCol = _columns.get(_columns.size() - 1);
      if (maxCol >= grid.getColumnCount()) {
        return false;
      }
    }
    return true;
  }

  // TODO this doesn't work properly
  // scrolling up triggers a node collapse
  // scrolling down or expanding the viewport down triggers a node expansion
  @Override
  Pair<Integer, Boolean> getChangedNode(ViewportDefinition viewportDefinition) {
    // Viewport definitions other than RectangularViewportDefinitions do not have changed nodes so just return null
    if (!(viewportDefinition instanceof RectangularViewportDefinition)) {
      return null;
    }
    List<Integer> newRows = ((RectangularViewportDefinition) viewportDefinition).getRows();

    //if the first rows aren't equal the user has scrolled, or if there are no rows, return null
    // TODO this logic doesn't cover these cases:
    //   * the user expands the viewport downwards by resizing the window
    //   * the user scrolls the viewport down but the previous top row is still included in the off-screen buffer zone
    if (_rows.isEmpty() || newRows.isEmpty() || (!_rows.get(0).equals(newRows.get(0)))) {
      return null;
    }
    // if the first rows are equal and the viewport has changed then the user has either expanded or collapsed a node
    // walk through the current and new list of rows
    for (int i = 0; i < Math.max(_rows.size(), newRows.size()); i++) {
      //if final node is expanded/collapsed then index will not be in list
      if (i >= _rows.size()) {
        // TODO this gives false positives when expanding the viewport down by resizing the window
        // top row in both viewports is the same because there's no scrolling but new rows are being added without
        // any nodes being expanded
        s_logger.debug("return #1");
        return Pairs.of(_rows.get(i - 1), true);
      }
      if (i >= newRows.size()) {
        // TODO this gives false positives scrolling slowly up to the top into the buffer zone
        // top row in both viewports is the same because of the extra hidden rows
        s_logger.debug("return #2");
        return Pairs.of(newRows.get(i - 1), false);
      }
      // if this object's row index is greater then the node has collapsed
      // the the other object's row index is greater then the node has expanded
      // the expanded / collapsed node is the row before the unequal row
      int oldRow = _rows.get(i);
      int newRow = newRows.get(i);
      if (oldRow == newRow) {
        continue;
      }
      if (newRow < oldRow) {
        s_logger.debug("return #3");
        return Pairs.of(newRows.get(i - 1), true);
      } else if (oldRow < newRow) {
        s_logger.debug("return #4");
        return Pairs.of(_rows.get(i - 1), false);
      }
    }
    // or resized the window - if the window has resized the row lists will be different lengths
    return null;
  }

  /* package */ List<Integer> getColumns() {
    return _columns;
  }

  /* package */ List<Integer> getRows() {
    return _rows;
  }

  /* package */ TypeFormatter.Format getFormat() {
    return _format;
  }

  @Override
  public String toString() {
    return "RectangularViewportDefinition [_rows=" + _rows + ", _columns=" + _columns + "]";
  }

  /**
   * Iterator that returns the viewports cells by traversing rows followed by columns.
   */
  private final class CellIterator implements Iterator<GridCell> {

    private final Iterator<Integer> _rowIterator = _rows.iterator();

    private Iterator<Integer> _colIterator;
    private int _rowIndex;

    private CellIterator() {
      initRow();
    }

    private void initRow() {
      if (_rowIterator.hasNext()) {
        _rowIndex = _rowIterator.next();
      }
      _colIterator = _columns.iterator();
    }

    @Override
    public boolean hasNext() {
      return _colIterator.hasNext() || _rowIterator.hasNext();
    }

    @Override
    public GridCell next() {
      if (!_colIterator.hasNext()) {
        initRow();
      }
      return new GridCell(_rowIndex, _colIterator.next(), getFormat());
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove not supported");
    }
  }
}
