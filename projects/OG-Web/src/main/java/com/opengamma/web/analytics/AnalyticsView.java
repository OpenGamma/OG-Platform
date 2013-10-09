/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.id.UniqueId;
import com.opengamma.web.analytics.formatting.TypeFormatter;

/**
 * This is the top level object of the back-end of the the analytics user interface.
 * A view displays analytics data for a view definition and a set of market data
 * (e.g. live, historical etc). A view manages two grids of data, one displaying the
 * portfolio analytics and the other displaying primitives (e.g. curves, surfaces).
 * Each of these grids can have any number of viewports which represent the portion
 * of the grid that the user is viewing.
 * <p>
 * Each top level grid can have also have any number of dependency graph grids
 * which show how the data in a grid cell is calculated.
 */
public interface AnalyticsView {

  /**
   * The type of grid.
   */
  public enum GridType {
    /**
     * Portfolio grid.
     */
    PORTFOLIO,
    /**
     * Primitives grid.
     */
    PRIMITIVES
  }

  /**
   * Updates the grid structures when the view definition compiles and its structure is available.
   * 
   * @param compiledViewDefinition  the compiled view definition whose data will be displayed in the grids
   * @param resolvedPortfolio  the view's portfolio with all securities resolved
   * @return the callback IDs of grids that were updated
   */
  List<String> updateStructure(CompiledViewDefinition compiledViewDefinition, Portfolio resolvedPortfolio);

  /**
   * Invoked if the view can't be built and started.
   *
   * @param t Exception that triggered the failure, possibly null
   * @return Callback ID of the error
   */
  String viewCompilationFailed(Throwable t);

  /**
   * Updates the data in the grids when a cycle completes in the calculation engine.
   * 
   * @param results  the results of the calculation cycle
   * @param viewCycle  the data associated with the calculation cycle
   * @return the callback IDs of the viewports whose data changed
   */
  List<String> updateResults(ViewResultModel results, ViewCycle viewCycle);

// -------- main grid --------

  /**
   * Returns the row and column structure of one of the top level grids.
   * 
   * @param gridType  the required grid structure, not null
   * @param viewportId  the ID of the viewport
   * @return the row and column structure of the specified grid
   */
  GridStructure getGridStructure(GridType gridType, int viewportId);

  /**
   * Returns the initial row and column structure of one of the top level grids.
   *
   * @param gridType  the required grid structure, not null
   * @return the row and column structure of the specified grid
   */
  GridStructure getInitialGridStructure(GridType gridType);

  /**
   * Creates a viewport for one of the top level grids. A viewport represents the visible portion of the grid. Any
   * grid cells that are scrolled off the screen are not part of the viewport. The client only receives data for
   * cells in the viewport and only receives notification of new data if data in the viewport changes. If the only data
   * that changes in a calculation cycle is not part of a viewport then no update needs to be sent to the client.
   * There can be any number of viewports for each grid.
   *
   * @param requestId  the request id
   * @param gridType  the required grid structure, not null
   * @param viewportId  the unique ID for the viewport, unique for each viewport in a view
   * @param callbackId  the value that is sent to the client with notification that new data is available for the
   *  viewport. The server makes no assumptions about its format other than the fact that it must be unique for each
   *  viewport in a view.
   * @param structureCallbackId  the value that is sent to the client with notification that new structure is available
   *  for the viewport. The server makes no assumptions about its format other than the fact that it must be unique for each
   *  viewport in a view.
   * @param viewportDefinition  defines the rows and columns in the viewport and whether the viewport's data should be
   *  expanded or a summary for data types which can't fit in a cell, e.g. vectors, matrices, curves.
   * @return true if there is data available for the new viewport
   */
  boolean createViewport(int requestId, GridType gridType, int viewportId, String callbackId, String structureCallbackId, ViewportDefinition viewportDefinition);

  /**
   * Updates a viewport. A viewport will be updated when the user scrolls the grid.
   *
   * @param gridType  the required grid structure, not null
   * @param viewportId  the ID of the viewport
   * @param viewportDefinition Defines the rows and columns in the viewport and whether the viewport's data should be
   * expanded or a summary for data types which can't fit in a cell, e.g. vectors, matrices, curves.
   * @return The viewport's callback ID if there is data available, null if not
   */
  String updateViewport(GridType gridType, int viewportId, ViewportDefinition viewportDefinition);

  /**
   * Deletes a viewport.
   * 
   * @param gridType  the required grid structure, not null
   * @param viewportId  the ID of the viewport
   */
  void deleteViewport(GridType gridType, int viewportId);

  /**
   * Returns the current data for a viewport.
   * 
   * @param gridType  the required grid structure, not null
   * @param viewportId  the ID of the viewport
   * @return the current data for the viewport
   */
  ViewportResults getData(GridType gridType, int viewportId);

  // -------- dependency graph grids --------

  // TODO specifying by row and col is a problem for two reasons
  // 1) if the structure changes we don't know if the cell has moved and where to
  // 2) there's a race condition if the structure changes as the client requests a depgraph - they might not get what they want
  // would be better to specify the row and col in a way that persists across recompilation of the view def
  // i.e. specify the target spec
  // for now send a version ID to the client so it can tell the data is stale? or have the client supply the ID of the
  // structure and perform that logic on the server?

  /**
   * Returns the grid structure for a dependency graph grid.
   *
   * @param gridType  the grid that the dependency graph grid belongs to
   * @param graphId  the ID of the dependency graph
   * @param viewportId  the ID of the viewport
   * @return the row and column structure of the grid
   */
  GridStructure getGridStructure(GridType gridType, int graphId, int viewportId);

