/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.id.ObjectId;
import com.opengamma.master.AbstractDocumentDataResource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for accessing time-series info.
 */
public class DataHistoricalTimeSeriesResource extends AbstractDocumentDataResource<HistoricalTimeSeriesInfoDocument> {

  /**
   * The time-series resource.
   */
  private final DataHistoricalTimeSeriesMasterResource _htsResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates dummy resource for the purpose of url resolution.
   *
   */
  DataHistoricalTimeSeriesResource() {
    _htsResource = null;
  }
  
  /**
   * Creates the resource.
   *
   * @param htsResource  the parent resource, not null
   * @param infoId  the time-series unique identifier, not null
   */
  public DataHistoricalTimeSeriesResource(final DataHistoricalTimeSeriesMasterResource htsResource, final ObjectId infoId) {
    ArgumentChecker.notNull(htsResource, "htsResource");
    ArgumentChecker.notNull(infoId, "infoId");
    _htsResource = htsResource;
    _urlResourceId = infoId;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the parent resource.
   *
   * @return the parent resource, not null
   */
  public DataHistoricalTimeSeriesMasterResource getParentResource() {
    return _htsResource;
  }

  /**
   * Gets the info identifier from the URL.
   *
   * @return the object identifier, not null
   */
  public ObjectId getUrlId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the time-series master.
   *
   * @return the time-series master, not null
   */
  public HistoricalTimeSeriesMaster getMaster() {
    return getParentResource().getHistoricalTimeSeriesMaster();
  }

  @GET
  @Path("versions")
  public Response history(@Context UriInfo uriInfo) {
    HistoricalTimeSeriesInfoHistoryRequest request = RestUtils.decodeQueryParams(uriInfo, HistoricalTimeSeriesInfoHistoryRequest.class);
    if (getUrlId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    HistoricalTimeSeriesInfoHistoryResult result = getMaster().history(request);
    return responseOkObject(result);
  }


  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
    return super.get(versionAsOf, correctedTo);
  }

  @POST
  public Response update(@Context UriInfo uriInfo, HistoricalTimeSeriesInfoDocument request) {
    return super.update(uriInfo, request);
  }

  @DELETE
  public void remove() {
    super.remove();
  }

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    return super.getVersioned(versionId);
  }


  @PUT
  @Path("versions/{versionId}")
  public Response replaceVersion(@PathParam("versionId") String versionId, List<HistoricalTimeSeriesInfoDocument> replacementDocuments) {
    return super.replaceVersion(versionId, replacementDocuments);
  }

  @PUT
  public Response replaceVersions(List<HistoricalTimeSeriesInfoDocument> replacementDocuments) {
    return super.replaceVersions(replacementDocuments);
  }

  @PUT
  @Path("all")
  public Response replaceAllVersions(List<HistoricalTimeSeriesInfoDocument> replacementDocuments) {
    return super.replaceAllVersions(replacementDocuments);
  }

  @Override
  protected String getResourceName() {
    return "infos";
  }

}
