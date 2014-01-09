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
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.config.FunctionRepositoryFactory;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.financial.security.lookup.SecurityAttributeMapper;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.web.analytics.formatting.TypeFormatter;

/**
 * Default implementation of {@link AnalyticsView}. This class isn't meant to be thread safe. A thread calling any method that mutates the state must have an exclusive lock. The get methods can safely
 * be called by multiple concurrent threads.
 * 
 * @see LockingAnalyticsView
 * @see com.opengamma.web.analytics Package concurrency notes
 */
/* package */class SimpleAnalyticsView implements AnalyticsView {

  private static final Logger s_logger = LoggerFactory.getLogger(SimpleAnalyticsView.class);
  private static final Portfolio EMPTY_PORTFOLIO = new SimplePortfolio("", new SimplePortfolioNode(UniqueId.of("EMPTY", "EMPTY"), ""));

  private final ResultsCache _cache = new ResultsCache();
  private final ComputationTargetResolver _targetResolver;
  private final FunctionRepositoryFactory _functions;
  private final String _viewId;
  private final ViewportListener _viewportListener;
  private final VersionCorrection _versionCorrection;
  private final Supplier<Portfolio> _portfolioSupplier;
  private final PortfolioEntityExtractor _portfolioEntityExtractor;
  private final UniqueId _viewDefinitionId;
  private final ErrorManager _errorManager;

  private PortfolioAnalyticsGrid _portfolioGrid;
  private MainAnalyticsGrid<?> _primitivesGrid;
  private CompiledViewDefinition _compiledViewDefinition;
  private CompiledViewDefinition _pendingStructureChange;
  private Portfolio _pendingPortfolio;

  /**
   * @param viewId client ID of the view
   * @param portfolioCallbackId ID that is passed to the listener when the structure of the portfolio grid changes. This class makes no assumptions about its value
   * @param primitivesCallbackId ID that is passed to the listener when the structure of the primitives grid changes. This class makes no assumptions about its value
   * @param targetResolver For looking up calculation targets by specification
   * @param viewportListener Notified when any viewport is created, updated or deleted
   * @param blotterColumnMapper For populating the blotter columns with details for each different security type
   * @param portfolioSupplier Supplies an up to date version of the portfolio
   * @param showBlotterColumns Whether the blotter columns should be shown in the portfolio analytics grid
   * @param errorManager Holds information about errors that occur compiling and executing the view
   */
  /* package */SimpleAnalyticsView(UniqueId viewDefinitionId, boolean primitivesOnly, VersionCorrection versionCorrection, String viewId, String portfolioCallbackId,
      String primitivesCallbackId, ComputationTargetResolver targetResolver, FunctionRepositoryFactory functions, ViewportListener viewportListener,
      SecurityAttributeMapper blotterColumnMapper, Supplier<Portfolio> portfolioSupplier, PortfolioEntityExtractor portfolioEntityExtractor, boolean showBlotterColumns,
      ErrorManager errorManager) {
    ArgumentChecker.notNull(viewDefinitionId, "viewDefinitionId");
    ArgumentChecker.notEmpty(viewId, "viewId");
    ArgumentChecker.notEmpty(portfolioCallbackId, "portfolioCallbackId");
    ArgumentChecker.notEmpty(primitivesCallbackId, "primitivesGridId");
    ArgumentChecker.notNull(targetResolver, "targetResolver");
    ArgumentChecker.notNull(functions, "functions");
    ArgumentChecker.notNull(viewportListener, "viewportListener");
    ArgumentChecker.notNull(blotterColumnMapper, "blotterColumnMappings");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(portfolioSupplier, "portfolioSupplier");
    ArgumentChecker.notNull(portfolioEntityExtractor, "portfolioEntityExtractor");
    ArgumentChecker.notNull(errorManager, "errorManager");
    _errorManager = errorManager;
    _viewDefinitionId = viewDefinitionId;
    _versionCorrection = versionCorrection;
    _viewId = viewId;
    _targetResolver = targetResolver;
    _functions = functions;
    _portfolioSupplier = portfolioSupplier;
    _portfolioEntityExtractor = portfolioEntityExtractor;
    Portfolio portfolio;
    if (primitivesOnly) {
      portfolio = null;
    } else {
      portfolio = EMPTY_PORTFOLIO;
    }
    if (showBlotterColumns) {
      _portfolioGrid = PortfolioAnalyticsGrid.forBlotter(portfolioCallbackId, portfolio, targetResolver, functions, viewportListener, blotterColumnMapper);
    } else {
      _portfolioGrid = PortfolioAnalyticsGrid.forAnalytics(portfolioCallbackId, portfolio, targetResolver, functions, viewportListener);
    }
    _primitivesGrid = PrimitivesAnalyticsGrid.empty(primitivesCallbackId);
    _viewportListener = viewportListener;
  }

  @Override
  public List<String> updateStructure(CompiledViewDefinition compiledViewDefinition, Portfolio portfolio) {
    if (_compiledViewDefinition != null) {
      // If we are between cycles then hold back the structure change until there's a set of results which use it,
      // allowing the user to continue browsing the current set of results in the meantime.
      _pendingStructureChange = compiledViewDefinition;
      _pendingPortfolio = portfolio;
      return Collections.emptyList();
    }
    doUpdateStructure(compiledViewDefinition, portfolio);
    return getGridIds();
  }

  @Override
  public String viewCompilationFailed(Throwable t) {
    s_logger.warn("View compilation failed, adding error {}", t);
    return _errorManager.add(t);
  }

  private void doUpdateStructure(CompiledViewDefinition compiledViewDefinition, Portfolio portfolio) {
    _compiledViewDefinition = compiledViewDefinition;
    if (portfolio != null) {
      List<UniqueIdentifiable> entities = PortfolioMapper.flatMap(portfolio.getRootNode(), _portfolioEntityExtractor);
      _cache.put(entities);
    }
    _portfolioGrid = _portfolioGrid.withUpdatedStructure(_compiledViewDefinition, portfolio);
    _primitivesGrid = new PrimitivesAnalyticsGrid(_compiledViewDefinition, _primitivesGrid.getCallbackId(), _targetResolver, _functions, _viewportListener);
  }

  private List<String> getGridIds() {
    List<String> gridIds = Lists.newArrayList();
    //callback ids for grid viewports
    for (PortfolioGridViewport viewport : _portfolioGrid.getViewports().values()) {
      gridIds.add(viewport.getStructureCallbackId());
      gridIds.add(viewport.getCallbackId());
    }
    //callback ids for depgraph grid viewports
    for (DependencyGraphGrid grid : _portfolioGrid.getDependencyGraphs().values()) {
      for (DependencyGraphViewport viewport : grid.getViewports().values()) {
        gridIds.add(viewport.getStructureCallbackId());
        gridIds.add(viewport.getCallbackId());
      }
    }
    gridIds.add(_portfolioGrid.getCallbackId());
    gridIds.add(_primitivesGrid.getCallbackId());
    gridIds.addAll(_portfolioGrid.getDependencyGraphCallbackIds());
    gridIds.addAll(_primitivesGrid.getDependencyGraphCallbackIds());
    return gridIds;
  }

  @Override
  public List<String> updateResults(ViewResultModel results, ViewCycle viewCycle) {
    List<String> updatedIds = Lists.newArrayList();
    boolean structureUpdated;
    _cache.put(results);
    if (_pendingStructureChange != null) {
      doUpdateStructure(_pendingStructureChange, _pendingPortfolio);
      structureUpdated = true;
      _pendingStructureChange = null;
      updatedIds.addAll(getGridIds());
    } else {
      structureUpdated = false;
    }
    if (!structureUpdated) {
      // Individual cell updates
      PortfolioAnalyticsGrid updatedPortfolioGrid = _portfolioGrid.withUpdatedTickAndPossiblyStructure(_cache);
      if (updatedPortfolioGrid == _portfolioGrid) {
        // no change to the grid structure, notify the data has changed
        updatedIds.addAll(_portfolioGrid.updateResults(_cache, viewCycle));
      } else {
        _portfolioGrid = updatedPortfolioGrid;
        // grid structure has changed due to the results, notify the grids need to be rebuilt
        updatedIds.add(_portfolioGrid.getCallbackId());
        updatedIds.addAll(_portfolioGrid.getDependencyGraphCallbackIds());
      }
      updatedIds.addAll(_primitivesGrid.updateResults(_cache, viewCycle));
    }
    return updatedIds;
  }

  private MainAnalyticsGrid<?> getGrid(GridType gridType) {
    switch (gridType) {
      case PORTFOLIO:
        return _portfolioGrid;
      case PRIMITIVES:
        return _primitivesGrid;
      default:
        throw new IllegalArgumentException("Unexpected grid type " + gridType);
    }
  }

  @Override
  public GridStructure getGridStructure(GridType gridType, int viewportId) {
    GridStructure gridStructure = getGrid(gridType).getViewport(viewportId).getGridStructure();
    s_logger.debug("Viewport {} and view {} returning grid structure for the {} grid: {}", viewportId, _viewId, gridType, gridStructure);
    return gridStructure;
  }

  @Override
  public GridStructure getInitialGridStructure(GridType gridType) {
    GridStructure gridStructure = getGrid(gridType).getGridStructure();
    s_logger.debug("View {} returning grid structure for the {} grid: {}", _viewId, gridType, gridStructure);
    return gridStructure;
  }

  @Override
  public boolean createViewport(int requestId, GridType gridType, int viewportId, String callbackId, String structureCallbackId, ViewportDefinition viewportDefinition) {
    boolean hasData = getGrid(gridType).createViewport(viewportId, callbackId, structureCallbackId, viewportDefinition, _cache);
    s_logger.debug("View {} created viewport ID {} for the {} grid from {}", _viewId, viewportId, gridType, viewportDefinition);
    return hasData;
  }

  @Override
  public String updateViewport(GridType gridType, int viewportId, ViewportDefinition viewportDefinition) {
    s_logger.debug("View {} updating viewport {} for {} grid to {}", _viewId, viewportId, gridType, viewportDefinition);
    return getGrid(gridType).updateViewport(viewportId, viewportDefinition, _cache);
  }

  @Override
  public void deleteViewport(GridType gridType, int viewportId) {
    s_logger.debug("View {} deleting viewport {} from the {} grid", _viewId, viewportId, gridType);
    getGrid(gridType).deleteViewport(viewportId);
  }

  @Override
  public ViewportResults getData(GridType gridType, int viewportId) {
    s_logger.debug("View {} getting data for viewport {} of the {} grid", _viewId, viewportId, gridType);
    return getGrid(gridType).getData(viewportId);
  }

  @Override
  public void openDependencyGraph(int requestId, GridType gridType, int graphId, String callbackId, int row, int col) {
    s_logger.debug("View {} opening dependency graph {} for cell ({}, {}) of the {} grid", _viewId, graphId, row, col, gridType);
    getGrid(gridType).openDependencyGraph(graphId, callbackId, row, col, _compiledViewDefinition, _viewportListener);
  }

  @Override
  public void openDependencyGraph(int requestId, GridType gridType, int graphId, String callbackId, String calcConfigName, ValueRequirement valueRequirement) {
    getGrid(gridType).openDependencyGraph(graphId, callbackId, calcConfigName, valueRequirement, _compiledViewDefinition, _viewportListener);
  }

  @Override
  public void closeDependencyGraph(GridType gridType, int graphId) {
    s_logger.debug("View {} closing dependency graph {} of the {} grid", _viewId, graphId, gridType);
    getGrid(gridType).closeDependencyGraph(graphId);
  }

  @Override
  public GridStructure getGridStructure(GridType gridType, int graphId, int viewportId) {
    DependencyGraphGridStructure gridStructure = getGrid(gridType).getGridStructure(graphId, viewportId);
    s_logger.debug("Viewport {} and view {} returning grid structure for dependency graph {} of the {} grid: {}", viewportId, _viewId, graphId, gridType, gridStructure);
    return gridStructure;
  }

  @Override
  public GridStructure getInitialGridStructure(GridType gridType, int graphId) {
    DependencyGraphGridStructure gridStructure = getGrid(gridType).getGridStructure(graphId);
    s_logger.debug("View {} returning grid structure for dependency graph {} of the {} grid: {}", _viewId, graphId, gridType, gridStructure);
    return gridStructure;
  }

  @Override
  public boolean createViewport(int requestId, GridType gridType, int graphId, int viewportId, String callbackId, String structureCallbackId, ViewportDefinition viewportDefinition) {
    boolean hasData = getGrid(gridType).createViewport(graphId, viewportId, callbackId, structureCallbackId, viewportDefinition, _cache);
    s_logger.debug("View {} created viewport ID {} for dependency graph {} of the {} grid using {}", _viewId, viewportId, graphId, gridType, viewportDefinition);
    return hasData;
  }

  @Override
  public String updateViewport(GridType gridType, int graphId, int viewportId, ViewportDefinition viewportDefinition) {
    s_logger.debug("View {} updating viewport for dependency graph {} of the {} grid using {}", _viewId, graphId, gridType, viewportDefinition);
    return getGrid(gridType).updateViewport(graphId, viewportId, viewportDefinition, _cache);
  }

  @Override
  public void deleteViewport(GridType gridType, int graphId, int viewportId) {
    s_logger.debug("View {} deleting viewport {} from dependency graph {} of the {} grid", _viewId, viewportId, graphId, gridType);
    getGrid(gridType).deleteViewport(graphId, viewportId);
  }

  @Override
  public ViewportResults getData(GridType gridType, int graphId, int viewportId) {
    s_logger.debug("View {} getting data for viewport {} of dependency graph {} of the {} grid", _viewId, viewportId, graphId, gridType);
    return getGrid(gridType).getData(graphId, viewportId);
  }

  @Override
  public List<String> portfolioChanged() {
    Portfolio portfolio = _portfolioSupplier.get();
    List<UniqueIdentifiable> entities = PortfolioMapper.flatMap(portfolio.getRootNode(), _portfolioEntityExtractor);
    _cache.put(entities);
    // TODO ignore for now, causes problems when the view take a long time to recompile
    /*_portfolioGrid = _portfolioGrid.withUpdatedRows(portfolio);
    // TODO this is pretty conservative, refreshes all grids because the portfolio structure has changed
    return getGridIds();*/
    return Collections.emptyList();
  }

  @Override
  public List<String> entityChanged(MasterChangeNotification<?> notification) {
    ChangeEvent event = notification.getEvent();
    if (isChangeRelevant(event)) {
      if (event.getType() == ChangeType.REMOVED) {
        // TODO clean up trades from cache if this is a position that has been removed
        _cache.remove(event.getObjectId());
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
   * Returns true if a change event invalidates any of this view's portfolio, including trades, securities and positions it refers to.
   * 
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

  @Override
  public ViewportResults getAllGridData(GridType gridType, TypeFormatter.Format format) {
    GridStructure gridStructure = getGrid(gridType).getGridStructure();
    List<Integer> rows = Lists.newArrayList();
    for (int i = 0; i < gridStructure.getRowCount(); i++) {
      rows.add(i);
    }
    List<Integer> cols = Lists.newArrayList();
    for (int i = 0; i < gridStructure.getColumnCount(); i++) {
      cols.add(i);
    }
    ViewportDefinition viewportDefinition = ViewportDefinition.create(Integer.MIN_VALUE, rows, cols, Lists.<GridCell>newArrayList(), format, false);

    String callbackId = GUIDGenerator.generate().toString();
    String structureCallbackId = GUIDGenerator.generate().toString();
    MainGridViewport viewport = getGrid(gridType).createViewport(viewportDefinition, callbackId, structureCallbackId, _cache);
    return viewport.getData();
  }

  @Override
  public UniqueId getViewDefinitionId() {
    return _viewDefinitionId;
  }

  @Override
  public List<ErrorInfo> getErrors() {
    return _errorManager.get();
  }

  @Override
  public void deleteError(long id) {
    _errorManager.delete(id);
  }
}
