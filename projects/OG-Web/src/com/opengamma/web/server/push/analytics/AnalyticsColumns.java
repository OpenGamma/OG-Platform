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

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.RequirementBasedColumnKey;

/**
 * A set of column groups and columns for a grid displaying analytics data. A column specifies a label for its
 * header, the type of data that it displays and how it is formatted. A column group specifies a number of
 * associated columns that are displayed and controlled as a unit.
 */
public class AnalyticsColumns {

  private final List<AnalyticsColumnGroup> _columnGroups;
  private final List<AnalyticsColumn<?>> _columns;
  private final Map<RequirementBasedColumnKey, Integer> _indexByRequiement;

  private AnalyticsColumns(List<AnalyticsColumnGroup> columnGroups, Map<RequirementBasedColumnKey, Integer> indexByRequiement) {
    List<AnalyticsColumn<?>> colList = new ArrayList<AnalyticsColumn<?>>();
    _columnGroups = columnGroups;
    for (AnalyticsColumnGroup group : columnGroups) {
      for (AnalyticsColumn<?> column : group.getColumns()) {
        colList.add(column);
      }
    }
    _columns = Collections.unmodifiableList(colList);
    _indexByRequiement = indexByRequiement;
  }

  /**
   * @return An empty set of columns.
   */
  /* package */ static AnalyticsColumns empty() {
    return new AnalyticsColumns(Collections.<AnalyticsColumnGroup>emptyList(),
                                Collections.<RequirementBasedColumnKey, Integer>emptyMap());
  }

  /**
   * @return
   */
  /* package */ static AnalyticsColumns portfolio(CompiledViewDefinition compiledViewDef) {
    ViewDefinition viewDef = compiledViewDef.getViewDefinition();
    Map<RequirementBasedColumnKey, Integer> indexMap = new HashMap<RequirementBasedColumnKey, Integer>();
    List<AnalyticsColumnGroup> columnGroups = new ArrayList<AnalyticsColumnGroup>();
    int colIndex = 1; // col 0 is the node name
    for (ViewCalculationConfiguration calcConfig : viewDef.getAllCalculationConfigurations()) {
      String configName = calcConfig.getName();
      List<AnalyticsColumn<?>> configColumns = new ArrayList<AnalyticsColumn<?>>();
      for (Pair<String, ValueProperties> portfolioOutput : calcConfig.getAllPortfolioRequirements()) {
        String valueName = portfolioOutput.getFirst();
        ValueProperties constraints = portfolioOutput.getSecond();
        RequirementBasedColumnKey columnKey = new RequirementBasedColumnKey(configName, valueName, constraints);
        if (!indexMap.containsKey(columnKey)) {
          indexMap.put(columnKey, colIndex);
          colIndex++;
          configColumns.add(new AnalyticsColumn<Object>(columnKey));
        }
      }
      columnGroups.add(new AnalyticsColumnGroup(configName, configColumns));
    }
    // TODO what about unsatisfied columns?
    // TODO fixed column group for the position name? what about the position column
    return new AnalyticsColumns(columnGroups, indexMap);
  }

  /**
   * @return
   */
  /* package */ static AnalyticsColumns primitives(CompiledViewDefinition compiledViewDef) {
    // TODO implement
    return AnalyticsColumns.empty();
  }

  /**
   * @return
   */
  /* package */ static AnalyticsColumns dependencyGraph() {
    // TODO implement
    return AnalyticsColumns.empty();
  }

  /* package */ int getIndexForRequirement(String calcConfigName, ValueRequirement requirement) {
    RequirementBasedColumnKey key =
        new RequirementBasedColumnKey(calcConfigName, requirement.getValueName(), requirement.getConstraints());
    return _indexByRequiement.get(key);
  }

  /* package */ AnalyticsColumn<?> getColumn(int index) {
    return _columns.get(index);
  }

}
