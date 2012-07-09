/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.RequirementBasedColumnKey;

/**
 *
 */
/* package */ abstract class MainGridStructure implements GridStructure {

  private static final Set<ValueRequirement> NO_MAPPINGS = ImmutableSet.of();

  private final List<Row> _rows;
  private final AnalyticsColumnGroups _columnGroups;
  /** Column keys in column index order. */
  private final List<RequirementBasedColumnKey> _columnKeys = Lists.newArrayList();
  /** Mappings of column key (based on {@link ValueRequirement}) to column index. */
  private final Map<RequirementBasedColumnKey, Integer> _colIndexByRequirement;
  /** Mappings of specification to requirements, keyed by calculation config name. */
  private final Map<String, Map<ValueSpecification, Set<ValueRequirement>>> _specsToReqs;
  /** Mappings of requirements to specifications. */
  private final Map<ValueRequirementKey, ValueSpecification> _reqsToSpecs;

  /* package */ MainGridStructure() {
    _columnGroups = AnalyticsColumnGroups.empty();
    _rows = Collections.emptyList();
    _colIndexByRequirement = Collections.emptyMap();
    _specsToReqs = Collections.emptyMap();
    _reqsToSpecs = Collections.emptyMap();
  }

  /* package */ MainGridStructure(CompiledViewDefinition compiledViewDef, List<Row> rows) {
    ViewDefinition viewDef = compiledViewDef.getViewDefinition();
    _colIndexByRequirement = Maps.newHashMap();
    // column group for the label column
    AnalyticsColumnGroup labelGroup = new AnalyticsColumnGroup("", ImmutableList.of(new AnalyticsColumn("Label", "")));
    List<AnalyticsColumnGroup> columnGroups = Lists.newArrayList(labelGroup);
    _specsToReqs = Maps.newHashMap();
    _reqsToSpecs = Maps.newHashMap();
    int colIndex = 1; // col 0 is the node name
    _columnKeys.add(null); // there is no key for the row label column, stick null in there to get the indices right
    for (ViewCalculationConfiguration calcConfig : viewDef.getAllCalculationConfigurations()) {
      String configName = calcConfig.getName();
      CompiledViewCalculationConfiguration compiledConfig = compiledViewDef.getCompiledCalculationConfiguration(configName);
      // store the mappings from outputs to requirements for each calc config
      Map<ValueSpecification, Set<ValueRequirement>> terminalOutputs = compiledConfig.getTerminalOutputSpecifications();
      _specsToReqs.put(configName, terminalOutputs);
      for (Map.Entry<ValueSpecification, Set<ValueRequirement>> entry : terminalOutputs.entrySet()) {
        for (ValueRequirement valueRequirement : entry.getValue()) {
          _reqsToSpecs.put(new ValueRequirementKey(valueRequirement, configName), entry.getKey());
        }
      }
      List<AnalyticsColumn> configColumns = new ArrayList<AnalyticsColumn>();

      List<RequirementBasedColumnKey> columnKeys = buildColumns(calcConfig);
      for (RequirementBasedColumnKey columnKey : columnKeys) {
        if (!_colIndexByRequirement.containsKey(columnKey)) {
          _colIndexByRequirement.put(columnKey, colIndex);
          colIndex++;
          _columnKeys.add(columnKey);
          configColumns.add(AnalyticsColumn.forKey(columnKey));
        }
      }
      columnGroups.add(new AnalyticsColumnGroup(configName, configColumns));
    }
    _columnGroups = new AnalyticsColumnGroups(columnGroups);
    _rows = rows;
  }

  /* package */ abstract List<RequirementBasedColumnKey> buildColumns(ViewCalculationConfiguration calcConfig);

  /* package */ Row getRowAtIndex(int rowIndex) {
    return _rows.get(rowIndex);
  }

  /* package */ int getColumnIndexForRequirement(String calcConfigName, ValueRequirement requirement) {
    RequirementBasedColumnKey key =
        new RequirementBasedColumnKey(calcConfigName, requirement.getValueName(), requirement.getConstraints());
    return _colIndexByRequirement.get(key);
  }

  /* package */ Set<ValueRequirement> getRequirementsForSpecification(String calcConfigName, ValueSpecification spec) {
    Map<ValueSpecification, Set<ValueRequirement>> specToReqs = _specsToReqs.get(calcConfigName);
    if (specToReqs == null) {
      return null;
    }
    Set<ValueRequirement> reqs = specToReqs.get(spec);
    if (reqs == null) {
      return NO_MAPPINGS;
    }
    return reqs;
  }

  /**
   * This is nasty but necessary. The type of the columns isn't known when the view compiles as engine functions
   * don't declare what type they produce. The only way to find out is to wait for the first set of results to
   * arrive and then lazily initialize the column types.
   * @param colIndex Column index
   * @param type Type of the values displayed in the column
   * @return {@code true} if the type was updated
   */
  /* package */ boolean setTypeForColumn(int colIndex, Class<?> type) {
    return _columnGroups.getColumn(colIndex).setType(type);
  }

  /**
   *
   * @param rowIndex
   * @param colIndex
   * @return Pair of value spec and calculation config name.
   * TODO need to specify this using a stable target ID to cope with dynamic reaggregation
   */
  /* package */ Pair<ValueSpecification, String> getTargetForCell(int rowIndex, int colIndex) {
    if (rowIndex < 0 || rowIndex >= getRowCount() || colIndex < 0 || colIndex >= getColumnCount()) {
      throw new IllegalArgumentException("Cell is outside grid bounds: row=" + rowIndex + ", col=" + colIndex +
                                             ", rowCount=" + getRowCount() + ", colCount=" + getColumnCount());
    }
    RequirementBasedColumnKey colKey = _columnKeys.get(colIndex);
    if (colKey == null) {
      return null;
    }
    Row row = _rows.get(rowIndex);
     ValueRequirement valueReq = new ValueRequirement(colKey.getValueName(), row.getTarget(), colKey.getValueProperties());
    String calcConfigName = colKey.getCalcConfigName();
    ValueRequirementKey requirementKey = new ValueRequirementKey(valueReq, calcConfigName);
    ValueSpecification valueSpec = _reqsToSpecs.get(requirementKey);
    return Pair.of(valueSpec, calcConfigName);
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

  /* package */ static class Row {

    private final ComputationTargetSpecification _target;
    private final String _name;

    /* package */ Row(ComputationTargetSpecification target, String name) {
      ArgumentChecker.notNull(target, "target");
      ArgumentChecker.notNull(name, "name");
      _target = target;
      _name = name;
    }

    /* package */ ComputationTargetSpecification getTarget() {
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

  /**
   * Map key that consists of a {@link ValueRequirement} and corresponding calculation configuration name.
   */
  private static class ValueRequirementKey {

    private final ValueRequirement _valueRequirement;
    private final String _calcConfigName;

    private ValueRequirementKey(ValueRequirement valueRequirement, String calcConfigName) {
      ArgumentChecker.notNull(valueRequirement, "valueRequirement");
      ArgumentChecker.notNull(calcConfigName, "calcConfigName");
      _valueRequirement = valueRequirement;
      _calcConfigName = calcConfigName;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ValueRequirementKey that = (ValueRequirementKey) o;

      if (!_calcConfigName.equals(that._calcConfigName)) {
        return false;
      }
      return _valueRequirement.equals(that._valueRequirement);
    }

    @Override
    public int hashCode() {
      int result = _valueRequirement.hashCode();
      result = 31 * result + _calcConfigName.hashCode();
      return result;
    }
  }
}
