/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.batch.rest;

import com.opengamma.batch.BatchMaster;
import com.opengamma.util.rest.AbstractResourceService;
import org.fudgemsg.FudgeContext;

import javax.ws.rs.Path;

/**
 * RESTful backend for {@link com.opengamma.financial.batch.BatchMaster}.
 */
@Path("batch")
public class BatchMasterService extends AbstractResourceService<BatchMaster, BatchMasterResource> {

  public BatchMasterService(FudgeContext fudgeContext) {
    super(fudgeContext);
  }

  @Override
  protected BatchMasterResource createResource(BatchMaster underlying) {
    return new BatchMasterResource(underlying);
  }

}
