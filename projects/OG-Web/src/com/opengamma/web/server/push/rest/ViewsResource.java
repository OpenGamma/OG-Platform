/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.time.Instant;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.push.ClientConnection;
import com.opengamma.web.server.push.ConnectionManager;
import com.opengamma.web.server.push.analytics.AnalyticsViewManager;
import com.opengamma.web.server.push.analytics.MarketDataSpecificationJsonReader;
import com.opengamma.web.server.push.analytics.ViewRequest;

/**
 * RESTful resource for creating and looking up views that calculate analytics data for a portfolio.
 */
@Path("views")
public class ViewsResource {

  /** For generating IDs for the views. */
  private static final AtomicLong s_nextViewId = new AtomicLong(0);

  /** For creating and retrieving views. */
  private final AnalyticsViewManager _viewManager;
  /** For looking up a client's connection. */
  private final ConnectionManager _connectionManager;

  public ViewsResource(AnalyticsViewManager viewManager, ConnectionManager connectionManager) {
    ArgumentChecker.notNull(viewManager, "viewManager");
    ArgumentChecker.notNull(connectionManager, "connectionManager");
    _viewManager = viewManager;
    _connectionManager = connectionManager;
  }

  /**
   * Returns a resource for a view.
   * @param viewId ID of the view
   * @return Resource for the view
   */
  @Path("{viewId}")
  public ViewResource getView(@PathParam("viewId") String viewId) {
    return new ViewResource(_viewManager.getView(viewId), _viewManager, viewId);
  }

  @POST
  public Response createView(@Context UriInfo uriInfo,
                             @FormParam("viewDefinitionId") String viewDefinitionId,
                             @FormParam("aggregators") List<String> aggregators,
                             @FormParam("marketDataProviders") String marketDataProviders,
                             @FormParam("valuationTime") String valuationTime,
                             @FormParam("portfolioVersionTime") String portfolioVersionTime,
                             @FormParam("portfolioCorrectionTime") String portfolioCorrectionTime,
                             @FormParam("clientId") String clientId) {
    ArgumentChecker.notNull(viewDefinitionId, "viewDefinitionId");
    ArgumentChecker.notNull(aggregators, "aggregators");
    ArgumentChecker.notNull(marketDataProviders, "marketDataProviders");
    ArgumentChecker.notNull(clientId, "clientId");
    List<MarketDataSpecification> marketDataSpecs = MarketDataSpecificationJsonReader.buildSpecifications(marketDataProviders);
    VersionCorrection versionCorrection = VersionCorrection.of(parseInstant(portfolioVersionTime),
                                                               parseInstant(portfolioCorrectionTime));
    ViewRequest viewRequest = new ViewRequest(UniqueId.parse(viewDefinitionId), aggregators, marketDataSpecs,
                                              parseInstant(valuationTime), versionCorrection);
    String viewId = Long.toString(s_nextViewId.getAndIncrement());
    URI portfolioGridUri = uriInfo.getAbsolutePathBuilder()
        .path(viewId)
        .path(ViewResource.class, "getPortfolioGrid")
        .build();
    URI primitivesGridUri = uriInfo.getAbsolutePathBuilder()
        .path(viewId)
        .path(ViewResource.class, "getPrimitivesGrid")
        .build();
    // TODO this is very obviously wrong - where can I get the user?
    UserPrincipal user = UserPrincipal.getTestUser();
    String userName = null;
    //String userName = user.getUserName();
    ClientConnection connection = _connectionManager.getConnectionByClientId(userName, clientId);
    _viewManager.createView(viewRequest, user, connection, viewId, portfolioGridUri.getPath(), primitivesGridUri.getPath());
    URI uri = uriInfo.getAbsolutePathBuilder().path(viewId).build();
    return Response.status(Response.Status.CREATED).header(HttpHeaders.LOCATION, uri).build();
  }

  /**
   * @param instantString An ISO-8601 string representing an instant or null
   * @return The parsed string or null if the input is null
   */
  private static Instant parseInstant(String instantString) {
    if (instantString == null) {
      return null;
    } else {
      return Instant.parse(instantString);
    }
  }
}

