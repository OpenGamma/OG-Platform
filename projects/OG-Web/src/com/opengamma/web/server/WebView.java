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
import java.util.concurrent.locks.ReentrantLock;

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
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.livedata.UserPrincipal;
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
  private final String _viewDefinitionName;
  private final ExecutorService _executorService;
  private final ResultConverterCache _resultConverterCache;
  
  private final ReentrantLock _updateLock = new ReentrantLock();
  private boolean _awaitingUpdate;
  
  private AtomicBoolean _isInit = new AtomicBoolean(false);
  
  private final Map<String, WebViewGrid> _gridsByName;
  private WebViewGrid _portfolioGrid;
  private WebViewGrid _primitivesGrid;

  public WebView(final Client local, final Client remote, final ViewClient client, final String viewDefinitionName,
      final UserPrincipal user, final ExecutorService executorService, final ResultConverterCache resultConverterCache) {    
    _local = local;
    _remote = remote;
    _client = client;
    _viewDefinitionName = viewDefinitionName;
    _executorService = executorService;
    _resultConverterCache = resultConverterCache;
    _gridsByName = new HashMap<String, WebViewGrid>();

    _client.setResultListener(new AbstractViewResultListener() {
      
      @Override
      public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition) {
        // TODO: support for changing compilation results     
        s_logger.warn("View definition compiled: {}", compiledViewDefinition.getViewDefinition().getName());
        initGrids(compiledViewDefinition);
      }
      
      @Override
      public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
        s_logger.info("New result arrived for view '{}'", getViewDefinitionName());
        _updateLock.lock();
        try {
          if (_awaitingUpdate) {
            _awaitingUpdate = false;
            sendUpdateAsync(fullResult);
          }
        } finally {
          _updateLock.unlock();
        }
      }

    });
    
    client.attachToViewProcess(viewDefinitionName, ExecutionOptions.realTime());
  }
  
  //-------------------------------------------------------------------------
  // Initialisation
  
  private void initGrids(CompiledViewDefinition compiledViewDefinition) {
    if (_isInit.getAndSet(true)) {
      // Already initialised
      return;
    }
    
    WebViewGrid portfolioGrid = new WebViewPortfolioGrid(compiledViewDefinition, getResultConverterCache(), getLocal(), getRemote());
    if (portfolioGrid.getGridStructure().isEmpty()) {
      _portfolioGrid = null;
    } else {
      _portfolioGrid = portfolioGrid;
      _gridsByName.put(_portfolioGrid.getName(), _portfolioGrid);
    }
    
    WebViewGrid primitivesGrid = new WebViewPrimitivesGrid(compiledViewDefinition, getResultConverterCache(), getLocal(), getRemote());
    if (primitivesGrid.getGridStructure().isEmpty()) {
      _primitivesGrid = null;
    } else {
      _primitivesGrid = primitivesGrid;
      _gridsByName.put(_primitivesGrid.getName(), _primitivesGrid);
    }
    
    notifyInitialized();
  }
  
  private void notifyInitialized() {
    getRemote().deliver(getLocal(), "/initialize", getJsonGridStructures(), null);
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
  
  public String getViewDefinitionName() {
    return _viewDefinitionName;
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

    _updateLock.lock();
    try {
      if (!immediateResponse) {
        _awaitingUpdate = true;
      } else {
        ViewComputationResultModel latestResult = getViewClient().getLatestResult();
        if (latestResult == null) {
          _awaitingUpdate = true;
        } else {
          sendUpdateAsync(latestResult);
          _awaitingUpdate = false;
        }
      }
    } finally {
      _updateLock.unlock();
    }
  }
  
  private SortedMap<Long, Long> processViewportData(Map<String, Object> viewportData) {
    SortedMap<Long, Long> result = new TreeMap<Long, Long>();
    if (viewportData.isEmpty()) {
      return result;
    }
    Object[] ids = (Object[]) viewportData.get("rowIds");
    Object[] lastTimes = (Object[]) viewportData.get("lastTimestamps");
    for (int i = 0; i < ids.length; i++) {
      if (ids[i] instanceof Number) {
        Long rowId = (Long) ids[i];
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

  private void sendUpdateAsync(final ViewComputationResultModel update) {
    getExecutorService().submit(new Runnable() {
      @Override
      public void run() {
        getRemote().startBatch();
        
        long liveDataTimestampMillis = update.getValuationTime().toEpochMillisLong();
        long resultTimestampMillis = update.getResultTimestamp().toEpochMillisLong();
        sendStartMessage(resultTimestampMillis, resultTimestampMillis - liveDataTimestampMillis);
        
        processResult(update);
        
        sendEndMessage();
        getRemote().endBatch();
      }
    });
  }
  
  private void processResult(ViewComputationResultModel resultModel) {
    long resultTimestamp = resultModel.getResultTimestamp().toEpochMillisLong();
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
   * Tells the remote client that updates are starting, relating to a particular timestamp.
   */
  private void sendStartMessage(long timestamp, long calculationLatency) {
    Map<String, Object> startMessage = new HashMap<String, Object>();
    startMessage.put("timestamp", timestamp);
    startMessage.put("latency", calculationLatency);
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
  
  public Map<String, Object> getJsonGridStructures() {
    Map<String, Object> gridStructures = new HashMap<String, Object>();
    if (getPrimitivesGrid() != null) {
      gridStructures.put("primitives", getPrimitivesGrid().getJsonGridStructure());
    }
    if (getPortfolioGrid() != null) {
      gridStructures.put("portfolio", getPortfolioGrid().getJsonGridStructure());
    }
    return gridStructures;
  }
  
  //-------------------------------------------------------------------------
  
  private ExecutorService getExecutorService() {
    return _executorService;
  }
  
  private WebViewGrid getPortfolioGrid() {
    return _portfolioGrid;
  }
  
  private WebViewGrid getPrimitivesGrid() {
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
