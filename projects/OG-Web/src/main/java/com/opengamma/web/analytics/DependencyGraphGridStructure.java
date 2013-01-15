/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Row and column structure for a grid that displays the dependency graph used when calculating a value.
 * Each row contains one calcuated value from the results, all other columns in the row contain metadata about
 * the value.
 */
public class DependencyGraphGridStructure implements GridStructure {

  private static final int TARGET_COL = 0;
  private static final int TARGET_TYPE_COL = 1;
  private static final int VALUE_NAME_COL = 2;
  private static final int VALUE_COL = 3;
  private static final int FUNCTION_NAME_COL = 4;
  private static final int PROPERTIES_COL = 5;

  /** {@link ValueSpecification}s for all rows in the grid in row index order. */
  private final List<ValueSpecification> _valueSpecs;
  /** Function names for all rows in the grid in row index order. */
  private final List<String> _fnNames;
  /** For looking up calculation targets using their specification. */
  private final ComputationTargetResolver _computationTargetResolver;
  /** The root node of the tree structure representing the rows. */
  private final AnalyticsNode _root;
  /** The calculation configuration name. */
  private final String _calcConfigName;
  /** The columns in the grid. */
  private final AnalyticsColumnGroups _columnGroups;

  /* package */ DependencyGraphGridStructure(AnalyticsNode root,
                                             String calcConfigName,
                                             List<ValueSpecification> valueSpecs,
                                             List<String> fnNames,
                                             ComputationTargetResolver targetResolver) {
    ArgumentChecker.notNull(root, "root");
    ArgumentChecker.notNull(valueSpecs, "valueSpecs");
    ArgumentChecker.notNull(fnNames, "fnNames");
    ArgumentChecker.notNull(targetResolver, "targetResolver");
    _root = root;
    _calcConfigName = calcConfigName;
    _valueSpecs = Collections.unmodifiableList(valueSpecs);
    _fnNames = Collections.unmodifiableList(fnNames);
    _computationTargetResolver = targetResolver;
    _columnGroups = new AnalyticsColumnGroups(ImmutableList.of(
        // fixed column group with one column for the row label
        new AnalyticsColumnGroup("", ImmutableList.<AnalyticsColumn>of(
            column("Target", 0))),
        // non-fixed columns
        new AnalyticsColumnGroup("", ImmutableList.<AnalyticsColumn>of(
            column("Type", 1),
            column("Value Name", 2),
            column("Value", null, 3),
            column("Function", 4),
            column("Properties", 5)))));
  }

  /**
   * Returns the value specifications used to calculate the values in the grid.
   * @return The value specifications used to calculate the values
   */
  /* package */ List<ValueSpecification> getValueSpecifications() {
    return _valueSpecs;
  }

  /**
   * Builds the results for a viewport.
   *
   * @param viewportDefinition Defines the viewport
   * @param cache Cache of results for the grid
   * @param previousResults The results before the latest calculation cycle, possibly null
   * @return The results for the cells in the viewport
   */
  /* package */ ViewportResults createResults(ViewportDefinition viewportDefinition,
                                              ResultsCache cache,
                                              ViewportResults previousResults) {
    List<ResultsCell> results = Lists.newArrayList();
    for (GridCell cell : viewportDefinition) {
      AnalyticsColumn column = _columnGroups.getColumn(cell.getColumn());
      results.add(column.getResults(cell.getRow(), cache));
    }
    Viewport.State state;
    if (results.equals(previousResults)) {
      state = Viewport.State.STALE_DATA;
    } else {
      state = Viewport.State.FRESH_DATA;
    }
    return new ViewportResults(results, viewportDefinition, _columnGroups, cache.getLastCalculationDuration(), state);
  }

  /**
   *
   * @param header The column header string
   * @param colIndex The column index
   * @return A column for displaying a string value
   */
  private AnalyticsColumn column(String header, int colIndex) {
    return column(header, String.class, colIndex);
  }

  /**
   *
   * @param header The column header string
   * @param type The type of value the column contains
   * @param colIndex The column index
   * @return A column for displaying values of the specified type
   */
  private AnalyticsColumn column(String header, Class<?> type, int colIndex) {
    DependencyGraphCellRenderer renderer = new DependencyGraphCellRenderer(colIndex,
                                                                           _valueSpecs,
                                                                           _fnNames,
                                                                           _computationTargetResolver,
                                                                           _calcConfigName);
    return new AnalyticsColumn(header, header, type, renderer);
  }

  @Override
  public int getRowCount() {
    return _valueSpecs.size();
  }

  @Override
  public int getColumnCount() {
    return _columnGroups.getColumnCount();
  }

  @Override
  public AnalyticsColumnGroups getColumnStructure() {
    return _columnGroups;
  }

