/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.reports;

import com.opengamma.web.server.WebGridCell;

/**
 * Contains a depdency graph grid's raw data.  <em>Not currenly used as dependency graphs have no support
 * for raw data</em>
 */
public class DependencyGraphGridData extends GridData {

  /** The cell in the parent grid that owns the dependency graph */
  private final WebGridCell _cell;
  
  public DependencyGraphGridData(String[][] headers, String[][] rows, WebGridCell cell) {
    super(rows, headers);
    _cell = cell;
  }

  /**
   * @return The cell in the parent grid that owns the dependency graph
   */
  public WebGridCell getCell() {
    return _cell;
  }
}
