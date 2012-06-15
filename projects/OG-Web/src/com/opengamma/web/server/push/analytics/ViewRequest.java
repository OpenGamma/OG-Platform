/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ViewRequest {

  private final UniqueId _viewDefinitionId;
  /*
  viewDefId
  aggregatorNames
  */

  public ViewRequest(UniqueId viewDefinitionId) {
    ArgumentChecker.notNull(viewDefinitionId, "viewDefinitionId");
    _viewDefinitionId = viewDefinitionId;
  }

  public UniqueId getViewDefinitionId() {
    return _viewDefinitionId;
  }
}
