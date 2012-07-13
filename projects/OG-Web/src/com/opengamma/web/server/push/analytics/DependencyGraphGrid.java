/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class DependencyGraphGrid extends AnalyticsGrid<DependencyGraphViewport> {

  private final String _calcConfigName;
  private final DependencyGraphGridStructure _gridStructure;

  private ViewCycle _latestCycle;
  private AnalyticsHistory _history;

  protected DependencyGraphGrid(DependencyGraphGridStructure gridStructure,
                                String calcConfigName,
                                String gridId,
                                ViewCycle cycle,
                                AnalyticsHistory history) {
    super(gridId);
    ArgumentChecker.notNull(gridStructure, "gridStructure");
    ArgumentChecker.notNull(calcConfigName, "calcConfigName");
    ArgumentChecker.notNull(gridId, "gridId");
    ArgumentChecker.notNull(cycle, "cycle");
    ArgumentChecker.notNull(history, "history");
    _gridStructure = gridStructure;
    _calcConfigName = calcConfigName;
    _latestCycle = cycle;
    _history = history;
  }

  /* package */
  static DependencyGraphGrid create(CompiledViewDefinition compiledViewDef,
                                    ValueSpecification target,
                                    String calcConfigName,
                                    ViewCycle cycle,
                                    AnalyticsHistory history,
                                    String gridId,
                                    ComputationTargetResolver targetResolver) {
    DependencyGraphStructureBuilder builder = new DependencyGraphStructureBuilder(compiledViewDef,
                                                                                  target,
                                                                                  calcConfigName,
                                                                                  targetResolver);
    return new DependencyGraphGrid(builder.getStructure(), calcConfigName, gridId, cycle, history);
  }

  @Override
  public DependencyGraphGridStructure getGridStructure() {
    return _gridStructure;
  }

  @Override
  protected DependencyGraphViewport createViewport(ViewportSpecification viewportSpec, String dataId) {
    return new DependencyGraphViewport(viewportSpec, _calcConfigName, _gridStructure, _latestCycle, _history, dataId);
  }

  public List<String> updateResults(ViewCycle cycle, AnalyticsHistory history) {
    _latestCycle = cycle;
    _history = history;
    List<String> updatedIds = Lists.newArrayList();
    for (DependencyGraphViewport viewport : _viewports.values()) {
      CollectionUtils.addIgnoreNull(updatedIds, viewport.updateResults(cycle, history));
    }
    return updatedIds;
  }

  public void updateViewport(String viewportId,
                             ViewportSpecification viewportSpec,
                             ViewCycle cycle,
                             AnalyticsHistory history) {
    getViewport(viewportId).update(viewportSpec, cycle, history);
  }
}
