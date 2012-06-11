/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

/**
 * TODO this isn't a reliable way to specify a cell. there's a race condition because the grid structure can change
 * on the server and the cell the client specifies no longer corresponds to the cell the user selected. it would
 * be better to specify the target spec? or a stable row ID that's generated on the server
 */
public class DependencyGraphRequest {

  private final int _row;
  private final int _column;

  public DependencyGraphRequest(int row, int column) {
    _row = row;
    _column = column;
  }

  public int getRow() {
    return _row;
  }

  public int getColumn() {
    return _column;
  }
}
