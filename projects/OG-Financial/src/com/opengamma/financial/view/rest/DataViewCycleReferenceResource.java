/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import com.opengamma.engine.view.calc.EngineResourceReference;
import com.opengamma.engine.view.calc.ViewCycle;

/**
 * RESTful resource for {@link EngineResourceManager<ViewCycle>}
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
