/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

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
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayHistoryRequest;
import com.opengamma.master.holiday.HolidayHistoryResult;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for a holiday.
 */
public class DataHolidayResource extends AbstractDocumentDataResource<HolidayDocument> {

  /**
   * The holidays resource.
   */
  private final DataHolidayMasterResource _holidaysResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates dummy resource for the purpose of url resolution.
   *
   */
  DataHolidayResource() {
    _holidaysResource = null;
  }

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
  public ObjectId getUrlId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the holiday master.
   *
   * @return the holiday master, not null
   */
  @Override
  protected HolidayMaster getMaster() {
    return getHolidaysResource().getHolidayMaster();
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions")
  public Response history(@Context UriInfo uriInfo) {
    HolidayHistoryRequest request = RestUtils.decodeQueryParams(uriInfo, HolidayHistoryRequest.class);
    if (getUrlId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    HolidayHistoryResult result = getMaster().history(request);
    return responseOkObject(result);
  }

  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
    return super.get(versionAsOf, correctedTo);
  }

  @POST
  public Response update(@Context UriInfo uriInfo, HolidayDocument request) {
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
  public Response replaceVersion(@PathParam("versionId") String versionId, List<HolidayDocument> replacementDocuments) {
    return super.replaceVersion(versionId, replacementDocuments);
  }

  @PUT
  public Response replaceVersions(List<HolidayDocument> replacementDocuments) {
    return super.replaceVersions(replacementDocuments);
  }

  @PUT
  @Path("all")
  public Response replaceAllVersions(List<HolidayDocument> replacementDocuments) {
    return super.replaceAllVersions(replacementDocuments);
  }

  @Override
  protected String getResourceName() {
    return "holidays";
  }

}
