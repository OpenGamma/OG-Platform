/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.grid;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.listener.AbstractViewResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.web.server.WebGridCell;
import com.opengamma.web.server.conversion.ConversionMode;
import com.opengamma.web.server.conversion.ResultConverterCache;
import com.opengamma.web.server.push.AnalyticsListener;
import com.opengamma.web.server.push.Viewport;
import com.opengamma.web.server.push.ViewportDefinition;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO return new viewport instance rather than implementing it?
 * TODO temporary name just to distinguish it from the similarly named class in the parent package
*/
/* package */ class PushWebView implements Viewport {

  private static final Logger s_logger = LoggerFactory.getLogger(PushWebView.class);

  private final ViewClient _viewClient;
  private final UniqueId _baseViewDefinitionId;
  private final UniqueId _viewDefinitionId;
  private final ResultConverterCache _resultConverterCache;
  private final Map<String,Object> _latestResults = new HashMap<String, Object>();
  private final Object _lock = new Object();

  private PushRequirementBasedWebViewGrid _portfolioGrid;
  private PushRequirementBasedWebViewGrid _primitivesGrid;

  private ViewportDefinition _viewportDefinition;
  private AnalyticsListener _listener;
  private Map<String,Object> _gridStructures;
  private boolean _initialized = false;
  private boolean _sendAnalyticsUpdates = false;

  public PushWebView(ViewClient viewClient,
                     ViewportDefinition viewportDefinition,
                     UniqueId baseViewDefinitionId,
                     UniqueId viewDefinitionId,
                     ResultConverterCache resultConverterCache,
                     AnalyticsListener listener) {
    _viewClient = viewClient;
    _baseViewDefinitionId = baseViewDefinitionId;
    _viewDefinitionId = viewDefinitionId;
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
        s_logger.info("New result arrived for view '{}'", _viewDefinitionId);
        updateResults();
      }

    });
    _viewClient.attachToViewProcess(viewDefinitionId, viewportDefinition.getExecutionOptions());
  }

  // TODO make sure an update event is published when the view defs compile?
  private void initGrids(CompiledViewDefinition compiledViewDefinition) {
    synchronized (_lock) {
      PushWebViewPortfolioGrid portfolioGrid =
          new PushWebViewPortfolioGrid(_viewClient, compiledViewDefinition, _resultConverterCache);

      _gridStructures = new HashMap<String, Object>();

      if (portfolioGrid.getGridStructure().isEmpty()) {
        _portfolioGrid = null;
      } else {
        _portfolioGrid = portfolioGrid;
        _gridStructures.put("portfolio", _portfolioGrid.getInitialJsonGridStructure());
        _gridStructures.put("portfolio", _portfolioGrid.getInitialJsonGridStructure());
      }

      PushRequirementBasedWebViewGrid primitivesGrid =
          new PushWebViewPrimitivesGrid(_viewClient, compiledViewDefinition, _resultConverterCache);

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
  
  public UniqueId getViewDefinitionId() {
    synchronized (_lock) {
      return _viewDefinitionId;
    }
  }

  /* package */ boolean matches(UniqueId baseViewDefinitionId, ViewportDefinition viewportDefinition) {
    synchronized (_lock) {
      return _baseViewDefinitionId.equals(baseViewDefinitionId) &&
          ObjectUtils.equals(_viewportDefinition.getExecutionOptions(), viewportDefinition.getExecutionOptions()) &&
          ObjectUtils.equals(_viewportDefinition.getAggregatorName(), viewportDefinition.getAggregatorName());
    }
  }

  private PushWebViewGrid getGridByName(String name) {
    if (_primitivesGrid != null) {
      if (_primitivesGrid.getName().equals(name)) {
        return _primitivesGrid;
      }
      PushWebViewGrid depGraphGrid = _primitivesGrid.getDepGraphGrid(name);
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
  /* package */ Viewport configureViewport(ViewportDefinition viewportDefinition, AnalyticsListener listener) {
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
    _primitivesGrid.setViewport(_viewportDefinition.getPrimitiveRows());
    List<WebGridCell> portfolioDepGraphCells = _viewportDefinition.getPortfolioDependencyGraphCells();
    List<WebGridCell> primitiveDepGraphCells = _viewportDefinition.getPrimitiveDependencyGraphCells();
    // view cycle access is required for dep graph access but performance is better if it is disabled
    _viewClient.setViewCycleAccessSupported(!portfolioDepGraphCells.isEmpty() || !primitiveDepGraphCells.isEmpty());
    _portfolioGrid.updateDepGraphCells(portfolioDepGraphCells);
    _primitivesGrid.updateDepGraphCells(primitiveDepGraphCells);
    updateResults();
  }

  private void updateResults() {
    synchronized (_lock) {
      if (!_viewClient.isResultAvailable()) {
        return;
      }
      ViewComputationResultModel resultModel = _viewClient.getLatestResult();
      long resultTimestamp = resultModel.getCalculationTime().toEpochMillisLong();
      Map<String, Object> portfolioResult = new HashMap<String, Object>();
      Map<String, Object> primitiveResult = new HashMap<String, Object>();

      for (ComputationTargetSpecification target : resultModel.getAllTargets()) {
        switch (target.getType()) {
          case PRIMITIVE:
            if (_primitivesGrid != null) {
              Map<String, Object> targetResult = _primitivesGrid.getTargetResult(target, resultModel.getTargetResult(target), resultTimestamp);
              if (targetResult != null) {
                Integer rowId = (Integer) targetResult.get("rowId");
                primitiveResult.put(Integer.toString(rowId), targetResult);
              }
            }
            break;
          case PORTFOLIO_NODE:
          case POSITION:
            if (_portfolioGrid != null) {
              Map<String, Object> targetResult = _portfolioGrid.getTargetResult(target, resultModel.getTargetResult(target), resultTimestamp);
              if (targetResult != null) {
                Integer rowId = (Integer) targetResult.get("rowId");
                portfolioResult.put(Integer.toString(rowId), targetResult);
              }
            }
        }
      }
      // TODO should these always be added or omitted if there are no dep graphs?
      if (_portfolioGrid != null) {
        portfolioResult.put("dependencyGraphs", _portfolioGrid.getDepGraphs(resultTimestamp));
      }
      if (_primitivesGrid != null) {
        primitiveResult.put("dependencyGraphs", _primitivesGrid.getDepGraphs(resultTimestamp));
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

  // TODO refactor this?
  /*public Pair<Instant, String> getGridContentsAsCsv(String gridName) {
    PushWebViewGrid grid = getGridByName(gridName);
    if (grid == null) {
      throw new DataNotFoundException("Unknown grid '" + gridName + "'");
    }
    ViewComputationResultModel latestResult = _viewClient.getLatestResult();
    if (latestResult == null) {
      return null;
    }
    String csv = grid.dumpContentsToCsv(latestResult);
    return Pair.of(latestResult.getValuationTime(), csv);
  }*/

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

  public UniqueId getBaseViewDefinitionId() {
    return _baseViewDefinitionId;
  }

  public String getAggregatorName() {
    return _viewportDefinition.getAggregatorName();
  }
}
