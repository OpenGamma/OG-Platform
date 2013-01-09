/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
/* package */ abstract class MainGridStructure implements GridStructure {

  private final List<Row> _rows;
  private final AnalyticsColumnGroups _columnGroups;
  /** Column keys in column index order. */
  private final List<ColumnKey> _columnKeys = Lists.newArrayList();
  /** Mappings of column key (based on {@link ValueRequirement}) to column index. */
  private final Map<ColumnKey, Integer> _colIndexByRequirement;
  /** Mappings of requirements to specifications. */
  private final ValueMappings _valueMappings;

  /* package */ MainGridStructure() {
    _columnGroups = AnalyticsColumnGroups.empty();
    _rows = Collections.emptyList();
    _colIndexByRequirement = Collections.emptyMap();
    _valueMappings = new ValueMappings();
  }

  /* package */ MainGridStructure(AnalyticsColumnGroup fixedColumns,
                                  CompiledViewDefinition compiledViewDef,
                                  List<Row> rows,
                                  ValueMappings valueMappings) {
    ArgumentChecker.notNull(valueMappings, "valueMappings");
    ArgumentChecker.notNull(fixedColumns, "fixedColumns");
    ArgumentChecker.notNull(compiledViewDef, "compiledViewDef");
    ArgumentChecker.notNull(rows, "rows");
    _valueMappings = valueMappings;
    ViewDefinition viewDef = compiledViewDef.getViewDefinition();
    _colIndexByRequirement = Maps.newHashMap();
    // column group for the label and quantity columns which don't come from the calculation results
    List<AnalyticsColumnGroup> columnGroups = Lists.newArrayList(fixedColumns);
    int fixedColCount = fixedColumns.getColumns().size();
    // insert null keys for fixed columns because they aren't referenced by key
    for (int i = 0; i < fixedColCount; i++) {
      _columnKeys.add(null);
    }
    // start the column index after the fixed columns
    int colIndex = fixedColCount;
    for (ViewCalculationConfiguration calcConfig : viewDef.getAllCalculationConfigurations()) {
      String configName = calcConfig.getName();
      List<AnalyticsColumn> configColumns = new ArrayList<AnalyticsColumn>();
      List<ColumnKey> columnKeys = buildColumns(calcConfig);
      for (ColumnKey columnKey : columnKeys) {
        if (!_colIndexByRequirement.containsKey(columnKey)) {
          _colIndexByRequirement.put(columnKey, colIndex);
          colIndex++;
          _columnKeys.add(columnKey);
          String valueName = columnKey.getValueName();
          Class<?> columnType = ValueTypes.getTypeForValueName(valueName);
          configColumns.add(AnalyticsColumn.forKey(columnKey, columnType));
        }
      }
      columnGroups.add(new AnalyticsColumnGroup(configName, configColumns));
    }
    _columnGroups = new AnalyticsColumnGroups(columnGroups);
    _rows = rows;
  }

  /* package */ abstract List<ColumnKey> buildColumns(ViewCalculationConfiguration calcConfig);

  /* package */ Row getRowAtIndex(int rowIndex) {
    return _rows.get(rowIndex);
  }

  /**
   *
   * @param rowIndex
   * @param colIndex
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

  /* package */ boolean isColumnFixed(int colIndex) {
    return colIndex < _columnGroups.getGroups().get(0).getColumns().size();
  }

  @Override
  public String toString() {
    return "MainGridStructure [_rows=" + _rows + ", _columnGroups=" + _columnGroups + "]";
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
