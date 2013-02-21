/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.PortfolioMapperFunction;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.id.ObjectId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.blotter.BlotterColumnMapper;

/**
 * Default implementation of {@link AnalyticsView}. This class isn't meant to be thread safe. A thread calling any
 * method that mutates the state must have an exclusive lock. The get methods can safely be called by multiple
 * concurrent threads.
 * @see LockingAnalyticsView
 * @see com.opengamma.web.analytics Package concurrency notes
 */
/* package */ class SimpleAnalyticsView implements AnalyticsView {

  private static final Logger s_logger = LoggerFactory.getLogger(SimpleAnalyticsView.class);

  private final ResultsCache _cache = new ResultsCache();
  private final ComputationTargetResolver _targetResolver;
  private final String _viewId;
  private final ViewportListener _viewportListener;

  private PortfolioAnalyticsGrid _portfolioGrid;
  private MainAnalyticsGrid _primitivesGrid;
  private CompiledViewDefinition _compiledViewDefinition;
  /** IDs of positions, trades and securities in the portfolio, used for filtering change events. */
  private Set<ObjectId> _entityIds = Sets.newHashSet();

  /**
   * @param viewId ID of the view
   * @param portoflioCallbackId ID that is passed to the listener when the structure of the portfolio grid changes.
   * This class makes no assumptions about its value
   * @param primitivesCallbackId ID that is passed to the listener when the structure of the primitives grid changes.
   * This class makes no assumptions about its value
   * @param targetResolver For looking up calculation targets by specification
   * @param viewportListener Notified when any viewport is created, updated or deleted
   * @param blotterColumnMapper For populating the blotter columns with details for each different security type
   * @param showBlotterColumns Whether the blotter columns should be shown in the portfolio analytics grid
   */
  /* package */ SimpleAnalyticsView(Portfolio portfolio,
                                    String viewId,
                                    String portoflioCallbackId,
                                    String primitivesCallbackId,
                                    ComputationTargetResolver targetResolver,
                                    ViewportListener viewportListener,
                                    BlotterColumnMapper blotterColumnMapper,
                                    boolean showBlotterColumns) {
    ArgumentChecker.notEmpty(viewId, "viewId");
    ArgumentChecker.notEmpty(portoflioCallbackId, "portoflioGridId");
    ArgumentChecker.notEmpty(primitivesCallbackId, "primitivesGridId");
    ArgumentChecker.notNull(targetResolver, "targetResolver");
    ArgumentChecker.notNull(viewportListener, "viewportListener");
    ArgumentChecker.notNull(blotterColumnMapper, "blotterColumnMappings");
    _viewId = viewId;
    _targetResolver = targetResolver;
    // TODO need a Supplier<Portfolio> that gets the latest/correct version from the master
    // TODO but when an update comes need to look at version/correction to see whether it affects us
    // should this class just know about the masters? or should that logic be wrapped?
    // need to know the version/correction time of the portfolio as well as the object ID
    // but should you be allowed to edit historical versions of the portfolio anyway?
    if (showBlotterColumns) {
      _portfolioGrid = PortfolioAnalyticsGrid.forBlotter(portoflioCallbackId,
                                                         portfolio,
                                                         targetResolver,
                                                         viewportListener,
                                                         blotterColumnMapper);
    } else {
      _portfolioGrid = PortfolioAnalyticsGrid.forAnalytics(portoflioCallbackId,
                                                           portfolio,
                                                           targetResolver,
                                                           viewportListener);
    }
    _primitivesGrid = PrimitivesAnalyticsGrid.empty(primitivesCallbackId);
    _viewportListener = viewportListener;
  }

  @Override
  public List<String> updateColumns(CompiledViewDefinition compiledViewDefinition) {
    _compiledViewDefinition = compiledViewDefinition;
    // TODO this loses all dependency graphs. new grid needs to rebuild graphs from old grid. need stable row and col IDs to do that
    /*PortfolioIdExtractor idExtractor = new PortfolioIdExtractor();
    List<Set<ObjectId>> ids = PortfolioMapper.map(portfolio.getRootNode(), idExtractor);
    Iterable<ObjectId> flattenedIds = Iterables.concat(ids);
    _entityIds = Sets.newHashSet(flattenedIds);*/
    ValueMappings valueMappings = new ValueMappings(_compiledViewDefinition);
    _portfolioGrid = _portfolioGrid.withUpdatedColumns(_compiledViewDefinition);
    _primitivesGrid = new PrimitivesAnalyticsGrid(_compiledViewDefinition,
                                                  _primitivesGrid.getCallbackId(),
                                                  _targetResolver,
                                                  valueMappings,
                                                  _viewportListener);
    List<String> gridIds = Lists.newArrayList();
    gridIds.add(_portfolioGrid.getCallbackId());
    gridIds.add(_primitivesGrid.getCallbackId());
    gridIds.addAll(_portfolioGrid.getDependencyGraphCallbackIds());
    gridIds.addAll(_primitivesGrid.getDependencyGraphCallbackIds());
    return gridIds;
  }

  @Override
  public List<String> updateResults(ViewResultModel results, ViewCycle viewCycle) {
    _cache.put(results);
    List<String> updatedIds = Lists.newArrayList();
    updatedIds.addAll(_portfolioGrid.updateResults(_cache, viewCycle));
    updatedIds.addAll(_primitivesGrid.updateResults(_cache, viewCycle));
    return updatedIds;
  }

  private MainAnalyticsGrid getGrid(GridType gridType) {
    switch (gridType) {
      case PORTFORLIO:
        return _portfolioGrid;
      case PRIMITIVES:
        return _primitivesGrid;
      default:
        throw new IllegalArgumentException("Unexpected grid type " + gridType);
    }
  }

  @Override
  public GridStructure getGridStructure(GridType gridType) {
    GridStructure gridStructure = getGrid(gridType).getGridStructure();
    s_logger.debug("View {} returning grid structure for the {} grid: {}", new Object[]{_viewId, gridType, gridStructure});
    return gridStructure;
  }

  @Override
  public boolean createViewport(int requestId, GridType gridType, int viewportId, String callbackId, ViewportDefinition viewportDefinition) {
    boolean hasData = getGrid(gridType).createViewport(viewportId, callbackId, viewportDefinition);
    s_logger.debug("View {} created viewport ID {} for the {} grid from {}",
                   new Object[]{_viewId, viewportId, gridType, viewportDefinition});
    return hasData;
  }

  @Override
  public String updateViewport(GridType gridType, int viewportId, ViewportDefinition viewportDefinition) {
    s_logger.debug("View {} updating viewport {} for {} grid to {}",
                   new Object[]{_viewId, viewportId, gridType, viewportDefinition});
    return getGrid(gridType).updateViewport(viewportId, viewportDefinition);
  }

  @Override
  public void deleteViewport(GridType gridType, int viewportId) {
    s_logger.debug("View {} deleting viewport {} from the {} grid", new Object[]{_viewId, viewportId, gridType});
    getGrid(gridType).deleteViewport(viewportId);
  }

  @Override
  public ViewportResults getData(GridType gridType, int viewportId) {
    s_logger.debug("View {} getting data for viewport {} of the {} grid", new Object[]{_viewId, viewportId, gridType});
    return getGrid(gridType).getData(viewportId);
  }

  @Override
  public void openDependencyGraph(int requestId, GridType gridType, int graphId, String callbackId, int row, int col) {
    s_logger.debug("View {} opening dependency graph {} for cell ({}, {}) of the {} grid",
                   new Object[]{_viewId, graphId, row, col, gridType});
    getGrid(gridType).openDependencyGraph(graphId, callbackId, row, col, _compiledViewDefinition, _viewportListener);
  }

  @Override
  public void closeDependencyGraph(GridType gridType, int graphId) {
    s_logger.debug("View {} closing dependency graph {} of the {} grid", new Object[]{_viewId, graphId, gridType});
    getGrid(gridType).closeDependencyGraph(graphId);
  }

  @Override
  public GridStructure getGridStructure(GridType gridType, int graphId) {
    DependencyGraphGridStructure gridStructure = getGrid(gridType).getGridStructure(graphId);
    s_logger.debug("View {} returning grid structure for dependency graph {} of the {} grid: {}",
                   new Object[]{_viewId, graphId, gridType, gridStructure});
    return gridStructure;
  }

  @Override
  public boolean createViewport(int requestId, GridType gridType, int graphId, int viewportId, String callbackId, ViewportDefinition viewportDefinition) {
    boolean hasData = getGrid(gridType).createViewport(graphId, viewportId, callbackId, viewportDefinition);
    s_logger.debug("View {} created viewport ID {} for dependency graph {} of the {} grid using {}",
                   new Object[]{_viewId, viewportId, graphId, gridType, viewportDefinition});
    return hasData;
  }

  @Override
  public String updateViewport(GridType gridType, int graphId, int viewportId, ViewportDefinition viewportDefinition) {
    s_logger.debug("View {} updating viewport for dependency graph {} of the {} grid using {}",
                   new Object[]{_viewId, graphId, gridType, viewportDefinition});
    return getGrid(gridType).updateViewport(graphId, viewportId, viewportDefinition);
  }

  @Override
  public void deleteViewport(GridType gridType, int graphId, int viewportId) {
    s_logger.debug("View {} deleting viewport {} from dependency graph {} of the {} grid",
                   new Object[]{_viewId, viewportId, graphId, gridType});
    getGrid(gridType).deleteViewport(graphId, viewportId);
  }

  @Override
  public ViewportResults getData(GridType gridType, int graphId, int viewportId) {
    s_logger.debug("View {} getting data for viewport {} of dependency graph {} of the {} grid",
                   new Object[]{_viewId, viewportId, graphId, gridType});
    return getGrid(gridType).getData(graphId, viewportId);
  }

  /*@Override
  public List<String> entityChanged(ChangeEvent event) {
    // TODO what if something is added and the view doesn't recompile? need to reload the portfolio manually
    // maybe need a Supplier<Portfolio> as well as ChangeNotifications for looking up modified objects
    // but if the view recompiles we're in danger of rebuilding the whole grid twice
    if (_entityIds.contains(event.getObjectId())) {
      // TODO this is the wrong thing to do
      // need map<objectId, object> that is always used for populating details
      // this method can just look up the object that changes, update the map and redraw
      return updateStructure();
    } else {
      return Collections.emptyList();
    }
  }*/
}

