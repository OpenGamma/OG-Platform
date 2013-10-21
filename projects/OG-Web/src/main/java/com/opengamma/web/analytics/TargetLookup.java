/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Allows the {@link ValueSpecification} and calculation configuration name to be looked up for a cell in the main
 * grid.
 * TODO this needs a better name, not sure what though
 */
/* package */ class TargetLookup {

  /** Mappings of requirements to specifications. */
  private final UnversionedValueMappings _valueMappings;
  /** The grid rows. */
  private final List<? extends MainGridStructure.Row> _rows;

  /* package */ TargetLookup(UnversionedValueMappings valueMappings, List<? extends MainGridStructure.Row> rows) {
    ArgumentChecker.notNull(valueMappings, "valueMappings");
    ArgumentChecker.notNull(rows, "rows");
    _valueMappings = valueMappings;
    _rows = Collections.unmodifiableList(rows);
  }

  /* package */ Pair<String, ValueRequirement> getRequirementForCell(int rowIndex, ColumnSpecification colSpec) {
    if (rowIndex < 0 || rowIndex >= _rows.size()) {
      throw new IllegalArgumentException("Row is outside grid bounds: row=" + rowIndex + ", rowCount=" + _rows.size());
    }
    if (colSpec == null) {
      return null;
    }
    MainGridStructure.Row row = _rows.get(rowIndex);
    ValueRequirement valueReq = new ValueRequirement(colSpec.getValueName(), row.getTarget(), colSpec.getValueProperties());
    String calcConfigName = colSpec.getCalcConfigName();
    return Pairs.of(calcConfigName, valueReq);
  }

  // TODO need to specify row using a stable target ID for the row to cope with dynamic aggregation
  /* package */ Pair<String, ValueSpecification> getTargetForCell(int rowIndex, ColumnSpecification colSpec) {
    if (rowIndex < 0 || rowIndex >= _rows.size()) {
      throw new IllegalArgumentException("Row is outside grid bounds: row=" + rowIndex + ", rowCount=" + _rows.size());
    }
    return getTargetForCell(_rows.get(rowIndex), colSpec);
  }

  private Pair<String, ValueSpecification> getTargetForCell(MainGridStructure.Row row, ColumnSpecification colSpec) {
    if (colSpec == null) {
      return null;
    }
    // TODO is this worth it? would it be better to have a map of cell->valueSpec? would use more memory but be less work
    ValueRequirement valueReq = new ValueRequirement(colSpec.getValueName(), row.getTarget(), colSpec.getValueProperties());
    String calcConfigName = colSpec.getCalcConfigName();
    ValueSpecification valueSpec = _valueMappings.getValueSpecification(calcConfigName, valueReq);
    if (valueSpec != null) {
      return Pairs.of(calcConfigName, valueSpec);
    } else {
      return null;
    }
  }

  /* package */ Iterator<Pair<String, ValueSpecification>> getTargetsForColumn(final ColumnSpecification colSpec) {
    return Iterators.transform(_rows.iterator(), new Function<MainGridStructure.Row, Pair<String, ValueSpecification>>() {
      @Override
      public Pair<String, ValueSpecification> apply(MainGridStructure.Row row) {
        return getTargetForCell(row, colSpec);
      }
    });
  }

  /* package */ int getRowCount() {
    return _rows.size();
  }

  /* package */ MainGridStructure.Row getRow(int index) {
    return _rows.get(index);
  }
}
