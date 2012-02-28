/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import java.net.URI;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayHistoryRequest;
import com.opengamma.master.holiday.HolidayHistoryResult;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for a holiday.
 */
public class DataHolidayResource extends AbstractDataResource {

  /**
   * The holidays resource.
   */
  private final DataHolidayMasterResource _holidaysResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates the resource.
   * 
   * @param holidaysResource  the parent resource, not null
   * @param holidayId  the holiday unique identifier, not null
   */
  public DataHolidayResource(final DataHolidayMasterResource holidaysResource, final ObjectId holidayId) {
    ArgumentChecker.notNull(holidaysResource, "holidaysResource");
    ArgumentChecker.notNull(holidayId, "holiday");
    _holidaysResource = holidaysResource;
    _urlResourceId = holidayId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the holidays resource.
   * 
   * @return the holidays resource, not null
   */
  public DataHolidayMasterResource getHolidaysResource() {
    return _holidaysResource;
  }

  /**
   * Gets the holiday identifier from the URL.
   * 
   * @return the unique identifier, not null
   */
  public ObjectId getUrlHolidayId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the holiday master.
   * 
   * @return the holiday master, not null
   */
  public HolidayMaster getHolidayMaster() {
    return getHolidaysResource().getHolidayMaster();
  }

  //-------------------------------------------------------------------------
  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
    VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    HolidayDocument result = getHolidayMaster().get(getUrlHolidayId(), vc);
    return responseOkFudge(result);
  }

  @POST
  public Response update(@Context UriInfo uriInfo, HolidayDocument request) {
    if (getUrlHolidayId().equals(request.getUniqueId().getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    HolidayDocument result = getHolidayMaster().update(request);
    URI uri = uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedFudge(uri, result);
  }

  @DELETE
  public void remove() {
    getHolidayMaster().remove(getUrlHolidayId().atLatestVersion());
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions")
  public Response history(@Context UriInfo uriInfo) {
    HolidayHistoryRequest request = RestUtils.decodeQueryParams(uriInfo, HolidayHistoryRequest.class);
    if (getUrlHolidayId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    HolidayHistoryResult result = getHolidayMaster().history(request);
    return responseOkFudge(result);
  }

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    UniqueId uniqueId = getUrlHolidayId().atVersion(versionId);
    HolidayDocument result = getHolidayMaster().get(uniqueId);
    return responseOkFudge(result);
  }

  @POST
  @Path("versions/{versionId}")
  public Response correct(@Context UriInfo uriInfo, @PathParam("versionId") String versionId, HolidayDocument request) {
    UniqueId uniqueId = getUrlHolidayId().atVersion(versionId);
    if (uniqueId.equals(request.getUniqueId()) == false) {
      throw new IllegalArgumentException("Document uniqueId does not match URI");
    }
    HolidayDocument result = getHolidayMaster().correct(request);
    URI uri = uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedFudge(uri, result);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @param vc  the version-correction locator, null for latest
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, ObjectIdentifiable objectId, VersionCorrection vc) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/holidays/{holidayId}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for the versions of the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, not null
   * @param request  the request, may be null
   * @return the URI, not null
   */
  public static URI uriVersions(URI baseUri, ObjectIdentifiable objectId, HolidayHistoryRequest request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/holidays/{holidayId}/versions");
    if (request != null) {
      RestUtils.encodeQueryParams(bld, request);
    }
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for a specific version of the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, not null
   * @return the URI, not null
   */
  public static URI uriVersion(URI baseUri, UniqueId uniqueId) {
    if (uniqueId.isLatest()) {
      return uri(baseUri, uniqueId, null);
    }
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/holidays/{holidayId}/versions/{versionId}");
    return bld.build(uniqueId.toLatest(), uniqueId.getVersion());
  }

}
