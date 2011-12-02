/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import javax.time.Instant;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.LiveResultsService;
import com.opengamma.web.server.LiveResultsServiceBean;
import com.opengamma.web.server.WebView;

/**
 * Temporary resource to supplement the CometD-based LiveResultsService with a RESTful API for certain pieces of
 * functionality.
 */
@Path("/analytics")
public class WebAnalyticsResource {

  private final LiveResultsServiceBean _liveResultsServiceBean;
  
  // TODO this shouldn't be necessary once Cometd has gone
  public WebAnalyticsResource(LiveResultsServiceBean liveResultsServiceBean) {
    // Have to inject the wrapper here as the actual service is not initialised until after the Bayeux service is available 
    _liveResultsServiceBean = liveResultsServiceBean;
  }
  
  private LiveResultsService getLiveResultsService() {
    return _liveResultsServiceBean.getLiveResultsService();
  }

  @GET
  @Path("{clientId}/{gridName}")
  @Produces("text/csv")
  public Response getGridCsv(@PathParam("clientId") String clientId, @PathParam("gridName") String gridName) {
    WebView clientView = getLiveResultsService().getClientView(clientId);
    if (clientView == null) {
      return null;
    }
    Pair<Instant, String> csvResult = clientView.getGridContentsAsCsv(gridName);
    Instant valuationTime = csvResult.getFirst();
    String csv = csvResult.getSecond();
    String filename = clientView.getViewDefinitionId() + " - " + gridName + " - " + valuationTime + ".csv";
    return Response.ok(csv).header("content-disposition", "attachment; filename=\"" + filename + "\"").build();
  }
  
}
