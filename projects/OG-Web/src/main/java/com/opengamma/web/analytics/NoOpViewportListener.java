/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

/**
 * {@link ViewportListener} implementation that does nothing.
 */
/* package */ class NoOpViewportListener implements ViewportListener {

  @Override
  public void viewportCreated(ViewportDefinition viewportDef, GridStructure gridStructure) {
    // do nothing
  }

  @Override
  public void viewportUpdated(ViewportDefinition currentDef, ViewportDefinition newDef, GridStructure gridStructure) {
    // do nothing
  }

  @Override
  public void viewportDeleted(ViewportDefinition viewportDef, GridStructure gridStructure) {
    // do nothing
  }
}
