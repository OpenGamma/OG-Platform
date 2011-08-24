/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.subscription;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueId;
import com.opengamma.web.server.WebGridCell;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 */
public class ViewportDefinition {

  private static final String DEPENDENCY_GRAPH_CELLS = "dependencyGraphCells";
  private static final String VIEW_DEFINITION_NAME = "viewDefinitionName";
  private static final String SNAPSHOT_ID = "snapshotId";
  private static final String PORTFOLIO_VIEWPORT = "portfolioViewport";
  private static final String PRIMITIVE_VIEWPORT = "primitiveViewport";
  private static final String ROWS = "rows";

  /** Row timestamp keyed on row number */
  private final SortedMap<Integer, Long> _portfolioRowTimstamps;
  private final SortedMap<Integer, Long> _primitiveRowTimstamps;
  private final List<WebGridCell> _portfolioDependencyGraphCells;
  private final List<WebGridCell> _primitiveDependencyGraphCells;
  private final String _viewDefinitionName;
  private final UniqueId _snapshotId;

  private ViewportDefinition(String viewDefinitionName,
                             UniqueId snapshotId,
                             SortedMap<Integer, Long> primitiveRowTimstamps,
                             SortedMap<Integer, Long> portfolioRowTimstamps,
                             List<WebGridCell> portfolioDependencyGraphCells,
                             List<WebGridCell> primitiveDependencyGraphCells) {
    // TODO check args
    // TODO wrap in immutable collections?
    _portfolioDependencyGraphCells = portfolioDependencyGraphCells;
    _primitiveDependencyGraphCells = primitiveDependencyGraphCells;
    _viewDefinitionName = viewDefinitionName;
    _snapshotId = snapshotId;
    _portfolioRowTimstamps = portfolioRowTimstamps;
    _primitiveRowTimstamps = primitiveRowTimstamps;
  }

  // TODO dependencyGraphCells optional? or compulsory even when it's empty?
  // TODO sanity check that the dep graph cells are all in the specified rows?
  public static ViewportDefinition fromJSON(String json) {
    try {
      // TODO some of the validation should be in the constructor
      JSONObject jsonObject = new JSONObject(json);

      SortedMap<Integer, Long> portfolioRows = getRows(jsonObject, PORTFOLIO_VIEWPORT);
      List<WebGridCell> portfolioDepGraphCells = getDepGraphCells(jsonObject, PORTFOLIO_VIEWPORT, portfolioRows.keySet());

      SortedMap<Integer, Long> primitiveRows = getRows(jsonObject, PRIMITIVE_VIEWPORT);
      List<WebGridCell> primitiveDepGraphCells = getDepGraphCells(jsonObject, PRIMITIVE_VIEWPORT, primitiveRows.keySet());

      String viewDefinitionName = jsonObject.getString(VIEW_DEFINITION_NAME);
      String snapshotIdStr = jsonObject.optString(SNAPSHOT_ID);
      UniqueId snapshotId;
      if (!StringUtils.isEmpty(snapshotIdStr)) {
        snapshotId = UniqueId.parse(snapshotIdStr);
      } else {
        snapshotId = null;
      }
      return new ViewportDefinition(viewDefinitionName,
                                    snapshotId,
                                    primitiveRows,
                                    portfolioRows,
                                    portfolioDepGraphCells,
                                    primitiveDepGraphCells);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to create ViewportDefinition from JSON: " + json, e);
    }
  }

  private static List<WebGridCell> getDepGraphCells(JSONObject jsonObject, String key, Set<Integer> rows) throws JSONException {
    JSONObject viewportJson = jsonObject.getJSONObject(key);
    JSONArray depGraphCellsArray = viewportJson.getJSONArray(DEPENDENCY_GRAPH_CELLS);
    List<WebGridCell> cells = new ArrayList<WebGridCell>();

    if (depGraphCellsArray != null) {
      for (int i = 0; i < depGraphCellsArray.length(); i++) {
        JSONArray cellArray = depGraphCellsArray.getJSONArray(i);
        int row = cellArray.getInt(0);
        int col = cellArray.getInt(1);
        if (!rows.contains(row)) {
          throw new OpenGammaRuntimeException("Unable to create ViewportDefinition from JSON, dependency graph " +
                                                  "cells must be in viewport rows, row: " + row);
        }
        if (row < 0 || col < 0) {
          throw new OpenGammaRuntimeException(
              "Unable to create ViewportDefinition from JSON, rows and cols must not be negative");
        }
        WebGridCell cell = new WebGridCell(row, col);
        cells.add(cell);
      }
    }
    return cells;
  }

  private static SortedMap<Integer, Long> getRows(JSONObject jsonObject, String viewportKey) throws JSONException {
    JSONObject viewportJson = jsonObject.getJSONObject(viewportKey);
    SortedMap<Integer, Long> rows = new TreeMap<Integer, Long>();
    JSONArray rowsArray = viewportJson.getJSONArray(ROWS);
    // TODO is this valid? what if a view is all primitives or all analytics?
    if (rowsArray.length() < 1) {
      throw new OpenGammaRuntimeException(
          "Unable to create ViewportDefinition from JSON, a viewport must contain at least one row");
    }
    for (int i = 0; i < rowsArray.length(); i++) {
      JSONArray rowArray = rowsArray.getJSONArray(i);
      int row = rowArray.getInt(0);
      if (row < 0) {
        throw new OpenGammaRuntimeException(
            "Unable to create ViewportDefinition from JSON, row numbers must not be negative");
      }
      if (rows.containsKey(row)) {
        throw new OpenGammaRuntimeException("Unable to create ViewportDefinition from JSON, duplicate row number: " + row);
      }
      long timestamp = rowArray.getLong(1);
      if (timestamp < 0) {
        throw new OpenGammaRuntimeException(
            "Unable to create ViewportDefinition from JSON, timestamps must not be negative: " + timestamp);
      }
      rows.put(row, timestamp);
    }
    return rows;
  }

  /**
   * @return Row timestamps for the portfolio grid keyed on row number
   */
  public SortedMap<Integer, Long> getPortfolioRows() {
    return _portfolioRowTimstamps;
  }

  /**
   * @return Row timestamps for the primitive grid keyed on row number
   */
  public SortedMap<Integer, Long> getPrimitiveRows() {
    return _primitiveRowTimstamps;
  }

  public List<WebGridCell> getPortfolioDependencyGraphCells() {
    return _portfolioDependencyGraphCells;
  }

  public List<WebGridCell> getPrimitiveDependencyGraphCells() {
    return _primitiveDependencyGraphCells;
  }

  public String getViewDefinitionName() {
    return _viewDefinitionName;
  }

  /**
   * @return The ID of the snapshot to use for the view data or {@code null} for live market data
   */
  public UniqueId getSnapshotId() {
    return _snapshotId;
  }
}
