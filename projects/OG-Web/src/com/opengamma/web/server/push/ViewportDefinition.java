/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.id.UniqueId;
import com.opengamma.web.server.WebGridCell;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * <p>A {@link Viewport} is a view onto a subset of a view client's data.  A {@link ViewportDefinition} contains</p>
 * <ul>
 *   <li>The name of the view definition</li>
 *   <li>Specification of the view's data - whether it's live data or a snapshot and what the source is</li>
 *   <li>Aggregator name (if required)</li>
 *   <li>
 *     For the portfolio and primitive grids:
 *     <ul>
 *       <li>Visible row numbers and the timestamp of the last update for each row</li>
 *       <li>Cells for which the dependency graph is required</li>
 *     </ul>
 *   </li>
 * </ul>
 */
public class ViewportDefinition {

  /** Constants used in the JSON representation of a {@code ViewportDefinition}, see {@link #fromJSON(String)} for the format */
  private static final String DEPENDENCY_GRAPH_CELLS = "dependencyGraphCells";
  private static final String VIEW_DEFINITION_NAME = "viewDefinitionName";
  private static final String SNAPSHOT_ID = "snapshotId";
  private static final String PORTFOLIO_VIEWPORT = "portfolioViewport";
  private static final String PRIMITIVE_VIEWPORT = "primitiveViewport";
  private static final String ROW_IDS = "rowIds";
  private static final String LAST_TIMESTAMPS = "lastTimestamps";
  private static final String MARKET_DATA_TYPE = "marketDataType";
  private static final String MARKET_DATA_PROVIDER = "marketDataProvider";
  private static final String AGGREGATOR_NAMES = "aggregatorNames";
  private static final String DEFAULT_LIVE_MARKET_DATA_NAME = "Automatic";

  /** Row timestamp keyed on row number */
  private final SortedMap<Integer, Long> _portfolioRowTimstamps;

  /** Row timestamp keyed on row number */
  private final SortedMap<Integer, Long> _primitiveRowTimstamps;

  /** Name of the view's aggregator, may be null */
  private final String _aggregatorName;

  /** Portfolio grid cells for which dependency graphs are required */
  private final List<WebGridCell> _portfolioDependencyGraphCells;

  /** Primitive grid cells for which dependency graphs are required */
  private final List<WebGridCell> _primitiveDependencyGraphCells;

  /** Name of the view definition */
  private final String _viewDefinitionName;

  // TODO this isn't really required any more, only used for testing. why?
  private final UniqueId _snapshotId;

  /** View's execution options */
  private final ViewExecutionOptions _executionOptions;

  public enum MarketDataType {
    snapshot, live
  }

  private ViewportDefinition(String viewDefinitionName,
                             UniqueId snapshotId,
                             SortedMap<Integer, Long> primitiveRowTimstamps,
                             SortedMap<Integer, Long> portfolioRowTimstamps,
                             List<WebGridCell> portfolioDependencyGraphCells,
                             List<WebGridCell> primitiveDependencyGraphCells,
                             MarketDataType marketDataType,
                             String marketDataProvider,
                             String aggregatorName) {
    // TODO check args
    // TODO wrap in immutable collections?
    _portfolioDependencyGraphCells = portfolioDependencyGraphCells;
    _primitiveDependencyGraphCells = primitiveDependencyGraphCells;
    _viewDefinitionName = viewDefinitionName;
    _snapshotId = snapshotId;
    _portfolioRowTimstamps = portfolioRowTimstamps;
    _primitiveRowTimstamps = primitiveRowTimstamps;
    _aggregatorName = aggregatorName;
    _executionOptions = createExecutionOptions(marketDataType, snapshotId, marketDataProvider);
  }

  private static ViewExecutionOptions createExecutionOptions(MarketDataType marketDataType,
                                                             UniqueId snapshotId,
                                                             String marketDataProvider) {
    MarketDataSpecification marketDataSpec;
    EnumSet<ViewExecutionFlags> flags;
    if (marketDataType == MarketDataType.live) {
      if (DEFAULT_LIVE_MARKET_DATA_NAME.equals(marketDataProvider)) {
        marketDataSpec = MarketData.live();
      } else {
        marketDataSpec = MarketData.live(marketDataProvider);
      }
      flags = ExecutionFlags.none().triggerOnMarketData().get();
    } else { // snapshot
      marketDataSpec = MarketData.user(snapshotId.toLatest());
      flags = ExecutionFlags.none().triggerOnMarketData().get();
    }
    return ExecutionOptions.infinite(marketDataSpec, flags);
  }

