/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import com.opengamma.engine.view.calc.EngineResourceManager;
import com.opengamma.engine.view.calc.EngineResourceReference;
import com.opengamma.engine.view.calc.ViewCycle;

/**
 * RESTful resource for {@link EngineResourceManager} on {@link ViewCycle}.
 */
public class DataViewCycleReferenceResource extends DataEngineResourceReferenceResource<ViewCycle> {

  protected DataViewCycleReferenceResource(DataEngineResourceManagerResource<ViewCycle> manager, long referenceId, EngineResourceReference<? extends ViewCycle> resourceReference) {
    super(manager, referenceId, resourceReference);
  }

  @Override
  protected Object getResourceResource(ViewCycle viewCycle) {
    return new DataViewCycleResource(viewCycle);
  }

}
