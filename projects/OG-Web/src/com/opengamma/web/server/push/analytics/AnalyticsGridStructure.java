/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

/**
 *
 */
public class AnalyticsGridStructure {

  private final AnalyticsRows _rows;
  private final AnalyticsColumns _columns;

  public AnalyticsGridStructure(AnalyticsRows rows, AnalyticsColumns columns) {
    _rows = rows;
    _columns = columns;
  }

  public AnalyticsRows getRows() {
    return _rows;
  }

  public AnalyticsColumns getColumns() {
    return _columns;
  }
}
