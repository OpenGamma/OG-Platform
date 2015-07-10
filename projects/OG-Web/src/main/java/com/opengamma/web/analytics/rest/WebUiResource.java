/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewClientState;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.util.auth.AuthUtils;
import com.opengamma.util.rest.RestUtils;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.analytics.AnalyticsView;
import com.opengamma.web.analytics.AnalyticsViewManager;
import com.opengamma.web.analytics.ErrorInfo;
import com.opengamma.web.analytics.GridCell;
import com.opengamma.web.analytics.GridStructure;
import com.opengamma.web.analytics.MarketDataSpecificationJsonReader;
import com.opengamma.web.analytics.ValueRequirementTargetForCell;
import com.opengamma.web.analytics.ViewRequest;
import com.opengamma.web.analytics.ViewportDefinition;
import com.opengamma.web.analytics.ViewportResults;
import com.opengamma.web.analytics.formatting.TypeFormatter;
import com.opengamma.web.analytics.json.ValueRequirementFormParam;
import com.opengamma.web.analytics.push.ClientConnection;
import com.opengamma.web.analytics.push.ConnectionManager;

/**
 * REST resource for the analytics grid. This resource class specifies the endpoints of every object in the
 * hierarchy of grids, dependency graphs and viewports in the analytics viewer.
 */
@Path("views")
public class WebUiResource {

  private static final Logger s_logger = LoggerFactory.getLogger(WebUiResource.class);
  private static final DateTimeFormatter CSV_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");

  /** For generating IDs for the views. */
  private static final AtomicLong s_nextViewId = new AtomicLong(0);
  /** For generating IDs for the viewports and dependency graphs. */
  private static final AtomicInteger s_nextId = new AtomicInteger(0);

  /** For creating and retrieving views. */
  private final AnalyticsViewManager _viewManager;
  /** For looking up a client's connection. */
  private final ConnectionManager _connectionManager;

  public WebUiResource(AnalyticsViewManager viewManager, ConnectionManager connectionManager) {
    ArgumentChecker.notNull(viewManager, "viewManager");
    ArgumentChecker.notNull(connectionManager, "connectionManager");
    _viewManager = viewManager;
    _connectionManager = connectionManager;
  }

  @POST
  public Response createView(@Context UriInfo uriInfo,
                             @Context HttpServletRequest httpRequest,
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
    List<MarketDataSpecification> marketDataSpecs =
        MarketDataSpecificationJsonReader.buildSpecifications(marketDataProviders);
    VersionCorrection versionCorrection = VersionCorrection.of(parseInstant(portfolioVersionTime),
                                                               parseInstant(portfolioCorrectionTime));
    ViewRequest viewRequest = _viewManager.createViewRequest(UniqueId.parse(viewDefinitionId), aggregators, marketDataSpecs,
                                              parseInstant(valuationTime), versionCorrection, blotterColumns);
    String viewId = Long.toString(s_nextViewId.getAndIncrement());
    URI portfolioGridUri = uriInfo.getAbsolutePathBuilder()
        .path(viewId)
        .path("portfolio")
        .build();
    URI primitivesGridUri = uriInfo.getAbsolutePathBuilder()
        .path(viewId)
        .path("primitives")
        .build();
    
    String userName = (AuthUtils.isPermissive() ? null : AuthUtils.getUserName());
    ClientConnection connection = _connectionManager.getConnectionByClientId(userName, clientId);
    URI uri = uriInfo.getAbsolutePathBuilder().path(viewId).build();
    ImmutableMap<String, Object> callbackMap =
        ImmutableMap.<String, Object>of("id", requestId, "message", uri.getPath());
    URI errorUri = uriInfo.getAbsolutePathBuilder()
        .path(viewId)
        .path("errors")
        .build();
    // Get session id or create one
    String sessionId = "session-id:" + httpRequest.getSession().getId();
    // Track user principal using session id rather than ip address
    UserPrincipal ogUserPrincipal = userName != null ? new UserPrincipal(userName, sessionId) : UserPrincipal.getTestUser();
    _viewManager.createView(viewRequest, clientId, ogUserPrincipal, connection, viewId, callbackMap,
                            portfolioGridUri.getPath(), primitivesGridUri.getPath(), errorUri.getPath());
    return Response.status(Response.Status.CREATED).build();
  }

  @Path("{viewId}")
  @DELETE
  public void deleteView(@PathParam("viewId") String viewId) {
    _viewManager.deleteView(viewId);
  }

