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
 *
 */
@Path("views/{viewId}/{gridType}/")
public class MainGridResource extends AbstractGridResource implements DependencyGraphOwnerResource {
  
  private static final DateTimeFormatter CSV_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");

  public MainGridResource(AnalyticsView.GridType gridType, AnalyticsView view) {
    super(gridType, view);
  }

  @Override
  public GridStructure getGridStructure() {
    return _view.getGridStructure(_gridType);
  }

  @Override
  /* package */ void createViewport(int requestId, int viewportId, String callbackId, ViewportDefinition viewportDefinition) {
    _view.createViewport(requestId, _gridType, viewportId, callbackId, viewportDefinition);
  }

  @Override
  public AbstractViewportResource getViewport(int viewportId) {
    return new MainGridViewportResource(_gridType, _view, viewportId);
  }

  @Override
  public Response openDependencyGraph(UriInfo uriInfo, int requestId, int row, int col) {
    int graphId = s_nextId.getAndIncrement();
    String graphIdStr = Integer.toString(graphId);
    URI graphUri = uriInfo.getAbsolutePathBuilder().path(graphIdStr).build();
    String callbackId = graphUri.getPath();
    _view.openDependencyGraph(requestId, _gridType, graphId, callbackId, row, col);
    return Response.status(Response.Status.CREATED).build();
  }

  @Override
  public AbstractGridResource getDependencyGraph(int graphId) {
    return new DependencyGraphResource(_gridType, _view, graphId);
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
    
    ViewportResults result = _view.getAllGridData(_gridType, Format.CELL);
    Instant valuationTime = result.getValuationTime() == null ? OpenGammaClock.getInstance().instant() : result.getValuationTime();
    LocalDateTime time = LocalDateTime.ofInstant(valuationTime, OpenGammaClock.getZone());
    
    String filename = String.format("%s-%s-%s.csv", _view.getViewDefinitionId(), _gridType.name().toLowerCase(), time.toString(CSV_TIME_FORMAT));
    response.addHeader("content-disposition", "attachment; filename=\"" + filename + "\"");
    return _view.getAllGridData(_gridType, Format.CELL);
  }
}
