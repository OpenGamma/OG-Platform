/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.conversion.ConversionMode;
import com.opengamma.web.server.conversion.ResultConverterCache;
import com.opengamma.web.server.push.subscription.Viewport;
import org.apache.commons.lang.ObjectUtils;
import org.cometd.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.time.Instant;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 */
public class WebView implements Viewport {

  private static final Logger s_logger = LoggerFactory.getLogger(WebView.class);

  private final ViewClient _client;
  private final String _viewDefinitionName;
  private final UniqueId _snapshotId;
  private final ExecutorService _executorService;
  private final ResultConverterCache _resultConverterCache;
  
  private final ReentrantLock _updateLock = new ReentrantLock();
  
  private boolean _awaitingNextUpdate;
  private boolean _continueUpdateThread;
  private boolean _updateThreadRunning;
  
  private AtomicBoolean _isInit = new AtomicBoolean(false);
  
  private final Map<String, WebViewGrid> _gridsByName;
  private RequirementBasedWebViewGrid _portfolioGrid;
  private RequirementBasedWebViewGrid _primitivesGrid;
  
  private final AtomicInteger _activeDepGraphCount = new AtomicInteger();

  public WebView(ViewClient client,
                 String viewDefinitionName,
                 UniqueId snapshotId,
                 UserPrincipal user, // TODO will this be needed in future?
                 ExecutorService executorService,
                 ResultConverterCache resultConverterCache) {
    _client = client;
    _viewDefinitionName = viewDefinitionName;
    _snapshotId = snapshotId;
    _executorService = executorService;
    _resultConverterCache = resultConverterCache;
    _gridsByName = new HashMap<String, WebViewGrid>();

    _client.setResultListener(new AbstractViewResultListener() {
      
      @Override
      public UserPrincipal getUser() {
        // Authentication needed
        return UserPrincipal.getLocalUser();
      }
      
      @Override
      public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {     
        s_logger.info("View definition compiled: {}", compiledViewDefinition.getViewDefinition().getName());
        initGrids(compiledViewDefinition);
      }
      
      @Override
      public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
        s_logger.info("New result arrived for view '{}'", getViewDefinitionName());
        _updateLock.lock();
        try {
          if (_awaitingNextUpdate) {
            _awaitingNextUpdate = false;
            sendImmediateUpdate();
          }
        } finally {
          _updateLock.unlock();
        }
      }

    });
    
