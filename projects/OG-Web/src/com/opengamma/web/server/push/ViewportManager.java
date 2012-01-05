/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.opengamma.DataNotFoundException;

/**
 * Creates and manages {@link Viewport} instances.
 */
public interface ViewportManager {

  /**
   * Creates a viewport and registers a listener that will be notified of any changes to its data or structure.
   * @param viewportId The viewport's ID, must be unique
   * @param previousViewportId ID of the previous viewport for this client.  Each client should only have one viewport
   * at a time.  This parameter allows the previous viewport to be cleaned up
   * @param viewportDefinition Defines the new viewport
   * @param listener Listener that will be notified of any changes to the viewport's data or structure
   * @return The new viewport
   */
  Viewport createViewport(String viewportId, String previousViewportId, ViewportDefinition viewportDefinition, AnalyticsListener listener);

  /**
   * Creates a viewport that doesn't notify client of changes.
   * TODO there is no timeout mechanism for viewports created this way, if they're not manually closed there is a leak
   * @param viewportId ID for the viewport
   * @param viewportDefinition Defines the new viewport
   * @return The viewport
   */
  Viewport createViewport(String viewportId, ViewportDefinition viewportDefinition);

  /**
   * Returns a viewport with the given ID
   * @param viewportId The viewport ID
   * @return The viewport with the specified ID
   * @throws DataNotFoundException If there is no viewport with the specified ID
   */
  Viewport getViewport(String viewportId);

  /**
   * Cleans up a viewport.
   * @param viewportId The viewport ID
   * @throws DataNotFoundException If there is no viewport with the specified ID
   */
  void closeViewport(String viewportId);
}
