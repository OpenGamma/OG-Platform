/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import javax.time.Instant;

import org.apache.commons.lang.ObjectUtils;
import org.cometd.Client;
import org.cometd.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.conversion.ResultConverterCache;

/**
 * 
 */
public class WebView {
  private static final Logger s_logger = LoggerFactory.getLogger(WebView.class);
  
  private static final String STARTED_DISPLAY_NAME = "Live";
  private static final String PAUSED_DISPLAY_NAME = "Paused";
  
  private final Client _local;
  private final Client _remote;
  private final ViewClient _client;
  private final UniqueId _baseViewDefinitionId;
  private final String _aggregatorName;
  private final UniqueId _viewDefinitionId;
  private final ViewExecutionOptions _executionOptions;
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

  public WebView(final Client local, final Client remote, final ViewClient client, final UniqueId baseViewDefinitionId,
      final String aggregatorName, final UniqueId viewDefinitionId, final ViewExecutionOptions executionOptions,
      final UserPrincipal user, final ExecutorService executorService, final ResultConverterCache resultConverterCache) {
    ArgumentChecker.notNull(executionOptions, "executionOptions");
    _local = local;
    _remote = remote;
    _client = client;
    _baseViewDefinitionId = baseViewDefinitionId;
    _aggregatorName = aggregatorName;
    _viewDefinitionId = viewDefinitionId;
    _executionOptions = executionOptions;
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
        s_logger.info("New result arrived for view '{}'", getViewDefinitionId());
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
    
    client.attachToViewProcess(viewDefinitionId, executionOptions);
  }
  
  //-------------------------------------------------------------------------
  // Initialisation
  
  private void initGrids(CompiledViewDefinition compiledViewDefinition) {
    _isInit.set(true);
    
    RequirementBasedWebViewGrid portfolioGrid = new WebViewPortfolioGrid(getViewClient(), compiledViewDefinition, getResultConverterCache(), getLocal(), getRemote());
    if (portfolioGrid.getGridStructure().isEmpty()) {
      _portfolioGrid = null;
    } else {
      _portfolioGrid = portfolioGrid;
      _gridsByName.put(_portfolioGrid.getName(), _portfolioGrid);
    }
    
    RequirementBasedWebViewGrid primitivesGrid = new WebViewPrimitivesGrid(getViewClient(), compiledViewDefinition, getResultConverterCache(), getLocal(), getRemote());
    if (primitivesGrid.getGridStructure().isEmpty()) {
      _primitivesGrid = null;
    } else {
      _primitivesGrid = primitivesGrid;
      _gridsByName.put(_primitivesGrid.getName(), _primitivesGrid);
    }
    
    notifyInitialized();
  }
  
  private void notifyInitialized() {
    getRemote().deliver(getLocal(), "/changeView", getInitialJsonGridStructures(), null);
  }
  
  /*package*/ void reconnected() {
    if (_isInit.get()) {
      notifyInitialized();
    }
  }
  
  //-------------------------------------------------------------------------
  // Update control
  
  public void pause() {
    getViewClient().pause();
    sendViewStatus(false, PAUSED_DISPLAY_NAME);
  }
  
  public void resume() {
    getViewClient().resume();
    sendViewStatus(true, STARTED_DISPLAY_NAME);
  }
  
  public void shutdown() {
    // Removes all listeners
    getViewClient().shutdown();
  }
  
  public UniqueId getBaseViewDefinitionId() {
    return _baseViewDefinitionId;
  }
  
  public String getAggregatorName() {
    return _aggregatorName;
  }
  
  public UniqueId getViewDefinitionId() {
    return _viewDefinitionId;
  }
  
  public ViewExecutionOptions getExecutionOptions() {
    return _executionOptions;
  }
  
  public boolean matches(UniqueId baseViewDefinitionId, String aggregatorName, ViewExecutionOptions executionOptions) {
    return getBaseViewDefinitionId().equals(baseViewDefinitionId)
        && ObjectUtils.equals(getAggregatorName(), aggregatorName) && ObjectUtils.equals(getExecutionOptions(), executionOptions);
  }
  
  public WebViewGrid getGridByName(String name) {
    return _gridsByName.get(name);
  }
  
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

  private void runUpdateThread() {
    getExecutorService().submit(new Runnable() {
      @Override
      public void run() {
        do {
          ViewComputationResultModel update = getViewClient().getLatestResult();
          
          getRemote().startBatch();
          
          long valuationTimeMillis = update.getValuationTime().toEpochMillisLong();
          long calculationDurationMillis = update.getCalculationDuration().toMillisLong();
          
          sendStartMessage(valuationTimeMillis, calculationDurationMillis);
          try {
            processResult(update);
          } catch (Exception e) {
            s_logger.error("Error processing result from view cycle " + update.getViewCycleId(), e);
          }
          sendEndMessage();
          
          getRemote().endBatch();
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
            getPrimitivesGrid().processTargetResult(target, resultModel.getTargetResult(target), resultTimestamp);
          }
          break;
        case PORTFOLIO_NODE:
        case POSITION:
          if (getPortfolioGrid() != null) {
            getPortfolioGrid().processTargetResult(target, resultModel.getTargetResult(target), resultTimestamp);
          }
          break;
        default:
          // Something that the client does not display
          continue;
      }
    }
  }
  
  /**
   * Tells the remote client that updates are starting.
   */
  private void sendStartMessage(long valuationTimeEpochMillis, long calculationDurationMillis) {
    Map<String, Object> startMessage = new HashMap<String, Object>();
    startMessage.put("valuationTime", valuationTimeEpochMillis);
    startMessage.put("calculationDuration", calculationDurationMillis);
    getRemote().deliver(getLocal(), "/updates/control/start", startMessage, null);
  }

  /**
   * Tells the remote client that updates have finished.
   */
  private void sendEndMessage() {
    getRemote().deliver(getLocal(), "/updates/control/end", new HashMap<String, Object>(), null);
  }
  
  private void sendViewStatus(boolean isRunning, String status) {
    Map<String, Object> output = new HashMap<String, Object>();
    output.put("isRunning", isRunning);
    output.put("status", status);
    getRemote().deliver(getLocal(), "/status", output, null);
  }
  
  //-------------------------------------------------------------------------
  
  public Map<String, Object> getInitialJsonGridStructures() {
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
  
  private Client getLocal() {
    return _local;
  }
  
  private Client getRemote() {
    return _remote;
  }
  
  private ResultConverterCache getResultConverterCache() {
    return _resultConverterCache;
  }
  
}