  /**
   * <p>Creates a {@link ViewportDefinition} from JSON.  The format is:</p>
   * <pre>
   *   {"viewDefinitionName": ...
   *    "marketDataType": ...
   *    "marketDataProvider": ...
   *    "snapshotId": ...
   *    "aggregatorName": ...
   *    "portfolioViewport":
   *     {"rowIds": [rowId1, rowId2, ...],
   *      "lastTimestamps": [timestamp1, timestamp2, ...],
   *      "dependencyGraphCells": [[row, col], [row, col], ...]},
   *    "primitiveViewport":
   *     {"rowIds": [rowId1, rowId2, ...],
   *      "lastTimestamps": [timestamp1, timestamp2, ...],
   *      "dependencyGraphCells": [[row, col], [row, col], ...]}}</pre>
   * <ul>
   *   <li>{@code viewDefinitionName}: name of the view definition</li>
   *   <li>{@code marketDataType}: {@code "live"} or {@code "snapshot"}</li>
   *   <li>{@code marketDataProvider}: name of the market data provider.  Only relevant for live data.  Omit or
   *   {@code "Automatic"} for default provider</li>
   *   <li>{@code snapshotId}: ID of the market data snapshot (see below).  Required if using a market data snapshot.
   *   <em>TODO No testing has been done using snapshots yet, only live data</em></li>
   *   <li>{@code portfolioViewport / primitiveViewport}: viewport definition for the separate grids showing portfolio
   *   and primitive data</li>
   *   <li>{@code rowIds}: The zero-based row indices whose data should be included in the results.</li>
   *   <li>{@code lastTimestamps}: The timestamp of the last update the client received for each row.  Each item
   *   in {@code lastTimestamps} refers to the row at the same index in {@code rowIds}.  Therefore {@code rowIds} and
   *   {@code lastTimestamps} must be the same length.  If no previous result has been received for the row then
   *   {@code null} should be sent.</li>
   *   <li>{@code dependencyGraphCells}: array of two-element arrays with the row and column numbers of cells whose
   *   dependency graph should be included in the results.
   * </ul>
   * @param json JSON representation of a {@link ViewportDefinition}
   * @return The viewport definition
   */
  public static ViewportDefinition fromJSON(String json) {
    try {

      // TODO some of the validation should be in the constructor
      JSONObject jsonObject = new JSONObject(json);

      SortedMap<Integer, Long> portfolioRows = getRows(jsonObject, PORTFOLIO_VIEWPORT);
      List<WebGridCell> portfolioDepGraphCells = getDepGraphCells(jsonObject,
                                                                  PORTFOLIO_VIEWPORT,
                                                                  portfolioRows.keySet());

      SortedMap<Integer, Long> primitiveRows = getRows(jsonObject, PRIMITIVE_VIEWPORT);
      List<WebGridCell> primitiveDepGraphCells = getDepGraphCells(jsonObject,
                                                                  PRIMITIVE_VIEWPORT,
                                                                  primitiveRows.keySet());

      String viewDefinitionName = jsonObject.getString(VIEW_DEFINITION_NAME);

      /* TODO better data spec format?
      marketData: {type: live, provider: ...}
      marketData: {type: snapshot, snapshotId: ...}
      no marketData = live data from default provider?
      */
      String marketDataTypeName = jsonObject.optString(MARKET_DATA_TYPE);
      MarketDataType marketDataType;
      if (StringUtils.isEmpty(marketDataTypeName)) {
        marketDataType = MarketDataType.live;
      } else {
        marketDataType = MarketDataType.valueOf(marketDataTypeName);
      }
      String marketDataProvider;
      UniqueId snapshotId;
      if (marketDataType == MarketDataType.snapshot) {
        // snapshot ID is compulsory if market data type is snapshot
        snapshotId = UniqueId.parse(jsonObject.getString(SNAPSHOT_ID));
        marketDataProvider = null;
      } else {
        // market data provider is optional for live data, blank means default provider
        marketDataProvider = jsonObject.optString(MARKET_DATA_PROVIDER);
        if (StringUtils.isEmpty(marketDataProvider)) {
          marketDataProvider = DEFAULT_LIVE_MARKET_DATA_NAME;
        }
        snapshotId = null;
      }
      String aggregatorName = jsonObject.optString(AGGREGATOR_NAMES);
      if (StringUtils.isEmpty(aggregatorName)) {
        aggregatorName = null;
      }
      return new ViewportDefinition(viewDefinitionName,
                                    snapshotId,
                                    primitiveRows,
                                    portfolioRows,
                                    portfolioDepGraphCells,
                                    primitiveDepGraphCells,
                                    marketDataType,
                                    marketDataProvider,
                                    aggregatorName);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Unable to create ViewportDefinition from JSON: " + json, e);
    }
  }

