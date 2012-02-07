/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.rest;

import com.opengamma.batch.BatchMaster;
import com.opengamma.batch.domain.MarketData;
import com.opengamma.batch.domain.RiskRun;
import com.opengamma.id.ObjectId;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.tuple.Pair;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Providers;
import java.util.List;

/**
 * RESTful resource for batch.
 * <p>
 * The batch resource receives and processes RESTful calls to the batch master.
 */
@Path("/batch")
public class BatchMasterResource extends AbstractDataResource {

  /**
   * The batch master.
   */
  private final BatchMaster _batchMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   *
   * @param batchMaster  the underlying batch master, not null
   */
  public BatchMasterResource(final BatchMaster batchMaster) {
    ArgumentChecker.notNull(batchMaster, "batchMaster");
    _batchMaster = batchMaster;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the batch master.
   *
   * @return the batch master, not null
   */
  public BatchMaster getMaster() {
    return _batchMaster;
  }

  /*
    Get snapshot by id
   */
  @Path("snapshot/{snapshotId}")
  public MarketDataResource snapshots(@PathParam("snapshotId") final String snapshotId) {
    ObjectId id = ObjectId.parse(snapshotId);
    return new MarketDataResource(id, getMaster());
  }

  /*
   Search for snapshots
  */
  @POST
  @Path("snapshot/search")
  @Consumes(FudgeRest.MEDIA)
  public Response searchSnapshots(PagingRequest pagingRequest) {
    Pair<List<MarketData>, Paging> result = getMaster().getMarketData(pagingRequest);
    return Response.ok(result).build();
  }

  @Path("run/{runId}")
  public BatchRunResource batchRuns(@QueryParam("runId") final String runId) {
    ObjectId id = ObjectId.parse(runId);
    return new BatchRunResource(id, getMaster());
  }

  @POST
  @Path("run/search")
  @Consumes(FudgeRest.MEDIA)
  public Response searchBatchRuns(BatchRunSearchRequest request) {
    Pair<List<RiskRun>, Paging> result = getMaster().searchRiskRun(request);
    return Response.ok(result).build();
  }  

}