  @Path("{viewId}/pauseOrResume")
  @PUT
  public Response pauseOrResumeView(@PathParam("viewId") String viewId,
                                    @FormParam("state") String state) {
    ViewClient viewClient = _viewManager.getViewCient(viewId);
    state = StringUtils.stripToNull(state);
    Response response = Response.status(Response.Status.BAD_REQUEST).build();
    if (state != null) {
      ViewClientState currentState = viewClient.getState();
      state = state.toUpperCase();
      switch (state) {
        case "PAUSE":
        case "P":
          if (currentState != ViewClientState.TERMINATED) {
            viewClient.pause();
            response = Response.ok().build();
          }
          break;
        case "RESUME":
        case "R":
          if (currentState != ViewClientState.TERMINATED) {
            viewClient.resume();
            response = Response.ok().build();
          }
          break;
        default:
          s_logger.warn("client {} requesting for invalid view client state change to {}", viewId, state);
          response = Response.status(Response.Status.BAD_REQUEST).build();
          break;
      }
    }
    return response;
  }

  @Path("{viewId}/{gridType}")
  @GET
  public GridStructure getGridStructure(@PathParam("viewId") String viewId,
                                        @PathParam("gridType") String gridType) {
    return _viewManager.getView(viewId).getInitialGridStructure(gridType(gridType));
  }

  @Path("{viewId}/{gridType}/viewports/{viewportId}/valuereq/{row}/{col}")
  @GET
  public ValueRequirementTargetForCell getValueRequirementForTargetForCell(@PathParam("viewId") String viewId,
                                                                           @PathParam("gridType") String gridType,
                                                                           @PathParam("row") int row,
                                                                           @PathParam("col") int col,
                                                                           @PathParam("viewportId") int viewportId) {

    GridStructure gridStructure =  _viewManager.getView(viewId).getGridStructure(gridType(gridType), viewportId);

    Pair<String, ValueRequirement> pair = gridStructure.getValueRequirementForCell(row, col);
    return new ValueRequirementTargetForCell(pair.getFirst(), pair.getSecond());

  }

  @Path("{viewId}/{gridType}/viewports")
  @POST
  public Response createViewport(@Context UriInfo uriInfo,
                                 @PathParam("viewId") String viewId,
                                 @PathParam("gridType") String gridType,
                                 @FormParam("requestId") int requestId,
                                 @FormParam("version") int version,
                                 @FormParam("rows") List<Integer> rows,
                                 @FormParam("columns") List<Integer> columns,
                                 @FormParam("cells") List<GridCell> cells,
                                 @FormParam("format") TypeFormatter.Format format,
                                 @FormParam("enableLogging") Boolean enableLogging) {
    ViewportDefinition viewportDefinition = ViewportDefinition.create(version, rows, columns, cells, format, enableLogging);
    int viewportId = s_nextId.getAndIncrement();
    String viewportIdStr = Integer.toString(viewportId);
    UriBuilder viewportUriBuilder = uriInfo.getAbsolutePathBuilder().path(viewportIdStr);
    String callbackId = viewportUriBuilder.build().getPath();
    String structureCallbackId = viewportUriBuilder.path("structure").build().getPath();
    _viewManager.getView(viewId).createViewport(requestId,
                                                gridType(gridType),
                                                viewportId,
                                                callbackId,
                                                structureCallbackId,
                                                viewportDefinition);
    return Response.status(Response.Status.CREATED).build();
  }

  @Path("{viewId}/{gridType}/viewports/{viewportId}")
  @PUT
  public void updateViewport(@PathParam("viewId") String viewId,
                             @PathParam("gridType") String gridType,
                             @PathParam("viewportId") int viewportId,
                             @FormParam("version") int version,
                             @FormParam("rows") List<Integer> rows,
                             @FormParam("columns") List<Integer> columns,
                             @FormParam("cells") List<GridCell> cells,
                             @FormParam("format") TypeFormatter.Format format,
                             @FormParam("enableLogging") Boolean enableLogging) {
    ViewportDefinition viewportDef = ViewportDefinition.create(version, rows, columns, cells, format, enableLogging);
    _viewManager.getView(viewId).updateViewport(gridType(gridType), viewportId, viewportDef);
  }

  @Path("{viewId}/{gridType}/viewports/{viewportId}/structure")
  @GET
  public GridStructure getViewportGridStructure(@PathParam("viewId") String viewId,
                                                @PathParam("gridType") String gridType,
                                                @PathParam("viewportId") int viewportId) {
    return _viewManager.getView(viewId).getGridStructure(gridType(gridType), viewportId);
  }

