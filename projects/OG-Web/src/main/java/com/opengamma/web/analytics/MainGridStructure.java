/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
  /* package */ abstract class MainGridStructure implements GridStructure {

  private final AnalyticsColumnGroups _columnGroups;
  /** Column keys in column index order. TODO use Map<Integer, ColumnKey>? nicer than having null entries */
  private final List<ColumnKey> _columnKeys = Lists.newArrayList();
  /** Mappings of column key (based on {@link ValueRequirement}) to column index. */
  private final Map<ColumnKey, Integer> _colIndexByRequirement;
  /** Mappings of requirements to specifications. */
  private final ValueMappings _valueMappings;
  private final List<Row> _rows;

  /* package */ MainGridStructure() {
    _columnGroups = AnalyticsColumnGroups.empty();
    _colIndexByRequirement = Collections.emptyMap();
    _valueMappings = new ValueMappings();
    _rows = Collections.emptyList();
  }

  /* package */ MainGridStructure(List<AnalyticsColumnGroup> staticColumns,
                                  Map<String, List<ColumnKey>> analyticsColumns,
                                  CompiledViewDefinition compiledViewDef,
                                  ValueMappings valueMappings,
                                  List<Row> rows) {
    ArgumentChecker.notNull(valueMappings, "valueMappings");
    ArgumentChecker.notNull(staticColumns, "staticColumns");
    ArgumentChecker.notNull(compiledViewDef, "compiledViewDef");
    ArgumentChecker.notNull(rows, "rows");
    _valueMappings = valueMappings;
    _colIndexByRequirement = Maps.newHashMap();
    _rows = rows;
    // columns which don't come from the calculation results
    List<AnalyticsColumnGroup> columnGroups = Lists.newArrayList(staticColumns);
    for (AnalyticsColumnGroup group : staticColumns) {
      int colCount = group.getColumns().size();
      // insert null keys for these columns because they aren't referenced by key when analytics results arrive
      for (int i = 0; i < colCount; i++) {
        _columnKeys.add(null);
      }
    }
    // start the column index after the static columns
    // TODO could the columns be passed in? would need a column subclass with a columnKey property
    int colIndex = _columnKeys.size() - 1;
    for (Map.Entry<String, List<ColumnKey>> entry : analyticsColumns.entrySet()) {
      String configName = entry.getKey();
      List<ColumnKey> columnKeys = entry.getValue();
      List<AnalyticsColumn> configColumns = Lists.newArrayList();
      for (ColumnKey columnKey : columnKeys) {
        if (!_colIndexByRequirement.containsKey(columnKey)) {
          _colIndexByRequirement.put(columnKey, colIndex);
          colIndex++;
          _columnKeys.add(columnKey);
          String valueName = columnKey.getValueName();
          Class<?> columnType = ValueTypes.getTypeForValueName(valueName);
          configColumns.add(AnalyticsColumn.forKey(columnKey, columnType, colIndex, this));
        }
      }
      columnGroups.add(new AnalyticsColumnGroup(configName, configColumns));
    }
    _columnGroups = new AnalyticsColumnGroups(columnGroups);
  }

  /**
   * Returns the calculation configuration name and value specification for a cell in the grid.
   * @param rowIndex The row index
   * @param colIndex The column index
   * @return Pair of value spec and calculation config name.
   * TODO need to specify this using a stable target ID to cope with dynamic reaggregation
   */
  @Override
  public Pair<String, ValueSpecification> getTargetForCell(int rowIndex, int colIndex) {
    if (rowIndex < 0 || rowIndex >= getRowCount() || colIndex < 0 || colIndex >= getColumnCount()) {
      throw new IllegalArgumentException("Cell is outside grid bounds: row=" + rowIndex + ", col=" + colIndex +
                                             ", rowCount=" + getRowCount() + ", colCount=" + getColumnCount());
    }
    ColumnKey colKey = _columnKeys.get(colIndex);
    if (colKey == null) {
      return null;
    }
    Row row = _rows.get(rowIndex);
    ValueRequirement valueReq = new ValueRequirement(colKey.getValueName(), row.getTarget(), colKey.getValueProperties());
    String calcConfigName = colKey.getCalcConfigName();
    ValueSpecification valueSpec = _valueMappings.getValueSpecification(calcConfigName, valueReq);
    if (valueSpec != null) {
      return Pair.of(calcConfigName, valueSpec);
    } else {
      return null;
    }
  }

  @Override
  public AnalyticsColumnGroups getColumnStructure() {
    return _columnGroups;
  }

  @Override
  public int getRowCount() {
    return _rows.size();
  }

  @Override
  public int getColumnCount() {
    return _columnGroups.getColumnCount();
  }

  public Class<?> getColumnType(int colIndex) {
    return _columnGroups.getColumn(colIndex).getType();
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
      AnalyticsColumn column = _columnGroups.getColumn(cell.getColumn());
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
