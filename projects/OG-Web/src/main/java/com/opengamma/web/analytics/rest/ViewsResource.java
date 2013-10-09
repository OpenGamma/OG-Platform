/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.AnalyticsViewManager;
import com.opengamma.web.analytics.MarketDataSpecificationJsonReader;
import com.opengamma.web.analytics.ViewRequest;
import com.opengamma.web.analytics.push.ClientConnection;
import com.opengamma.web.analytics.push.ConnectionManager;

/**
 * RESTful resource for creating and looking up views that calculate analytics data for a portfolio.
 * @deprecated in favour of {@link WebUiResource}
 */
@Path("views")
@Deprecated
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
    return new ViewResource(_viewManager.getViewCient(viewId), _viewManager.getView(viewId), _viewManager, viewId);
  }

  @POST
  public Response createView(@Context UriInfo uriInfo,
                             @Context SecurityContext securityContext,
                             @FormParam("requestId") String requestId,
                             @FormParam("viewDefinitionId") String viewDefinitionId,
                             @FormParam("aggregators") List<String> aggregators,
                             @FormParam("marketDataProviders") String marketDataProviders,
                             @FormParam("valuationTime") String valuationTime,
                             @FormParam("portfolioVersionTime") String portfolioVersionTime,
                             @FormParam("portfolioCorrectionTime") String portfolioCorrectionTime,
                             @FormParam("clientId") String clientId,
                             @FormParam("blotter") Boolean blotter) {
    ArgumentChecker.notEmpty(requestId, "requestId");
    ArgumentChecker.notEmpty(viewDefinitionId, "viewDefinitionId");
    ArgumentChecker.notNull(aggregators, "aggregators");
    ArgumentChecker.notEmpty(marketDataProviders, "marketDataProviders");
    ArgumentChecker.notEmpty(clientId, "clientId");
    boolean blotterColumns = blotter == null ? false : blotter;
    List<MarketDataSpecification> marketDataSpecs = MarketDataSpecificationJsonReader.buildSpecifications(marketDataProviders);
    VersionCorrection versionCorrection = VersionCorrection.of(parseInstant(portfolioVersionTime),
                                                               parseInstant(portfolioCorrectionTime));
    ViewRequest viewRequest = new ViewRequest(UniqueId.parse(viewDefinitionId), aggregators, marketDataSpecs,
                                              parseInstant(valuationTime), versionCorrection, blotterColumns);
    String viewId = Long.toString(s_nextViewId.getAndIncrement());
    URI portfolioGridUri = uriInfo.getAbsolutePathBuilder()
        .path(viewId)
        .path(ViewResource.class, "getPortfolioGrid")
        .build();
    URI primitivesGridUri = uriInfo.getAbsolutePathBuilder()
        .path(viewId)
        .path(ViewResource.class, "getPrimitivesGrid")
        .build();
    Principal userPrincipal = securityContext.getUserPrincipal();
    String userName = userPrincipal != null ? userPrincipal.getName() : null;
    ClientConnection connection = _connectionManager.getConnectionByClientId(userName, clientId);
    URI uri = uriInfo.getAbsolutePathBuilder().path(viewId).build();
    ImmutableMap<String, Object> callbackMap = ImmutableMap.<String, Object>of("id", requestId, "message", uri.getPath());
    URI errorUri = uriInfo.getAbsolutePathBuilder()
        .path(viewId)
        .path(ViewResource.class, "getErrors")
        .build();
    UserPrincipal ogUserPrincipal = userName != null ? UserPrincipal.getLocalUser(userName) : UserPrincipal.getTestUser();
    _viewManager.createView(viewRequest, clientId, ogUserPrincipal, connection, viewId, callbackMap,
                            portfolioGridUri.getPath(), primitivesGridUri.getPath(), errorUri.getPath());
    return Response.status(Response.Status.CREATED).build();
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

