/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.reports;

/**
 * Contains the raw data for a portfolio or primitives grid.
 */
public class GridData {

  private final String[][] _headers;
  private final String[][] _rows;

  public GridData(String[][] rows, String[][] headers) {
    this._rows = rows;
    this._headers = headers;
  }

  /**
   * @return An empty set of data
   */
  public static GridData empty() {
    return new GridData(new String[][]{}, new String[][]{});
  }

  // TODO why is this a 2D array?
  public String[][] getHeaders() {
    return _headers;
  }

  public String[][] getRows() {
    return _rows;
  }
}
