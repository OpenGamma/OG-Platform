/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

/**
 * Listener for changes in the analytics for a particular view.
 */
/* package */ public interface AnalyticsListener {

  /**
   * Invoked when the view's analytics data changes.
   */
  void dataChanged();

  /**
   * Invoked when the structure of one of the view's grids changes.
   */
  void gridStructureChanged();
}
