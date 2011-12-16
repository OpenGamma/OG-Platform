/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.conversion.ConversionMode;

import java.util.Map;

/**
 * A view onto a subset of the analytics data from a view client.
 */
public interface Viewport {

  /**
   * @return JSON representation of the structure of the view's grids
   * TODO this should probably be a proper class but that would require serious refactoring of the web code
   */
  Map<String, Object> getGridStructure();

  /**
   * @return JSON containing the latest analytics data for the viewport's visible cells
   * TODO this should probably be a proper class but that would require serious refactoring of the web code
   */
  Map<String, Object> getLatestResults();

  /**
   * Pauses or unpauses the view associated with this viewport.
   * @param run {@code true} if the view should run, {@code false} if it should pause
   */
  void setRunning(boolean run);

  // TODO could we just return everything in one lot of CSV? that would be consistent with the grid structure and results
  // TODO does it make any sense to have several CSV files created from different data? shouldn't they be atomic?
  // TODO having specific CSV methods is pretty ugly. what about PDFs? excel worksheets? some other format?
  // TODO if we're keeping the CSV method(s) it might be better to switch back to the old method of requesting by name. cleaner interface

  /**
   * Returns the portfolio grid data as CSV
   * @return Filename for a CSV file and the CSV content
   */
  Pair<String, String> getPortfolioCsv();

  /**
   * Returns the dependency graph data for a cell in the portfolio grid as CSV.
   * @param row Row index of the cell
   * @param col Column index of the cell
   * @return Filename for a CSV file and the CSV content for the dependency graph
   */
  Pair<String, String> getPortfolioCsv(int row, int col);

  /**
   * Returns the primitives grid data as CSV
   * @return Filename for a CSV file and the CSV content
   */
  Pair<String, String> getPrimitivesCsv();

  /**
   * Returns the dependency graph data for a cell in the primitives grid as CSV.
   * @param row Row index of the cell
   * @param col Column index of the cell
   * @return Filename for a CSV file and the CSV content for the dependency graph
   */
  Pair<String, String> getPrimitivesCsv(int row, int col);
}
