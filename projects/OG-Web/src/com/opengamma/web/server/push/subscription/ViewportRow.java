package com.opengamma.web.server.push.subscription;

/**
 *
 */
public class ViewportRow {

  private final int _rowId;
  private final long _timestamp;

  public ViewportRow(int rowId, long timestamp) {
    _rowId = rowId;
    _timestamp = timestamp;
  }

  public int getRowId() {
    return _rowId;
  }

  public long getTimestamp() {
    return _timestamp;
  }

  @Override
  public String toString() {
    return "ViewportRow{_rowId=" + _rowId + ", _timestamp=" + _timestamp + '}';
  }
}
