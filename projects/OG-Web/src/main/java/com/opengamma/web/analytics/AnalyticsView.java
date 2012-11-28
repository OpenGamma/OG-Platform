/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;

/**
 * <p>This is the top level object of the back-end of the the analytics user interface. A view displays analytics data
 * for a view definition and a set of market data (e.g. live, historical etc). A view manages two grids of data, one
 * displaying the portfolio analytics and the other displaying primitives (e.g. curves, surfaces). Each of these grids
 * can have any number of viewports which represent the portion of the grid that the user is viewing.</p>
 * <p>Each top level grid can have also have any number of dependency graph grids which show how the data in
 * a grid cell is calculated.</p>
 */
public interface AnalyticsView {

  public enum GridType {
    PORTFORLIO,
    PRIMITIVES
  }

  /**
   * Updates the grid structures when the view definition compliles and its struture is available.
   * @param compiledViewDefinition The compiled view definition whose data will be displayed in the grids
   * @return Callback IDs of grids that were updated
   */
  List<String> updateStructure(CompiledViewDefinition compiledViewDefinition);

  /**
   * Updates the data in the grids when a cycle completes in the calculation engine.
   * @param results The results of the calculation cycle.
   * @param viewCycle Data associated with the calculation cycle.
   * @return Callback IDs of the viewports whose data changed
   */
  List<String> updateResults(ViewResultModel results, ViewCycle viewCycle);

// -------- main grid --------

  /**
   * Returns the row and column structure of one of the top level grids.
   * @param gridType Specifies which grid structure is required.
   * @return The row and column structure of the specified grid.
   */
  GridStructure getGridStructure(GridType gridType);

  /**
   * Creates a viewport for one of the top level grids. A viewport represents the visible portion of the grid. Any
   * grid cells that are scrolled off the screen are not part of the viewport. The client only receives data for
   * cells in the viewport and only receives notification of new data if data in the viewport changes. If the only data
   * that changes in a calculation cycle is not part of a viewport then no update needs to be sent to the client.
   * There can be any number of viewports for each grid.
   *
   * @param gridType Specifies the grid.
   * @param viewportId A unique ID for the viewport
   * than the fact that it must be unique for each viewport in a view.
   * @param callbackId A value that is sent to the client with notification that new data is available for the
   * viewport. The server makes no assumptions about its format other than the fact that it must be unique for each
   * viewport in a view.
   * @param viewportDefinition Defines the rows and columns in the viewport and whether the viewport's data should be
   * expanded or a summary for data types which can't fit in a cell, e.g. vectors, matrices, curves.
   * @return true if there is data available for the new viewport
   */
  boolean createViewport(int requestId, GridType gridType, int viewportId, String callbackId, ViewportDefinition viewportDefinition);

  /**
   * Updates a viewport. A viewport will be updated when the user scrolls the grid.
   *
   * @param gridType Specifies the grid
   * @param viewportId ID of the viewport
   * @param viewportDefinition Defines the rows and columns in the viewport and whether the viewport's data should be
   * expanded or a summary for data types which can't fit in a cell, e.g. vectors, matrices, curves.
   * @return The viewport's callback ID if there is data available, null if not
   */
  String updateViewport(GridType gridType, int viewportId, ViewportDefinition viewportDefinition);

  /**
   * Deletes a viewport.
   * @param gridType Specifies the grid
   * @param viewportId ID of the viewport
   */
  void deleteViewport(GridType gridType, int viewportId);

  /**
   * Returns the current data for a viewport.
   * @param gridType Specifies the grid
   * @param viewportId ID of the viewport
   * @return The current data for the viewport.
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
   * Opens a grid showing the dependency graph of calculations for a cell in one of the main grids.
   * @param gridType Specifies which of the main grids
   * @param graphId A unique ID for the dependency graph grid
   * @param callbackId A value that is sent to the client with notification that the structure has changed.
   * The server makes no assumptions about its format other than the fact that it must be unique for each grid in a view.
   * @param row The row of the cell whose dependency graph should be opened
   * @param col The column of the cell whose dependency graph should be opened
   */
  void openDependencyGraph(int requestId, GridType gridType, int graphId, String callbackId, int row, int col);

  /**
   * Closes a depdency graph.
   * @param gridType Specifies which of the main grids the dependency graph grid belongs to
   * @param graphId The ID of the dependency graph
   */
  void closeDependencyGraph(GridType gridType, int graphId);

  /**
   * Returns the grid structure for a dependency graph grid.
   * @param gridType Specifies which of the main grids the dependency graph grid belongs to
   * @param graphId The ID of the dependency graph
   * @return The row and column structure of the grid
   */
  GridStructure getGridStructure(GridType gridType, int graphId);

  /**
   * Creates a viewport for a dependency graph grid. A viewport represents the visible portion of the grid. Any
   * grid cells that are scrolled off the screen are not part of the viewport. The client only receives data for
   * cells in the viewport and only receives notification of new data if data in the viewport changes. If the only data
   * that changes in a calculation cycle is not part of a viewport then no update needs to be sent to the client.
   * There can be any number of viewports for each grid.
   *
   * @param gridType Specifies which of the main grids the dependency graph grid belongs to
   * @param graphId The ID of the dependency graph
   * @param viewportId A unique ID for the viewport
   * @param callbackId A value that is sent to the client with notification that new data is available for the
   * viewport. The server makes no assumptions about its format other than the fact that it must be unique for each
   * viewport in a view.
   * @param viewportDefinition Defines the rows and columns in the viewport and whether the viewport's data should be
   * expanded or a summary for data types which can't fit in a cell, e.g. vectors, matrices, curves.
   * @return {@code true} if there is data available for the new viewport
   */
  boolean createViewport(int requestId, GridType gridType, int graphId, int viewportId, String callbackId, ViewportDefinition viewportDefinition);

  /**
   * Updates a viewport of a dependency graph grid. A viewport will be updated when the user scrolls the grid.
   *
   * @param gridType Specifies which of the main grids the dependency graph grid belongs to
   * @param graphId The ID of the dependency graph
   * @param viewportId ID of the viewport
   * @param viewportDefinition Defines the rows and columns in the viewport and whether the viewport's data should be
   * expanded or a summary for data types which can't fit in a cell, e.g. vectors, matrices, curves.
   * @return The viewport's callback ID if there is data available, null if not
   */
  String updateViewport(GridType gridType, int graphId, int viewportId, ViewportDefinition viewportDefinition);

  /**
   * Deletes a viewport from a dependency graph grid.
   * @param gridType Specifies which of the main grids the dependency graph grid belongs to
   * @param graphId ID of the dependency graph
   * @param viewportId ID of the viewport
   */
  void deleteViewport(GridType gridType, int graphId, int viewportId);

  /**
   * Returns the current data for a viewport of a dependency graph grid.
   * @param gridType Specifies which of the main grids the dependency graph grid belongs to
   * @param graphId The ID of the dependency graph
   * @param viewportId ID of the viewport
   * @return The current data for the viewport.
   */
  ViewportResults getData(GridType gridType, int graphId, int viewportId);
}
