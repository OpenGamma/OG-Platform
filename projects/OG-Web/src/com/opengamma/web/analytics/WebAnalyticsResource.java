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

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
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
   * The binder.
   */
  private final Binder _binder;
  /**
   * The supplier.
   */
  private final Supplier<LiveResultsService> _cachedBinder;

  // TODO this shouldn't be necessary once Cometd has gone
  public WebAnalyticsResource(LiveResultsServiceBean liveResultsServiceBean) {
    // Have to inject the wrapper here as the actual service is not initialised until after the Bayeux service is available 
    _binder = new Binder(liveResultsServiceBean);
    _cachedBinder = Suppliers.memoize(_binder);
  }

  @Override
  public void setServletContext(ServletContext servletContext) {
    _binder.setServletContext(servletContext);
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("{clientId}/{gridName}")
  @Produces("text/csv")
  public Response getGridCsv(@PathParam("clientId") String clientId, @PathParam("gridName") String gridName) {
    WebView clientView = _cachedBinder.get().getClientView(clientId);
    if (clientView == null) {
      return null;
    }
    Pair<Instant, String> csvResult = clientView.getGridContentsAsCsv(gridName);
    Instant valuationTime = csvResult.getFirst();
    String csv = csvResult.getSecond();
    String filename = clientView.getViewDefinitionId() + " - " + gridName + " - " + valuationTime + ".csv";
    return Response.ok(csv).header("content-disposition", "attachment; filename=\"" + filename + "\"").build();
  }

  /**
   * Binds the parts together, ensuring that the initialization is delayed until cometd starts.
   * The binder is used via the caching memoized supplier.
   */
  private static final class Binder implements Supplier<LiveResultsService> {
    private final LiveResultsServiceBean _liveResultsServiceBean;
    private ServletContext _servletContext;

    private Binder(LiveResultsServiceBean liveResultsServiceBean) {
      _liveResultsServiceBean = liveResultsServiceBean;
    }

    void setServletContext(ServletContext servletContext) {
      _servletContext = servletContext;  // cannot init here
    }

    @Override
    public LiveResultsService get() {
      _liveResultsServiceBean.init(_servletContext);
      return _liveResultsServiceBean.getLiveResultsService();
    }
  }

}
