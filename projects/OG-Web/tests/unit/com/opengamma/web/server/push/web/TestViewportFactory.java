package com.opengamma.web.server.push.web;

import com.opengamma.web.server.push.subscription.AnalyticsListener;
import com.opengamma.web.server.push.subscription.Viewport;
import com.opengamma.web.server.push.subscription.ViewportDefinition;
import com.opengamma.web.server.push.subscription.ViewportFactory;

/**
 *
 */
public class TestViewportFactory implements ViewportFactory {

  @Override
  public Viewport createViewport(String clientId, ViewportDefinition viewportDefinition, AnalyticsListener listener) {
    throw new UnsupportedOperationException("createViewport not implemented");
  }
}