// TODO does this need to extract mappings from ID to target?
/**
 * Function for traversing a portfolio and extracting the IDs of all positions, trades and securities.
 * TODO doesn't fully work for swaptions (or any other security that has an OTC underlying and links via ExternalId)
 */
/* package */ class PortfolioIdExtractor implements PortfolioMapperFunction<Set<ObjectId>> {

  @Override
  public Set<ObjectId> apply(PortfolioNode node) {
    return Collections.emptySet();
  }

  @Override
  public Set<ObjectId> apply(PortfolioNode parent, Position position) {
    Set<ObjectId> ids = Sets.newHashSet();
    ids.add(position.getUniqueId().getObjectId());
    for (Trade trade : position.getTrades()) {
      ids.add(trade.getUniqueId().getObjectId());
    }
    // portfolios are fully resolved
    // TODO this won't work for swaptions, need to get the underlying ID
    ids.add(position.getSecurityLink().getTarget().getUniqueId().getObjectId());
    return ids;
  }
}

// TODO different methods / maps for positions, trades and securities? what about quantities? do that here?
/* package */ class EntityLookup {

  private final Map<ObjectId, Object> _entities = Maps.newHashMap();

  /* package */ Object get(ObjectId id) {
    return _entities.get(id);
  }

  /* package */ void set(ObjectId id, Object object) {
    _entities.put(id, object);
  }
}
