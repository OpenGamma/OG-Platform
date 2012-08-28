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
   * @param gridId ID of the grid. This is the ID that was passed in when the grid or view was created
   */
  void gridStructureChanged(String gridId);

  /**
   * Notification that the row and column structure has changed for multiple grids
   * @param gridIds IDs of the grid. These are the IDs that were passed in when the grids or view were created
   */
  void gridStructureChanged(List<String> gridIds);

  /**
   * Notification that the data displayed in a grid has changed
   * @param dataId ID of the data. This is the ID that was passed in when the the viewport was created
   */
  void gridDataChanged(String dataId);

  /**
   * Notification that the data displayed in has changed in multiple grids
   * @param dataIds IDs of the data. These are the IDs that were passed in when the the viewports were created
   */
  void gridDataChanged(List<String> dataIds);
}
