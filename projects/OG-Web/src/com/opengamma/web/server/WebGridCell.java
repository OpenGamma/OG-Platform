/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

/**
 * References a cell in a grid.
 */
public class WebGridCell {
  
  private final int _rowId;
  private final int _columnId;
  
  public WebGridCell(int rowId, int columnId) {
    _rowId = rowId;
    _columnId = columnId;
  }
  
  public static WebGridCell of(int rowId, int columnId) {
    return new WebGridCell(rowId, columnId);
  }
  
  public int getRowId() {
    return _rowId;
  }
  
  public int getColumnId() {
    return _columnId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _columnId;
    result = prime * result + _rowId;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof WebGridCell)) {
      return false;
    }
    WebGridCell other = (WebGridCell) obj;
    if (_columnId != other._columnId) {
      return false;
    }
    if (_rowId != other._rowId) {
      return false;
    }
    return true;
  }

}
