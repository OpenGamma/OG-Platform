/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.rest;

import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;

import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionSource;
import com.opengamma.util.rest.AbstractResourceService;

/**
 * RESTful backend for {@link RemoteInterpolatedYieldCurveDefinitionSource}.
 */
@Path("interpolatedYieldCurveDefinitionSource")
public class InterpolatedYieldCurveDefinitionSourceService extends AbstractResourceService<InterpolatedYieldCurveDefinitionSource, InterpolatedYieldCurveDefinitionSourceResource> {

  public InterpolatedYieldCurveDefinitionSourceService(final FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  @Override
  protected InterpolatedYieldCurveDefinitionSourceResource createResource(InterpolatedYieldCurveDefinitionSource underlying) {
    return new InterpolatedYieldCurveDefinitionSourceResource(underlying, getFudgeContext());
  }

}
