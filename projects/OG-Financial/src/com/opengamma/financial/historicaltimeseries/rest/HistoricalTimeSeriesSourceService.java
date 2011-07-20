/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.historicaltimeseries.rest;

import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.util.rest.AbstractResourceService;

/**
 * RESTful backend for {@link RemoteHistoricalTimeSeriesSource}.
 */
@Path("historicalTimeSeriesSource")
public class HistoricalTimeSeriesSourceService extends AbstractResourceService<HistoricalTimeSeriesSource, HistoricalTimeSeriesSourceResource> {

  public HistoricalTimeSeriesSourceService(FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  @Override
  protected HistoricalTimeSeriesSourceResource createResource(HistoricalTimeSeriesSource underlying) {
    return new HistoricalTimeSeriesSourceResource(getFudgeContext(), underlying);
  }

}
