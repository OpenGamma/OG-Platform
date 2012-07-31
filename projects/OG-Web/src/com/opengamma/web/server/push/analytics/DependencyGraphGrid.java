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
  private ResultsCache _cache;

  protected DependencyGraphGrid(DependencyGraphGridStructure gridStructure,
                                String calcConfigName,
                                String gridId,
                                ViewCycle cycle,
                                ResultsCache cache) {
    super(gridId);
    ArgumentChecker.notNull(gridStructure, "gridStructure");
    ArgumentChecker.notNull(calcConfigName, "calcConfigName");
    ArgumentChecker.notNull(gridId, "gridId");
    ArgumentChecker.notNull(cycle, "cycle");
    ArgumentChecker.notNull(cache, "history");
    _gridStructure = gridStructure;
    _calcConfigName = calcConfigName;
    _latestCycle = cycle;
    _cache = cache;
  }

  /* package */
  static DependencyGraphGrid create(CompiledViewDefinition compiledViewDef,
                                    ValueSpecification target,
                                    String calcConfigName,
                                    ViewCycle cycle,
                                    ResultsCache cache,
                                    String gridId,
                                    ComputationTargetResolver targetResolver) {
    DependencyGraphStructureBuilder builder = new DependencyGraphStructureBuilder(compiledViewDef,
                                                                                  target,
                                                                                  calcConfigName,
                                                                                  targetResolver);
    return new DependencyGraphGrid(builder.getStructure(), calcConfigName, gridId, cycle, cache);
  }

  @Override
  public DependencyGraphGridStructure getGridStructure() {
    return _gridStructure;
  }

  @Override
  protected DependencyGraphViewport createViewport(ViewportSpecification viewportSpec, String dataId) {
    return new DependencyGraphViewport(viewportSpec, _calcConfigName, _gridStructure, _latestCycle, _cache, dataId);
  }

  public List<String> updateResults(ViewCycle cycle, ResultsCache cache) {
    _latestCycle = cycle;
    _cache = cache;
    List<String> updatedIds = Lists.newArrayList();
    for (DependencyGraphViewport viewport : _viewports.values()) {
      CollectionUtils.addIgnoreNull(updatedIds, viewport.updateResults(cycle, cache));
    }
    return updatedIds;
  }

  public long updateViewport(String viewportId,
                             ViewportSpecification viewportSpec,
                             ViewCycle cycle,
                             ResultsCache cache) {
    return getViewport(viewportId).update(viewportSpec, cycle, cache);
  }
}
