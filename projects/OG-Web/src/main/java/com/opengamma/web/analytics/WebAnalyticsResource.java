/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import javax.servlet.ServletContext;
import javax.time.Instant;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.springframework.web.context.ServletContextAware;

import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.LiveResultsService;
import com.opengamma.web.server.LiveResultsServiceBean;
import com.opengamma.web.server.WebView;

/**
 * Temporary resource to supplement the CometD-based LiveResultsService with a RESTful API
 * for certain pieces of functionality.
 */
@Path("/analytics")
public class WebAnalyticsResource implements ServletContextAware {

  /**
   * Attribute name for the servlet context.
   */
  public static final String LIVE_RESULTS_SERVICE_ATTRIBUTE = WebAnalyticsResource.class.getName() + ".LiveResultsServiceBean";
  /**
   * The binder.
   */
  private final LiveResultsServiceBean _liveResultsServiceBean;

  /**
   * Initializes the resource.
   * 
   * @param liveResultsServiceBean  the bean, not null
   */
  public WebAnalyticsResource(LiveResultsServiceBean liveResultsServiceBean) {
    // TODO this shouldn't be necessary once Cometd has gone
    // Have to inject the wrapper here as the actual service is not initialised until after the Bayeux service is available 
    _liveResultsServiceBean = liveResultsServiceBean;
  }

  @Override
  public void setServletContext(ServletContext servletContext) {
    // this is picked up by the WebAnalyticsBayeuxInitializer
    servletContext.setAttribute(LIVE_RESULTS_SERVICE_ATTRIBUTE, _liveResultsServiceBean);
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("{clientId}/{gridName}")
  @Produces("text/csv")
  public Response getGridCsv(@PathParam("clientId") String clientId, @PathParam("gridName") String gridName) {
    LiveResultsService liveResultsService = _liveResultsServiceBean.getLiveResultsService();
    if (liveResultsService == null) {
      throw new IllegalStateException("LiveResultsService not initialized");
    }
    WebView clientView = liveResultsService.getClientView(clientId);
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
