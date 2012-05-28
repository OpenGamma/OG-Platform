/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import javax.ws.rs.Path;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.push.analytics.AnalyticsView;

/**
 *
 */
public class ViewResource {

  private final AnalyticsView _view;

  public ViewResource(AnalyticsView view) {
    ArgumentChecker.notNull(view, "view");
    _view = view;
  }

  @Path("portfolio")
  public PortfolioGridResource getPortfolioGrid() {
    return new PortfolioGridResource(_view);
  }

  @Path("primitives")
  public PrimitivesGridResource getPrimitivesGrid() {
    return new PrimitivesGridResource(_view);
  }
}
