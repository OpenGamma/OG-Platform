/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

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
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeHistoryRequest;
import com.opengamma.master.exchange.ExchangeHistoryResult;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for an exchange.
 */
public class DataExchangeResource extends AbstractDocumentDataResource<ExchangeDocument> {

  /**
   * The exchanges resource.
   */
  private final DataExchangeMasterResource _exchangesResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates dummy resource for the purpose of url resolution.
   *
   */
  DataExchangeResource() {
    _exchangesResource = null;
  }

  /**
   * Creates the resource.
   *
   * @param exchangesResource  the parent resource, not null
   * @param exchangeId  the exchange unique identifier, not null
   */
  public DataExchangeResource(final DataExchangeMasterResource exchangesResource, final ObjectId exchangeId) {
    ArgumentChecker.notNull(exchangesResource, "exchangesResource");
    ArgumentChecker.notNull(exchangeId, "exchange");
    _exchangesResource = exchangesResource;
    _urlResourceId = exchangeId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the exchanges resource.
   *
   * @return the exchanges resource, not null
   */
  public DataExchangeMasterResource getExchangesResource() {
    return _exchangesResource;
  }

  /**
   * Gets the exchange identifier from the URL.
   *
   * @return the unique identifier, not null
   */
  public ObjectId getUrlId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the exchange master.
   *
   * @return the exchange master, not null
   */
  public ExchangeMaster getMaster() {
    return getExchangesResource().getExchangeMaster();
  }

  @GET
  @Path("versions")
  public Response history(@Context UriInfo uriInfo) {
    ExchangeHistoryRequest request = RestUtils.decodeQueryParams(uriInfo, ExchangeHistoryRequest.class);
    if (getUrlId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    ExchangeHistoryResult result = getMaster().history(request);
    return responseOkObject(result);
  }

  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
    return super.get(versionAsOf, correctedTo);
  }

  @POST
  public Response update(@Context UriInfo uriInfo, ExchangeDocument request) {
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
  public Response replaceVersion(@PathParam("versionId") String versionId, List<ExchangeDocument> replacementDocuments) {
    return super.replaceVersion(versionId, replacementDocuments);
  }

  @PUT
  public Response replaceVersions(List<ExchangeDocument> replacementDocuments) {
    return super.replaceVersions(replacementDocuments);
  }

  @PUT
  @Path("all")
  public Response replaceAllVersions(List<ExchangeDocument> replacementDocuments) {
    return super.replaceAllVersions(replacementDocuments);
  }

  @Override
  protected String getResourceName() {
    return "exchanges";
  }

}
