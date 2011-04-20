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
  
  private final long _rowId;
  private final long _columnId;
  
  public WebGridCell(long rowId, long columnId) {
    _rowId = rowId;
    _columnId = columnId;
  }
  
  public static WebGridCell of(long rowId, long columnId) {
    return new WebGridCell(rowId, columnId);
  }
  
  public long getRowId() {
    return _rowId;
  }
  
  public long getColumnId() {
    return _columnId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (_columnId ^ (_columnId >>> 32));
    result = prime * result + (int) (_rowId ^ (_rowId >>> 32));
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
