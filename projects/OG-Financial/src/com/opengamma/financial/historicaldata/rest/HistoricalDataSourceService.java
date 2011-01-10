/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.historicaldata.rest;

import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;

import com.opengamma.core.historicaldata.HistoricalDataSource;
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
