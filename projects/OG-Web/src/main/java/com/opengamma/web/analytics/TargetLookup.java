/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Allows the {@link ValueSpecification} and calculation configuration name to be looked up for a cell in the main
 * grid.
 */
/* package */ class TargetLookup {

  /** Mappings of requirements to specifications. */
  private final ValueMappings _valueMappings;
  /** The grid rows. */
  private final List<? extends MainGridStructure.Row> _rows;

  /* package */ TargetLookup(ValueMappings valueMappings, List<? extends MainGridStructure.Row> rows) {
    ArgumentChecker.notNull(valueMappings, "valueMappings");
    ArgumentChecker.notNull(rows, "rows");
    _valueMappings = valueMappings;
    _rows = rows;
  }

  // TODO need to specify row using a stable target ID for the row to cope with dynamic reaggregation
  /* package */ Pair<String, ValueSpecification> getTargetForCell(int rowIndex, ColumnSpecification colSpec) {
    if (rowIndex < 0 || rowIndex >= _rows.size()) {
      throw new IllegalArgumentException("Row is outside grid bounds: row=" + rowIndex + ", rowCount=" + _rows.size());
    }
    if (colSpec == null) {
      return null;
    }
    MainGridStructure.Row row = _rows.get(rowIndex);
    ValueRequirement valueReq = new ValueRequirement(colSpec.getValueName(), row.getTarget(), colSpec.getValueProperties());
    String calcConfigName = colSpec.getCalcConfigName();
    ValueSpecification valueSpec = _valueMappings.getValueSpecification(calcConfigName, valueReq);
    if (valueSpec != null) {
      return Pair.of(calcConfigName, valueSpec);
    } else {
      return null;
    }
  }

  /* package */ int getRowCount() {
    return _rows.size();
  }
}
