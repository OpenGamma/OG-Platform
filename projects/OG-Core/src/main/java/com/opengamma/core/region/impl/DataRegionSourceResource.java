/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.region.impl;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for regions.
 * <p>
 * The regions resource receives and processes RESTful calls to the region source.
 */
@Path("regionSource")
public class DataRegionSourceResource extends AbstractDataResource {

  /**
   * The region source.
   */
  private final RegionSource _regSource;

  /**
   * Creates the resource, exposing the underlying source over REST.
   * 
   * @param regionSource  the underlying region source, not null
   */
  public DataRegionSourceResource(final RegionSource regionSource) {
    ArgumentChecker.notNull(regionSource, "regionSource");
    _regSource = regionSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the region source.
   * 
   * @return the region source, not null
   */
  public RegionSource getRegionSource() {
    return _regSource;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path("regions")
  public Response search(
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo,
      @QueryParam("id") List<String> externalIdStrs) {
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    final ExternalIdBundle bundle = ExternalIdBundle.parse(externalIdStrs);
    Collection<? extends Region> result = getRegionSource().get(bundle, vc);
    return responseOkObject(FudgeListWrapper.of(result));
  }

  @GET
  @Path("regions/{regionId}")
  public Response get(
      @PathParam("regionId") String idStr,
      @QueryParam("version") String version,
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo) {
    final ObjectId objectId = ObjectId.parse(idStr);
    if (version != null) {
      final Region result = getRegionSource().get(objectId.atVersion(version));
      return responseOkObject(result);
    } else {
      final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
      Region result = getRegionSource().get(objectId, vc);
      return responseOkObject(result);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param vc  the version-correction, null means latest
   * @param bundle  the bundle, may be null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri, VersionCorrection vc, ExternalIdBundle bundle) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("regions");
    if (vc != null) {
      bld.queryParam("versionAsof", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    bld.queryParam("id", bundle.toStringList().toArray());
    return bld.build();
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, may be null
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri, UniqueId uniqueId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("regions/{regionId}");
    if (uniqueId.getVersion() != null) {
      bld.queryParam("version", uniqueId.getVersion());
    }
    return bld.build(uniqueId.getObjectId());
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, may be null
   * @param vc  the version-correction, null means latest
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri, ObjectId objectId, VersionCorrection vc) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("regions/{regionId}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectId);
  }

  // deprecated
  //-------------------------------------------------------------------------
  @GET
  @Path("regionSearches/highest")
  public Response searchHighest(@QueryParam("id") List<String> externalIdStrs) {
    final ExternalIdBundle bundle = ExternalIdBundle.parse(externalIdStrs);
    Region result = getRegionSource().getHighestLevelRegion(bundle);
    return responseOkObject(result);
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param bundle  the bundle, may be null
   * @return the URI, not null
   */
  public static URI uriSearchHighest(URI baseUri, ExternalIdBundle bundle) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("regionSearches/highest");
    bld.queryParam("id", bundle.toStringList().toArray());
    return bld.build();
  }

}
