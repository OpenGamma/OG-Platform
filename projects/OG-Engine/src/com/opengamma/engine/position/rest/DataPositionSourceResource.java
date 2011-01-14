/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position.rest;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for a position source.
 */
@Path("/data/sources/position")
public class DataPositionSourceResource extends AbstractDataResource {

  /**
   * The injected position source.
   */
  private final PositionSource _positionSource;

  /**
   * Creates the resource.
   * 
   * @param positionSource  the position source, not null
   */
  public DataPositionSourceResource(final PositionSource positionSource) {
    ArgumentChecker.notNull(positionSource, "positionSource");
    _positionSource = positionSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the position source.
   * 
   * @return the position source, not null
   */
  public PositionSource getPositionSource() {
    return _positionSource;
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("portfolios/{portfolioId}")
  public Response getPortfolio(@PathParam("portfolioId") String portfolioId) {
    UniqueIdentifier uniqueId = UniqueIdentifier.parse(portfolioId);
    Portfolio result = getPositionSource().getPortfolio(uniqueId);
    return Response.ok(result).build();
  }

  @GET
  @Path("nodes/{nodeId}")
  public Response getNode(@PathParam("nodeId") String nodeId) {
    UniqueIdentifier uniqueId = UniqueIdentifier.parse(nodeId);
    PortfolioNode result = getPositionSource().getPortfolioNode(uniqueId);
    return Response.ok(result).build();
  }

  @GET
  @Path("positions/{positionId}")
  public Response getPosition(@PathParam("positionId") String positionId) {
    UniqueIdentifier uniqueId = UniqueIdentifier.parse(positionId);
    Position result = getPositionSource().getPosition(uniqueId);
    return Response.ok(result).build();
  }

  @GET
  @Path("trades/{tradeId}")
  public Response getTrade(@PathParam("tradeId") String tradeId) {
    UniqueIdentifier uniqueId = UniqueIdentifier.parse(tradeId);
    Trade result = getPositionSource().getTrade(uniqueId);
    return Response.ok(result).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for a portfolio.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the resource identifier, not null
   * @return the URI, not null
   */
  public static URI uriPortfolio(URI baseUri, UniqueIdentifier uniqueId) {
    return UriBuilder.fromUri(baseUri).path("/sources/position/portfolios/{portfolioId}").build(uniqueId);
  }

  /**
   * Builds a URI for a node.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the resource identifier, not null
   * @return the URI, not null
   */
  public static URI uriNode(URI baseUri, UniqueIdentifier uniqueId) {
    return UriBuilder.fromUri(baseUri).path("/sources/position/nodes/{nodeId}").build(uniqueId);
  }

  /**
   * Builds a URI for a position.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the resource identifier, not null
   * @return the URI, not null
   */
  public static URI uriPosition(URI baseUri, UniqueIdentifier uniqueId) {
    return UriBuilder.fromUri(baseUri).path("/sources/position/positions/{positionId}").build(uniqueId);
  }

  /**
   * Builds a URI for a trade.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the resource identifier, not null
   * @return the URI, not null
   */
  public static URI uriTrade(URI baseUri, UniqueIdentifier uniqueId) {
    return UriBuilder.fromUri(baseUri).path("/sources/position/trades/{tradeId}").build(uniqueId);
  }

}
