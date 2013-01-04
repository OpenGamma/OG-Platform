/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

/**
 * Interface for classes that need to know when viewports are created, modified and deleted.
 */
/* package */ interface ViewportListener {

  void viewportCreated(ViewportDefinition viewportDef, GridStructure gridStructure);

  void viewportUpdated(ViewportDefinition currentDef, ViewportDefinition newDef, GridStructure gridStructure);

  void viewportDeleted(ViewportDefinition viewportDef, GridStructure gridStructure);
}
