/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

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
 *
 */
public class DependencyGraphGridStructure implements GridBounds {

  private final AnalyticsNode _root;

  public static final AnalyticsColumnGroups COLUMN_GROUPS = new AnalyticsColumnGroups(ImmutableList.of(
      // fixed column group with one column for the row label
      new AnalyticsColumnGroup("", ImmutableList.<AnalyticsColumn>of(
          column("Target"))),
      // non-fixed columns
      new AnalyticsColumnGroup("", ImmutableList.<AnalyticsColumn>of(
          column("Type"),
          column("Value Name"),
          column("Value"),
          column("Function"),
          column("Properties")))));

  /** {@link ValueSpecification}s and function names for all rows in the grid in row index order. */
  private final List<Row> _rows;

  private final ComputationTargetResolver _computationTargetResolver;
  private final ResultsFormatter _formatter;

  /* package */ DependencyGraphGridStructure(AnalyticsNode root,
                                             List<Row> rows,
                                             ComputationTargetResolver targetResolver,
                                             ResultsFormatter formatter) {
    _rows = rows;
    ArgumentChecker.notNull(root, "root");
    ArgumentChecker.notNull(rows, "rows");
    ArgumentChecker.notNull(targetResolver, "targetResolver");
    ArgumentChecker.notNull(formatter, "formatter");
    _formatter = formatter;
    _root = root;
    _computationTargetResolver = targetResolver;
  }

  /* package */ List<ValueSpecification> getValueSpecificationsForRows(List<Integer> rows) {
    List<ValueSpecification> valueSpecs = Lists.newArrayList();
    for (Integer rowIndex : rows) {
      valueSpecs.add(getTargetForRow(rowIndex));
    }
    return valueSpecs;
  }

  private ValueSpecification getTargetForRow(Integer rowIndex) {
    return _rows.get(rowIndex).getValueSpec();
  }

  /* package */ List<List<Object>> createResultsForViewport(ViewportSpecification viewportSpec,
                                                            Map<ValueSpecification, Object> results) {
    List<List<Object>> resultsList = Lists.newArrayList();
    for (Integer rowIndex : viewportSpec.getRows()) {
      resultsList.add(createResultsForRow(rowIndex, viewportSpec.getColumns(), results));
    }
    return resultsList;
  }

  private List<Object> createResultsForRow(int rowIndex, SortedSet<Integer> cols, Map<ValueSpecification, Object> results) {
    Object value = results.get(getTargetForRow(rowIndex));
    Row row = _rows.get(rowIndex);
    List<Object> rowResults = Lists.newArrayListWithCapacity(cols.size());
    for (Integer colIndex : cols) {
      rowResults.add(getValueForColumn(colIndex, row, value));
    }
    return rowResults;
  }

  /* package */ Object getValueForColumn(int colIndex, Row row, Object value) {
    ValueSpecification valueSpec = row.getValueSpec();
    switch (colIndex) {
      case 0: // target
        return getTargetName(valueSpec.getTargetSpecification());
      case 1: // target type
        return getTargetTypeName(valueSpec.getTargetSpecification().getType());
      case 2: // value name
        return valueSpec.getValueName();
      case 3: // value
        return _formatter.formatValueForDisplay(value, valueSpec);
      case 4: // function name
        return row.getFunctionName();
      case 5: // properties
        return getValuePropertiesForDisplay(valueSpec.getProperties());
      default: // never happen
        throw new IllegalArgumentException("Column index " + colIndex + " is invalid");
    }
  }

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
    return sb.length() == 0 ? null : sb.toString();
  }

  private static AnalyticsColumn column(String header) {
    return new AnalyticsColumn(header, header);
  }

  @Override
  public int getRowCount() {
    return _rows.size();
  }

  @Override
  public int getColumnCount() {
    return COLUMN_GROUPS.getColumnCount();
  }

  public AnalyticsNode getRoot() {
    return _root;
  }

  // TODO do I really want to do this up front for all rows? these grids are big
  /* package */ static class Row {

    private final ValueSpecification _valueSpec;
    private final String _functionName;

    Row(ValueSpecification valueSpec, String functionName) {
      ArgumentChecker.notNull(valueSpec, "valueSpec");
      ArgumentChecker.notNull(functionName, "functionName");
      _valueSpec = valueSpec;
      _functionName = functionName;
    }

    /* package */ ValueSpecification getValueSpec() {
      return _valueSpec;
    }

    /* package */ String getFunctionName() {
      return _functionName;
    }
  }
}
