/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.ObjectUtils;
import org.cometd.bayeux.server.LocalSession;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewTargetResultModel;
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

  private final LocalSession _local;
  private final ServerSession _remote;
  private final ViewClient _client;
  private final UniqueId _baseViewDefinitionId;
  private final String _aggregatorName;
  private final UniqueId _viewDefinitionId;
  private final ViewExecutionOptions _executionOptions;
  private final ExecutorService _executorService;
  private final ResultConverterCache _resultConverterCache;
  private final ComputationTargetResolver _computationTargetResolver;
  private final ReentrantLock _updateLock = new ReentrantLock();

  private boolean _awaitingNextUpdate;
  private boolean _continueUpdateThread;
  private boolean _updateThreadRunning;

  private final AtomicBoolean _isInit = new AtomicBoolean(false);

  private final Map<String, WebViewGrid> _gridsByName;
  private RequirementBasedWebViewGrid _portfolioGrid;
  private RequirementBasedWebViewGrid _primitivesGrid;

  private final AtomicInteger _activeDepGraphCount = new AtomicInteger();

  public WebView(final LocalSession local, final ServerSession remote, final ViewClient client, final UniqueId baseViewDefinitionId,
                 final String aggregatorName, final UniqueId viewDefinitionId, final ViewExecutionOptions executionOptions,
                 final UserPrincipal user, final ExecutorService executorService, final ResultConverterCache resultConverterCache, final ComputationTargetResolver computationTargetResolver) {
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
    _computationTargetResolver = computationTargetResolver;
    _client.setResultListener(new AbstractViewResultListener() {

      @Override
      public UserPrincipal getUser() {
        // Authentication needed
        return UserPrincipal.getLocalUser();
      }

      @Override
      public void viewDefinitionCompiled(final CompiledViewDefinition compiledViewDefinition, final boolean hasMarketDataPermissions) {
        s_logger.info("View definition compiled: {}", compiledViewDefinition.getViewDefinition().getName());
        initGrids(compiledViewDefinition);
      }

      @Override
      public void cycleCompleted(final ViewComputationResultModel fullResult, final ViewDeltaResultModel deltaResult) {
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

  private void initGrids(final CompiledViewDefinition compiledViewDefinition) {
    _isInit.set(true);

    final RequirementBasedWebViewGrid portfolioGrid = new WebViewPortfolioGrid(
        getViewClient(), compiledViewDefinition, getResultConverterCache(), getLocal(), getRemote(), getComputationTargetResolver());
    if (portfolioGrid.getGridStructure().isEmpty()) {
      _portfolioGrid = null;
    } else {
      _portfolioGrid = portfolioGrid;
      _gridsByName.put(_portfolioGrid.getName(), _portfolioGrid);
    }

    final RequirementBasedWebViewGrid primitivesGrid = new WebViewPrimitivesGrid(
        getViewClient(), compiledViewDefinition, getResultConverterCache(), getLocal(), getRemote(), getComputationTargetResolver());
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

  public boolean matches(final UniqueId baseViewDefinitionId, final String aggregatorName, final ViewExecutionOptions executionOptions) {
    return getBaseViewDefinitionId().equals(baseViewDefinitionId)
        && ObjectUtils.equals(getAggregatorName(), aggregatorName) && ObjectUtils.equals(getExecutionOptions(), executionOptions);
  }

  public WebViewGrid getGridByName(final String name) {
    return _gridsByName.get(name);
  }

  @SuppressWarnings("unchecked")
  public void triggerUpdate(final ServerMessage message) {
    final Map<String, Object> dataMap = (Map<String, Object>) message.getData();
    boolean immediateResponse = (Boolean) dataMap.get("immediateResponse");

    if (getPortfolioGrid() != null) {
      final Map<String, Object> portfolioViewport = (Map<String, Object>) dataMap.get("portfolioViewport");
      getPortfolioGrid().setViewport(processViewportData(portfolioViewport));
    }

    if (getPrimitivesGrid() != null) {
      final Map<String, Object> primitiveViewport = (Map<String, Object>) dataMap.get("primitiveViewport");
      getPrimitivesGrid().setViewport(processViewportData(primitiveViewport));
    }

    final Map<String, Map<String, Object>> depGraphViewportMap = (Map<String, Map<String, Object>>) dataMap.get("depGraphViewport");
    for (final Map.Entry<String, Map<String, Object>> depGraphViewportEntry : depGraphViewportMap.entrySet()) {
      final Pair<RequirementBasedWebViewGrid, WebGridCell> depGraphCell = processCellId(depGraphViewportEntry.getKey());
      final SortedMap<Integer, Long> viewportMap = processViewportData(depGraphViewportEntry.getValue());
      final RequirementBasedWebViewGrid parentDepGraphGrid = depGraphCell.getFirst();
      final WebGridCell cellId = depGraphCell.getSecond();
      parentDepGraphGrid.setDepGraphViewport(cellId, viewportMap);
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

  private Pair<RequirementBasedWebViewGrid, WebGridCell> processCellId(final String encodedCellId) {
    final String[] cellIdFields = encodedCellId.split("-");
    final String parentGridName = cellIdFields[0];
    final int rowId = Integer.parseInt(cellIdFields[1]);
    final int colId = Integer.parseInt(cellIdFields[2]);
    return Pair.of((RequirementBasedWebViewGrid) getGridByName(parentGridName), new WebGridCell(rowId, colId));
  }

  private static SortedMap<Integer, Long> processViewportData(final Map<String, Object> viewportData) {
    final SortedMap<Integer, Long> result = new TreeMap<Integer, Long>();
    if (viewportData.isEmpty()) {
      return result;
    }
    final Object[] ids = (Object[]) viewportData.get("rowIds");
    final Object[] lastTimes = (Object[]) viewportData.get("lastTimestamps");
    for (int i = 0; i < ids.length; i++) {
      if (ids[i] instanceof Number) {
        final long jsRowId = (Long) ids[i];
        final int rowId = (int) jsRowId;
        if (lastTimes[i] != null) {
          final Long lastTime = (Long) lastTimes[i];
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
          final ViewComputationResultModel update = getViewClient().getLatestResult();

          getRemote().startBatch();

          final long valuationTimeMillis = update.getViewCycleExecutionOptions().getValuationTime().toEpochMilli();
          final long calculationDurationMillis = update.getCalculationDuration().toMillis();

          sendStartMessage(valuationTimeMillis, calculationDurationMillis);
          try {
            processResult(update);
          } catch (final Exception e) {
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

  private void processResult(final ViewComputationResultModel resultModel) {
    final long resultTimestamp = resultModel.getCalculationTime().toEpochMilli();

    if (getPrimitivesGrid() != null) {
      final ComputationTargetSpecification[] targets = getPrimitivesGrid().getGridStructure().getTargets();
      for (int i = 0; i < targets.length; i++) {
        final ComputationTargetSpecification target = targets[i];
        getPrimitivesGrid().processTargetResult(i, target, resultModel.getTargetResult(target), resultTimestamp);
      }
      getPrimitivesGrid().processDepGraphs(resultTimestamp);
    }

    if (getPortfolioGrid() != null) {
      ComputationTargetSpecification nodeSpec = null;
      // [PLAT-2286] This is a hack to almost support trivial nesting, e.g. for the PositionWeight function. Recognition of complex target types needs doing properly.
      final ComputationTargetSpecification[] targets = getPortfolioGrid().getGridStructure().getTargets();
      for (int i = 0; i < targets.length; i++) {
        final ComputationTargetSpecification target = targets[i];
        ViewTargetResultModel targetResult = resultModel.getTargetResult(target);
        if (target.getType().isTargetType(ComputationTargetType.PORTFOLIO_NODE)) {
          nodeSpec = target;
        } else if ((nodeSpec != null) && target.getType().isTargetType(ComputationTargetType.POSITION)) {
          final ViewTargetResultModel scopedResult = resultModel.getTargetResult(nodeSpec.containing(ComputationTargetType.POSITION, target.getUniqueId()));
          if (scopedResult != null) {
            if (targetResult != null) {
              final Map<String, Collection<ComputedValueResult>> newResult = new HashMap<String, Collection<ComputedValueResult>>();
              for (final String calcConfig : targetResult.getCalculationConfigurationNames()) {
                newResult.put(calcConfig, new ArrayList<ComputedValueResult>(targetResult.getAllValues(calcConfig)));
              }
              for (final String calcConfig : scopedResult.getCalculationConfigurationNames()) {
                final Collection<ComputedValueResult> scopedValues = scopedResult.getAllValues(calcConfig);
                Collection<ComputedValueResult> values = newResult.get(calcConfig);
                if (values == null) {
                  values = new ArrayList<ComputedValueResult>(scopedValues.size());
                  newResult.put(calcConfig, values);
                }
                for (final ComputedValueResult value : scopedValues) {
                  values.add(new ComputedValueResult(new ValueSpecification(value.getSpecification().getValueName(), target, value.getSpecification().getProperties()), value.getValue(), value
                      .getAggregatedExecutionLog()));
                }
              }
              targetResult = new ViewTargetResultModel() {

                @Override
                public Collection<String> getCalculationConfigurationNames() {
                  return newResult.keySet();
                }

                @Override
                public Collection<ComputedValueResult> getAllValues(final String calcConfigurationName) {
                  return newResult.get(calcConfigurationName);
                }

              };
            } else {
              targetResult = scopedResult;
            }
          }
        }
        getPortfolioGrid().processTargetResult(i, target, targetResult, resultTimestamp);
      }
      getPortfolioGrid().processDepGraphs(resultTimestamp);
    }
  }

  /**
   * Tells the remote client that updates are starting.
   */
  private void sendStartMessage(final long valuationTimeEpochMillis, final long calculationDurationMillis) {
    final Map<String, Object> startMessage = new HashMap<String, Object>();
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

  private void sendViewStatus(final boolean isRunning, final String status) {
    final Map<String, Object> output = new HashMap<String, Object>();
    output.put("isRunning", isRunning);
    output.put("status", status);
    getRemote().deliver(getLocal(), "/status", output, null);
  }

  //-------------------------------------------------------------------------

  public Map<String, Object> getInitialJsonGridStructures() {
    final Map<String, Object> gridStructures = new HashMap<String, Object>();
    if (getPrimitivesGrid() != null) {
      gridStructures.put("primitives", getPrimitivesGrid().getInitialJsonGridStructure());
    }
    if (getPortfolioGrid() != null) {
      gridStructures.put("portfolio", getPortfolioGrid().getInitialJsonGridStructure());
    }
    return gridStructures;
  }

  public void setIncludeDepGraph(final String parentGridName, final WebGridCell cell, final boolean includeDepGraph) {
    final WebViewGrid parentGrid = getGridByName(parentGridName);
    if (parentGrid == null || !(parentGrid instanceof RequirementBasedWebViewGrid)) {
      throw new IllegalArgumentException("Invalid grid for dependency graph introspection: " + parentGridName);
    }
    final RequirementBasedWebViewGrid depGraphParentGrid = (RequirementBasedWebViewGrid) parentGrid;
    if (includeDepGraph) {
      if (_activeDepGraphCount.getAndIncrement() == 0) {
        getViewClient().setViewCycleAccessSupported(true);
      }
    } else {
      if (_activeDepGraphCount.decrementAndGet() == 0) {
        getViewClient().setViewCycleAccessSupported(false);
      }
    }
    final WebViewGrid grid = depGraphParentGrid.setIncludeDepGraph(cell, includeDepGraph);
    if (grid != null) {
      if (includeDepGraph) {
        registerGrid(grid);
      } else {
        unregisterGrid(grid.getName());
      }
    }
  }

  public Pair<Instant, String> getGridContentsAsCsv(final String gridName) {
    final WebViewGrid grid = getGridByName(gridName);
    if (grid == null) {
      throw new OpenGammaRuntimeException("Unknown grid '" + gridName + "'");
    }
    final ViewComputationResultModel latestResult = getViewClient().getLatestResult();
    if (latestResult == null) {
      return null;
    }
    final String csv = grid.dumpContentsToCsv(latestResult);
    return Pair.of(latestResult.getViewCycleExecutionOptions().getValuationTime(), csv);
  }

  //-------------------------------------------------------------------------

  private void registerGrid(final WebViewGrid grid) {
    _gridsByName.put(grid.getName(), grid);
  }

  private void unregisterGrid(final String gridName) {
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

  private LocalSession getLocal() {
    return _local;
  }

  private ServerSession getRemote() {
    return _remote;
  }

  private ResultConverterCache getResultConverterCache() {
    return _resultConverterCache;
  }

  private ComputationTargetResolver getComputationTargetResolver() {
    return _computationTargetResolver;
  }

}
