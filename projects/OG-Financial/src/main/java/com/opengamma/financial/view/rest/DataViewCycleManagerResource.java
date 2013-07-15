/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import com.opengamma.engine.resource.EngineResourceManager;
import com.opengamma.engine.resource.EngineResourceReference;
import com.opengamma.engine.view.cycle.ViewCycle;

/**
 * RESTful resource for {@link EngineResourceManager} on {@link ViewCycle}.
 */
public class DataViewCycleManagerResource extends DataEngineResourceManagerResource<ViewCycle> {

  protected DataViewCycleManagerResource(URI baseUri, EngineResourceManager<? extends ViewCycle> manager) {
    super(baseUri, manager);
  }
  
  @Override
  protected DataEngineResourceReferenceResource<ViewCycle> createReferenceResource(long referenceId, EngineResourceReference<? extends ViewCycle> reference) {
    return new DataViewCycleReferenceResource(this, referenceId, reference);
  }

}
