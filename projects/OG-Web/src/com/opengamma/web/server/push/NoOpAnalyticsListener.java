/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

/**
 * {@link AnalyticsListener} that does nothing when its methods are called.  This is useful when an object
 * publishes notifications that we're not interested in.  The alternative would be for the object that
 * publishes the notifications to do null checks every time is uses a listener.
 */
public class NoOpAnalyticsListener implements AnalyticsListener {

  /**
   * Does nothing.
   */
  @Override
  public void dataChanged() {
    // do nothing
  }

  /**
   * Does nothing.
   */
  @Override
  public void gridStructureChanged() {
    // do nothing
  }
}
