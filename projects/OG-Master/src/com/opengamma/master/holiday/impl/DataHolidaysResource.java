/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import com.opengamma.id.ObjectId;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidayMetaDataRequest;
import com.opengamma.master.holiday.HolidayMetaDataResult;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for holidays.
 * <p>
 * The holidays resource receives and processes RESTful calls to the holiday master.
 */
@Path("/holMaster")
public class DataHolidaysResource extends AbstractDataResource {

  /**
   * The holiday master.
   */
  private final HolidayMaster _holMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   * 
   * @param holidayMaster  the underlying holiday master, not null
   */
  public DataHolidaysResource(final HolidayMaster holidayMaster) {
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
  @Path("metaData")
  public Response metaData(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    HolidayMetaDataRequest request = new HolidayMetaDataRequest();
    if (msgBase64 != null) {
      request = decodeBean(HolidayMetaDataRequest.class, providers, msgBase64);
    }
    HolidayMetaDataResult result = getHolidayMaster().metaData(request);
    return Response.ok(result).build();
  }

  @HEAD
  @Path("holidays")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return Response.ok().build();
  }

  @GET
  @Path("holidays")
  public Response search(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    HolidaySearchRequest request = decodeBean(HolidaySearchRequest.class, providers, msgBase64);
    HolidaySearchResult result = getHolidayMaster().search(request);
    return Response.ok(result).build();
  }

  @POST
  @Path("holidays")
  @Consumes(FudgeRest.MEDIA)
  public Response add(@Context UriInfo uriInfo, HolidayDocument request) {
    HolidayDocument result = getHolidayMaster().add(request);
    URI createdUri = DataHolidayResource.uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return Response.created(createdUri).entity(result).build();
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
   * @param searchMsg  the search message, may be null
   * @return the URI, not null
   */
  public static URI uriMetaData(URI baseUri, String searchMsg) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/metaData");
    if (searchMsg != null) {
      bld.queryParam("msg", searchMsg);
    }
    return bld.build();
  }

  /**
   * Builds a URI for all holidays.
   * 
   * @param baseUri  the base URI, not null
   * @param searchMsg  the search message, may be null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, String searchMsg) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/holidays");
    if (searchMsg != null) {
      bld.queryParam("msg", searchMsg);
    }
    return bld.build();
  }

}
