/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.rest;

import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;

import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionMaster;
import com.opengamma.util.rest.AbstractResourceService;

/**
 * RESTful backend for {@link RemoteInterpolatedYieldCurveDefinitionMaster}.
 */
@Path("interpolatedYieldCurveDefinitionMaster")
public class InterpolatedYieldCurveDefinitionMasterService extends AbstractResourceService<InterpolatedYieldCurveDefinitionMaster, InterpolatedYieldCurveDefinitionMasterResource> {

  public InterpolatedYieldCurveDefinitionMasterService(final FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  @Override
  protected InterpolatedYieldCurveDefinitionMasterResource createResource(InterpolatedYieldCurveDefinitionMaster underlying) {
    return new InterpolatedYieldCurveDefinitionMasterResource(underlying, getFudgeContext());
  }

}
