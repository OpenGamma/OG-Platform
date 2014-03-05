/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.id.ObjectId;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidayMetaDataRequest;
import com.opengamma.master.holiday.HolidayMetaDataResult;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for holidays.
 * <p>
 * The holidays resource receives and processes RESTful calls to the holiday master.
 */
@Path("holidayMaster")
public class DataHolidayMasterResource extends AbstractDataResource {

  /**
   * The holiday master.
   */
  private final HolidayMaster _holMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   *
   * @param holidayMaster  the underlying holiday master, not null
   */
  public DataHolidayMasterResource(final HolidayMaster holidayMaster) {
    ArgumentChecker.notNull(holidayMaster, "holidayMaster");
    _holMaster = holidayMaster;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the holiday master.
   *
   * @return the holiday master, not null
   */
  public HolidayMaster getHolidayMaster() {
    return _holMaster;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path("metaData")
  public Response metaData(@Context UriInfo uriInfo) {
    HolidayMetaDataRequest request = RestUtils.decodeQueryParams(uriInfo, HolidayMetaDataRequest.class);
    HolidayMetaDataResult result = getHolidayMaster().metaData(request);
    return responseOkObject(result);
  }

  @POST
  @Path("holidaySearches")
  public Response search(HolidaySearchRequest request) {
    HolidaySearchResult result = getHolidayMaster().search(request);
    return responseOkObject(result);
  }

  @POST
  @Path("holidays")
  public Response add(@Context UriInfo uriInfo, HolidayDocument request) {
    HolidayDocument result = getHolidayMaster().add(request);
    URI createdUri = (new DataHolidayResource()).uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedObject(createdUri, result);
  }

  //-------------------------------------------------------------------------
  @Path("holidays/{holidayId}")
  public DataHolidayResource findHoliday(@PathParam("holidayId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    return new DataHolidayResource(this, id);
  }

  //-------------------------------------------------------------------------

  /**
   * Builds a URI for holiday meta-data.
   *
   * @param baseUri  the base URI, not null
   * @param request  the request, may be null
   * @return the URI, not null
   */
  public static URI uriMetaData(URI baseUri, HolidayMetaDataRequest request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("metaData");
    if (request != null) {
      RestUtils.encodeQueryParams(bld, request);
    }
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("holidaySearches");
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriAdd(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("holidays");
    return bld.build();
  }

}