  @Path("{viewId}/{gridType}/viewports/{viewportId}")
  @GET
  public ViewportResults getViewportData(@PathParam("viewId") String viewId,
                                         @PathParam("gridType") String gridType,
                                         @PathParam("viewportId") int viewportId) {
    return _viewManager.getView(viewId).getData(gridType(gridType), viewportId);
  }

  @Path("{viewId}/{gridType}/viewports/{viewportId}")
  @DELETE
  public void deleteViewport(@PathParam("viewId") String viewId,
                             @PathParam("gridType") String gridType,
                             @PathParam("viewportId") int viewportId) {
    _viewManager.getView(viewId).deleteViewport(gridType(gridType), viewportId);
  }

  @Path("{viewId}/{gridType}/depgraphs")
  @POST
  public Response openDependencyGraph(@Context UriInfo uriInfo,
                                      @PathParam("viewId") String viewId,
                                      @PathParam("gridType") String gridType,
                                      @FormParam("requestId") int requestId,
                                      @FormParam("row") Integer row,
                                      @FormParam("col") Integer col,
                                      @FormParam("colset") String calcConfigName,
                                      @FormParam("req") ValueRequirementFormParam valueRequirementParam) {
    int graphId = s_nextId.getAndIncrement();
    String graphIdStr = Integer.toString(graphId);
    URI graphUri = uriInfo.getAbsolutePathBuilder().path(graphIdStr).build();
    String callbackId = graphUri.getPath();
    if (row != null && col != null) {
      _viewManager.getView(viewId).openDependencyGraph(requestId, gridType(gridType), graphId, callbackId, row, col);
    } else if (calcConfigName != null && valueRequirementParam != null) {
      ValueRequirement valueRequirement = valueRequirementParam.getValueRequirement();
      _viewManager.getView(viewId).openDependencyGraph(requestId,
                                                       gridType(gridType),
                                                       graphId,
                                                       callbackId,
                                                       calcConfigName,
                                                       valueRequirement);
    }
    return Response.status(Response.Status.CREATED).build();
  }

  @Path("{viewId}/{gridType}/depgraphs/{depgraphId}")
  @GET
  public GridStructure getDependencyGraphGridStructure(@PathParam("viewId") String viewId,
                                                       @PathParam("gridType") String gridType,
                                                       @PathParam("depgraphId") int depgraphId) {
    return _viewManager.getView(viewId).getInitialGridStructure(gridType(gridType), depgraphId);
  }

  @Path("{viewId}/{gridType}/depgraphs/{depgraphId}")
  @DELETE
  public void deleteDependencyGraph(@PathParam("viewId") String viewId,
                                    @PathParam("gridType") String gridType,
                                    @PathParam("depgraphId") int depgraphId) {
    _viewManager.getView(viewId).closeDependencyGraph(gridType(gridType), depgraphId);
  }

  @Path("{viewId}/{gridType}/depgraphs/{depgraphId}/viewports")
  @POST
  public Response createDependencyGraphViewport(@Context UriInfo uriInfo,
                                                @PathParam("viewId") String viewId,
                                                @PathParam("gridType") String gridType,
                                                @PathParam("depgraphId") int depgraphId,
                                                @FormParam("requestId") int requestId,
                                                @FormParam("version") int version,
                                                @FormParam("rows") List<Integer> rows,
                                                @FormParam("columns") List<Integer> columns,
                                                @FormParam("cells") List<GridCell> cells,
                                                @FormParam("format") TypeFormatter.Format format,
                                                @FormParam("enableLogging") Boolean enableLogging) {
    ViewportDefinition viewportDefinition = ViewportDefinition.create(version, rows, columns, cells, format, enableLogging);
    int viewportId = s_nextId.getAndIncrement();
    String viewportIdStr = Integer.toString(viewportId);
    UriBuilder viewportUriBuilder = uriInfo.getAbsolutePathBuilder().path(viewportIdStr);
    String callbackId = viewportUriBuilder.build().getPath();
    String structureCallbackId = viewportUriBuilder.path("structure").build().getPath();
    _viewManager.getView(viewId).createViewport(requestId,
                                                gridType(gridType),
                                                depgraphId,
                                                viewportId,
                                                callbackId,
                                                structureCallbackId,
                                                viewportDefinition);
    return Response.status(Response.Status.CREATED).build();
  }

