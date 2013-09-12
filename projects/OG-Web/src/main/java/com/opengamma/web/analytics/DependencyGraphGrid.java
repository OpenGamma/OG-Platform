/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.util.ArgumentChecker;

/**
 * Grid for displaying the dependency graph for a cell.
 * This graph contains all the calculation steps used in deriving the cell's value.
 * The first row of the grid shows the cell's value and subsequent rows show a
 * tree structure containing the dependency graph.
 */
public final class DependencyGraphGrid extends AnalyticsGrid<DependencyGraphViewport> {

  /** The config name. */
  private final String _calcConfigName;
  /** The grid structure. */
  private final DependencyGraphGridStructure _gridStructure;
  /**
   * Each dependency graph maintains its own cache of results.
   * The values in a dependency graph aren't necessarily view output values (apart from the root)
   * and therefore aren't included in the main results model and main results cache.
   */
  private ViewCycle _latestCycle;

  //-------------------------------------------------------------------------
  /**
   * Creates a new grid for displaying a dependency graph of calculations.
   *
   *
   * @param compiledViewDef  the view definition from which the graph and calculations were derived
   * @param requirement  requirement that requested the target
   * @param target  the object whose dependency graph is being displayed
   * @param calcConfigName  the calculation configuration used for the calculations, not null
   * @param cycle  the view cycle that calculated the results, not null
   * @param callbackId  the ID that's passed to listeners when the row and column structure of the grid changes
   * @param targetResolver  the resolver for looking up the target of the calculation given its specification
   * @param viewportListener  receives notifications when any viewport changes, not null       @return the grid, not null
   */
  /* package */ static DependencyGraphGrid create(CompiledViewDefinition compiledViewDef,
                                                  ValueRequirement requirement,
                                                  ValueSpecification target,
                                                  String calcConfigName,
                                                  ViewCycle cycle,
                                                  String callbackId,
                                                  ComputationTargetResolver targetResolver,
                                                  ViewportListener viewportListener) {
    DependencyGraphStructureBuilder builder =
        new DependencyGraphStructureBuilder(compiledViewDef, requirement, target, calcConfigName, targetResolver, cycle);
    return new DependencyGraphGrid(builder.getStructure(), calcConfigName, callbackId, cycle, viewportListener);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   */
  private DependencyGraphGrid(DependencyGraphGridStructure gridStructure,
                              String calcConfigName,
                              String callbackId,
                              ViewCycle cycle,
                              ViewportListener viewportListener) {
    super(viewportListener, callbackId);
    ArgumentChecker.notNull(gridStructure, "gridStructure");
    ArgumentChecker.notNull(calcConfigName, "calcConfigName");
    ArgumentChecker.notNull(callbackId, "callbackId");
    ArgumentChecker.notNull(cycle, "cycle");
    _gridStructure = gridStructure;
    _calcConfigName = calcConfigName;
    _latestCycle = cycle;
  }

  //-------------------------------------------------------------------------
  @Override
  public DependencyGraphGridStructure getGridStructure() {
    return _gridStructure;
  }

  @Override
  protected ViewCycle getViewCycle() {
    return _latestCycle;
  }

  @Override
  protected DependencyGraphViewport createViewport(ViewportDefinition viewportDefinition,
                                                   String callbackId,
                                                   String structureCallbackId,
                                                   ResultsCache cache) {
    return new DependencyGraphViewport(_calcConfigName, _gridStructure, callbackId, structureCallbackId,
                                       viewportDefinition, _latestCycle, cache);
  }

  /* package */ List<String> updateResults(ViewCycle cycle, ResultsCache cache) {
    _latestCycle = cycle;
    List<String> updatedIds = Lists.newArrayList();
    for (DependencyGraphViewport viewport : getViewports().values()) {
      viewport.updateResults(cycle, cache);
      if (viewport.getState() == Viewport.State.FRESH_DATA) {
        updatedIds.add(viewport.getCallbackId());
      }
    }
    return updatedIds;
  }

}
