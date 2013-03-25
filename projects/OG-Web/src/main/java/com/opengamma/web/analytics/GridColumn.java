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

  /** The column header. */
  private final String _header;
  /** Description of the column. */
  private final String _description;
  /** Type of data displayed in the column, null if unknown or if different rows contain different types. */
  private final Class<?> _type;
  /** Converts cell data to strings or objects for display in the client. */
  private final CellRenderer _renderer;
  /** Specifies the analytics data displayed in the column, null if the column data doesn't come from the engine. */
  private final ColumnSpecification _columnSpec;
  /** Null if this column doesn't display exploded data, otherwise the key into the exploded data */
  private final Object _inlineKey;
  /** Null if this column doesn't display exploded data, otherwise the index in the set of exploded data columns */
  private final Integer _inlineIndex;

  /* package */ GridColumn(String header, String description, Class<?> type, CellRenderer renderer) {
    this(header, description, type, renderer, null, null, null);
  }

  /* package */ GridColumn(String header,
                           String description,
                           Class<?> type,
                           CellRenderer renderer,
                           ColumnSpecification columnSpec) {
    this(header, description, type, renderer, columnSpec, null, null);
  }

  /* package */ GridColumn(String header,
                           String description,
                           Class<?> type,
                           CellRenderer renderer,
                           ColumnSpecification columnSpec,
                           Object inlineKey,
                           Integer inlineIndex) {
    ArgumentChecker.notNull(header, "header");
    ArgumentChecker.notNull(renderer, "renderer");
    _inlineIndex = inlineIndex;
    _columnSpec = columnSpec;
    _header = header;
    _renderer = renderer;
    if (description != null) {
      _description = description;
    } else {
      _description = header;
    }
    _type = type;
    _inlineKey = inlineKey;
  }

  /**
   * Factory method that creates a column for a column specification, calculation configutation and data type.
   * @param columnSpec The column specification
   * @param columnType Type of data displayed in the column
   * @return A column for displaying data calculated for the requirement and calculation configuration
   */
  /* package */ static GridColumn forSpec(ColumnSpecification columnSpec,
                                          Class<?> columnType,
                                          TargetLookup targetLookup) {
    return forSpec(columnSpec.getValueName(), columnSpec, columnType, targetLookup, null, null);
  }

  /**
   * Factory method to create a column for inlined values. These are single values (e.g. vectors) displayed over
   * multiple columns.
   * @param headerSuffix The header suffix. Header is derived from the value name (common to all columns for the value)
   * and the suffix
   * @param columnSpec Specification of the column's value
   * @param columnType The type of the column's value
   * @param targetLookup For looking up values to populate the column
   * @param inlineIndex The index of the individual data item in this column. This is used to extract each cell's data
   * from the value.
   * @return The column
   */
  /* package */ static GridColumn forSpec(String header,
                                          ColumnSpecification columnSpec,
                                          Class<?> columnType,
                                          TargetLookup targetLookup,
                                          Object inlineKey,
                                          Integer inlineIndex) {
    CellRenderer renderer = new AnalyticsRenderer(columnSpec, targetLookup);
    return new GridColumn(header,
                          createDescription(columnSpec.getValueProperties()),
                          columnType,
                          renderer,
                          columnSpec,
                          inlineKey,
                          inlineIndex);
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

  /* package */ Integer getInlineIndex() {
    return _inlineIndex;
  }

  /* package */ ResultsCell buildResults(int rowIndex, ResultsCache cache) {
    return _renderer.getResults(rowIndex, cache, _type, _inlineKey);
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

    ResultsCell getResults(int rowIndex, ResultsCache cache, Class<?> columnType, Object inlineKey);
  }
}
