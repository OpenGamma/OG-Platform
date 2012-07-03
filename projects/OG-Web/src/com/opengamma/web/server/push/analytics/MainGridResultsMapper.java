/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.RequirementBasedColumnKey;

/**
 * TODO merge with MainGridStructure
 */
public class MainGridResultsMapper {

  private static final Set<ValueRequirement> NO_MAPPINGS = ImmutableSet.of();

  private final List<AnalyticsColumnGroup> _columnGroups;

  private final Map<RequirementBasedColumnKey, Integer> _indexByRequirement;
  /** Mappings of specification to requirements, keyed by calculation config name. */
  private final Map<String, Map<ValueSpecification, Set<ValueRequirement>>> _specsToReqs;

  private MainGridResultsMapper(List<AnalyticsColumnGroup> columnGroups,
                                Map<RequirementBasedColumnKey, Integer> indexByRequirement,
                                Map<String, Map<ValueSpecification, Set<ValueRequirement>>> specsToReqs) {
    _columnGroups = columnGroups;
    _indexByRequirement = new HashMap<RequirementBasedColumnKey, Integer>(indexByRequirement);
    _specsToReqs = specsToReqs;
  }

  /**
   * @return An empty mapper.
   */
  /* package */ static MainGridResultsMapper empty() {
    return new MainGridResultsMapper(Collections.<AnalyticsColumnGroup>emptyList(),
                                     Collections.<RequirementBasedColumnKey, Integer>emptyMap(),
                                     Collections.<String, Map<ValueSpecification,Set<ValueRequirement>>>emptyMap());
  }

  // TODO combine with primitives()
  /* package */ static MainGridResultsMapper portfolio(CompiledViewDefinition compiledViewDef) {
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

      for (Pair<String, ValueProperties> portfolioOutput : calcConfig.getAllPortfolioRequirements()) {
        String valueName = portfolioOutput.getFirst();
        ValueProperties constraints = portfolioOutput.getSecond();
        RequirementBasedColumnKey columnKey = new RequirementBasedColumnKey(configName, valueName, constraints);
        if (!indexMap.containsKey(columnKey)) {
          indexMap.put(columnKey, colIndex);
          colIndex++;
          configColumns.add(AnalyticsColumn.forKey(columnKey));
        }
      }
      columnGroups.add(new AnalyticsColumnGroup(configName, configColumns));
    }
    // TODO what about unsatisfied columns?
    return new MainGridResultsMapper(columnGroups, indexMap, specsToReqs);
  }

  // TODO combine with portfolio()
/* package */ static MainGridResultsMapper primitives(CompiledViewDefinition compiledViewDef) {
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

      for (ValueRequirement specificRequirement : calcConfig.getSpecificRequirements()) {
        if (specificRequirement.getTargetSpecification().getType() == ComputationTargetType.PRIMITIVE) {
          String valueName = specificRequirement.getValueName();
          ValueProperties constraints = specificRequirement.getConstraints();
          RequirementBasedColumnKey columnKey = new RequirementBasedColumnKey(configName, valueName, constraints);
          if (!indexMap.containsKey(columnKey)) {
            indexMap.put(columnKey, colIndex);
            colIndex++;
            configColumns.add(AnalyticsColumn.forKey(columnKey));
          }
        }
      }
      columnGroups.add(new AnalyticsColumnGroup(configName, configColumns));
    }
    return new MainGridResultsMapper(columnGroups, indexMap, specsToReqs);
  }

  // TODO are any of these needed for dependency graphs?
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

  /* package */ List<AnalyticsColumnGroup> getColumnGroups() {
    return _columnGroups;
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

  @Override
  public String toString() {
    return "AnalyticsResultsMapper [" +
        "_columnGroups=" + _columnGroups +
        ", _indexByRequirement=" + _indexByRequirement +
        ", _specsToReqs=" + _specsToReqs +
        "]";
  }
}
