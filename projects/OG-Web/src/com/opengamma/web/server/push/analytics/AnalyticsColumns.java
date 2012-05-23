/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * A set of column groups and columns for a grid displaying analytics data. A column specifies a label for its
 * header, the type of data that it displays and how it is formatted. A column group specifies a number of
 * associated columns that are displayed and controlled as a unit.
 */
public class AnalyticsColumns {

  private final List<ColumnGroup> _columnGroups;

  private AnalyticsColumns(List<ColumnGroup> columnGroups) {
    _columnGroups = columnGroups;
  }

  /**
   * @return An empty set of columns.
   */
  public static AnalyticsColumns empty() {
    return new AnalyticsColumns(Collections.<ColumnGroup>emptyList());
  }

  // TODO different factory methods for portfolio and primitives columns. and what about dependency graphs?
  /* package */ static AnalyticsColumns create(CompiledViewDefinition compiledViewDef) {
    // TODO implement AnalyticsColumns.create()
    throw new UnsupportedOperationException("create not implemented");
  }

  /**
   * Specifies the header label of a column and the type of data it displays.
   * @param <T> The type of data the column displays.
   */
  public static class Column<T> {

    private final String _header;
    private final Class<T> _type;
    // TODO formatting

    public Column(String header, Class<T> type) {
      ArgumentChecker.notNull(header, "header");
      ArgumentChecker.notNull(type, "type");
      _header = header;
      _type = type;
    }
  }

  public static class ColumnGroup {

    private final String _name;
    private final List<Column<?>> _cols;

    public ColumnGroup(String name, List<Column<?>> cols) {
      ArgumentChecker.notNull(name, "name");
      ArgumentChecker.notNull(cols, "cols");
      _name = name;
      _cols = ImmutableList.copyOf(cols);
    }
  }
}
