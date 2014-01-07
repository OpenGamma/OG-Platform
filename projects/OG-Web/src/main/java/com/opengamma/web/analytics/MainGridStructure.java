/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Column structure of the grid used to display analytics data.
 */
/* package */ abstract class MainGridStructure implements GridStructure {

  /** For mapping cells to values in the results. */
  private final UnversionedValueMappings _valueMappings;

  /** The complete column structure. */
  private final GridColumnGroups _columnGroups;

  /** The fixed column structure. */
  private final GridColumnGroup _fixedColumnGroup;

  /** The non fixed column structure. */
  private final GridColumnGroups _nonFixedColumnGroups;

  /** For looking up the underlying target of a grid cell. */
  private final TargetLookup _targetLookup;

  /** The root node of the portfolio structure. */
  private final AnalyticsNode _rootNode;

  /* package */ MainGridStructure() {
    _columnGroups = GridColumnGroups.empty();
    _fixedColumnGroup = GridColumnGroup.empty();
    _nonFixedColumnGroups = GridColumnGroups.empty();
    _valueMappings = new UnversionedValueMappings();
    _targetLookup = new TargetLookup(_valueMappings, Collections.<Row>emptyList());
    _rootNode = null;
  }

  // TODO refactor this to pass in columns instead of column keys?
  // column would need to return its key (null for static and blotter columns)
  // could pass all columns in a single List<GridColumnGroup> or GridColumnGroups instance
  /* package */ MainGridStructure(GridColumnGroup fixedColumns,
                                  GridColumnGroups nonFixedColumns,
                                  TargetLookup targetLookup,
                                  AnalyticsNode rootNode,
                                  UnversionedValueMappings valueMappings) {
    ArgumentChecker.notNull(targetLookup, "targetLookup");
    ArgumentChecker.notNull(nonFixedColumns, "nonFixedColumns");
    ArgumentChecker.notNull(fixedColumns, "fixedColumns");
    ArgumentChecker.notNull(valueMappings, "valueMappings");
    List<GridColumnGroup> columnGroups = Lists.newArrayList(fixedColumns);
    columnGroups.addAll(nonFixedColumns.getGroups());
    _rootNode = rootNode;
    _columnGroups = new GridColumnGroups(columnGroups);
    _fixedColumnGroup = fixedColumns;
    _nonFixedColumnGroups = nonFixedColumns;
    _targetLookup = targetLookup;
    _valueMappings = valueMappings;
  }

  /* package */ MainGridStructure(GridColumnGroup fixedColumns,
                                  GridColumnGroups nonFixedColumns,
                                  TargetLookup targetLookup,
                                  UnversionedValueMappings valueMappings) {
    ArgumentChecker.notNull(targetLookup, "targetLookup");
    ArgumentChecker.notNull(nonFixedColumns, "nonFixedColumns");
    ArgumentChecker.notNull(fixedColumns, "fixedColumns");
    ArgumentChecker.notNull(valueMappings, "valueMappings");
    List<GridColumnGroup> columnGroups = Lists.newArrayList(fixedColumns);
    columnGroups.addAll(nonFixedColumns.getGroups());
    _columnGroups = new GridColumnGroups(columnGroups);
    _targetLookup = targetLookup;
    _fixedColumnGroup = fixedColumns;
    _nonFixedColumnGroups = nonFixedColumns;
    _rootNode = null;
    _valueMappings = valueMappings;
  }

    /**
     * Returns the calculation configuration name and value specification for a cell in the grid.
     * @param rowIndex The row index
     * @param colIndex The column index
     * @return Pair of value spec and calculation config name.
     * TODO need to specify row using a stable target ID for the row to cope with dynamic aggregation
     */
  @Override
  public Pair<String, ValueSpecification> getValueSpecificationForCell(int rowIndex, int colIndex) {
    if (rowIndex < 0 || rowIndex >= getRowCount() || colIndex < 0 || colIndex >= getColumnCount()) {
      throw new IllegalArgumentException("Cell is outside grid bounds: row=" + rowIndex + ", col=" + colIndex +
                                             ", rowCount=" + getRowCount() + ", colCount=" + getColumnCount());
    }
    return _targetLookup.getTargetForCell(rowIndex, _columnGroups.getColumn(colIndex).getSpecification());
  }
  /**
   * Returns the calculation configuration name and value requirement for a cell in the grid.
   * @param rowIndex The row index
   * @param colIndex The column index
   * @return Pair of value requirement and calculation config name.
   * TODO need to specify row using a stable target ID for the row to cope with dynamic aggregation
   */
  @Override
  public Pair<String, ValueRequirement> getValueRequirementForCell(int rowIndex, int colIndex) {
    if (rowIndex < 0 || rowIndex >= getRowCount() || colIndex < 0 || colIndex >= getColumnCount()) {
      throw new IllegalArgumentException("Cell is outside grid bounds: row=" + rowIndex + ", col=" + colIndex +
                                             ", rowCount=" + getRowCount() + ", colCount=" + getColumnCount());
    }
    return _targetLookup.getRequirementForCell(rowIndex, _columnGroups.getColumn(colIndex).getSpecification());
  }

  @Override
  public GridColumnGroups getColumnStructure() {
    return _columnGroups;
  }

  @Override
  public GridColumnGroup getFixedColumns() {
    return _fixedColumnGroup;
  }

  @Override
  public GridColumnGroups getNonFixedColumns() {
    return _nonFixedColumnGroups;
  }

  @Override
  public int getRowCount() {
    return _targetLookup.getRowCount();
  }

  @Override
  public int getColumnCount() {
    return _columnGroups.getColumnCount();
  }

  public TargetLookup getTargetLookup() {
    return _targetLookup;
  }

  /**
   * @return The root node of the portfolio structure.
   */
  public AnalyticsNode getRootNode() {
    return _rootNode;
  }

  @Override
  public String toString() {
    return "MainGridStructure [_columnGroups=" + _columnGroups + "]";
  }

  public Pair<ViewportResults, Viewport.State> createResults(ViewportDefinition viewportDefinition,
                                                                    ResultsCache cache) {
    boolean updated = false;
    boolean hasData = false;
    List<ResultsCell> results = Lists.newArrayList();
    for (GridCell cell : viewportDefinition) {
      GridColumn column = _columnGroups.getColumn(cell.getColumn());
      ResultsCell resultsCell = column.buildResults(cell.getRow(), cell.getFormat(), cache);
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
                                                          cache.getLastCalculationDuration(), cache.getValuationTime());
    return Pairs.of(viewportResults, state);
  }

  /**
   * @return For mapping cells to values in the results.
   */
  protected UnversionedValueMappings getValueMappings() {
    return _valueMappings;
  }

  /**
   * A row in the grid.
   */
  /* package */ static class Row {

    /** The row's target. */
    private final ComputationTargetReference _target;
    /** The row label. */
    private final String _name;

    /* package */ Row(ComputationTargetReference target, String name) {
      ArgumentChecker.notNull(target, "target");
      ArgumentChecker.notNull(name, "name");
      _target = target;
      _name = name;
    }

    /* package */ ComputationTargetReference getTarget() {
      return _target;
    }

    /* package */ String getName() {
      return _name;
    }

    @Override
    public String toString() {
      return "Row [_target=" + _target + ", _name='" + _name + '\'' + "]";
    }
  }
}