  /** Helper method to extract the dependency graph cells from JSON */
  private static List<WebGridCell> getDepGraphCells(JSONObject jsonObject, String key, Set<Integer> rows) throws JSONException {
    JSONObject viewportJson = jsonObject.optJSONObject(key);
    List<WebGridCell> cells = new ArrayList<WebGridCell>();
    if (viewportJson == null) {
      return cells;
    }
    JSONArray depGraphCellsArray = viewportJson.optJSONArray(DEPENDENCY_GRAPH_CELLS);

    if (depGraphCellsArray != null) {
      for (int i = 0; i < depGraphCellsArray.length(); i++) {
        JSONArray cellArray = depGraphCellsArray.getJSONArray(i);
        int row = cellArray.getInt(0);
        int col = cellArray.getInt(1);
        if (!rows.contains(row)) {
          throw new IllegalArgumentException("Unable to create ViewportDefinition from JSON, dependency graph " +
                                                 "cells must be in viewport rows, row: " + row);
        }
        if (row < 0 || col < 0) {
          throw new IllegalArgumentException(
              "Unable to create ViewportDefinition from JSON, rows and cols must not be negative");
        }
        WebGridCell cell = new WebGridCell(row, col);
        cells.add(cell);
      }
    }
    return cells;
  }

  /** Helper method to get the row numbers and timestamps from JSON */
  private static SortedMap<Integer, Long> getRows(JSONObject jsonObject, String viewportId) throws JSONException {
    JSONObject viewportJson = jsonObject.optJSONObject(viewportId);
    SortedMap<Integer, Long> rows = new TreeMap<Integer, Long>();
    if (viewportJson == null) {
      return rows;
    }
    JSONArray rowIds = viewportJson.getJSONArray(ROW_IDS);
    if (rowIds.length() < 1) {
      throw new IllegalArgumentException(
          "Unable to create ViewportDefinition from JSON, a viewport must contain at least one row");
    }
    JSONArray lastTimestamps = viewportJson.getJSONArray(LAST_TIMESTAMPS);
    if (rowIds.length() != lastTimestamps.length()) {
      throw new IllegalArgumentException(
          "Unable to create ViewportDefinition from JSON, the viewport definition must specify the same number " +
              "of rows and timestamps");
    }
    for (int i = 0; i < rowIds.length(); i++) {
      int rowId = rowIds.getInt(i);
      if (rowId < 0) {
        throw new IllegalArgumentException(
            "Unable to create ViewportDefinition from JSON, row numbers must not be negative");
      }
      if (rows.containsKey(rowId)) {
        throw new IllegalArgumentException("Unable to create ViewportDefinition from JSON, duplicate row number: " + rowId);
      }
      Long timestamp = lastTimestamps.optLong(i);
      if (timestamp < 0) {
        throw new IllegalArgumentException(
            "Unable to create ViewportDefinition from JSON, timestamps must not be negative: " + timestamp);
      } else if (timestamp == 0) {
        timestamp = null;
      }
      rows.put(rowId, timestamp);
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

  /**
   * @return The cells in the portfolio grid for which dependency graphs are required.
   */
  public List<WebGridCell> getPortfolioDependencyGraphCells() {
    return _portfolioDependencyGraphCells;
  }

  /**
   * @return The cells in the primitives grid for which dependency graphs are required.
   */
  public List<WebGridCell> getPrimitiveDependencyGraphCells() {
    return _primitiveDependencyGraphCells;
  }

  /**
   * @return The name of the viewport's view definition.
   */
  public String getViewDefinitionName() {
    return _viewDefinitionName;
  }

  /**
   * @return The ID of the snapshot to use for the view data or {@code null} for live market data
   */
  /* package */ UniqueId getSnapshotId() {
    return _snapshotId;
  }

  /**
   * @return The view's execution options.
   */
  public ViewExecutionOptions getExecutionOptions() {
    return _executionOptions;
  }

  /**
   * @return The aggregator name, may be null if the view isn't aggregated.
   */
  public String getAggregatorName() {
    return _aggregatorName;
  }
}
