/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import com.opengamma.engine.view.calc.EngineResourceManager;
import com.opengamma.engine.view.calc.EngineResourceReference;
import com.opengamma.engine.view.calc.ViewCycle;

/**
 * RESTful resource for {@link EngineResourceManager<ViewCycle>}
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
