/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.financial.security.lookup.SecurityAttributeMapper;
import com.opengamma.id.ObjectId;

/**
 * A grid for displaying portfolio analytics data.
 */
/* package */ class PortfolioAnalyticsGrid extends MainAnalyticsGrid {

  private final PortfolioGridStructure _gridStructure;

  /* package */ PortfolioAnalyticsGrid(PortfolioGridStructure gridStructure,
                                       String gridId,
                                       ComputationTargetResolver targetResolver,
                                       ViewportListener viewportListener) {
    super(AnalyticsView.GridType.PORTFORLIO, gridStructure, gridId, targetResolver, viewportListener);
    _gridStructure = gridStructure;
  }

  /* package */ PortfolioAnalyticsGrid(PortfolioGridStructure gridStructure,
                                       String gridId,
                                       ComputationTargetResolver targetResolver,
                                       ViewportListener viewportListener,
                                       Map<Integer, MainGridViewport> viewports) {
    super(AnalyticsView.GridType.PORTFORLIO, gridStructure, gridId, targetResolver, viewportListener, viewports);
    _gridStructure = gridStructure;
  }

  /* package */ PortfolioAnalyticsGrid withUpdatedRows(Portfolio portfolio) {
    PortfolioGridStructure updatedStructure = _gridStructure.withUpdatedRows(portfolio);
    return new PortfolioAnalyticsGrid(updatedStructure, getCallbackId(), getTargetResolver(), getViewportListener());
  }

  /* package */ PortfolioAnalyticsGrid withUpdatedStructure(CompiledViewDefinition compiledViewDef, Portfolio portfolio) {
    PortfolioGridStructure updatedStructure = _gridStructure.withUpdatedStructure(compiledViewDef, portfolio);
    return new PortfolioAnalyticsGrid(updatedStructure,
                                      getCallbackId(),
                                      getTargetResolver(),
                                      getViewportListener(),
                                      getViewports());
  }

  /* package */ PortfolioAnalyticsGrid withUpdatedStructure(ResultsCache cache) {
    PortfolioGridStructure updatedStructure = _gridStructure.withUpdatedStructure(cache);
    // TODO this smells bad but avoids throwing away any viewports, depgraphs etc
    // TODO implement equals()?
    if (updatedStructure == _gridStructure) {
      return this;
    } else {
      return new PortfolioAnalyticsGrid(updatedStructure, getCallbackId(), getTargetResolver(), getViewportListener());
    }
  }

  /* package */ static PortfolioAnalyticsGrid forAnalytics(String gridId,
                                                           Portfolio portfolio,
                                                           ComputationTargetResolver targetResolver,
                                                           ViewportListener viewportListener) {
    PortfolioGridStructure gridStructure = PortfolioGridStructure.create(portfolio, new ValueMappings());
    return new PortfolioAnalyticsGrid(gridStructure, gridId, targetResolver, viewportListener);
  }

  /* package */ static PortfolioAnalyticsGrid forBlotter(String gridId,
                                                         Portfolio portfolio,
                                                         ComputationTargetResolver targetResolver,
                                                         ViewportListener viewportListener,
                                                         SecurityAttributeMapper blotterColumnMapper) {
    PortfolioGridStructure gridStructure = BlotterGridStructure.create(portfolio, blotterColumnMapper);
    return new PortfolioAnalyticsGrid(gridStructure, gridId, targetResolver, viewportListener);
  }

  /* package */ List<String> updateEntities(ResultsCache cache, List<ObjectId> entityIds) {
    List<String> ids = Lists.newArrayList();
    for (MainGridViewport viewport : getViewports().values()) {
      viewport.updateResults(cache);
      ids.add(viewport.getCallbackId());
    }
    return ids;
  }
}
