/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.region.rest;

import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;

import com.opengamma.core.region.RegionSource;
import com.opengamma.util.rest.AbstractResourceService;

/**
 * RESTful backend for {@link RemoteRegionSource}.
 */
@Path("regionSource")
public class RegionSourceService extends AbstractResourceService<RegionSource, RegionSourceResource> {

  public RegionSourceService(final FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  @Override
  protected RegionSourceResource createResource(RegionSource underlying) {
    return new RegionSourceResource(underlying, getFudgeContext());
  }

}
