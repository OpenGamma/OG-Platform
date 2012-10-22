/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeMap;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
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

  /**
   * Map of target types to displayable names.
   */
  public static final ComputationTargetTypeMap<String> TARGET_TYPE_NAMES = createTargetTypeNames();

  /** {@link ValueSpecification}s for all rows in the grid in row index order. */
  private final List<ValueSpecification> _valueSpecs;
  /** Function names for all rows in the grid in row index order. */
  private final List<String> _fnNames;
  /** For looking up calculation targets using their specification. */
  private final ComputationTargetResolver _computationTargetResolver;
  /** The root node of the tree structure representing the rows. */
  private final AnalyticsNode _root;

  /* package */ DependencyGraphGridStructure(AnalyticsNode root,
                                             List<ValueSpecification> valueSpecs,
                                             List<String> fnNames,
                                             ComputationTargetResolver targetResolver) {

    ArgumentChecker.notNull(root, "root");
    ArgumentChecker.notNull(valueSpecs, "valueSpecs");
    ArgumentChecker.notNull(fnNames, "fnNames");
    ArgumentChecker.notNull(targetResolver, "targetResolver");
    _root = root;
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
      ResultsCache.Result cacheResult = cache.getResult(calcConfigName, _valueSpecs.get(rowIndex), null);
      Object value = cacheResult.getValue();
      ValueSpecification spec = _valueSpecs.get(rowIndex);
      String fnName = _fnNames.get(rowIndex);
      results.add(createValueForColumn(cell.getColumn(), spec, fnName, value, cache, calcConfigName));
    }
    return results;
  }

  /**
   * Builds a the result for a single grid cell.
   * @param colIndex Index of the column in the grid
   * @param valueSpec The specifications of the cell's value
   * @param fnName The name of the function that calculated the cell's value
   * @param value The cell's value
   * @param cache Cache of results for the grid, used for building the history of values for the cell
   * @param calcConfigName Calculation configuration used when calculating the dependency graph
   * @return Cell containing the result and possibly history
   */
  /* package */ ViewportResults.Cell createValueForColumn(int colIndex,
                                                          ValueSpecification valueSpec,
                                                          String fnName,
                                                          Object value,
                                                          ResultsCache cache,
                                                          String calcConfigName) {
    switch (colIndex) {
      case 0: // target
        return ViewportResults.stringCell(getTargetName(valueSpec.getTargetSpecification()), colIndex);
      case 1: // target type
        return ViewportResults.stringCell(TARGET_TYPE_NAMES.get(valueSpec.getTargetSpecification().getType()), colIndex);
      case 2: // value name
        return ViewportResults.stringCell(valueSpec.getValueName(), colIndex);
      case 3: // value
        Collection<Object> cellHistory = cache.getHistory(calcConfigName, valueSpec);
        return ViewportResults.valueCell(value, valueSpec, cellHistory, colIndex);
      case 4: // function name
        return ViewportResults.stringCell(fnName, colIndex);
      case 5: // properties
        return ViewportResults.stringCell(getValuePropertiesForDisplay(valueSpec.getProperties()), colIndex);
      default: // never happen
        throw new IllegalArgumentException("Column index " + colIndex + " is invalid");
    }
  }

  private static ComputationTargetTypeMap<String> createTargetTypeNames() {
    final ComputationTargetTypeMap<String> map = new ComputationTargetTypeMap<String>();
    map.put(ComputationTargetType.PORTFOLIO_NODE, "Agg");
    map.put(ComputationTargetType.POSITION, "Pos");
    map.put(ComputationTargetType.SECURITY, "Sec");
    map.put(ComputationTargetType.ANYTHING, "Prim");
    map.put(ComputationTargetType.NULL, "Prim");
    map.put(ComputationTargetType.TRADE, "Trade");
    return map;
  }

  /**
   * @param targetSpec Specification of the target for a grid row
   * @return The name of the target
   */
  private String getTargetName(final ComputationTargetSpecification targetSpec) {
    ComputationTarget target = _computationTargetResolver.resolve(targetSpec, VersionCorrection.LATEST);
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

  /**
   * @return The root of the node structure representing the dependency graph
   */
  public AnalyticsNode getRoot() {
    return _root;
  }
}
