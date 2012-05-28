/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import com.opengamma.web.server.push.analytics.AnalyticsResults;
import com.opengamma.web.server.push.analytics.AnalyticsView;
import com.opengamma.web.server.push.analytics.ViewportRequest;

/**
 *
 */
public class PortfolioViewportResource extends AbstractViewportResource {

  public PortfolioViewportResource(AnalyticsView view, String viewportId) {
    super(view, viewportId);
  }

  @Override
  public void update(ViewportRequest viewportRequest) {
    _view.updatePortfolioViewport(_viewportId, viewportRequest);
  }

  @Override
  public void delete(String viewportId) {
    _view.deletePortfolioViewport(viewportId);
  }

  @Override
  public AnalyticsResults getData() {
    return _view.getPortfolioData(_viewportId);
  }
}
