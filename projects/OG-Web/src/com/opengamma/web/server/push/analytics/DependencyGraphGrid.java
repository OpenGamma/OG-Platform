/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

/**
 *
 */
public class DependencyGraphGrid extends AnalyticsGrid {

  protected DependencyGraphGrid(AnalyticsGridStructure gridStructure, String gridId) {
    super(gridStructure, gridId);
  }

  @Override
  protected AnalyticsViewport createViewport(AnalyticsGridStructure gridStructure,
                                             ViewportSpecification viewportSpecification,
                                             AnalyticsHistory history,
                                             String dataId) {
    // TODO implement createViewport()
    throw new UnsupportedOperationException("createViewport not implemented");
  }
}
