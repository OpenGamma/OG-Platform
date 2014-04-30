/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for a trade in the position master.
 */
public class DataTradeResource extends AbstractDataResource {

  /**
   * The positions resource.
   */
  private final DataPositionMasterResource _parentResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates the resource.
   * 
   * @param parentResource  the parent resource, not null
   * @param positionId  the position unique identifier, not null
   */
  public DataTradeResource(final DataPositionMasterResource parentResource, final ObjectId positionId) {
    ArgumentChecker.notNull(parentResource, "parentResource");
    ArgumentChecker.notNull(positionId, "position");
    _parentResource = parentResource;
    _urlResourceId = positionId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the parent resource.
   * 
   * @return the parent resource, not null
   */
  public DataPositionMasterResource getParentResource() {
    return _parentResource;
  }

  /**
   * Gets the position identifier from the URL.
   * 
   * @return the unique identifier, not null
   */
  public ObjectId getUrlTradeId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the position master.
   * 
   * @return the position master, not null
   */
  public PositionMaster getPositionMaster() {
    return getParentResource().getPositionMaster();
  }

  //-------------------------------------------------------------------------
  @GET
  public Response get() {
    UniqueId tradeId = getUrlTradeId().atLatestVersion();
    ManageableTrade result = getPositionMaster().getTrade(tradeId);
    return responseOkObject(result);
  }

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    UniqueId tradeId = getUrlTradeId().atVersion(versionId);
    ManageableTrade result = getPositionMaster().getTrade(tradeId);
    return responseOkObject(result);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for the trade.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @param vc  the version-correction locator, null for latest
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, ObjectIdentifiable objectId, VersionCorrection vc) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("trades/{tradeId}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for a specific version of the trade.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, not null
   * @return the URI, not null
   */
  public static URI uriVersion(URI baseUri, UniqueId uniqueId) {
    if (uniqueId.isLatest()) {
      return uri(baseUri, uniqueId, null);
    }
    return UriBuilder.fromUri(baseUri).path("/trades/{tradeId}/versions/{versionId}")
      .build(uniqueId.toLatest(), uniqueId.getVersion());
  }

}
