/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Row and column structure for a grid that displays the dependency graph used when calculating a value.
 * Each row contains one calcuated value from the results, all other columns in the row contain meta data about
 * the value.
 */
public class DependencyGraphGridStructure implements GridStructure {

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

  /** {@link ValueSpecification}s and function names for all rows in the grid in row index order. */
  private final List<Row> _rows;
  /** For looking up calculation targets using their specification. */
  private final ComputationTargetResolver _computationTargetResolver;
  /** The root node of the tree structure representing the rows. */
  private final AnalyticsNode _root;

  /* package */ DependencyGraphGridStructure(AnalyticsNode root,
                                             List<Row> rows,
                                             ComputationTargetResolver targetResolver) {
    ArgumentChecker.notNull(root, "root");
    ArgumentChecker.notNull(rows, "rows");
    ArgumentChecker.notNull(targetResolver, "targetResolver");
    _rows = rows;
    _root = root;
    _computationTargetResolver = targetResolver;
  }

  /**
   * Returns the value specifications used to calculate the values for a range of rows.
   * @param rows The row indices
   * @return The value specifications used to calculate the row values
   */
  /* package */ List<ValueSpecification> getValueSpecificationsForRows(List<Integer> rows) {
    List<ValueSpecification> valueSpecs = Lists.newArrayList();
    for (Integer rowIndex : rows) {
      valueSpecs.add(getValueSpecificationForRow(rowIndex));
    }
    return valueSpecs;
  }

  /**
   * Returns the value specification used to calculate the value for a row.
   * @param rowIndex The row index
   * @return The value specifications used to calculate the row value
   */
  private ValueSpecification getValueSpecificationForRow(Integer rowIndex) {
    return _rows.get(rowIndex).getValueSpec();
  }

  /**
   * Builds the results for a viewport given a set of results for the entire grid and a definition of the viewport.
   * @param viewportSpec Defines the viewport
   * @param results The results for each row, keyed on the row's {@link ValueSpecification}
   * @param cache Cache of results for the grid, used for building the history
   * @param calcConfigName Calculation configuration used when calculating the dependency graph
   * @return The results for the rows in the viewport
   */
  /* package */ List<List<ViewportResults.Cell>> createResultsForViewport(ViewportSpecification viewportSpec,
                                                                          Map<ValueSpecification, Object> results,
                                                                          ResultsCache cache,
                                                                          String calcConfigName) {
    List<List<ViewportResults.Cell>> resultsList = Lists.newArrayList();
    for (Integer rowIndex : viewportSpec.getRows()) {
      resultsList.add(createResultsForRow(rowIndex, viewportSpec.getColumns(), results, cache, calcConfigName));
    }
    return resultsList;
  }

  /**
   * Builds the results for a single row in the viewport.
   * @param rowIndex The row index in the grid
   * @param cols The columns visible in the viewport
   * @param results The results for each row, keyed on the row's {@link ValueSpecification}
   * @param cache Cache of results for the grid, used for building the history
   * @param calcConfigName Calculation configuration used when calculating the dependency graph
   * @return The results for the row
   */
  private List<ViewportResults.Cell> createResultsForRow(int rowIndex,
                                                         SortedSet<Integer> cols,
                                                         Map<ValueSpecification, Object> results,
                                                         ResultsCache cache,
                                                         String calcConfigName) {
    Object value = results.get(getValueSpecificationForRow(rowIndex));
    Row row = _rows.get(rowIndex);
    List<ViewportResults.Cell> rowResults = Lists.newArrayListWithCapacity(cols.size());
    for (Integer colIndex : cols) {
      rowResults.add(createValueForColumn(colIndex, row, value, cache, calcConfigName));
    }
    return rowResults;
  }

  /**
   * Builds a the result for a single grid cell.
   * @param colIndex Index of the column in the grid
   * @param row Index of the row in the grid
   * @param value The cell's value
   * @param cache Cache of results for the grid, used for building the history of values for the cell
   * @param calcConfigName Calculation configuration used when calculating the dependency graph
   * @return Cell containing the result and possibly history
   */
  /* package */ ViewportResults.Cell createValueForColumn(int colIndex,
                                                          Row row,
                                                          Object value,
                                                          ResultsCache cache,
                                                          String calcConfigName) {
    ValueSpecification valueSpec = row.getValueSpec();
    switch (colIndex) {
      case 0: // target
        return ViewportResults.stringCell(getTargetName(valueSpec.getTargetSpecification()));
      case 1: // target type
        return ViewportResults.stringCell(getTargetTypeName(valueSpec.getTargetSpecification().getType()));
      case 2: // value name
        return ViewportResults.stringCell(valueSpec.getValueName());
      case 3: // value
        Collection<Object> cellHistory = cache.getHistory(calcConfigName, valueSpec);
        return ViewportResults.valueCell(value, valueSpec, cellHistory);
      case 4: // function name
        return ViewportResults.stringCell(row.getFunctionName());
      case 5: // properties
        return ViewportResults.stringCell(getValuePropertiesForDisplay(valueSpec.getProperties()));
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
    return _rows.size();
  }

  @Override
  public int getColumnCount() {
    return COLUMN_GROUPS.getColumnCount();
  }

  @Override
  public AnalyticsColumnGroups getColumnStructure() {
    return COLUMN_GROUPS;
  }

  /**
   * @return The root of the node structure representing the dependency graph
   */
  public AnalyticsNode getRoot() {
    return _root;
  }

  /**
   * A row in the dependency graph grid.
   */
  /* package */ static class Row {

    /** Specification of the row's target. */
    private final ValueSpecification _valueSpec;
    /** Name of the function used to calculate the row's value. */
    private final String _functionName;

    Row(ValueSpecification valueSpec, String functionName) {
      ArgumentChecker.notNull(valueSpec, "valueSpec");
      ArgumentChecker.notNull(functionName, "functionName");
      _valueSpec = valueSpec;
      _functionName = functionName;
    }

    /**
     * @return Specification of the row's target
     */
    /* package */ ValueSpecification getValueSpec() {
      return _valueSpec;
    }

    /**
     * @return Name of the function used to calculate the row's value
     */
    /* package */ String getFunctionName() {
      return _functionName;
    }
  }
}
