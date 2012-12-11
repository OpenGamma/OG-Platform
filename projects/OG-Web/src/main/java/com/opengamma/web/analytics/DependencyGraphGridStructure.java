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

  /** The fixed set of columns used in all dependency graph grids. */
  public static final AnalyticsColumnGroups COLUMN_GROUPS = new AnalyticsColumnGroups(ImmutableList.of(
      // fixed column group with one column for the row label
      new AnalyticsColumnGroup("", ImmutableList.<AnalyticsColumn>of(
          column("Target"))),
      // non-fixed columns
      new AnalyticsColumnGroup("", ImmutableList.<AnalyticsColumn>of(
          column("Type"),
          column("Value Name"),
          column("Value", null),
          column("Function"),
          column("Properties")))));

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
  }

  /**
   * Returns the value specifications used to calculate the values in the grid.
   * @return The value specifications used to calculate the values
   */
  /* package */ List<ValueSpecification> getValueSpecifications() {
    return _valueSpecs;
  }

  /**
   * Builds the results for a viewport given a set of results for the entire grid and a definition of the viewport.
   * @param viewportDefinition Defines the viewport
   * @param cache Cache of results for the grid
   * @param calcConfigName Calculation configuration used when calculating the dependency graph
   * @return The results for the rows in the viewport
   */
  /* package */ List<ViewportResults.Cell> createResultsForViewport(ViewportDefinition viewportDefinition,
                                                                    ResultsCache cache,
                                                                    String calcConfigName) {
    List<ViewportResults.Cell> results = Lists.newArrayList();
    for (GridCell cell : viewportDefinition) {
      int rowIndex = cell.getRow();
      ValueSpecification spec = _valueSpecs.get(rowIndex);
      ResultsCache.Result cacheResult = cache.getResult(calcConfigName, spec, null);
      Collection<Object> history = cacheResult.getHistory();
      Object value = cacheResult.getValue();
      AggregatedExecutionLog executionLog = cacheResult.getAggregatedExecutionLog();
      String fnName = _fnNames.get(rowIndex);
      results.add(createValueForColumn(cell.getColumn(), spec, fnName, value, history, executionLog));
    }
    return results;
  }

  /**
   * Builds a the result for a single grid cell.
   *
   * @param colIndex Index of the column in the grid
   * @param valueSpec The specifications of the cell's value
   * @param fnName The name of the function that calculated the cell's value
   * @param value The cell's value
   * @param executionLog Log generated when the value was calculated
   * @return Cell containing the result and possibly history
   */
  /* package */ ViewportResults.Cell createValueForColumn(int colIndex,
                                                          ValueSpecification valueSpec,
                                                          String fnName,
                                                          Object value,
                                                          Collection<Object> history,
                                                          AggregatedExecutionLog executionLog) {
    switch (colIndex) {
      case TARGET_COL:
        return ViewportResults.objectCell(getTargetName(valueSpec.getTargetSpecification()), colIndex);
      case TARGET_TYPE_COL:
        return ViewportResults.objectCell(getTargetTypeName(valueSpec.getTargetSpecification().getType()), colIndex);
      case VALUE_NAME_COL:
        return ViewportResults.objectCell(valueSpec.getValueName(), colIndex);
      case VALUE_COL:
        return ViewportResults.valueCell(value, valueSpec, history, executionLog, colIndex);
      case FUNCTION_NAME_COL:
        return ViewportResults.objectCell(fnName, colIndex);
      case PROPERTIES_COL:
        return ViewportResults.objectCell(getValuePropertiesForDisplay(valueSpec.getProperties()), colIndex);
      default: // never happen
        throw new IllegalArgumentException("Column index " + colIndex + " is invalid");
    }
  }

  /**
   * @param targetType The type of the row's target
   * @return The string to display in the target type column
   */
  /* package */ static String getTargetTypeName(ComputationTargetType targetType) {
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
   * Formats a set of {@link ValueProperties} for display in the grid
   * @param properties The value properties
   * @return A formatted version of the properties
   */
  /* package */ static String getValuePropertiesForDisplay(ValueProperties properties) {
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
   * @param header The column header string
   * @return A column for displaying a string value
   */
  private static AnalyticsColumn column(String header) {
    return column(header, String.class);
  }

  /**
   * @param header The column header string
   * @param type The type of value the column contains
   * @return A column for displaying values of the specified type
   */
  private static AnalyticsColumn column(String header, Class<?> type) {
    return new AnalyticsColumn(header, header, type);
  }

  @Override
  public int getRowCount() {
    return _valueSpecs.size();
  }

  @Override
  public int getColumnCount() {
    return COLUMN_GROUPS.getColumnCount();
  }

  @Override
  public AnalyticsColumnGroups getColumnStructure() {
    return COLUMN_GROUPS;
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
}
