/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.opengamma.web.server.push.reports.ViewportData;

import java.util.Map;

/**
 * A view onto a subset of the analytics data from a view client.
 */
public interface Viewport {

  /**
   * @return JSON representation of the structure of the view's grids
   * TODO this should probably return a proper class but that would require serious refactoring of the grid code
   */
  Map<String, Object> getGridStructure();

  /**
   * @return JSON containing the latest analytics data for the viewport's visible cells
   * TODO this should probably return a proper class but that would require serious refactoring of the grid code
   */
  Map<String, Object> getLatestResults();

  /**
   * Pauses or unpauses the view client associated with this viewport.
   * @param run {@code true} if the view client should run, {@code false} if it should pause
   */
  void setRunning(boolean run);

  /**
   * @return The raw data for all of this viewport's grids.
   */
  ViewportData getRawData();
}
