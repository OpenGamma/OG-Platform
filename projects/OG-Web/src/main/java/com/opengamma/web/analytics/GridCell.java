/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.util.ArgumentChecker;

/**
 * Represents a cell in a grid displaying analytics data. This class is also useful for specifying cell co-ordinates as
 * parameters to JAX-RS HTTP methods. The JAX-RS container can automatically create objects from form parameters if the
 * class has a constructor with a single string parameter.
 */
public class GridCell implements Comparable<GridCell> {

  /** Row index, not null. */
  private final Integer _row;
  /** Column index, not null. */
  private final Integer _column;

  /**
   * @param cell The row and column indices separated by a comma, e.g. "12,46", not null, indices must be non-negative
   */
  public GridCell(String cell) {
    ArgumentChecker.notNull(cell, "cell");
    String[] rowCol = cell.split(",");
    if (rowCol.length != 2) {
      throw new IllegalArgumentException("Cell must be specified as 'row,col'");
    }
    _row = Integer.parseInt(rowCol[0].trim());
    _column = Integer.parseInt(rowCol[1].trim());
    validate();
  }

  /**
   * @param row Row index, not negative
   * @param column Column index, not negative
   */
  public GridCell(int row, int column) {
    _row = row;
    _column = column;
    validate();
  }

  private void validate() {
    if (_row < 0 || _column < 0) {
      throw new IllegalArgumentException("row and column must not be negative");
    }
  }

  /**
   * @return The row index
   */
  /* package */ int getRow() {
    return _row;
  }

  /**
   * @return The column index
   */
  /* package */ int getColumn() {
    return _column;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GridCell gridCell = (GridCell) o;

    if (!_column.equals(gridCell._column)) {
      return false;
    }
    if (!_row.equals(gridCell._row)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = _row;
    result = 31 * result + _column;
    return result;
  }

  /**
   * A cell is greater than another cell if its row index is higher or it's row index is the same and its column index
   * is higher.
   */
  @Override
  public int compareTo(GridCell other) {
    int rowComp = _row.compareTo(other._row);
    if (rowComp != 0) {
      return rowComp;
    }
    return _column.compareTo(other._column);
  }

  @Override
  public String toString() {
    return "GridCell [_row=" + _row + ", _column=" + _column + "]";
  }
}
