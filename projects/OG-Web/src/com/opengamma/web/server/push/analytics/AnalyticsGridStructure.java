/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

/**
 * TODO something needs to generate a compact representation of the grid's tree structure. this class? the message body writer?
 */
public class AnalyticsGridStructure {

  private final AnalyticsNode _root;
  private final AnalyticsColumns _columns;

  public AnalyticsGridStructure(AnalyticsNode root, AnalyticsColumns columns) {
    _root = root;
    _columns = columns;
  }

  public AnalyticsNode getRoot() {
    return _root;
  }

  public AnalyticsColumns getColumns() {
    return _columns;
  }
}
