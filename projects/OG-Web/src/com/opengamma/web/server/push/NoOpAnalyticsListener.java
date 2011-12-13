/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

/**
 *
 */
public class NoOpAnalyticsListener implements AnalyticsListener {

  @Override
  public void dataChanged() {
    // do nothing
  }

  @Override
  public void gridStructureChanged() {
    // do nothing
  }
}
