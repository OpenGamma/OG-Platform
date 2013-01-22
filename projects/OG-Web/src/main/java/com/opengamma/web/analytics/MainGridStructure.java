/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
  /* package */ abstract class MainGridStructure implements GridStructure {

  private final GridColumnGroups _columnGroups;
  private final TargetLookup _targetLookup;

  /* package */ MainGridStructure() {
    _columnGroups = GridColumnGroups.empty();
    _targetLookup = new TargetLookup(new ValueMappings(), Collections.<Row>emptyList());
  }

  // TODO refactor this to pass in columns instead of column keys?
  // column would need to return its key (null for static and blotter columns)
  // could pass all columns in a single List<GridColumnGroup> or GridColumnGroups instance
  /* package */ MainGridStructure(GridColumnGroups columnGroups, TargetLookup targetLookup) {
    ArgumentChecker.notNull(targetLookup, "targetLookup");
    ArgumentChecker.notNull(columnGroups, "columnGroups");
    _columnGroups = columnGroups;
    _targetLookup = targetLookup;
  }

    /**
     * Returns the calculation configuration name and value specification for a cell in the grid.
     * @param rowIndex The row index
     * @param colIndex The column index
     * @return Pair of value spec and calculation config name.
     * TODO need to specify row using a stable target ID for the row to cope with dynamic reaggregation
     */
  @Override
  public Pair<String, ValueSpecification> getTargetForCell(int rowIndex, int colIndex) {
    if (rowIndex < 0 || rowIndex >= getRowCount() || colIndex < 0 || colIndex >= getColumnCount()) {
      throw new IllegalArgumentException("Cell is outside grid bounds: row=" + rowIndex + ", col=" + colIndex +
                                             ", rowCount=" + getRowCount() + ", colCount=" + getColumnCount());
    }
    return _targetLookup.getTargetForCell(rowIndex, _columnGroups.getColumn(colIndex).getSpecification());
  }

  @Override
  public GridColumnGroups getColumnStructure() {
    return _columnGroups;
  }

  @Override
  public int getRowCount() {
    return _targetLookup.getRowCount();
  }

  @Override
  public int getColumnCount() {
    return _columnGroups.getColumnCount();
  }

  @Override
  public String toString() {
    return "MainGridStructure [_columnGroups=" + _columnGroups + "]";
  }

  /* package */ Pair<ViewportResults, Viewport.State> createResults(ViewportDefinition viewportDefinition,
                                                                    ResultsCache cache) {
    boolean updated = false;
    boolean hasData = false;
    List<ResultsCell> results = Lists.newArrayList();
    for (GridCell cell : viewportDefinition) {
      GridColumn column = _columnGroups.getColumn(cell.getColumn());
      ResultsCell resultsCell = column.getResults(cell.getRow(), cache);
      updated = updated || resultsCell.isUpdated();
      if (resultsCell.getValue() != null) {
        hasData = true;
      }
      results.add(resultsCell);
    }
    Viewport.State state;
    if (updated) {
      state = Viewport.State.FRESH_DATA;
    } else if (hasData) {
      state = Viewport.State.STALE_DATA;
    } else {
      state = Viewport.State.EMPTY;
    }
    ViewportResults viewportResults = new ViewportResults(results,
                                                          viewportDefinition,
                                                          _columnGroups,
                                                          cache.getLastCalculationDuration());
    return Pair.of(viewportResults, state);
  }

  // TODO do I need different subclasses for portfolios and primitives, primitives version has security?
  /* package */ static class Row {

    private final ComputationTargetSpecification _target;
    private final String _name;
    private final BigDecimal _quantity;

    /* package */ Row(ComputationTargetSpecification target, String name) {
      this(target, name, null);
    }

    /* package */ Row(ComputationTargetSpecification target, String name, BigDecimal quantity) {
      ArgumentChecker.notNull(target, "target");
      ArgumentChecker.notNull(name, "name");
      _target = target;
      _name = name;
      _quantity = quantity;
    }

    /* package */ ComputationTargetSpecification getTarget() {
      return _target;
    }

    /* package */ String getName() {
      return _name;
    }

    // TODO this is specific to the portfolio grid
    /* package */ BigDecimal getQuantity() {
      return _quantity;
    }

    @Override
    public String toString() {
      return "Row [_target=" + _target + ", _name='" + _name + '\'' + ", _quantity=" + _quantity + "]";
    }
  }
}
