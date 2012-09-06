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
 * Grid for displaying the dependency graph for a cell. This graph contains all the calculation steps used
 * in deriving the cell's value. The first row of the grid shows the cell's value and subsequent rows show a
 * tree structure containing the dependency graph.
 */
public class DependencyGraphGrid extends AnalyticsGrid<DependencyGraphViewport> {

  private final String _calcConfigName;
  private final DependencyGraphGridStructure _gridStructure;
  /**
   * Each dependency graph maintains its own cache of results. The values in a dependency graph aren't necessarily
   * view output values (apart from the root) and therefore aren't included in the main results model and main results
   * cache.
   */
  private final ResultsCache _cache = new ResultsCache();

  private ViewCycle _latestCycle;

  private DependencyGraphGrid(DependencyGraphGridStructure gridStructure,
                              String calcConfigName,
                              String callbackId,
                              ViewCycle cycle) {
    super(callbackId);
    ArgumentChecker.notNull(gridStructure, "gridStructure");
    ArgumentChecker.notNull(calcConfigName, "calcConfigName");
    ArgumentChecker.notNull(callbackId, "callbackId");
    ArgumentChecker.notNull(cycle, "cycle");
    _gridStructure = gridStructure;
    _calcConfigName = calcConfigName;
    _latestCycle = cycle;
  }

  /**
   * Creates a new grid for displaying a dependency graph of calculations.
   * @param compiledViewDef The view definition from which the graph and calculations were derived
   * @param target The object whose dependency graph is being displayed
   * @param calcConfigName The calculation configuration used for the calculations
   * @param cycle The view cycle that calculated the results
   * @param callbackId ID that's passed to listeners when the row and column structure of the grid changes
   * @param targetResolver For looking up the target of the calculation given its specification
   * @return The grid
   */
  /* package */ static DependencyGraphGrid create(CompiledViewDefinition compiledViewDef,
                                                  ValueSpecification target,
                                                  String calcConfigName,
                                                  ViewCycle cycle,
                                                  String callbackId,
                                                  ComputationTargetResolver targetResolver) {
    DependencyGraphStructureBuilder builder =
        new DependencyGraphStructureBuilder(compiledViewDef, target, calcConfigName, targetResolver);
    return new DependencyGraphGrid(builder.getStructure(), calcConfigName, callbackId, cycle);
  }

  @Override
  public DependencyGraphGridStructure getGridStructure() {
    return _gridStructure;
  }

  @Override
  protected DependencyGraphViewport createViewport(ViewportSpecification viewportSpec, String callbackId) {
    return new DependencyGraphViewport(viewportSpec, _calcConfigName, _gridStructure, _latestCycle, _cache, callbackId);
  }

  /* package */ List<String> updateResults(ViewCycle cycle) {
    _latestCycle = cycle;
    List<String> updatedIds = Lists.newArrayList();
    for (DependencyGraphViewport viewport : _viewports.values()) {
      CollectionUtils.addIgnoreNull(updatedIds, viewport.updateResults(cycle, _cache));
    }
    return updatedIds;
  }

  /* package */ long updateViewport(int viewportId, ViewportSpecification viewportSpec, ViewCycle cycle, ResultsCache cache) {
    return getViewport(viewportId).update(viewportSpec, cycle, cache);
  }
}
