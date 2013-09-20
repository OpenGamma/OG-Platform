/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import java.net.URI;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.util.rest.RestUtils;
import com.opengamma.web.analytics.AnalyticsView;
import com.opengamma.web.analytics.GridStructure;
import com.opengamma.web.analytics.ViewportDefinition;
import com.opengamma.web.analytics.ViewportResults;
import com.opengamma.web.analytics.formatting.TypeFormatter.Format;

/**
 * The @Path at this point is "views/{viewId}/{gridType}/"
 * for example "/jax/views/2/primitives"
 * @deprecated in favour of {@link WebUiResource}
 */
@Deprecated
public class MainGridResource extends AbstractGridResource implements DependencyGraphOwnerResource {
  
  private static final DateTimeFormatter CSV_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");

  public MainGridResource(AnalyticsView.GridType gridType, AnalyticsView view) {
    super(gridType, view);
  }

  /**
   * @return The initial row and column structure of the grid
   * subsequent requests will need to be made to the viewport
   */
  @Override
  public GridStructure getInitialGridStructure() {
    return getView().getInitialGridStructure(getGridType());
  }

  @Override
  /* package */ void createViewport(int requestId, int viewportId, String callbackId, String structureCallbackId, ViewportDefinition viewportDefinition) {
    getView().createViewport(requestId, getGridType(), viewportId, callbackId, structureCallbackId, viewportDefinition);
  }

  @Override
  public AbstractViewportResource getViewport(int viewportId) {
    return new MainGridViewportResource(getGridType(), getView(), viewportId);
  }

  @Override
  public Response openDependencyGraph(UriInfo uriInfo, int requestId, int row, int col) {
    int graphId = s_nextId.getAndIncrement();
    String graphIdStr = Integer.toString(graphId);
    URI graphUri = uriInfo.getAbsolutePathBuilder().path(graphIdStr).build();
    String callbackId = graphUri.getPath();
    getView().openDependencyGraph(requestId, getGridType(), graphId, callbackId, row, col);
    return Response.status(Response.Status.CREATED).build();
  }

  @Override
  public AbstractGridResource getDependencyGraph(int graphId) {
    return new DependencyGraphResource(getGridType(), getView(), graphId);
  }
  
  /**
   * Produces view port results as CSV
   * 
   * @param response the injected servlet response, not null.
   * @return The view port result as csv
   */
  @GET
  @Path("data")
  @Produces(RestUtils.TEXT_CSV)
  public ViewportResults getViewportResultAsCsv(@Context HttpServletResponse response) {
    ArgumentChecker.notNull(response, "response");
    
    ViewportResults result = getView().getAllGridData(getGridType(), Format.CELL);
    Instant valuationTime = result.getValuationTime() == null ? OpenGammaClock.getInstance().instant() : result.getValuationTime();
    LocalDateTime time = LocalDateTime.ofInstant(valuationTime, OpenGammaClock.getZone());
    
    String filename = String.format("%s-%s-%s.csv", getView().getViewDefinitionId(), getGridType().name().toLowerCase(), time.toString(CSV_TIME_FORMAT));
    response.addHeader("content-disposition", "attachment; filename=\"" + filename + "\"");
    return getView().getAllGridData(getGridType(), Format.CELL);
  }
}
