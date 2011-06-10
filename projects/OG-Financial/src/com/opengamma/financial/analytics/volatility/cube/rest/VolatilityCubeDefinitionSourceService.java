/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube.rest;

import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;

import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.util.rest.AbstractResourceService;


/**
 * RESTful backend for {@link RemoteVolatilityCubeDefinitionSource}.
 */
@Path("volatilityCubeDefinitionSource")
public class VolatilityCubeDefinitionSourceService extends AbstractResourceService<VolatilityCubeDefinitionSource, VolatilityCubeDefinitionSourceResource>  {

  public VolatilityCubeDefinitionSourceService(final FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  @Override
  protected VolatilityCubeDefinitionSourceResource createResource(VolatilityCubeDefinitionSource underlying) {
    return new VolatilityCubeDefinitionSourceResource(underlying, getFudgeContext());
  }
}
