/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.conversion.ResultConverterCache;
import com.opengamma.web.server.push.subscription.AnalyticsListener;
import com.opengamma.web.server.push.subscription.Viewport;
import com.opengamma.web.server.push.subscription.ViewportDefinition;
import com.opengamma.web.server.push.subscription.ViewportFactory;
import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Connects the REST interface to the engine.
 */
public class LiveResultsService implements ViewportFactory {

  private static final Logger s_logger = LoggerFactory.getLogger(LiveResultsService.class);

  private final Map<String, WebView> _clientViews = new HashMap<String, WebView>();
  private final ViewProcessor _viewProcessor;
  private final UserPrincipal _user;
  private final ResultConverterCache _resultConverterCache;

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
    synchronized (_clientViews) {

      if (_clientViews.containsKey(clientId)) {
        WebView view = _clientViews.remove(clientId);
        view.shutdown();
      }
    }
  }

  // used by the REST interface that gets analytics as CSV - will that be moved here?
  // TODO might be better to pass the call through rather than returning the WebView and calling that
  public WebView getClientView(String clientId) {
    synchronized (_clientViews) {
      return _clientViews.get(clientId);
    }
  }

  @Override
  public Viewport createViewport(String clientId, ViewportDefinition viewportDefinition, AnalyticsListener listener) {
    UniqueId snapshotId = viewportDefinition.getSnapshotId();
    String viewDefinitionName = viewportDefinition.getViewDefinitionName();
    synchronized (_clientViews) {
      WebView webView = _clientViews.get(clientId);
      // TODO is this relevant any more?
      if (webView != null) {
        if (webView.matches(viewDefinitionName, snapshotId)) {
          // Already initialized
          // this used to deliver the grid structure to the client
          // TODO is there any possibility the WebView won't have a compiled view def at this point?
          return webView.configureViewport(viewportDefinition, listener);
        }
        // Existing view is different - client is switching views
        webView.shutdown();
        _clientViews.remove(clientId);
      }
      ViewClient viewClient = _viewProcessor.createViewClient(_user);
      try {
        webView = new WebView(viewClient, viewDefinitionName, snapshotId, _resultConverterCache);
      } catch (Exception e) {
        viewClient.shutdown();
        throw new OpenGammaRuntimeException("Error attaching client to view definition '" + viewDefinitionName + "'", e);
      }
      _clientViews.put(clientId, webView);
      // TODO at this point the view def won't be compiled so the grids won't exist and the viewport can't be configured
      return webView.configureViewport(viewportDefinition, listener);
    }
  }

  // TODO rename / move logic into createViewport()
  // called when the client wants more data
  /*public void processUpdateRequest(Client remote, Message message) {
    s_logger.info("Received portfolio data request from {}, getting client view...", remote);
    webView.triggerUpdate(message);
    if (webView == null) {
      // Disconnected client has come back to life
      return;
    }
  }*/
  
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

  // TODO this logic needs to be moved to to view view initialisation
  /*@SuppressWarnings("unchecked")
  public void processDepGraphRequest(Client remote, Message message) {
    WebView webView = getClientView(remote.getId());
    if (webView == null) {
      return;
    }
    Map<String, Object> dataMap = (Map<String, Object>) message.getData();
    String gridName = (String) dataMap.get("gridName");
    long jsRowId = (Long) dataMap.get("rowId");
    long jsColId = (Long) dataMap.get("colId");
    boolean includeDepGraph = (Boolean) dataMap.get("includeDepGraph");
    webView.setIncludeDepGraph(gridName, WebGridCell.of((int) jsRowId, (int) jsColId), includeDepGraph);
  }*/

  // TODO this should go in a REST resource (if there isn't one already)
/*
  private List<String> getViewNames() {
    Set<String> availableViewNames = _viewProcessor.getViewDefinitionRepository().getDefinitionNames();
    s_logger.debug("Available view names: " + availableViewNames);
    return new ArrayList<String>(availableViewNames);
  }
*/

  // TODO this should go in a REST resource (if there isn't one already)
/*
  private Map<String, Map<String, String>> getSnapshotDetails() {
    MarketDataSnapshotSearchRequest snapshotSearchRequest = new MarketDataSnapshotSearchRequest();
    snapshotSearchRequest.setIncludeData(false);
    MarketDataSnapshotSearchResult snapshotSearchResult = _snapshotMaster.search(snapshotSearchRequest);
    List<ManageableMarketDataSnapshot> snapshots = snapshotSearchResult.getMarketDataSnapshots();
    
    Map<String, Map<String, String>> snapshotsByBasisView = new HashMap<String, Map<String, String>>();
    for (ManageableMarketDataSnapshot snapshot : snapshots) {
      if (snapshot.getUniqueId() == null) {
        s_logger.warn("Ignoring snapshot with null unique identifier {}", snapshot.getName());
        continue;
      }
      if (StringUtils.isBlank(snapshot.getName())) {
        s_logger.warn("Ignoring snapshot {} with no name", snapshot.getUniqueId());
        continue;
      }
      if (s_guidPattern.matcher(snapshot.getName()).find()) {
        s_logger.debug("Ignoring snapshot which appears to have an auto-generated name: {}", snapshot.getName());
        continue;
      }
      String basisViewName = snapshot.getBasisViewName() != null ? snapshot.getBasisViewName() : "unknown";
      Map<String, String> snapshotsForBasisView = snapshotsByBasisView.get(basisViewName);
      if (snapshotsForBasisView == null) {
        snapshotsForBasisView = new HashMap<String, String>();
        snapshotsByBasisView.put(basisViewName, snapshotsForBasisView);
      }
      snapshotsForBasisView.put(snapshot.getUniqueId().toString(), snapshot.getName());
    }
    return snapshotsByBasisView;
  }
*/

  // TODO this will be replaced by the creation of a new viewport
  /*@SuppressWarnings("unchecked")
  public void processChangeViewRequest(Client remote, Message message) {
    Map<String, Object> data = (Map<String, Object>) message.getData();
    String viewName = (String) data.get("viewName");
    String snapshotIdString = (String) data.get("snapshotId");
    UniqueId snapshotId = !StringUtils.isBlank(snapshotIdString) ? UniqueId.parse(snapshotIdString) : null;
    s_logger.info("Initializing view '{}' with snapshot '{}' for client '{}'", new Object[] {viewName, snapshotId, remote});
    initializeClientView(remote, viewName, snapshotId, getUser(remote));
  }*/

  // TODO this is on Viewport. remove?
  /*public void processPauseRequest(Client remote, Message message) {
    WebView webView = getClientView(remote.getId());
    if (webView == null) {
      return;
    }
    webView.pause();
  }*/
  
  // TODO this is on Viewport. remove?
  /*public void processResumeRequest(Client remote, Message message) {
    WebView webView = getClientView(remote.getId());
    if (webView == null) {
      return;
    }
    webView.resume();
  }*/
}
