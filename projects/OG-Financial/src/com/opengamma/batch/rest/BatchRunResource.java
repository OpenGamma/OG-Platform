/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.rest;


import com.opengamma.batch.BatchMaster;
import com.opengamma.batch.domain.RiskRun;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.id.ObjectId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;
import com.opengamma.util.tuple.Pair;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

/**
 * RESTful resource for batch.
 * <p>
 * The batch resource receives and processes RESTful calls to the batch master.
 */
public class BatchRunResource extends AbstractDataResource {

  /**
   * The batch master.
   */
  private final BatchMaster _batchMaster;

  /**
   * The batch run unique id.
   */
  private final ObjectId _batchRunId;

  /**
   * Creates the resource, exposing the underlying master over REST.
   *
   * @param batchMaster  the underlying batch master, not null
   */
  public BatchRunResource(final ObjectId batchRunId, final BatchMaster batchMaster) {    
    ArgumentChecker.notNull(batchRunId, "batchRunId");
    ArgumentChecker.notNull(batchMaster, "batchMaster");
    _batchMaster = batchMaster;
    _batchRunId = batchRunId;
  }

  /**
   * Gets the batch master.
   *
   * @return the batch master, not null
   */
  public BatchMaster getMaster() {
    return _batchMaster;
  }


  @DELETE
  public void deleteBatchRun() {
    getMaster().deleteRiskRun(_batchRunId);
  }

  @GET
  public Response get() {
    RiskRun result = getMaster().getRiskRun(_batchRunId);
    return Response.ok(result).build();
  }
  
  @GET
  @Path("values")
  public Response getBatchValues(PagingRequest pagingRequest) {
    Pair<List<ViewResultEntry>, Paging> result = getMaster().getBatchValues(_batchRunId, pagingRequest);
    return Response.ok(result).build();
  }


  /**
   * Builds a URI for all batch runs.
   *
   * @param baseUri  the base URI, not null
   * @param searchMsg  the search message, may be null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("batchRun/search");
    return bld.build();
  }

  /**
   * Builds a URI for a specific uid of the resource.
   *
   * @param baseUri  the base URI, not null
   * @param batchRunId  the resource identifier, not null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, ObjectId batchRunId) {
    return UriBuilder.fromUri(baseUri).path("/batchRun/{uid}").build(batchRunId);
  }
  
  /**
   * Builds a URI for getBatchValues.
   * 
   * @param baseUri  the base URI, not null
   * @param batchRunId the batch id which values we want to build the uri for                          
   * @param request  the paging request, may be null           
   * @return the URI, not null
   */
  public static URI uriBatchValues(URI baseUri, ObjectId batchRunId) {
    return UriBuilder.fromUri(baseUri).path("/batchRun/{uid}/values").build(batchRunId);
  }


}
