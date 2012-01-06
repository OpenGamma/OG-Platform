/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.batch.rest;

import com.opengamma.financial.batch.BatchMaster;
import com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesMasterResource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.util.rest.AbstractResourceService;
import org.fudgemsg.FudgeContext;

import javax.ws.rs.Path;

/**
 * RESTful backend for {@link com.opengamma.financial.batch.BatchMaster}.
 */
@Path("batchMaster")
public class BatchMasterService extends AbstractResourceService<BatchMaster, BatchMasterResource> {

  public BatchMasterService(FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  @Override
  protected BatchMasterResource createResource(BatchMaster underlying) {
    return new BatchMasterResource(getFudgeContext(), underlying);
  }

}
