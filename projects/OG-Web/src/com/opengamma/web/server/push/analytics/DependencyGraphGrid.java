/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

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
  private ViewCycle _latestCycle;
  private AnalyticsHistory _history;
  private final DependencyGraphGridStructure _gridStructure;

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

  public void updateResults(ViewCycle cycle, AnalyticsHistory history) {
    _latestCycle = cycle;
    _history = history;
    for (DependencyGraphViewport viewport : _viewports.values()) {
      viewport.updateResults(cycle, history);
    }
  }

  public void updateViewport(String viewportId,
                             ViewportSpecification viewportSpec,
                             ViewCycle cycle,
                             AnalyticsHistory history) {
    getViewport(viewportId).update(viewportSpec, cycle, history);
  }
}
