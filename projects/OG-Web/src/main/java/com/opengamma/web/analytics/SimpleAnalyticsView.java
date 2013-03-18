/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeType;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.PortfolioMapper;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.position.ManageablePosition;
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
  private final VersionCorrection _versionCorrection;
  private final Supplier<Portfolio> _portfolioSupplier;
  private final PortfolioEntityExtractor _portfolioEntityExtractor;

  private PortfolioAnalyticsGrid _portfolioGrid;
  private MainAnalyticsGrid _primitivesGrid;
  private CompiledViewDefinition _compiledViewDefinition;

  /**
   * @param viewId ID of the view
   * @param portoflioCallbackId ID that is passed to the listener when the structure of the portfolio grid changes.
   * This class makes no assumptions about its value
   * @param primitivesCallbackId ID that is passed to the listener when the structure of the primitives grid changes.
 * This class makes no assumptions about its value
   * @param targetResolver For looking up calculation targets by specification
   * @param viewportListener Notified when any viewport is created, updated or deleted
   * @param blotterColumnMapper For populating the blotter columns with details for each different security type
   * @param portfolioSupplier Supplies an up to date version of the portfolio
   * @param showBlotterColumns Whether the blotter columns should be shown in the portfolio analytics grid
   */
  /* package */ SimpleAnalyticsView(Portfolio portfolio,
                                    VersionCorrection versionCorrection,
                                    String viewId,
                                    String portoflioCallbackId,
                                    String primitivesCallbackId,
                                    ComputationTargetResolver targetResolver,
                                    ViewportListener viewportListener,
                                    BlotterColumnMapper blotterColumnMapper,
                                    Supplier<Portfolio> portfolioSupplier,
                                    PortfolioEntityExtractor portfolioEntityExtractor,
                                    boolean showBlotterColumns) {
    ArgumentChecker.notEmpty(viewId, "viewId");
    ArgumentChecker.notEmpty(portoflioCallbackId, "portoflioGridId");
    ArgumentChecker.notEmpty(primitivesCallbackId, "primitivesGridId");
    ArgumentChecker.notNull(targetResolver, "targetResolver");
    ArgumentChecker.notNull(viewportListener, "viewportListener");
    ArgumentChecker.notNull(blotterColumnMapper, "blotterColumnMappings");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(portfolioSupplier, "portfolioSupplier");
    ArgumentChecker.notNull(portfolioEntityExtractor, "portfolioEntityExtractor");
    _versionCorrection = versionCorrection;
    _viewId = viewId;
    _targetResolver = targetResolver;
    _portfolioSupplier = portfolioSupplier;
    _portfolioEntityExtractor = portfolioEntityExtractor;
    List<UniqueIdentifiable> entities;
    if (portfolio != null) {
      entities = PortfolioMapper.flatMap(portfolio.getRootNode(), _portfolioEntityExtractor);
    } else {
      entities = Collections.emptyList();
    }
    _cache.put(entities);
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
  public List<String> updateStructure(CompiledViewDefinition compiledViewDefinition) {
    _compiledViewDefinition = compiledViewDefinition;
    // TODO this loses all dependency graphs. new grid needs to rebuild graphs from old grid. need stable IDs to do that
    _portfolioGrid = _portfolioGrid.withUpdatedStructure(_compiledViewDefinition);
    _primitivesGrid = new PrimitivesAnalyticsGrid(_compiledViewDefinition,
                                                  _primitivesGrid.getCallbackId(),
                                                  _targetResolver,
                                                  _viewportListener);
    return getGridIds();
  }

  private List<String> getGridIds() {
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
    boolean hasData = getGrid(gridType).createViewport(viewportId, callbackId, viewportDefinition, _cache);
    s_logger.debug("View {} created viewport ID {} for the {} grid from {}",
                   new Object[]{_viewId, viewportId, gridType, viewportDefinition});
    return hasData;
  }

  @Override
  public String updateViewport(GridType gridType, int viewportId, ViewportDefinition viewportDefinition) {
    s_logger.debug("View {} updating viewport {} for {} grid to {}",
                   new Object[]{_viewId, viewportId, gridType, viewportDefinition});
    return getGrid(gridType).updateViewport(viewportId, viewportDefinition, _cache);
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
    boolean hasData = getGrid(gridType).createViewport(graphId, viewportId, callbackId, viewportDefinition, _cache);
    s_logger.debug("View {} created viewport ID {} for dependency graph {} of the {} grid using {}",
                   new Object[]{_viewId, viewportId, graphId, gridType, viewportDefinition});
    return hasData;
  }

  @Override
  public String updateViewport(GridType gridType, int graphId, int viewportId, ViewportDefinition viewportDefinition) {
    s_logger.debug("View {} updating viewport for dependency graph {} of the {} grid using {}",
                   new Object[]{_viewId, graphId, gridType, viewportDefinition});
    return getGrid(gridType).updateViewport(graphId, viewportId, viewportDefinition, _cache);
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

  @Override
  public List<String> portfolioChanged() {
    Portfolio portfolio = _portfolioSupplier.get();
    List<UniqueIdentifiable> entities = PortfolioMapper.flatMap(portfolio.getRootNode(), _portfolioEntityExtractor);
    _cache.put(entities);
    _portfolioGrid = _portfolioGrid.withUpdatedRows(portfolio);
    // TODO this is pretty conservative, refreshes all grids because the portfolio structure has changed
    return getGridIds();
  }

  @Override
  public List<String> entityChanged(MasterChangeNotification<?> notification) {
    ChangeEvent event = notification.getEvent();
    if (isChangeRelevant(event)) {
      if (event.getType() == ChangeType.REMOVED) {
        // TODO clean up trades from cache if this is a position that has been removed
        _cache.remove(notification.getEntity().getUniqueId().getObjectId());
        _portfolioGrid = _portfolioGrid.withUpdatedRows(_portfolioSupplier.get());
        // return the IDs of all grids because the portfolio structure has changed
        // TODO if we had separate IDs for rows and columns it would save the client rebuilding the column metadata
        return getGridIds();
      } else {
        UniqueIdentifiable entity = notification.getEntity();
        _cache.put(entity);
        List<ObjectId> entityIds = Lists.newArrayList(entity.getUniqueId().getObjectId());
        // TODO get rid of this duplication when ManageablePosition implements Position
        // TODO would it be nicer to have a getEntities() method on MasterChangeNotification?
        // would need different impls for different entity types. probably not worth it
        if (entity instanceof Position) {
          for (Trade trade : ((Position) entity).getTrades()) {
            entityIds.add(trade.getUniqueId().getObjectId());
            _cache.put(trade);
          }
        } else if (entity instanceof ManageablePosition) {
          for (Trade trade : ((ManageablePosition) entity).getTrades()) {
            entityIds.add(trade.getUniqueId().getObjectId());
            _cache.put(trade);
          }
        }
        List<String> ids = _portfolioGrid.updateEntities(_cache, entityIds);
        s_logger.debug("Entity changed {}, firing updates for viewports {}", notification.getEntity().getUniqueId(), ids);
        return ids;
      }
    } else {
      return Collections.emptyList();
    }
  }

  /**
   * Returns true if a change event invalidates any of this view's portfolio, including trades, securities and positions
   * it refers to.
   * @param event The event
   * @return true if the portfolio or positions, trades or securities it refers to have changed
   */
  private boolean isChangeRelevant(ChangeEvent event) {
    // if the correctedTo time is non-null then we're looking at corrections up to a fixed point in the past and
    // new corrections can't affect our version
    if (_versionCorrection.getCorrectedTo() != null) {
      return false;
    }
    // there's no way we can know about an object if it's just been added. and if the portfolio is modified we will
    // cache any newly added positions etc when traversing the new portfolio structure
    if (event.getType() == ChangeType.ADDED) {
      return false;
    }
    if (_cache.getEntity(event.getObjectId()) == null) {
      return false;
    }
    Instant versionInstant = _versionCorrection.getVersionAsOf();
    Instant eventFrom = event.getVersionFrom();
    Instant eventTo = event.getVersionTo();
    if (versionInstant == null) {
      // if the version time is null (latest) and eventTo is null (latest) then handle the change
      // if the version time is null (latest) and eventTo isn't null the event doesn't affect the latest version
      return eventTo == null;
    }
    // check whether the range of the changed version contains our version instance
    if (eventFrom.isAfter(versionInstant)) {
      return false;
    }
    if (eventTo != null && eventTo.isBefore(versionInstant)) {
      return false;
    }
    return true;
  }
}