  @Path("{viewId}/{gridType}/depgraphs/{depgraphId}/viewports/{viewportId}")
  @PUT
  public void updateDependencyGraphViewport(@PathParam("viewId") String viewId,
                                            @PathParam("gridType") String gridType,
                                            @PathParam("depgraphId") int depgraphId,
                                            @PathParam("viewportId") int viewportId,
                                            @FormParam("version") int version,
                                            @FormParam("rows") List<Integer> rows,
                                            @FormParam("columns") List<Integer> columns,
                                            @FormParam("cells") List<GridCell> cells,
                                            @FormParam("format") TypeFormatter.Format format,
                                            @FormParam("enableLogging") Boolean enableLogging) {
    ViewportDefinition viewportDef = ViewportDefinition.create(version, rows, columns, cells, format, enableLogging);
    _viewManager.getView(viewId).updateViewport(gridType(gridType), depgraphId, viewportId, viewportDef);
  }

  @Path("{viewId}/{gridType}/depgraphs/{depgraphId}/viewports/{viewportId}/structure")
  @GET
  public GridStructure getDependencyGraphViewportGridStructure(@PathParam("viewId") String viewId,
                                                               @PathParam("gridType") String gridType,
                                                               @PathParam("depgraphId") int depgraphId,
                                                               @PathParam("viewportId") int viewportId) {
    GridStructure g = _viewManager.getView(viewId).getGridStructure(gridType(gridType), depgraphId, viewportId);
    return g;
  }

  @Path("{viewId}/{gridType}/depgraphs/{depgraphId}/viewports/{viewportId}")
  @GET
  public ViewportResults getDependencyGraphViewportData(@PathParam("viewId") String viewId,
                                                        @PathParam("gridType") String gridType,
                                                        @PathParam("depgraphId") int depgraphId,
                                                        @PathParam("viewportId") int viewportId) {
    return _viewManager.getView(viewId).getData(gridType(gridType), depgraphId, viewportId);
  }

  @Path("{viewId}/{gridType}/depgraphs/{depgraphId}/viewports/{viewportId}")
  @DELETE
  public void deleteDependencyGraphViewport(@PathParam("viewId") String viewId,
                                            @PathParam("gridType") String gridType,
                                            @PathParam("depgraphId") int depgraphId,
                                            @PathParam("viewportId") int viewportId) {
    _viewManager.getView(viewId).deleteViewport(gridType(gridType), depgraphId, viewportId);
  }

  @Path("{viewId}/errors")
  @GET
  public List<ErrorInfo> getErrors(@PathParam("viewId") String viewId) {
    return _viewManager.getView(viewId).getErrors();
  }


  @Path("{viewId}/errors/{errorId}")
  @DELETE
  public void deleteError(@PathParam("viewId") String viewId, @PathParam("errorId") long errorId) {
    _viewManager.getView(viewId).deleteError(errorId);
  }

  /**
   * Produces view port results as CSV
   *
   * @param response the injected servlet response, not null.
   * @param viewId ID of the view
   * @param gridTypeStr the grid type, 'portfolio' or 'primitives'
   * @return The view port result as csv
   */
  @GET
  @Path("{viewId}/{gridType}/data")
  @Produces(RestUtils.TEXT_CSV)
  public ViewportResults getViewportResultAsCsv(@PathParam("viewId") String viewId,
                                                @PathParam("gridType") String gridTypeStr,
                                                @Context HttpServletResponse response) {
    AnalyticsView view = _viewManager.getView(viewId);
    AnalyticsView.GridType gridType = gridType(gridTypeStr);
    ViewportResults result = view.getAllGridData(gridType, TypeFormatter.Format.CELL);
    Instant valuationTime;
    if (result.getValuationTime() != null) {
      valuationTime = result.getValuationTime();
    } else {
      valuationTime = OpenGammaClock.getInstance().instant();
    }
    LocalDateTime time = LocalDateTime.ofInstant(valuationTime, OpenGammaClock.getZone());

    String filename = String.format("%s-%s-%s.csv",
                                    view.getViewDefinitionId(),
                                    gridType.name().toLowerCase(),
                                    time.toString(CSV_TIME_FORMAT));
    response.addHeader("content-disposition", "attachment; filename=\"" + filename + "\"");
    return view.getAllGridData(gridType, TypeFormatter.Format.CELL);
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

  private static AnalyticsView.GridType gridType(String gridType) {
    return AnalyticsView.GridType.valueOf(gridType.toUpperCase());
  }
}
