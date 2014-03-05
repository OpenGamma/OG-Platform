/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.rest;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinitionDocument;
import com.opengamma.id.ObjectId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for the yield curve master.
 * <p>
 * This resource receives and processes RESTful calls to the master.
 */
@Path("yieldCurveDefinitionMaster")
public class DataInterpolatedYieldCurveDefinitionMasterResource extends AbstractDataResource {

  /**
   * The master.
   */
  private final InterpolatedYieldCurveDefinitionMaster _master;

  /**
   * Creates the resource, exposing the underlying master over REST.
   *
   * @param master  the underlying master, not null
   */
  public DataInterpolatedYieldCurveDefinitionMasterResource(final InterpolatedYieldCurveDefinitionMaster master) {
    ArgumentChecker.notNull(master, "master");
    _master = master;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the master.
   *
   * @return the master, not null
   */
  public InterpolatedYieldCurveDefinitionMaster getInterpolatedYieldCurveDefinitionMaster() {
    return _master;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("definitions")
  public Response status() {
    // simple GET to quickly return as a ping
    return responseOk();
  }

  @POST
  @Path("definitions")
  public Response add(@Context UriInfo uriInfo, YieldCurveDefinitionDocument document) {
    YieldCurveDefinitionDocument result = getInterpolatedYieldCurveDefinitionMaster().add(document);
    URI createdUri = (new DataInterpolatedYieldCurveDefinitionResource()).uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedObject(createdUri, result);
  }

  @POST
  @Path("definitions/save")  // not the best URI
  public Response addOrUpdate(@Context UriInfo uriInfo, YieldCurveDefinitionDocument document) {
    YieldCurveDefinitionDocument result = getInterpolatedYieldCurveDefinitionMaster().addOrUpdate(document);
    URI createdUri = (new DataInterpolatedYieldCurveDefinitionResource()).uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedObject(createdUri, result);
  }

  //-------------------------------------------------------------------------
  @Path("definitions/{definitionId}")
  public DataInterpolatedYieldCurveDefinitionResource findByObjectId(@PathParam("definitionId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    return new DataInterpolatedYieldCurveDefinitionResource(this, id);
  }

  //-------------------------------------------------------------------------

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/definitions");
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriAddOrUpdate(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/definitions/save");
    return bld.build();
  }

}
