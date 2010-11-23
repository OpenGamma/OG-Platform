/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.historicaldata.rest;

import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;

import com.opengamma.engine.historicaldata.HistoricalDataSource;
import com.opengamma.util.rest.AbstractResourceService;

/**
 * RESTful backend for {@link RemoteHistoricalDataSource}.
 */
@Path("historicalDataSource")
public class HistoricalDataSourceService extends AbstractResourceService<HistoricalDataSource, HistoricalDataSourceResource> {

  public HistoricalDataSourceService(FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  @Override
  protected HistoricalDataSourceResource createResource(HistoricalDataSource underlying) {
    return new HistoricalDataSourceResource(getFudgeContext(), underlying);
  }

}
