/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for positions.
 * <p>
 * This resource receives and processes RESTful calls to the position source.
 */
@Path("positionSource")
public class DataPositionSourceResource extends AbstractDataResource {

  /**
   * The position source.
   */
  private final PositionSource _posSource;

  /**
   * Creates the resource, exposing the underlying source over REST.
   * 
   * @param positionSource  the underlying position source, not null
   */
  public DataPositionSourceResource(final PositionSource positionSource) {
    ArgumentChecker.notNull(positionSource, "positionSource");
    _posSource = positionSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the position source.
   * 
   * @return the position source, not null
   */
  public PositionSource getPositionSource() {
    return _posSource;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path("portfolios/{portfolioId}")
  public Response getPortfolio(
      @PathParam("portfolioId") String idStr,
      @QueryParam("version") String version,
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo) {
    final ObjectId objectId = ObjectId.parse(idStr);
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    if (version != null) {
      final Portfolio result = getPositionSource().getPortfolio(objectId.atVersion(version), vc);
      return responseOkObject(result);
    } else {
      final Portfolio result = getPositionSource().getPortfolio(objectId, vc);
      return responseOkObject(result);
    }
  }

  @GET
  @Path("nodes/{nodeId}")
  public Response getNode(
      @PathParam("nodeId") String idStr,
      @QueryParam("version") String version,
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo) {
    final ObjectId objectId = ObjectId.parse(idStr);
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    final PortfolioNode result = getPositionSource().getPortfolioNode(objectId.atVersion(version), vc);
    return responseOkObject(result);
  }

  @GET
  @Path("positions/{positionId}")
  public Response getPosition(
      @PathParam("positionId") String idStr,
      @QueryParam("version") String version,
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo) {
    final ObjectId objectId = ObjectId.parse(idStr);
    final Position result;
    if (version != null) {
      result = getPositionSource().getPosition(objectId.atVersion(version));
    } else {
      result = getPositionSource().getPosition(objectId, VersionCorrection.parse(versionAsOf, correctedTo));
    }
    return responseOkObject(result);
  }

  @GET
  @Path("trades/{tradeId}")
  public Response getTrade(
      @PathParam("tradeId") String idStr,
      @QueryParam("version") String version) {
    final ObjectId objectId = ObjectId.parse(idStr);
    final Trade result = getPositionSource().getTrade(objectId.atVersion(version));
    return responseOkObject(result);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, may be null
   * @return the URI, not null
   */
  public static URI uriGetPortfolio(URI baseUri, UniqueId uniqueId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("portfolios/{portfolioId}");
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
  public static URI uriGetPortfolio(URI baseUri, ObjectId objectId, VersionCorrection vc) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("portfolios/{portfolioId}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectId);
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, may be null
   * @return the URI, not null
   */
  public static URI uriGetNode(URI baseUri, UniqueId uniqueId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("nodes/{nodeId}");
    if (uniqueId.getVersion() != null) {
      bld.queryParam("version", uniqueId.getVersion());
    }
    return bld.build(uniqueId.getObjectId());
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, may be null
   * @return the URI, not null
   */
  public static URI uriGetPosition(URI baseUri, UniqueId uniqueId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("positions/{positionId}");
    if (uniqueId.getVersion() != null) {
      bld.queryParam("version", uniqueId.getVersion());
    }
    return bld.build(uniqueId.getObjectId());
  }

  public static URI uriGetPosition(final URI baseUri, final ObjectId objectId, final VersionCorrection versionCorrection) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("positions/{positionId}");
    if (versionCorrection != null) {
      bld.queryParam("versionAsOf", versionCorrection.getVersionAsOfString());
      bld.queryParam("correctedTo", versionCorrection.getCorrectedToString());
    }
    return bld.build(objectId);
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, may be null
   * @return the URI, not null
   */
  public static URI uriGetTrade(URI baseUri, UniqueId uniqueId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("trades/{tradeId}");
    if (uniqueId.getVersion() != null) {
      bld.queryParam("version", uniqueId.getVersion());
    }
    return bld.build(uniqueId.getObjectId());
  }

}
