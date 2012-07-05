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
 * TODO different subclasses for portfolio and primitives? portfolio has a tree, primitives is flat
 */
/* package */ abstract class MainGridStructure implements GridBounds {

  private static final Set<ValueRequirement> NO_MAPPINGS = ImmutableSet.of();

  private final List<Row> _rows;
  private final AnalyticsColumnGroups _columnGroups;
  private final Map<RequirementBasedColumnKey, Integer> _indexByRequirement;
  /** Mappings of specification to requirements, keyed by calculation config name. */
  private final Map<String, Map<ValueSpecification, Set<ValueRequirement>>> _specsToReqs;

  /* package */ MainGridStructure() {
    _columnGroups = AnalyticsColumnGroups.empty();
    _rows = Collections.emptyList();
    _indexByRequirement = Collections.emptyMap();
    _specsToReqs = Collections.emptyMap();
  }

  /* package */ MainGridStructure(CompiledViewDefinition compiledViewDef, List<Row> rows) {
    ViewDefinition viewDef = compiledViewDef.getViewDefinition();
    // map of column index keyed by column key
    Map<RequirementBasedColumnKey, Integer> indexMap = Maps.newHashMap();
    // column group for the label column
    AnalyticsColumnGroup labelGroup = new AnalyticsColumnGroup("", ImmutableList.of(new AnalyticsColumn("Label", "")));
    List<AnalyticsColumnGroup> columnGroups = Lists.newArrayList(labelGroup);
    Map<String, Map<ValueSpecification, Set<ValueRequirement>>> specsToReqs = Maps.newHashMap();
    int colIndex = 1; // col 0 is the node name
    for (ViewCalculationConfiguration calcConfig : viewDef.getAllCalculationConfigurations()) {
      String configName = calcConfig.getName();
      CompiledViewCalculationConfiguration compiledConfig = compiledViewDef.getCompiledCalculationConfiguration(configName);
      // store the mappings from outputs to requirements for each calc config
      specsToReqs.put(configName, compiledConfig.getTerminalOutputSpecifications());
      List<AnalyticsColumn> configColumns = new ArrayList<AnalyticsColumn>();

      List<RequirementBasedColumnKey> columnKeys = buildColumns(calcConfig);
      for (RequirementBasedColumnKey columnKey : columnKeys) {
        if (!indexMap.containsKey(columnKey)) {
          indexMap.put(columnKey, colIndex);
          colIndex++;
          configColumns.add(AnalyticsColumn.forKey(columnKey));
        }
      }
      columnGroups.add(new AnalyticsColumnGroup(configName, configColumns));
    }
    _columnGroups = new AnalyticsColumnGroups(columnGroups);
    _rows = rows;
    _indexByRequirement = indexMap;
    _specsToReqs = specsToReqs;
  }

  abstract List<RequirementBasedColumnKey> buildColumns(ViewCalculationConfiguration calcConfig);

  /* package */ Row getRowAtIndex(int rowIndex) {
    return _rows.get(rowIndex);
  }

  /* package */ int getColumnIndexForRequirement(String calcConfigName, ValueRequirement requirement) {
    RequirementBasedColumnKey key =
        new RequirementBasedColumnKey(calcConfigName, requirement.getValueName(), requirement.getConstraints());
    return _indexByRequirement.get(key);
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
   *
   * @param row
   * @param col
   * @return Pair of value spec and calculation config name.
   * TODO need to reverse the map of valueSpec->set(valueReq)
   * TODO need to specify this using a stable target ID to cope with dynamic reaggregation
   */
  /* package */ Pair<ValueSpecification, String> getTargetForCell(int row, int col) {
    throw new UnsupportedOperationException();
  }

  public List<AnalyticsColumnGroup> getColumnGroups() {
    return _columnGroups.getGroups();
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
}
