/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.config.FunctionRepositoryFactory;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.financial.security.lookup.SecurityAttributeMapper;
import com.opengamma.id.ObjectId;

/**
 * A grid for displaying portfolio analytics data.
 */
/* package */class PortfolioAnalyticsGrid extends MainAnalyticsGrid<PortfolioGridViewport> {

  private final PortfolioGridStructure _gridStructure;

  /* package */PortfolioAnalyticsGrid(PortfolioGridStructure gridStructure, String gridId, ComputationTargetResolver targetResolver, FunctionRepositoryFactory functions,
      ViewportListener viewportListener) {
    super(AnalyticsView.GridType.PORTFOLIO, gridId, targetResolver, functions, viewportListener);
    _gridStructure = gridStructure;
  }

  /**
   * Creates a new grid when the structure is updated using an existing grid as the basis
   * 
   * @param gridStructure The updated grid structure
   * @param previousGrid The old grid whose state is the basis for the new grid
   */
  /* package */PortfolioAnalyticsGrid(PortfolioGridStructure gridStructure, PortfolioAnalyticsGrid previousGrid, CompiledViewDefinition compiledViewDef) {
    super(AnalyticsView.GridType.PORTFOLIO, previousGrid, compiledViewDef, gridStructure.getValueMappings());
    _gridStructure = gridStructure;
  }

  /* package */PortfolioAnalyticsGrid withUpdatedRows(Portfolio portfolio) {
    PortfolioGridStructure updatedStructure = _gridStructure.withUpdatedRows(portfolio);
    return new PortfolioAnalyticsGrid(updatedStructure, getCallbackId(), getTargetResolver(), getFunctionRepository(), getViewportListener());
  }

  /**
   * Updates with changed structure when the view recompiles
   * 
   * @param portfolio the portfolio of positions
   * @param compiledViewDef the basic state required for computation of a view
   * @return new PortfolioAnalyticsGrid
   */
  /* package */PortfolioAnalyticsGrid withUpdatedStructure(CompiledViewDefinition compiledViewDef, Portfolio portfolio) {
    PortfolioGridStructure updatedStructure = _gridStructure.withUpdatedStructure(compiledViewDef, portfolio);
    for (PortfolioGridViewport viewport : getViewports().values()) {
      viewport.updateResultsAndStructure(updatedStructure);
    }
    return new PortfolioAnalyticsGrid(updatedStructure, this, compiledViewDef);
  }

  /**
   * Updates on for each tick, returns this if structure remains the same
   * 
   * @param cache the result cache
   * @return PortfolioAnalyticsGrid
   */
  /* package */PortfolioAnalyticsGrid withUpdatedTickAndPossiblyStructure(ResultsCache cache) {
    PortfolioGridStructure updatedStructure = _gridStructure.withUpdatedStructure(cache);
    // TODO this smells bad but avoids throwing away any viewports, depgraphs etc
    // TODO implement equals()?
    if (updatedStructure == _gridStructure) {
      return this;
    } else {
      return new PortfolioAnalyticsGrid(updatedStructure, this, getViewCycle().getCompiledViewDefinition());
    }
  }

  /* package */static PortfolioAnalyticsGrid forAnalytics(String gridId, Portfolio portfolio, ComputationTargetResolver targetResolver, FunctionRepositoryFactory functions,
      ViewportListener viewportListener) {
    PortfolioGridStructure gridStructure = PortfolioGridStructure.create(portfolio, new UnversionedValueMappings());
    return new PortfolioAnalyticsGrid(gridStructure, gridId, targetResolver, functions, viewportListener);
  }

  /* package */static PortfolioAnalyticsGrid forBlotter(String gridId, Portfolio portfolio, ComputationTargetResolver targetResolver, FunctionRepositoryFactory functions,
      ViewportListener viewportListener, SecurityAttributeMapper blotterColumnMapper) {
    PortfolioGridStructure gridStructure = BlotterGridStructure.create(portfolio, blotterColumnMapper);
    return new PortfolioAnalyticsGrid(gridStructure, gridId, targetResolver, functions, viewportListener);
  }

  /* package */List<String> updateEntities(ResultsCache cache, List<ObjectId> entityIds) {
    List<String> ids = Lists.newArrayList();
    for (MainGridViewport viewport : getViewports().values()) {
      viewport.updateResults(cache);
      ids.add(viewport.getCallbackId());
    }
    return ids;
  }

  @Override
  PortfolioGridViewport createViewport(ViewportDefinition viewportDefinition, String callbackId, String structureCallbackId, ResultsCache cache) {
    return new PortfolioGridViewport(_gridStructure, callbackId, structureCallbackId, viewportDefinition, getViewCycle(), cache);
  }

  @Override
  MainGridStructure getGridStructure() {
    return _gridStructure;
  }
}
