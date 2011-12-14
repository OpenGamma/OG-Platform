/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.opengamma.web.server.conversion.ConversionMode;

import java.util.Map;

/**
 * A view onto a subset of the analytics data from a view client.
 */
public interface Viewport {

  /**
   * @return JSON representation of the structure of the view's grids
   * TODO this should probably be a proper class but that would require serious refactoring of the web code
   */
  Map<String, Object> getGridStructure();

  /**
   * @return JSON containing the latest analytics data for the viewport's visible cells
   * TODO this should probably be a proper class but that would require serious refactoring of the web code
   */
  Map<String, Object> getLatestResults();

  /**
   * Pauses or unpauses the view associated with this viewport.
   * @param run {@code true} if the view should run, {@code false} if it should pause
   */
  void setRunning(boolean run);

  /**
   * Switches the view between producing full or summary data.
   * @param mode The new mode
   */
  void setConversionMode(ConversionMode mode);
}
