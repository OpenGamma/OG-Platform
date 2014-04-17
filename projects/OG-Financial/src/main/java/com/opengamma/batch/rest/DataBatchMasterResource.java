/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.batch.BatchMasterWriter;
import com.opengamma.batch.domain.MarketData;
import com.opengamma.batch.domain.RiskRun;
import com.opengamma.id.ObjectId;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.tuple.Pair;

/**
 * RESTful resource for batch.
 * <p>
 * The batch resource receives and processes RESTful calls to the batch master.
 */
@Path("/batch")
public class DataBatchMasterResource extends AbstractDataResource {

  /**
   * The batch master.
   */
  private final BatchMasterWriter _batchMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   *
   * @param batchMaster  the underlying batch master, not null
   */
  public DataBatchMasterResource(final BatchMasterWriter batchMaster) {
    ArgumentChecker.notNull(batchMaster, "batchMaster");
    _batchMaster = batchMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the batch master.
   *
   * @return the batch master, not null
   */
  public BatchMasterWriter getMaster() {
    return _batchMaster;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  /*
   Search for market data snapshots
  */
  @POST
  @Path("marketData/search")
  @Consumes(FudgeRest.MEDIA)
  public Response searchMarketData(PagingRequest pagingRequest) {
    Pair<List<MarketData>, Paging> result = getMaster().getMarketData(pagingRequest);
    return responseOkObject(result);
  }

  /*
   Get market data snapshot by id
  */
  @GET
  @Path("marketData/{id}")
  public DataMarketDataResource getMarketData(@PathParam("id") final String snapshotId) {
    ObjectId id = ObjectId.parse(snapshotId);
    return new DataMarketDataResource(id, getMaster());
  }

  @POST
  @Path("run/search")
  @Consumes(FudgeRest.MEDIA)
  public Response searchBatchRuns(BatchRunSearchRequest request) {
    Pair<List<RiskRun>, Paging> result = getMaster().searchRiskRun(request);
    return responseOkObject(result);
  }  

  @Path("run/{id}")
  public DataBatchRunResource batchRuns(@PathParam("id") final String runId) {
    ObjectId id = ObjectId.parse(runId);
    return new DataBatchRunResource(id, getMaster());
  }

}
