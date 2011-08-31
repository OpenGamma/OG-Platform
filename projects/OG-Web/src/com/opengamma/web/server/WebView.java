/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import com.opengamma.DataNotFoundException;
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
import com.opengamma.web.server.push.AnalyticsListener;
import com.opengamma.web.server.push.Viewport;
import com.opengamma.web.server.push.ViewportDefinition;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.time.Instant;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO CONCURRENCY - I've scrapped all the locking, needs to be reviewed and replaced
 * TODO return new viewport instance rather than implementing it?
 */
public class WebView implements Viewport {

  private static final Logger s_logger = LoggerFactory.getLogger(WebView.class);

  private final ViewClient _viewClient;
  private final String _viewDefinitionName;
  private final UniqueId _snapshotId;
  private final ResultConverterCache _resultConverterCache;
  private final Map<String,Object> _latestResults = new HashMap<String, Object>();
  private final Object _lock = new Object();

  private RequirementBasedWebViewGrid _portfolioGrid;
  private RequirementBasedWebViewGrid _primitivesGrid;

  // TODO get the state from the grids
  private final AtomicInteger _activeDepGraphCount = new AtomicInteger();
  private ViewportDefinition _viewportDefinition;
  private AnalyticsListener _listener;
  private Map<String,Object> _gridStructures;
  private boolean _initialized = false;
  private boolean _sendAnalyticsUpdates = false;