    MarketDataSpecification marketDataSpec;
    EnumSet<ViewExecutionFlags> flags;
    if (snapshotId != null) {
      marketDataSpec = MarketData.user(snapshotId.toLatest());
      flags = ExecutionFlags.none().triggerOnMarketData().get();
    } else {
      marketDataSpec = MarketData.live();
      flags = ExecutionFlags.triggersEnabled().get();
    }
    ViewExecutionOptions executionOptions = ExecutionOptions.infinite(marketDataSpec, flags);
    client.attachToViewProcess(viewDefinitionName, executionOptions);
  }
  
  //-------------------------------------------------------------------------
  // Initialisation
  
  private void initGrids(CompiledViewDefinition compiledViewDefinition) {
    _isInit.set(true);

    RequirementBasedWebViewGrid portfolioGrid = new WebViewPortfolioGrid(getViewClient(),
                                                                         compiledViewDefinition,
                                                                         getResultConverterCache());
    if (portfolioGrid.getGridStructure().isEmpty()) {
      _portfolioGrid = null;
    } else {
      _portfolioGrid = portfolioGrid;
      _gridsByName.put(_portfolioGrid.getName(), _portfolioGrid);
    }

    RequirementBasedWebViewGrid primitivesGrid = new WebViewPrimitivesGrid(getViewClient(),
                                                                           compiledViewDefinition,
                                                                           getResultConverterCache());
    if (primitivesGrid.getGridStructure().isEmpty()) {
      _primitivesGrid = null;
    } else {
      _primitivesGrid = primitivesGrid;
      _gridsByName.put(_primitivesGrid.getName(), _primitivesGrid);
    }
    // TODO store grid structure
  }
  
  //-------------------------------------------------------------------------
  // Update control

  /* package */ void pause() {
    getViewClient().pause();
  }

  /* package */ void resume() {
    getViewClient().resume();
  }

  /* package */ void shutdown() {
    // Removes all listeners
    getViewClient().shutdown();
  }
  
  public String getViewDefinitionName() {
    return _viewDefinitionName;
  }
  
  private UniqueId getSnapshotId() {
    return _snapshotId;
  }

  /* package */ boolean matches(String viewDefinitionName, UniqueId snapshotId) {
    return getViewDefinitionName().equals(viewDefinitionName) && ObjectUtils.equals(getSnapshotId(), snapshotId);
  }

  /* package */ WebViewGrid getGridByName(String name) {
    return _gridsByName.get(name);
  }

  /**
   * @param message Contains data map with fields:
   * <pre>
   *   immediateResponse: boolean<br/>
   *   portfolioViewport, primitiveViewport: {rowIds: Long[], lastTimestamps: Long[]}
   * </pre>
   */
  @SuppressWarnings("unchecked")
  public void triggerUpdate(Message message) {
    Map<String, Object> dataMap = (Map<String, Object>) message.getData();
    boolean immediateResponse = (Boolean) dataMap.get("immediateResponse");
    
    if (getPortfolioGrid() != null) {
      Map<String, Object> portfolioViewport = (Map<String, Object>) dataMap.get("portfolioViewport");
      getPortfolioGrid().setViewport(processViewportData(portfolioViewport));
    }
    
    if (getPrimitivesGrid() != null) {
      Map<String, Object> primitiveViewport = (Map<String, Object>) dataMap.get("primitiveViewport");
      getPrimitivesGrid().setViewport(processViewportData(primitiveViewport));
    }

    // Can only provide an immediate response if there is a result available
    immediateResponse &= getViewClient().isResultAvailable();
    
    _updateLock.lock();
    try {
      if (immediateResponse) {
        sendImmediateUpdate();
      } else {
        _awaitingNextUpdate = true;
      }
    } finally {
      _updateLock.unlock();
    }
  }

  /**
   * @param viewportData {@code {rowIds: Long[], lastTimestamps: Long[]}},
   * {@code rowIds[i]} corresponds to {@code lastTimestamps[i]}
   * @return Sorted map of last timestamps keyed by row ID
   */
  private SortedMap<Integer, Long> processViewportData(Map<String, Object> viewportData) {
    SortedMap<Integer, Long> result = new TreeMap<Integer, Long>();
    if (viewportData.isEmpty()) {
      return result;
    }
    Object[] ids = (Object[]) viewportData.get("rowIds");
    Object[] lastTimes = (Object[]) viewportData.get("lastTimestamps");
    for (int i = 0; i < ids.length; i++) {
      if (ids[i] instanceof Number) {
        long jsRowId = (Long) ids[i];
        int rowId = (int) jsRowId;
        if (lastTimes[i] != null) {
          Long lastTime = (Long) lastTimes[i];
          result.put(rowId, lastTime);
        } else {
          result.put(rowId, null);
        }
      } else {
        throw new OpenGammaRuntimeException("Unexpected type of webId: " + ids[i]);
      }
    }
    return result;
  }
  
  // TODO is this still required? jetty's thread pool should take care of this as long as the listener returns quickly
  private void sendImmediateUpdate() {
    _updateLock.lock();
    try {
      if (!_updateThreadRunning) {
        _updateThreadRunning = true;
        runUpdateThread();
      } else {
        _continueUpdateThread = true;
      }
    } finally {
      _updateLock.unlock();
    }
  }

  // TODO is this still required? jetty's thread pool should take care of this as long as the listener returns quickly
  private void runUpdateThread() {
    getExecutorService().submit(new Runnable() {
      @Override
      public void run() {
        do {
          ViewComputationResultModel update = getViewClient().getLatestResult();

          try {
            processResult(update);
          } catch (Exception e) {
            s_logger.error("Error processing result from view cycle " + update.getViewCycleId(), e);
          }

        } while (continueUpdateThread());
      }
    });
  }
  
  private boolean continueUpdateThread() {
    _updateLock.lock();
    try {
      if (_continueUpdateThread) {
        _continueUpdateThread = false;
        return true;
      } else {
        _updateThreadRunning = false;
        return false;
      }
    } finally {
      _updateLock.unlock();
    }
  }
  
  private void processResult(ViewComputationResultModel resultModel) {
    long resultTimestamp = resultModel.getCalculationTime().toEpochMillisLong();
    
    for (ComputationTargetSpecification target : resultModel.getAllTargets()) {
      switch (target.getType()) {
        case PRIMITIVE:
          if (getPrimitivesGrid() != null) {
            Map<String, Object> primitiveResult =
                getPrimitivesGrid().processTargetResult(target, resultModel.getTargetResult(target), resultTimestamp);
            // TODO stash the result
          }
          break;
        case PORTFOLIO_NODE:
        case POSITION:
          if (getPortfolioGrid() != null) {
            Map<String, Object> portfolioResult =
                getPortfolioGrid().processTargetResult(target, resultModel.getTargetResult(target), resultTimestamp);
            // TODO stash the result
          }
      }
    }
  }
  
  private Map<String, Object> getInitialJsonGridStructures() {
    Map<String, Object> gridStructures = new HashMap<String, Object>();
    if (getPrimitivesGrid() != null) {
      gridStructures.put("primitives", getPrimitivesGrid().getInitialJsonGridStructure());
    }
    if (getPortfolioGrid() != null) {
      gridStructures.put("portfolio", getPortfolioGrid().getInitialJsonGridStructure());
    }
    return gridStructures;
  }
  
  public void setIncludeDepGraph(String parentGridName, WebGridCell cell, boolean includeDepGraph) {
    if (!getPortfolioGrid().getName().equals(parentGridName)) {
      throw new OpenGammaRuntimeException("Invalid or unknown grid for dependency graph viewing: " + parentGridName);
    }
    
    if (includeDepGraph) {
      if (_activeDepGraphCount.getAndIncrement() == 0) {
        getViewClient().setViewCycleAccessSupported(true);
      }
    } else {
      if (_activeDepGraphCount.decrementAndGet() == 0) {
        getViewClient().setViewCycleAccessSupported(false);
      }
    }
    WebViewGrid grid = getPortfolioGrid().setIncludeDepGraph(cell, includeDepGraph);
    if (grid != null) {
      if (includeDepGraph) {
        registerGrid(grid);
      } else {
        unregisterGrid(grid.getName());
      }
    }
  }
  
  public Pair<Instant, String> getGridContentsAsCsv(String gridName) {
    WebViewGrid grid = getGridByName(gridName);
    if (grid == null) {
      throw new OpenGammaRuntimeException("Unknown grid '" + gridName + "'");
    }
    ViewComputationResultModel latestResult = getViewClient().getLatestResult();
    if (latestResult == null) {
      return null;
    }
    String csv = grid.dumpContentsToCsv(latestResult);
    return Pair.of(latestResult.getValuationTime(), csv);
  }
  
  //-------------------------------------------------------------------------
  
  private void registerGrid(WebViewGrid grid) {
    _gridsByName.put(grid.getName(), grid);
  }
  
  private void unregisterGrid(String gridName) {
    _gridsByName.remove(gridName);
  }
  
  //-------------------------------------------------------------------------
  
  private ExecutorService getExecutorService() {
    return _executorService;
  }
  
  private RequirementBasedWebViewGrid getPortfolioGrid() {
    return _portfolioGrid;
  }
  
  private RequirementBasedWebViewGrid getPrimitivesGrid() {
    return _primitivesGrid;
  }
  
  private ViewClient getViewClient() {
    return _client;
  }
  
  private ResultConverterCache getResultConverterCache() {
    return _resultConverterCache;
  }

  @Override
  public Map<String, Object> getGridStructure() {
    throw new UnsupportedOperationException("getGridStructure not implemented");
  }

  @Override
  public Map<String, Object> getLatestData() {
    throw new UnsupportedOperationException("getLatestData not implemented");
  }

  @Override
  public void setRunning(boolean run) {
    throw new UnsupportedOperationException("setRunning not implemented");
  }

  @Override
  public void setConversionMode(ConversionMode mode) {
    throw new UnsupportedOperationException("setConversionMode not implemented");
  }
}