  @Override
  public Pair<String, ValueSpecification> getTargetForCell(int row, int col) {
    if (_calcConfigName == null || col != VALUE_COL) {
      return null;
    }
    ValueSpecification valueSpec = _valueSpecs.get(row);
    return valueSpec != null ? Pair.of(_calcConfigName, valueSpec) : null;
  }

  /**
   * @return The root of the node structure representing the dependency graph
   */
  public AnalyticsNode getRoot() {
    return _root;
  }

  private static class DependencyGraphCellRenderer implements AnalyticsColumn.CellRenderer {

    private final int _colIndex;
    private final List<ValueSpecification> _valueSpecs;
    private final List<String> _fnNames;
    /** For looking up calculation targets using their specification. */
    private final ComputationTargetResolver _computationTargetResolver;
    /** The calculation configuration name. */
    private final String _calcConfigName;


    private DependencyGraphCellRenderer(int colIndex,
                                        List<ValueSpecification> valueSpecs,
                                        List<String> fnNames,
                                        ComputationTargetResolver computationTargetResolver,
                                        String calcConfigName) {
      _calcConfigName = calcConfigName;
      ArgumentChecker.notNull(valueSpecs, "valueSpecs");
      ArgumentChecker.notNull(fnNames, "fnNames");
      ArgumentChecker.notNull(computationTargetResolver, "computationTargetResolver");
      _computationTargetResolver = computationTargetResolver;
      _colIndex = colIndex;
      _valueSpecs = valueSpecs;
      _fnNames = fnNames;
    }

    @Override
    public ResultsCell getResults(int rowIndex, ResultsCache cache) {
      ValueSpecification valueSpec = _valueSpecs.get(rowIndex);
      switch (_colIndex) {
        case TARGET_COL:
          return ViewportResults.objectCell(getTargetName(valueSpec.getTargetSpecification()), _colIndex);
        case TARGET_TYPE_COL:
          return ViewportResults.objectCell(getTargetTypeName(valueSpec.getTargetSpecification().getType()), _colIndex);
        case VALUE_NAME_COL:
          return ViewportResults.objectCell(valueSpec.getValueName(), _colIndex);
        case VALUE_COL:
          ResultsCache.Result cacheResult = cache.getResult(_calcConfigName, valueSpec, null);
          Collection<Object> history = cacheResult.getHistory();
          Object value = cacheResult.getValue();
          AggregatedExecutionLog executionLog = cacheResult.getAggregatedExecutionLog();
          return ViewportResults.valueCell(value, valueSpec, history, executionLog, _colIndex, cacheResult.isUpdated());
        case FUNCTION_NAME_COL:
          String fnName = _fnNames.get(rowIndex);
          return ViewportResults.objectCell(fnName, _colIndex);
        case PROPERTIES_COL:
          return ViewportResults.objectCell(getValuePropertiesForDisplay(valueSpec.getProperties()), _colIndex);
        default: // never happen
          throw new IllegalArgumentException("Column index " + _colIndex + " is invalid");
      }
    }

    /**
     * Formats a set of {@link ValueProperties} for display in the grid
     * @param properties The value properties
     * @return A formatted version of the properties
     */
    private String getValuePropertiesForDisplay(ValueProperties properties) {
      StringBuilder sb = new StringBuilder();
      boolean isFirst = true;
      for (String property : properties.getProperties()) {
        if (ValuePropertyNames.FUNCTION.equals(property)) {
          continue;
        }
        if (isFirst) {
          isFirst = false;
        } else {
          sb.append("; ");
        }
        sb.append(property).append("=");
        Set<String> propertyValues = properties.getValues(property);
        if (propertyValues.isEmpty()) {
          sb.append("*");
        } else {
          boolean isFirstValue = true;
          for (String value : propertyValues) {
            if (isFirstValue) {
              isFirstValue = false;
            } else {
              sb.append(", ");
            }
            sb.append(value);
          }
        }
      }
      return sb.toString();
    }


    /**
     * @param targetSpec Specification of the target for a grid row
     * @return The name of the target
     */
    private String getTargetName(final ComputationTargetSpecification targetSpec) {
      ComputationTarget target = _computationTargetResolver.resolve(targetSpec);
      if (target != null) {
        return target.getName();
      } else {
        UniqueId uid = targetSpec.getUniqueId();
        if (uid != null) {
          return uid.toString();
        } else {
          return targetSpec.getType().toString();
        }
      }
    }


    /**
     * @param targetType The type of the row's target
     * @return The string to display in the target type column
     */
    private String getTargetTypeName(ComputationTargetType targetType) {
      switch (targetType) {
        case PORTFOLIO_NODE:
          return "Agg";
        case POSITION:
          return "Pos";
        case SECURITY:
          return "Sec";
        case PRIMITIVE:
          return "Prim";
        case TRADE:
          return "Trade";
        default:
          return null;
      }
    }
  }
}
