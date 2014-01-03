/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import org.threeten.bp.Instant;

import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.config.FunctionRepositoryFactory;
import com.opengamma.engine.management.ValueMappings;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.util.ArgumentChecker;

/**
 * Grid for displaying the dependency graph for a cell. This graph contains all the calculation steps used in deriving the cell's value. The first row of the grid shows the cell's value and subsequent
 * rows show a tree structure containing the dependency graph.
 */
public final class DependencyGraphGrid extends AnalyticsGrid<DependencyGraphViewport> {

  /** The config name. */
  private final String _calcConfigName;
  /** The grid structure. */
  private final ValueRequirement _targetRequirement;
  /** The root vale specification. */
  private final DependencyGraphGridStructure _gridStructure;
  /**
   * Each dependency graph maintains its own cache of results. The values in a dependency graph aren't necessarily view output values (apart from the root) and therefore aren't included in the main
   * results model and main results cache.
   */
  private ViewCycle _latestCycle;

  //-------------------------------------------------------------------------
  /**
   * Creates a new grid for displaying a dependency graph of calculations.
   * 
   * @param compiledViewDef the view definition from which the graph and calculations were derived
   * @param valueRequirement target value requirement
   * @param calcConfigName the calculation configuration used for the calculations, not null
   * @param cycle the view cycle that calculated the results, not null
   * @param callbackId the ID that's passed to listeners when the row and column structure of the grid changes
   * @param targetResolver the resolver for looking up the target of the calculation given its specification
   * @param functions the function repository for any additional function metadata, not null
   * @param viewportListener receives notifications when any viewport changes, not null
   * @return the grid, not null
   */
  /* package */static DependencyGraphGrid create(CompiledViewDefinition compiledViewDef, ValueRequirement valueRequirement, String calcConfigName, ViewCycle cycle, String callbackId,
      ComputationTargetResolver targetResolver, FunctionRepositoryFactory functions, ViewportListener viewportListener, ValueMappings valueMappings) {
    DependencyGraphStructureBuilder builder = new DependencyGraphStructureBuilder(compiledViewDef, valueRequirement, calcConfigName, targetResolver, functions.constructRepository(Instant
        .now()), cycle, valueMappings);
    return new DependencyGraphGrid(builder.getStructure(), calcConfigName, callbackId, cycle, viewportListener, valueRequirement);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   */
  private DependencyGraphGrid(DependencyGraphGridStructure gridStructure, String calcConfigName, String callbackId, ViewCycle cycle, ViewportListener viewportListener,
      ValueRequirement target) {
    super(viewportListener, callbackId);
    ArgumentChecker.notNull(gridStructure, "gridStructure");
    ArgumentChecker.notNull(calcConfigName, "calcConfigName");
    ArgumentChecker.notNull(callbackId, "callbackId");
    ArgumentChecker.notNull(cycle, "cycle");
    ArgumentChecker.notNull(target, "target");
    _gridStructure = gridStructure;
    _calcConfigName = calcConfigName;
    _latestCycle = cycle;
    _targetRequirement = target;
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

  public ValueRequirement getTargetValueRequirement() {
    return _targetRequirement;
  }

  @Override
  protected DependencyGraphViewport createViewport(ViewportDefinition viewportDefinition, String callbackId, String structureCallbackId, ResultsCache cache) {
    return new DependencyGraphViewport(_calcConfigName, _gridStructure, callbackId, structureCallbackId, viewportDefinition, _latestCycle, cache);
  }

  /* package */List<String> updateResults(ViewCycle cycle, ResultsCache cache) {
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
