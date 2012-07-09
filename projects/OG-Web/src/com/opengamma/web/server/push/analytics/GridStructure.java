/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

/**
 *
 */
public interface GridStructure {

  int getRowCount();

  int getColumnCount();

  AnalyticsColumnGroups getColumnStructure();
}
