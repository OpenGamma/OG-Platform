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
 * A set of column groups and columns for a grid displaying analytics data. A column specifies a label for its
 * header, the type of data that it displays and how it is formatted. A column group specifies a number of
 * associated columns that are displayed and controlled as a unit.
 */
public class AnalyticsColumns {

  private static final Set<ValueRequirement> NO_MAPPINGS = ImmutableSet.of();

  private final List<AnalyticsColumnGroup> _columnGroups;
  private final List<AnalyticsColumn> _columns;
  private final Map<RequirementBasedColumnKey, Integer> _indexByRequirement;
  // TODO should this be a field of the grid structure rather than the columns?
  /** Mappings of specification to requirements, keyed by calculation config name. */
  private final Map<String, Map<ValueSpecification, Set<ValueRequirement>>> _specsToReqs;

  private AnalyticsColumns(List<AnalyticsColumnGroup> columnGroups,
                           Map<RequirementBasedColumnKey, Integer> indexByRequirement,
                           // TODO is this needed for primitives? depgraphs? do I need a portfolio-specific subclass?
                           Map<String, Map<ValueSpecification, Set<ValueRequirement>>> specsToReqs) {
    List<AnalyticsColumn> colList = new ArrayList<AnalyticsColumn>();
    _columnGroups = columnGroups;
    for (AnalyticsColumnGroup group : columnGroups) {
      for (AnalyticsColumn column : group.getColumns()) {
        colList.add(column);
      }
    }
    _columns = Collections.unmodifiableList(colList);
    _indexByRequirement = new HashMap<RequirementBasedColumnKey, Integer>(indexByRequirement);
    _specsToReqs = specsToReqs;
  }

  /**
   * @return An empty set of columns.
   */
  /* package */ static AnalyticsColumns empty() {
    return new AnalyticsColumns(Collections.<AnalyticsColumnGroup>emptyList(),
                                Collections.<RequirementBasedColumnKey, Integer>emptyMap(),
                                Collections.<String, Map<ValueSpecification,Set<ValueRequirement>>>emptyMap());
  }

  // TODO combine with primitives()
  /* package */ static AnalyticsColumns portfolio(CompiledViewDefinition compiledViewDef) {
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
    // TODO fixed column group for the position name? what about the position column
    return new AnalyticsColumns(columnGroups, indexMap, specsToReqs);
  }

  // TODO combine with portfolio()
/* package */ static AnalyticsColumns primitives(CompiledViewDefinition compiledViewDef) {
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
    return new AnalyticsColumns(columnGroups, indexMap, specsToReqs);
  }

  /**
   * @return
   */
  /* package */ static AnalyticsColumns dependencyGraph() {
    // TODO implement
    return AnalyticsColumns.empty();
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

  /* package */ AnalyticsColumn getColumn(int index) {
    return _columns.get(index);
  }

  public List<AnalyticsColumnGroup> getColumnGroups() {
    return _columnGroups;
  }

  public int getColumnCount() {
    return _columns.size();
  }

  @Override
  public String toString() {
    return "AnalyticsColumns [" +
        "_columnGroups=" + _columnGroups +
        ", _columns=" + _columns +
        ", _indexByRequirement=" + _indexByRequirement +
        ", _specsToReqs=" + _specsToReqs + "]";
  }
}
