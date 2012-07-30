/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

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
   */
  void updateStructure(CompiledViewDefinition compiledViewDefinition);

  /**
   * Updates the data in the grids when a cycle completes in the calculation engine.
   * @param results The results of the calculation cycle.
   * @param viewCycle Data associated with the calculation cycle.
   */
  void updateResults(ViewResultModel results, ViewCycle viewCycle);

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
   * @param gridType Specifies the grid.
   * @param viewportId A unique ID for the viewport. The server makes no assumptions about its format other
   * than the fact that it must be unique for each viewport in a view.
   * @param dataId A unique ID for the viewport's data - this is the value that is sent to the client with notification
   * that new data is available for the viewport. The server makes no assumptions about its format other
   * than the fact that it must be unique for each viewport in a view.
   * @param viewportSpec Defines the rows and columns in the viewport and whether the viewport's data should be
   * expanded or a summary for data types which can't fit in a cell, e.g. vectors, matrices, curves.
   * @return The version of the viewport. This allows the client to ensure that data received for the viewport
   * corresponds to the current viewport structure. If the client makes an asynchronous request for data and the
   * viewport structure changes at the same time then there is a race condition and it is possible the client could
   * display the old viewport's data in the updated viewport. The viewport version allows this situation to be detected
   * and avoided.
   */
  long createViewport(GridType gridType, String viewportId, String dataId, ViewportSpecification viewportSpec);

  /**
   * Updates a viewport. A viewport will be updated when the user scrolls the grid.
   * @param gridType Specifies the grid
   * @param viewportId ID of the viewport
   * @param viewportSpec Defines the rows and columns in the viewport and whether the viewport's data should be
   * expanded or a summary for data types which can't fit in a cell, e.g. vectors, matrices, curves.
   * @return The version of the viewport. This allows the client to ensure that data received for the viewport
   * corresponds to the current viewport structure. If the client makes an asynchronous request for data and the
   * viewport structure changes at the same time then there is a race condition and it is possible the client could
   * display the old viewport's data in the updated viewport. The viewport version allows this situation to be detected
   * and avoided.
   */
  long updateViewport(GridType gridType, String viewportId, ViewportSpecification viewportSpec);

  /**
   * Deletes a viewport.
   * @param gridType Specifies the grid
   * @param viewportId ID of the viewport
   */
  void deleteViewport(GridType gridType, String viewportId);

  /**
   * Returns the current data for a viewport.
   * @param gridType Specifies the grid
   * @param viewportId ID of the viewport
   * @return The current data for the viewport.
   */
  ViewportResults getData(GridType gridType, String viewportId);

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
   * @param graphId A unique ID for the dependency graph grid. The server makes no assumptions about its format other
   * than the fact that it must be unique for each dependency graph grid in a view.
   * @param gridId A unique ID for the grid's structure - this is the value that is sent to the client with notification
   * that the structure has changed. The server makes no assumptions about its format other than the fact that it
   * must be unique for each grid in a view.
   * @param row The row of the cell whose dependency graph should be opened
   * @param col The column of the cell whose dependency graph should be opened
   */
  void openDependencyGraph(GridType gridType, String graphId, String gridId, int row, int col);

  /**
   * Closes a depdency graph.
   * @param gridType Specifies which of the main grids the dependency graph grid belongs to
   * @param graphId The ID of the grid
   */
  void closeDependencyGraph(GridType gridType, String graphId);

  /**
   * Returns the grid structure for a dependency graph grid.
   * @param gridType Specifies which of the main grids the dependency graph grid belongs to
   * @param graphId The ID of the grid
   * @return The row and column structure of the grid
   */
  GridStructure getGridStructure(GridType gridType, String graphId);

  /**
   * Creates a viewport for a dependency graph grid. A viewport represents the visible portion of the grid. Any
   * grid cells that are scrolled off the screen are not part of the viewport. The client only receives data for
   * cells in the viewport and only receives notification of new data if data in the viewport changes. If the only data
   * that changes in a calculation cycle is not part of a viewport then no update needs to be sent to the client.
   * There can be any number of viewports for each grid.
   * @param gridType Specifies which of the main grids the dependency graph grid belongs to
   * @param graphId The ID of the grid
   * @param viewportId A unique ID for the viewport. The server makes no assumptions about its format other
   * than the fact that it must be unique for each viewport in a view.
   * @param dataId A unique ID for the viewport's data - this is the value that is sent to the client with notification
   * that new data is available for the viewport. The server makes no assumptions about its format other
   * than the fact that it must be unique for each viewport in a view.
   * @param viewportSpec Defines the rows and columns in the viewport and whether the viewport's data should be
   * expanded or a summary for data types which can't fit in a cell, e.g. vectors, matrices, curves.
   * @return The version of the viewport. This allows the client to ensure that data received for the viewport
   * corresponds to the current viewport structure. If the client makes an asynchronous request for data and the
   * viewport structure changes at the same time then there is a race condition and it is possible the client could
   * display the old viewport's data in the updated viewport. The viewport version allows this situation to be detected
   * and avoided.
   */
  long createViewport(GridType gridType, String graphId, String viewportId, String dataId, ViewportSpecification viewportSpec);


  /**
   * Updates a viewport of a dependency graph grid. A viewport will be updated when the user scrolls the grid.
   * @param gridType Specifies which of the main grids the dependency graph grid belongs to
   * @param graphId The ID of the grid
   * @param viewportId ID of the viewport
   * @param viewportSpec Defines the rows and columns in the viewport and whether the viewport's data should be
   * expanded or a summary for data types which can't fit in a cell, e.g. vectors, matrices, curves.
   * @return The version of the viewport. This allows the client to ensure that data received for the viewport
   * corresponds to the current viewport structure. If the client makes an asynchronous request for data and the
   * viewport structure changes at the same time then there is a race condition and it is possible the client could
   * display the old viewport's data in the updated viewport. The viewport version allows this situation to be detected
   * and avoided.
   */
  long updateViewport(GridType gridType, String graphId, String viewportId, ViewportSpecification viewportSpec);

  /**
   * Deletes a viewport from a dependency graph grid.
   * @param gridType Specifies which of the main grids the dependency graph grid belongs to
   * @param viewportId ID of the viewport
   */
  void deleteViewport(GridType gridType, String graphId, String viewportId);

  /**
   * Returns the current data for a viewport of a dependency graph grid.
   * @param gridType Specifies which of the main grids the dependency graph grid belongs to
   * @param graphId The ID of the grid
   * @param viewportId ID of the viewport
   * @return The current data for the viewport.
   */
  ViewportResults getData(GridType gridType, String graphId, String viewportId);
}
