/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.grid;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.financial.aggregation.AggregationFunction;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.AggregatedViewDefinitionManager;
import com.opengamma.web.server.conversion.ResultConverterCache;
import com.opengamma.web.server.push.AnalyticsListener;
import com.opengamma.web.server.push.NoOpAnalyticsListener;
import com.opengamma.web.server.push.Viewport;
import com.opengamma.web.server.push.ViewportDefinition;
import com.opengamma.web.server.push.ViewportManager;
import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Connects the REST interface to the engine.
 * TODO temporary name just to distinguish it from the similarly named class in the parent package
 */
/* package */ class PushLiveResultsService implements ViewportManager {

  private static final Logger s_logger = LoggerFactory.getLogger(PushLiveResultsService.class);

  /** Client's web view keyed on viewport ID */
  private final Map<String, PushWebView> _viewportIdToView = new HashMap<String, PushWebView>();
  private final ViewProcessor _viewProcessor;
  private final UserPrincipal _user;
  private final ResultConverterCache _resultConverterCache;
  private final Object _lock = new Object();
  private final AggregatedViewDefinitionManager _aggregatedViewDefinitionManager;

  public PushLiveResultsService(ViewProcessor viewProcessor,
                                PositionSource positionSource,
                                SecuritySource securitySource,
                                PortfolioMaster userPortfolioMaster,
                                PositionMaster userPositionMaster,
                                ManageableViewDefinitionRepository userViewDefinitionRepository,
                                UserPrincipal user,
                                FudgeContext fudgeContext,
                                List<AggregationFunction<?>> portfolioAggregators) {
    ArgumentChecker.notNull(viewProcessor, "viewProcessor");
    ArgumentChecker.notNull(user, "user");

    _viewProcessor = viewProcessor;
    _user = user;
    _resultConverterCache = new ResultConverterCache(fudgeContext);
    _aggregatedViewDefinitionManager = new AggregatedViewDefinitionManager(positionSource,
                                                                           securitySource,
                                                                           viewProcessor.getViewDefinitionRepository(),
                                                                           userViewDefinitionRepository,
                                                                           userPortfolioMaster,
                                                                           userPositionMaster,
                                                                           mapPortfolioAggregators(portfolioAggregators));
  }

  public void closeViewport(String viewportId) {
    s_logger.debug("Closing viewport with ID " + viewportId);
    PushWebView view = null;
    if (viewportId != null) {
      synchronized (_lock) {
        view = _viewportIdToView.remove(viewportId);
      }
    }
    if (view != null) {
      shutDownWebView(view);
    }
  }

  // TODO why is this here and not in the constructor of AggregatedViewDefinitionManager?
  private static Map<String, AggregationFunction<?>> mapPortfolioAggregators(List<AggregationFunction<?>> portfolioAggregators) {
    Map<String, AggregationFunction<?>> result = new HashMap<String, AggregationFunction<?>>();
    for (AggregationFunction<?> portfolioAggregator : portfolioAggregators) {
      result.put(portfolioAggregator.getName(), portfolioAggregator);
    }
    return result;
  }

  // TODO this leaks views at the moment - a timeout mechanism is needed for views with no client
  @Override
  public Viewport createViewport(String viewportId, ViewportDefinition viewportDefinition) {
    synchronized (_lock) {
      UniqueId baseViewDefinitionId = getViewDefinitionId(viewportDefinition.getViewDefinitionName());
      PushWebView webView;
      String aggregatorName = viewportDefinition.getAggregatorName();
      ViewClient viewClient = _viewProcessor.createViewClient(_user);
      try {
        UniqueId viewDefinitionId = _aggregatedViewDefinitionManager.getViewDefinitionId(baseViewDefinitionId, aggregatorName);
        webView = new PushWebView(viewClient, viewportDefinition, baseViewDefinitionId, viewDefinitionId, _resultConverterCache, new NoOpAnalyticsListener());
      } catch (Exception e) {
        viewClient.shutdown();
        throw new OpenGammaRuntimeException("Error attaching client to view definition '" + baseViewDefinitionId + "'", e);
      }
      _viewportIdToView.put(viewportId, webView);
      return webView;
    }
  }

  @Override
  public Viewport createViewport(String viewportId,
                                 String previousViewportId,
                                 ViewportDefinition viewportDefinition,
                                 AnalyticsListener listener) {
    synchronized (_lock) {
      UniqueId baseViewDefinitionId = getViewDefinitionId(viewportDefinition.getViewDefinitionName());
      // TODO only need the client ID so we can find the previous viewport. can't the client supply that instead?
      PushWebView webView;
      String aggregatorName = viewportDefinition.getAggregatorName();

      if (previousViewportId != null) {
        webView = _viewportIdToView.get(previousViewportId);
        // TODO is this relevant any more?
        if (webView != null) {
          if (webView.matches(baseViewDefinitionId, viewportDefinition)) {
            // Already initialized
            // TODO is there any possibility the WebView won't have a compiled view def at this point?
            return webView.configureViewport(viewportDefinition, listener);
          }
          // Existing view is different - client is switching views
          shutDownWebView(webView);
          _viewportIdToView.remove(previousViewportId);
        }
      }

      ViewClient viewClient = _viewProcessor.createViewClient(_user);
      try {
        UniqueId viewDefinitionId = _aggregatedViewDefinitionManager.getViewDefinitionId(baseViewDefinitionId, aggregatorName);
        webView = new PushWebView(viewClient, viewportDefinition, baseViewDefinitionId, viewDefinitionId, _resultConverterCache, listener);
      } catch (Exception e) {
        viewClient.shutdown();
        throw new OpenGammaRuntimeException("Error attaching client to view definition '" + baseViewDefinitionId + "'", e);
      }
      _viewportIdToView.put(viewportId, webView);
      return webView;
    }
  }

  private UniqueId getViewDefinitionId(String viewDefinitionName) {
    ViewDefinition view = _viewProcessor.getViewDefinitionRepository().getDefinition(viewDefinitionName);
    if (view == null) {
      throw new OpenGammaRuntimeException("Unable to find view definition with name " + viewDefinitionName);
    }
    return view.getUniqueId().toLatest();
  }

  private void shutDownWebView(PushWebView webView) {
    webView.shutdown();
    _aggregatedViewDefinitionManager.releaseViewDefinition(webView.getBaseViewDefinitionId(), webView.getAggregatorName());
  }

  @Override
  public Viewport getViewport(String viewportId) {
    synchronized (_lock) {
      PushWebView view = _viewportIdToView.get(viewportId);
      if (view == null) {
        throw new DataNotFoundException("Unable to find viewport for key: " + viewportId);
      }
      return view;
    }
  }
}
