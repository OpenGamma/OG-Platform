/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.rest;

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

import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinitionDocument;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for a yield curve definition.
 */
public class DataInterpolatedYieldCurveDefinitionResource extends AbstractDataResource {

  /**
   * The parent resource.
   */
  private final DataInterpolatedYieldCurveDefinitionMasterResource _parentResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates the resource.
   * 
   * @param parentResource  the parent resource, not null
   * @param definitionId  the definition unique identifier, not null
   */
  public DataInterpolatedYieldCurveDefinitionResource(final DataInterpolatedYieldCurveDefinitionMasterResource parentResource, final ObjectId definitionId) {
    ArgumentChecker.notNull(parentResource, "definitionsResource");
    ArgumentChecker.notNull(definitionId, "definition");
    _parentResource = parentResource;
    _urlResourceId = definitionId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the parent resource.
   * 
   * @return the parent resource, not null
   */
  public DataInterpolatedYieldCurveDefinitionMasterResource getParentResource() {
    return _parentResource;
  }

  /**
   * Gets the definition identifier from the URL.
   * 
   * @return the unique identifier, not null
   */
  public ObjectId getUrlDefinitionId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the definition master.
   * 
   * @return the definition master, not null
   */
  public InterpolatedYieldCurveDefinitionMaster getInterpolatedYieldCurveDefinitionMaster() {
    return getParentResource().getInterpolatedYieldCurveDefinitionMaster();
  }

  //-------------------------------------------------------------------------
  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo) {
    VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    YieldCurveDefinitionDocument result = getInterpolatedYieldCurveDefinitionMaster().get(getUrlDefinitionId(), vc);
    return Response.ok(result).build();
  }

  @POST
  public Response update(@Context UriInfo uriInfo, YieldCurveDefinitionDocument request) {
    if (getUrlDefinitionId().equals(request.getUniqueId().getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    YieldCurveDefinitionDocument result = getInterpolatedYieldCurveDefinitionMaster().update(request);
    URI uri = uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return Response.created(uri).entity(result).build();
  }

  @DELETE
  public Response remove() {
    getInterpolatedYieldCurveDefinitionMaster().remove(getUrlDefinitionId().atLatestVersion());
    return Response.noContent().build();
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId) {
    UniqueId uniqueId = getUrlDefinitionId().atVersion(versionId);
    YieldCurveDefinitionDocument result = getInterpolatedYieldCurveDefinitionMaster().get(uniqueId);
    return Response.ok(result).build();
  }

  @POST
  @Path("versions/{versionId}")
  public Response correct(@Context UriInfo uriInfo, @PathParam("versionId") String versionId, YieldCurveDefinitionDocument request) {
    UniqueId uniqueId = getUrlDefinitionId().atVersion(versionId);
    if (uniqueId.equals(request.getUniqueId()) == false) {
      throw new IllegalArgumentException("Document uniqueId does not match URI");
    }
    YieldCurveDefinitionDocument result = getInterpolatedYieldCurveDefinitionMaster().correct(request);
    URI uri = uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return Response.created(uri).entity(result).build();
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
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/definitions/{definitionId}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
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
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/definitions/{definitionId}/versions/{versionId}");
    return bld.build(uniqueId.getObjectId(), uniqueId.getVersion());
  }

}