  public WebView(ViewClient viewClient,
                 String viewDefinitionName,
                 UniqueId snapshotId,
                 ResultConverterCache resultConverterCache,
                 ViewportDefinition viewportDefinition,
                 AnalyticsListener listener) {
    _viewClient = viewClient;
    _viewDefinitionName = viewDefinitionName;
    _snapshotId = snapshotId;
    _resultConverterCache = resultConverterCache;
    _viewportDefinition = viewportDefinition;
    _listener = listener;

    _viewClient.setResultListener(new AbstractViewResultListener() {
      
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
        updateResults();
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
    _viewClient.attachToViewProcess(viewDefinitionName, executionOptions);
  }

  // TODO make sure an update event is published when the view defs compile?
  private void initGrids(CompiledViewDefinition compiledViewDefinition) {
    synchronized (_lock) {
      WebViewPortfolioGrid portfolioGrid = new WebViewPortfolioGrid(_viewClient, compiledViewDefinition, _resultConverterCache);

      _gridStructures = new HashMap<String, Object>();

      if (portfolioGrid.getGridStructure().isEmpty()) {
        _portfolioGrid = null;
      } else {
        _portfolioGrid = portfolioGrid;
        _gridStructures.put("portfolio", _portfolioGrid.getInitialJsonGridStructure());
        _gridStructures.put("portfolio", _portfolioGrid.getInitialJsonGridStructure());
      }

      RequirementBasedWebViewGrid primitivesGrid = new WebViewPrimitivesGrid(_viewClient, compiledViewDefinition, _resultConverterCache);
      if (primitivesGrid.getGridStructure().isEmpty()) {
        _primitivesGrid = null;
      } else {
        _primitivesGrid = primitivesGrid;
        _gridStructures.put("primitives", _primitivesGrid.getInitialJsonGridStructure());
      }
      _initialized = true;
      _listener.gridStructureChanged();
      configureGridViewports();
    }
  }

  /* package */ void pause() {
    synchronized (_lock) {
      _viewClient.pause();
    }
  }

  /* package */ void resume() {
    synchronized (_lock) {
      _viewClient.resume();
    }
  }

  /* package */ void shutdown() {
    // Removes all listeners
    synchronized (_lock) {
      _viewClient.shutdown();
    }
  }
  
  public String getViewDefinitionName() {
    synchronized (_lock) {
      return _viewDefinitionName;
    }
  }

  /* package */ boolean matches(String viewDefinitionName, UniqueId snapshotId) {
    synchronized (_lock) {
      return _viewDefinitionName.equals(viewDefinitionName) && ObjectUtils.equals(_snapshotId, snapshotId);
    }
  }

  private WebViewGrid getGridByName(String name) {
    if (_primitivesGrid != null) {
      if (_primitivesGrid.getName().equals(name)) {
        return _primitivesGrid;
      }
      WebViewGrid depGraphGrid = _primitivesGrid.getDepGraphGrid(name);
      if (depGraphGrid != null) {
        return depGraphGrid;
      }
    }
    if (_portfolioGrid != null) {
      if (_portfolioGrid.getName().equals(name)) {
        return _portfolioGrid;
      } else {
        return _portfolioGrid.getDepGraphGrid(name);
      }
    }
    return null;
  }

  /**
   *
   */
  /* package */ Viewport configureViewport(ViewportDefinition viewportDefinition,
                                           AnalyticsListener listener,
                                           String viewportKey) {
    synchronized (_lock) {
      _viewportDefinition = viewportDefinition;
      _listener = listener;
      configureGridViewports();
      return this;
    }
  }

  private void configureGridViewports() {
    if (!_initialized) {
      return;
    }
    _portfolioGrid.setViewport(_viewportDefinition.getPortfolioRows());
    _portfolioGrid.updateDepGraphCells(_viewportDefinition.getPortfolioDependencyGraphCells());
    _primitivesGrid.setViewport(_viewportDefinition.getPrimitiveRows());
    _primitivesGrid.updateDepGraphCells(_viewportDefinition.getPrimitiveDependencyGraphCells());
    // TODO _client.setViewCycleAccessSupported()?
    updateResults();
  }

  private void updateResults() {
    synchronized (_lock) {
      if (!_viewClient.isResultAvailable()) {
        return;
      }
      ViewComputationResultModel resultModel = _viewClient.getLatestResult();
      long resultTimestamp = resultModel.getCalculationTime().toEpochMillisLong();
      HashMap<Integer, Map<String, Object>> portfolioResult = new HashMap<Integer, Map<String, Object>>();
      HashMap<Integer, Map<String, Object>> primitiveResult = new HashMap<Integer, Map<String, Object>>();

      for (ComputationTargetSpecification target : resultModel.getAllTargets()) {
        switch (target.getType()) {
          case PRIMITIVE:
            if (_primitivesGrid != null) {
              Map<String, Object> targetResult = _primitivesGrid.getTargetResult(target, resultModel.getTargetResult(target), resultTimestamp);
              if (targetResult != null) {
                Integer rowId = (Integer) targetResult.get("rowId");
                primitiveResult.put(rowId, targetResult);
              }
            }
            break;
          case PORTFOLIO_NODE:
          case POSITION:
            if (_portfolioGrid != null) {
              Map<String, Object> targetResult = _portfolioGrid.getTargetResult(target, resultModel.getTargetResult(target), resultTimestamp);
              if (targetResult != null) {
                Integer rowId = (Integer) targetResult.get("rowId");
                portfolioResult.put(rowId, targetResult);
              }
            }
        }
      }
      _latestResults.clear();
      _latestResults.put("portfolio", portfolioResult);
      _latestResults.put("primitive", primitiveResult);
      if (_sendAnalyticsUpdates) {
        _sendAnalyticsUpdates = false;
        _listener.dataChanged();
      }
    }
  }

  // TODO this logic need to go in configureViewport
  private void setIncludeDepGraph(WebGridCell cell, boolean includeDepGraph) {
    // TODO this is ugly, the dep graph count belongs in the portfolio grid
    if (includeDepGraph) {
      if (_activeDepGraphCount.getAndIncrement() == 0) {
        _viewClient.setViewCycleAccessSupported(true);
      }
    } else {
      if (_activeDepGraphCount.decrementAndGet() == 0) {
        _viewClient.setViewCycleAccessSupported(false);
      }
    }
    /*WebViewGrid grid = _portfolioGrid.setIncludeDepGraph(cell, includeDepGraph);
    if (grid != null) {
      if (includeDepGraph) {
        _gridsByName.put(grid.getName(), grid);
      } else {
        _gridsByName.remove(grid.getName());
      }
    }*/
  }

  // TODO refactor this?
  // TODO CONCURRENCY
  public Pair<Instant, String> getGridContentsAsCsv(String gridName) {
    WebViewGrid grid = getGridByName(gridName);
    if (grid == null) {
      throw new DataNotFoundException("Unknown grid '" + gridName + "'");
    }
    ViewComputationResultModel latestResult = _viewClient.getLatestResult();
    if (latestResult == null) {
      return null;
    }
    String csv = grid.dumpContentsToCsv(latestResult);
    return Pair.of(latestResult.getValuationTime(), csv);
  }

  @Override
  public Map<String, Object> getGridStructure() {
    synchronized (_lock) {
      return _gridStructures;
    }
  }

  @Override
  public Map<String, Object> getLatestResults() {
    synchronized (_lock) {
      _sendAnalyticsUpdates = true;
      return _latestResults;
    }
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
