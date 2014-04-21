/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.rest;

import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.opengamma.batch.BatchMaster;
import com.opengamma.batch.domain.RiskRun;
import com.opengamma.engine.view.ViewResultEntry;
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
public class DataBatchRunResource extends AbstractDataResource {

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
   * @param batchRunId  the run ID, not null
   * @param batchMaster  the underlying batch master, not null
   */
  public DataBatchRunResource(final ObjectId batchRunId, final BatchMaster batchMaster) {    
    ArgumentChecker.notNull(batchRunId, "batchRunId");
    ArgumentChecker.notNull(batchMaster, "batchMaster");
    _batchMaster = batchMaster;
    _batchRunId = batchRunId;
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

  //-------------------------------------------------------------------------
  @DELETE
  public void deleteBatchRun() {
    getMaster().deleteRiskRun(_batchRunId);
  }

  @GET
  public Response get() {
    RiskRun result = getMaster().getRiskRun(_batchRunId);
    return responseOkObject(result);
  }

  @POST
  @Path("values")
  @Consumes(FudgeRest.MEDIA)
  public Response getBatchValues(PagingRequest pagingRequest) {
    Pair<List<ViewResultEntry>, Paging> result = getMaster().getBatchValues(_batchRunId, pagingRequest);
    return responseOkObject(result);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for all batch runs.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("run/search");
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
    return UriBuilder.fromUri(baseUri).path("/run/{uid}").build(batchRunId);
  }

  /**
   * Builds a URI for getBatchValues.
   * 
   * @param baseUri  the base URI, not null
   * @param batchRunId the batch id which values we want to build the uri for                          
   * @return the URI, not null
   */
  public static URI uriBatchValues(URI baseUri, ObjectId batchRunId) {
    return UriBuilder.fromUri(baseUri).path("/run/{uid}/values").build(batchRunId);
  }

}
