/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.AggregatedViewDefinitionManager;

/**
 * Wrapper that hides a bit of the ugliness of {@link AggregatedViewDefinitionManager}.
 */
/* package */ final class AggregatedViewDefinition {

  private final AggregatedViewDefinitionManager _aggregatedViewDefManager;
  private final UniqueId _baseViewDefId;
  private final List<String> _aggregatorNames;
  private final UniqueId _id;

  /* package */ AggregatedViewDefinition(AggregatedViewDefinitionManager aggregatedViewDefManager, ViewRequest viewRequest) {
    ArgumentChecker.notNull(aggregatedViewDefManager, "aggregatedViewDefManager");
    ArgumentChecker.notNull(viewRequest, "viewRequest");
    _aggregatedViewDefManager = aggregatedViewDefManager;
    _baseViewDefId = viewRequest.getViewDefinitionId();
    _aggregatorNames = viewRequest.getAggregators();
    try {
      _id = _aggregatedViewDefManager.getViewDefinitionId(_baseViewDefId, _aggregatorNames);
    } catch (Exception e) {
      close();
      throw new OpenGammaRuntimeException("Failed to get aggregated view definition", e);
    }
  }

  /* package */ UniqueId getUniqueId() {
    return _id;
  }

  /* package */ void close() {
    _aggregatedViewDefManager.releaseViewDefinition(_baseViewDefId, _aggregatorNames);
  }
}
