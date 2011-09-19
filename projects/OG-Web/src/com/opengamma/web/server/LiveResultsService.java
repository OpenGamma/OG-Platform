/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.conversion.ResultConverterCache;
import com.opengamma.web.server.push.AnalyticsListener;
import com.opengamma.web.server.push.Viewport;
import com.opengamma.web.server.push.ViewportDefinition;
import com.opengamma.web.server.push.ViewportFactory;
import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Connects the REST interface to the engine.
 * TODO REST interface for live market data source details - formerly pushlished over Cometd, see getLiveMarketDataSourceDetails from old versions of this file
 */
public class LiveResultsService implements ViewportFactory {

  private static final Logger s_logger = LoggerFactory.getLogger(LiveResultsService.class);

  private final Map<String, WebView> _clientViews = new HashMap<String, WebView>();
  private final Map<String, String> _clientIdToViewportKey = new HashMap<String, String>();
  private final ViewProcessor _viewProcessor;
  private final UserPrincipal _user;
  private final ResultConverterCache _resultConverterCache;
  private final Object _lock = new Object();

  public LiveResultsService(ViewProcessor viewProcessor, UserPrincipal user, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(viewProcessor, "viewProcessor");
    ArgumentChecker.notNull(user, "user");

    _viewProcessor = viewProcessor;
    _user = user;
    _resultConverterCache = new ResultConverterCache(fudgeContext);
  }
  
  // TODO this needs to be called from the update manager when the client disconnects
  public void clientDisconnected(String clientId) {
    s_logger.debug("Client " + clientId + " disconnected");
    WebView view = null;
    synchronized (_lock) {
      String viewportKey = _clientIdToViewportKey.remove(clientId);
      if (viewportKey != null) {
        view = _clientViews.remove(clientId);
      }
    }
    if (view != null) {
      view.shutdown();
    }
  }

  // used by the REST interface that gets analytics as CSV - will that be moved here?
  // TODO might be better to pass the call through rather than returning the WebView and calling that
  public WebView getClientView(String clientId) {
    synchronized (_lock) {
      return _clientViews.get(clientId);
    }
  }

  @Override
  public Viewport createViewport(String clientId, String viewportKey, ViewportDefinition viewportDefinition, AnalyticsListener listener) {
    String viewDefinitionName = viewportDefinition.getViewDefinitionName();
    synchronized (_lock) {
      String currentKey = _clientIdToViewportKey.remove(clientId);
      WebView webView = null;
      if (currentKey != null) {
        webView = _clientViews.get(currentKey);
      }
      // TODO is this relevant any more?
      if (webView != null) {
        // TODO execution options need to be in the viewport def
        if (webView.matches(viewDefinitionName, viewportDefinition.getExecutionOptions())) {
          // Already initialized
          // this used to deliver the grid structure to the client
          // TODO is there any possibility the WebView won't have a compiled view def at this point?
          return webView.configureViewport(viewportDefinition, listener, viewportKey);
        }
        // Existing view is different - client is switching views
        webView.shutdown();
        _clientViews.remove(viewportKey);
      }
      ViewClient viewClient = _viewProcessor.createViewClient(_user);
      try {
        webView = new WebView(viewClient, viewDefinitionName, _resultConverterCache, viewportDefinition, listener);
      } catch (Exception e) {
        viewClient.shutdown();
        throw new OpenGammaRuntimeException("Error attaching client to view definition '" + viewDefinitionName + "'", e);
      }
      _clientViews.put(clientId, webView);
      _clientIdToViewportKey.put(clientId, viewportKey);
      return webView;
    }
  }

  @Override
  public Viewport getViewport(String viewportKey) {
    synchronized (_lock) {
      WebView view = _clientViews.get(viewportKey);
      if (view == null) {
        throw new DataNotFoundException("Unable to find viewport for key: " + viewportKey);
      }
      return view;
    }
  }

  /*@SuppressWarnings("unchecked")
  public void processUpdateModeRequest(Client remote, Message message) {
    WebView webView = getClientView(remote.getId());
    if (webView == null) {
      return;
    }
    Map<String, Object> dataMap = (Map<String, Object>) message.getData();
    String gridName = (String) dataMap.get("gridName");
    long jsRowId = (Long) dataMap.get("rowId");
    long jsColId = (Long) dataMap.get("colId");
    ConversionMode mode = ConversionMode.valueOf((String) dataMap.get("mode"));
    WebViewGrid grid = webView.getGridByName(gridName);
    if (grid == null) {
      s_logger.warn("Request to change update mode for cell in unknown grid '{}'", gridName);
    } else {
      grid.setConversionMode(WebGridCell.of((int) jsRowId, (int) jsColId), mode);
    }
  }*/
}
