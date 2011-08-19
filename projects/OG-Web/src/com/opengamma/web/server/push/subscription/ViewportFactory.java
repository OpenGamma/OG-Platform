/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

import com.opengamma.id.UniqueId;

/**
 * provides data for a viewport onto a grid of analytics
 * exists to break the link between the web push code and the view client so the web code can be tested without an engine
 * TODO rename ViewportManager and have a bit more functionality? map clientIds to viewports and clean up when clients disconnect?
 */
public interface ViewportFactory {

  Viewport createViewport(String clientId, UniqueId viewClientId, ViewportDefinition viewportDefinition, AnalyticsListener listener);
}
