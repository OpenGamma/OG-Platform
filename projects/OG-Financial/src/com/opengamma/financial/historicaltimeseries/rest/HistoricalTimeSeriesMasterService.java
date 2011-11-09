/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.historicaltimeseries.rest;

import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;

import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.util.rest.AbstractResourceService;

/**
 * RESTful backend for {@link RemoteHistoricalTimeSeriesMaster}.
 */
@Path("historicalTimeSeriesMaster")
public class HistoricalTimeSeriesMasterService extends AbstractResourceService<HistoricalTimeSeriesMaster, HistoricalTimeSeriesMasterResource> {

  public HistoricalTimeSeriesMasterService(FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  @Override
  protected HistoricalTimeSeriesMasterResource createResource(HistoricalTimeSeriesMaster underlying) {
    return new HistoricalTimeSeriesMasterResource(getFudgeContext(), underlying);
  }

}
