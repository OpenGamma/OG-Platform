/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.List;

/**
 * Listener that receives notifications when the data or structure changes in an analytics grid.
 */
public interface AnalyticsViewListener {

  /**
   * Notification that the row and column structure of a grid has changed
   * @param callbackId The ID that was passed in when the grid or view was created
   */
  void gridStructureChanged(String callbackId);

  /**
   * Notification that the row and column structure has changed for multiple grids
   * @param callbackIds IDs that were passed in when the grids or view were created
   */
  void gridStructureChanged(List<String> callbackIds);

  /**
   * Notification that the data displayed in a grid has changed
   * @param callbackId The ID that was passed in when the the viewport was created
   */
  void gridDataChanged(String callbackId);

  /**
   * Notification that the data displayed in has changed in multiple grids
   * @param callbackIds IDs that were passed in when the the viewports were created
   */
  void gridDataChanged(List<String> callbackIds);
}
