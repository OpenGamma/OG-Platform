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

import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.push.ConnectionManagerImpl;
import com.opengamma.web.server.push.analytics.AnalyticsViewListener;
import com.opengamma.web.server.push.analytics.AnalyticsViewManager;
import com.opengamma.web.server.push.analytics.ViewRequest;

/**
 *
 */
@Path("views")
public class ViewsResource {

  private static final AtomicLong s_nextViewId = new AtomicLong(0);

  private final AnalyticsViewManager _viewManager;
  // TODO listen for connections closing and close any associated views?
  // shutdown listener on connection?
  private final ConnectionManagerImpl _connectionManager;

  public ViewsResource(AnalyticsViewManager viewManager, ConnectionManagerImpl connectionManager) {
    ArgumentChecker.notNull(viewManager, "viewManager");
    ArgumentChecker.notNull(connectionManager, "connectionManager");
    _viewManager = viewManager;
    _connectionManager = connectionManager;
  }

  @Path("{viewId}")
  public ViewResource getView(@PathParam("viewId") String viewId) {
    return new ViewResource(_viewManager.getView(viewId), _viewManager, viewId);
  }

  @POST
  public Response createView(@Context UriInfo uriInfo,
                             @FormParam("viewDefinitionId") String viewDefinitionId,
                             @FormParam("aggregators") List<String> aggregators,
                             @FormParam("live") boolean live,
                             @FormParam("provider") String provider,
                             @FormParam("snapshotId") String snapshotId,
                             @FormParam("versionDateTime") String versionDateTime,
                             @FormParam("clientId") String clientId) {
    ViewRequest.MarketData marketData;
    if (live) {
      marketData = new ViewRequest.Live(provider);
    } else {
      VersionCorrection versionCorrection;
      if (versionDateTime != null) {
        versionCorrection = VersionCorrection.ofVersionAsOf(Instant.parse(versionDateTime));
      } else {
        versionCorrection = VersionCorrection.LATEST;
      }
      marketData = new ViewRequest.Snapshot(UniqueId.parse(snapshotId), versionCorrection);
    }
    ViewRequest viewRequest = new ViewRequest(UniqueId.parse(viewDefinitionId), aggregators, marketData);
    String viewId = Long.toString(s_nextViewId.getAndIncrement());
    URI portfolioGridUri = uriInfo.getAbsolutePathBuilder()
        .path(viewId)
        .path(ViewResource.class, "getPortfolioGrid")
        .path(AbstractGridResource.class, "getGridStructure").build();
    URI primitivesGridUri = uriInfo.getAbsolutePathBuilder()
        .path(viewId)
        .path(ViewResource.class, "getPrimitivesGrid")
        .path(AbstractGridResource.class, "getGridStructure").build();
    // TODO this is very obviously wrong - where can I get the user?
    UserPrincipal user = UserPrincipal.getTestUser();
    String userName = null;
    //String userName = user.getUserName();
    AnalyticsViewListener listener = _connectionManager.getConnectionByClientId(userName, clientId);
    _viewManager.createView(viewRequest,
                            user,
                            listener,
                            viewId,
                            portfolioGridUri.getPath(),
                            primitivesGridUri.getPath());
    URI uri = uriInfo.getAbsolutePathBuilder().path(viewId).build();
    return Response.status(Response.Status.CREATED).header(HttpHeaders.LOCATION, uri).build();
  }
}