  /**
   * Returns the initial grid structure for a dependency graph grid.
   *
   * @param gridType  the grid that the dependency graph grid belongs to
   * @param graphId  the ID of the dependency graph
   * @return the row and column structure of the grid
   */
  GridStructure getInitialGridStructure(GridType gridType, int graphId);


  /**
   * Opens a grid showing the dependency graph of calculations for a cell in one of the main grids.
   * TODO should include the structure version otherwise there's a minor race condition
   * 
   * @param requestId  the ID of the request
   * @param gridType Specifies which of the main grids
   * @param graphId A unique ID for the dependency graph grid
   * @param callbackId A value that is sent to the client with notification that the structure has changed.
   * The server makes no assumptions about its format other than the fact that it must be unique for each grid in a view.
   * @param row The row of the cell whose dependency graph should be opened
   * @param col The column of the cell whose dependency graph should be opened
   */
  void openDependencyGraph(int requestId, GridType gridType, int graphId, String callbackId, int row, int col);

  /**
   * Opens a grid showing the dependency graph of calculations for a cell in one of the main grids. This is used by
   * the client to reconnect after a server restart when the server has lost all the view state.
   *
   * @param requestId  the ID of the request
   * @param gridType Specifies which of the main grids
   * @param graphId A unique ID for the dependency graph grid
   * @param callbackId A value that is sent to the client with notification that the structure has changed.
   * The server makes no assumptions about its format other than the fact that it must be unique for each grid in a view.
   * @param calcConfigName Name of the calculation configuration containing the value we're interested in
   * @param valueRequirement Requirement which requests the value we're interested in
   */
  void openDependencyGraph(int requestId,
                           GridType gridType,
                           int graphId,
                           String callbackId,
                           String calcConfigName,
                           ValueRequirement valueRequirement);

  /**
   * Closes a dependency graph.
   * 
   * @param gridType  the grid that the dependency graph grid belongs to
   * @param graphId  the ID of the dependency graph
   */
  void closeDependencyGraph(GridType gridType, int graphId);

  /**
   * Creates a viewport for a dependency graph grid. A viewport represents the visible portion of the grid. Any
   * grid cells that are scrolled off the screen are not part of the viewport. The client only receives data for
   * cells in the viewport and only receives notification of new data if data in the viewport changes. If the only data
   * that changes in a calculation cycle is not part of a viewport then no update needs to be sent to the client.
   * There can be any number of viewports for each grid.
   *
   * @param requestId  the ID of the request
   * @param gridType  the grid that the dependency graph grid belongs to
   * @param graphId  the ID of the dependency graph
   * @param viewportId  the unique ID for the viewport
   * @param callbackId  the value that is sent to the client with notification that new data is available for the
   *  viewport. The server makes no assumptions about its format other than the fact that it must be unique for each
   *  viewport in a view.
   * @param structureCallbackId  the value that is sent to the client with notification that new structure is available
   *  for the viewport. The server makes no assumptions about its format other than the fact that it must be unique for
   *  each viewport in a view.
   * @param viewportDefinition  defines the rows and columns in the viewport and whether the viewport's data should be
   *  expanded or a summary for data types which can't fit in a cell, e.g. vectors, matrices, curves.
   * @return true if there is data available for the new viewport
   */
  boolean createViewport(int requestId, GridType gridType, int graphId, int viewportId, String callbackId, String structureCallbackId, ViewportDefinition viewportDefinition);

  /**
   * Updates a viewport of a dependency graph grid. A viewport will be updated when the user scrolls the grid.
   *
   * @param gridType  the grid that the dependency graph grid belongs to
   * @param graphId  the ID of the dependency graph
   * @param viewportId  the ID of the viewport
   * @param viewportDefinition  defines the rows and columns in the viewport and whether the viewport's data should be
   *  expanded or a summary for data types which can't fit in a cell, e.g. vectors, matrices, curves.
   * @return the viewport's callback ID if there is data available, null if not
   */
  String updateViewport(GridType gridType, int graphId, int viewportId, ViewportDefinition viewportDefinition);

  /**
   * Deletes a viewport from a dependency graph grid.
   * 
   * @param gridType  the grid that the dependency graph grid belongs to
   * @param graphId  the ID of the dependency graph
   * @param viewportId  the ID of the viewport
   */
  void deleteViewport(GridType gridType, int graphId, int viewportId);

  /**
   * Returns the current data for a viewport of a dependency graph grid.
   * 
   * @param gridType  the grids that the dependency graph grid belongs to
   * @param graphId  the ID of the dependency graph
   * @param viewportId  the ID of the viewport
   * @return the current data for the viewport
   */
  ViewportResults getData(GridType gridType, int graphId, int viewportId);

  List<String> entityChanged(MasterChangeNotification<?> notification);

  List<String> portfolioChanged();
  
  /**
   * Returns the current data for all cells in a grid without publishing it.
   * 
   * @param gridType  the grid type, not null.
   * @param format  the type formatter type, not null.
   * @return the current data for the viewport
   */
  ViewportResults getAllGridData(GridType gridType, TypeFormatter.Format format);
  
  /**
   * Gets the id of the view definition that produces this analytics view.
   * 
   * @return the view definition unique id.
   */
  UniqueId getViewDefinitionId();

  /**
   * Returns information about an error that occurred in the server
   * @return The error, not null
   * @throws DataNotFoundException If the ID is unknown
   */
  List<ErrorInfo> getErrors();

  /**
   * Deletes an error that a client is no longer interested in
   *
   * @param id The error ID. This is pushed to the client as a notification
   * @throws DataNotFoundException If the ID is unknown
   */
  void deleteError(long id);
}
