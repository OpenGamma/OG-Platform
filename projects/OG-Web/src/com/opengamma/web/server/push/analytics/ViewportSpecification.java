/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.opengamma.util.ArgumentChecker;

/**
 * TODO should there be subclasses for portfolio, depgraph, primitives? and associated visitors
 * TODO expanded cells that need full data (matrices, vectors, curves etc)
 */
public class ViewportSpecification {

  private final List<Integer> _rows;
  private final SortedSet<Integer> _columns;

  public ViewportSpecification(List<Integer> rows, SortedSet<Integer> columns) {
    ArgumentChecker.notNull(rows, "rows");
    ArgumentChecker.notNull(columns, "columns");
    _rows = ImmutableList.copyOf(rows);
    _columns = ImmutableSortedSet.copyOf(columns);
  }

  public static ViewportSpecification empty() {
    return new ViewportSpecification(Collections.<Integer>emptyList(), new TreeSet<Integer>());
  }

  public List<Integer> getRows() {
    return _rows;
  }

  public SortedSet<Integer> getColumns() {
    return _columns;
  }
}
