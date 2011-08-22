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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class ViewportDefinition {

  private static final String ROWS = "rows";
  private static final String DEPENDENCY_GRAPH_CELLS = "dependencyGraphCells";
  private static final String VIEW_DEFINITION_NAME = "viewDefinitionName";
  private static final String SNAPSHOT_ID = "snapshotId";

  private final List<ViewportRow> _rows;
  private final List<WebGridCell> _dependencyGraphCells;
  private final String _viewDefinitionName;
  private final UniqueId _snapshotId;

  private ViewportDefinition(String viewDefinitionName,
                             UniqueId snapshotId,
                             List<ViewportRow> rows,
                             List<WebGridCell> dependencyGraphCells) {
    // TODO check args
    // TODO wrap in immutable collections?
    _rows = rows;
    _dependencyGraphCells = dependencyGraphCells;
    _viewDefinitionName = viewDefinitionName;
    _snapshotId = snapshotId;
  }

  // TODO dependencyGraphCells optional? or compulsory even when it's empty?
  // TODO sanity check that the dep graph cells are all in the specified rows?
  public static ViewportDefinition fromJSON(String json) {
    try {
      JSONObject jsonObject = new JSONObject(json);
      JSONArray rowsArray = jsonObject.getJSONArray(ROWS);
      List<ViewportRow> rows = new ArrayList<ViewportRow>();
      if (rowsArray.length() < 1) {
        throw new OpenGammaRuntimeException(
            "Unable to create ViewportDefinition from JSON, a viewport must contain at least one row: " + json);
      }
      Set<Integer> rowIdSet = new HashSet<Integer>();
      for (int i = 0; i < rowsArray.length(); i++) {
        JSONArray rowArray = rowsArray.getJSONArray(i);
        int rowId = rowArray.getInt(0);
        if (rowId < 0) {
          throw new OpenGammaRuntimeException(
              "Unable to create ViewportDefinition from JSON, rowIds must not be negative: " + json);
        }
        if (rowIdSet.contains(rowId)) {
          throw new OpenGammaRuntimeException(
              "Unable to create ViewportDefinition from JSON, duplicate rowId: " + rowId + " in JSON: " + json);
        }
        long timestamp = rowArray.getLong(1);
        if (timestamp < 0) {
          throw new OpenGammaRuntimeException(
              "Unable to create ViewportDefinition from JSON, timestamps must not be negative: " + json);
        }
        ViewportRow row = new ViewportRow(rowId, timestamp);
        rows.add(row);
        rowIdSet.add(rowId);
      }
      JSONArray depGraphCellsArray = jsonObject.optJSONArray(DEPENDENCY_GRAPH_CELLS);
      List<WebGridCell> depGraphCells = new ArrayList<WebGridCell>();
      if (depGraphCellsArray != null) {
        for (int i = 0; i < depGraphCellsArray.length(); i++) {
          JSONArray cellArray = depGraphCellsArray.getJSONArray(i);
          int row = cellArray.getInt(0);
          int col = cellArray.getInt(1);
          if (!rowIdSet.contains(row)) {
            throw new OpenGammaRuntimeException("Unable to create ViewportDefinition from JSON, dependency graph " +
                                                    "cells must be in viewport rows, row: " + row + ", JSON: " + json);
          }
          if (row < 0 || col < 0) {
            throw new OpenGammaRuntimeException(
                "Unable to create ViewportDefinition from JSON, rows and cols must not be negative: " + json);
          }
          WebGridCell cell = new WebGridCell(row, col);
          depGraphCells.add(cell);
        }
      }
      String viewDefinitionName = jsonObject.getString(VIEW_DEFINITION_NAME);
      String snapshotIdStr = jsonObject.optString(SNAPSHOT_ID);
      UniqueId snapshotId;
      if (!StringUtils.isEmpty(snapshotIdStr)) {
        snapshotId = UniqueId.parse(snapshotIdStr);
      } else {
        snapshotId = null;
      }
      return new ViewportDefinition(viewDefinitionName, snapshotId, rows, depGraphCells);
    } catch (JSONException e) {
      throw new OpenGammaRuntimeException("Unable to create ViewportDefinition from JSON: " + json, e);
    }
  }

  public List<ViewportRow> getRows() {
    return _rows;
  }

  public List<WebGridCell> getDependencyGraphCells() {
    return _dependencyGraphCells;
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
