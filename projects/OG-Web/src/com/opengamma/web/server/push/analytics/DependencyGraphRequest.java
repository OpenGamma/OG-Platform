/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

/**
 *
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
