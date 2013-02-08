/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Set;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.util.ArgumentChecker;

/**
 * Specifies the header label of a column and the type of data it displays.
 */
/* package */ class GridColumn {

  private final String _header;
  private final String _description;
  private final Class<?> _type;
  private final CellRenderer _renderer;
  private final ColumnSpecification _columnSpec;

  /* package */ GridColumn(String header, String description, Class<?> type, CellRenderer renderer) {
    this(header, description, type, renderer, null);
  }

  /* package */ GridColumn(String header,
                           String description,
                           Class<?> type,
                           CellRenderer renderer,
                           ColumnSpecification columnSpec) {
    ArgumentChecker.notNull(header, "header");
    ArgumentChecker.notNull(renderer, "renderer");
    _columnSpec = columnSpec;
    _header = header;
    _renderer = renderer;
    if (description != null) {
      _description = description;
    } else {
      _description = header;
    }
    _type = type;
  }

  /**
   * Factory method that creates a column for a key based requirement and calculation configutation and a column type.
   *
   *
   * @param columnSpec
   * @param columnType Type of data displayed in the column
   * @return A column for displaying data calculated for the requirement and calculation configuration
   */
  /* package */ static GridColumn forKey(ColumnSpecification columnSpec,
                                         Class<?> columnType,
                                         TargetLookup targetLookup) {
    CellRenderer renderer = new AnalyticsRenderer(columnSpec, targetLookup);
    return new GridColumn(createHeader(columnSpec),
                          createDescription(columnSpec.getValueProperties()),
                          columnType,
                          renderer,
                          columnSpec);
  }

  /**
   * @return Label text for the column header
   */
  /* package */ String getHeader() {
    return _header;
  }

  /**
   * @return Description of the column's data
   */
  /* package */ String getDescription() {
    return _description;
  }

  /**
   * @return Type of data displayed in the column, can be null if the type is unknown or can change
   */
  /* package */ Class<?> getType() {
    return _type;
  }

  /**
   * @return The specification of this column's analytics data or null if it displays static data.
   */
  /* package */ ColumnSpecification getSpecification() {
    return _columnSpec;
  }

  /* package */ ResultsCell getResults(int rowIndex, ResultsCache cache) {
    return _renderer.getResults(rowIndex, cache, _type);
  }

  private static String createHeader(ColumnSpecification columnKey) {
    String header;
    String normalizedConfigName = columnKey.getCalcConfigName().toLowerCase().trim();
    if ("default".equals(normalizedConfigName) || "portfolio".equals(normalizedConfigName)) {
      header = columnKey.getValueName();
    } else {
      header = columnKey.getCalcConfigName() + "/" + columnKey.getValueName();
    }
    return header;
  }

  private static String createDescription(ValueProperties constraints) {
    if (constraints.isEmpty()) {
      return "No constraints";
    }
    StringBuilder sb = new StringBuilder();
    boolean firstProperty = true;
    for (String propertyName : constraints.getProperties()) {
      if (ValuePropertyNames.FUNCTION.equals(propertyName)) {
        continue;
      }
      if (firstProperty) {
        firstProperty = false;
      } else {
        sb.append("; \n");
      }
      sb.append(propertyName).append("=");
      Set<String> propertyValues = constraints.getValues(propertyName);
      boolean isOptional = constraints.isOptional(propertyName);
      if (propertyValues.size() == 0) {
        sb.append("[empty]");
      } else if (propertyValues.size() == 1 && !isOptional) {
        sb.append(propertyValues.iterator().next());
      } else {
        sb.append("(");
        boolean firstValue = true;
        for (String propertyValue : propertyValues) {
          if (firstValue) {
            firstValue = false;
          } else {
            sb.append(", ");
          }
          sb.append(propertyValue);
        }
        sb.append(")");
      }
      if (isOptional) {
        sb.append("?");
      }
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    return "AnalyticsColumn [" +
        "_header='" + _header + '\'' +
        ", _type=" + _type +
        ", _description='" + _description + '\'' +
        "]";
  }

  // TODO merge this into the AnalyticsColumn and create subclasses for each of the renderer classes
  /* package */ interface CellRenderer {

    ResultsCell getResults(int rowIndex, ResultsCache cache, Class<?> columnType);
  }
}
