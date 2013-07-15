/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.formatting.TypeFormatter;

/**
 * Represents a cell in a grid displaying analytics data.
 * This class is also useful for specifying cell co-ordinates as parameters to JAX-RS HTTP methods.
 * The JAX-RS container can automatically create objects from form parameters if the
 * class has a constructor with a single string parameter.
 */
public class GridCell implements Comparable<GridCell> {

  /** Row index, not null. */
  private final int _row;
  /** Column index, not null. */
  private final int _column;
  /** The cell's format */
  private final TypeFormatter.Format _format;

  /**
   * Creates an instance from a packed text format.
   * 
   * @param cell  the row and column indices separated by a comma, for example "12,46", not null, indices must be non-negative
   */
  public GridCell(String cell) {
    // used by JAX-RS
    ArgumentChecker.notNull(cell, "cell");
    String[] rowCol = cell.split(",");
    if (rowCol.length < 3) {
      throw new IllegalArgumentException("Cell must be specified as 'row,col,\"format\"'");
    }
    _row = Integer.parseInt(rowCol[0].trim());
    _column = Integer.parseInt(rowCol[1].trim());
    _format = TypeFormatter.Format.valueOf(rowCol[2].trim());
    validate();
  }

  /**
   * Creates an instance.
   * 
   * @param row  the row index, not negative
   * @param column  the column index, not negative
   * @param format  the formatter to use, not null
   */
  public GridCell(int row, int column, TypeFormatter.Format format) {
    ArgumentChecker.notNull(format, "format");
    _row = row;
    _column = column;
    _format = format;
    validate();
  }

  private void validate() {
    if (_row < 0 || _column < 0) {
      throw new IllegalArgumentException("row and column must not be negative");
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the row.
   * 
   * @return the row index
   */
  /* package */ int getRow() {
    return _row;
  }

  /**
   * Gets the column.
   * 
   * @return the column index
   */
  /* package */ int getColumn() {
    return _column;
  }

  /**
   * Gets the formatter.
   * 
   * @return the format applied to the cell's data
   */
  /* package */ TypeFormatter.Format getFormat() {
    return _format;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GridCell cell = (GridCell) o;

    if (_column != cell._column) {
      return false;
    }
    if (_row != cell._row) {
      return false;
    }
    if (_format != cell._format) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = _row;
    result = 31 * result + _column;
    result = 31 * result + _format.hashCode();
    return result;
  }

  /**
   * A cell is greater than another cell if its row index is higher or it's row
   * index is the same and its column index is higher.
   * 
   * @param other  the other cell, not null
   * @return the comparison result
   */
  @Override
  public int compareTo(GridCell other) {
    int rowComp = Integer.compare(_row, other._row);
    if (rowComp != 0) {
      return rowComp;
    }
    int colComp = Integer.compare(_column, other._column);
    if (colComp != 0) {
      return colComp;
    }
    return _format.compareTo(other._format);
  }

  @Override
  public String toString() {
    return "GridCell [_row=" + _row + ", _column=" + _column + ", _format=" + _format + "]";
  }

}
