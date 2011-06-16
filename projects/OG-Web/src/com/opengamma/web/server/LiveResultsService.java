/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang.StringUtils;
import org.cometd.Bayeux;
import org.cometd.Client;
import org.cometd.ClientBayeuxListener;
import org.cometd.Message;
import org.cometd.server.BayeuxService;
import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.marketdatasnapshot.ManageableMarketDataSnapshot;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.conversion.ConversionMode;
import com.opengamma.web.server.conversion.ResultConverterCache;

/**
 * The core of the back-end to the web client, providing the implementation of the Bayeux protocol.
 */
public class LiveResultsService extends BayeuxService implements ClientBayeuxListener {

  private static final Logger s_logger = LoggerFactory.getLogger(LiveResultsService.class);

  private Map<String, WebView> _clientViews = new HashMap<String, WebView>();
  
  /**
   * The executor service used to call web clients back asynchronously.
   */
  private final ExecutorService _executorService;
    
  private final ViewProcessor _viewProcessor;
  private final MarketDataSnapshotMaster _snapshotMaster;
  private final UserPrincipal _user;
  private final ResultConverterCache _resultConverterCache;
  
  public LiveResultsService(final Bayeux bayeux, final ViewProcessor viewProcessor,
      final MarketDataSnapshotMaster snapshotMaster, final UserPrincipal user, final ExecutorService executorService, final FudgeContext fudgeContext) {
    super(bayeux, "processPortfolioRequest");
    ArgumentChecker.notNull(bayeux, "bayeux");
    ArgumentChecker.notNull(viewProcessor, "viewProcessor");
    ArgumentChecker.notNull(snapshotMaster, "snapshotMaster");
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notNull(executorService, "executorService");
    
    _viewProcessor = viewProcessor;
    _snapshotMaster = snapshotMaster;
    _user = user;
    _executorService = executorService;
    _resultConverterCache = new ResultConverterCache(fudgeContext);
    
    s_logger.info("Subscribing to services");
    subscribe("/service/initData", "processInitDataRequest");
    subscribe("/service/initialize", "processInitializeRequest");
    subscribe("/service/updates", "processUpdateRequest");
    subscribe("/service/updates/mode", "processUpdateModeRequest");
    subscribe("/service/updates/depgraph", "processDepGraphRequest");
    subscribe("/service/currentview/pause", "processPauseRequest");
    subscribe("/service/currentview/resume", "processResumeRequest");
    getBayeux().addListener(this);
    s_logger.info("Finished subscribing to services");
  }
  
  @Override
  public void clientAdded(Client client) {
    s_logger.debug("Client " + client.getId() + " connected");
  }
  
  @Override
  public void clientRemoved(Client client) {
    // Tidy up
    s_logger.debug("Client " + client.getId() + " disconnected");
    if (_clientViews.containsKey(client)) {
      WebView view = _clientViews.remove(client.getId());
      view.shutdown();
    }
  }
  
  public WebView getClientView(String clientId) {
    synchronized (_clientViews) {
      return _clientViews.get(clientId);
    }
  }

  private void initializeClientView(final Client remote, final String viewDefinitionName, final UserPrincipal user) {
    synchronized (_clientViews) {
      WebView webView = _clientViews.get(remote.getId());
      if (webView != null) {
        if (webView.getViewDefinitionName().equals(viewDefinitionName)) {
          // Already initialized
          webView.reconnected();
          return;
        }
        // Existing view is different - client is switching views
        webView.shutdown();
        _clientViews.remove(remote.getId());
      }
      
      ViewClient viewClient = getViewProcessor().createViewClient(user);
      try {
        webView = new WebView(getClient(), remote, viewClient, viewDefinitionName, user, getExecutorService(), getResultConverterCache());
      } catch (Exception e) {
        viewClient.shutdown();
        throw new OpenGammaRuntimeException("Error attaching client to view definition '" + viewDefinitionName + "'", e);
      }
      _clientViews.put(remote.getId(), webView);
    }
  }

  private UserPrincipal getUser(Client remote) {
    return _user;
  }
  
  private ExecutorService getExecutorService() {
    return _executorService;
  }

  private ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }
  
  private ResultConverterCache getResultConverterCache() {
    return _resultConverterCache;
  }
  
  public void processUpdateRequest(Client remote, Message message) {
    s_logger.info("Received portfolio data request from {}, getting client view...", remote);
    WebView webView = getClientView(remote.getId());
    if (webView == null) {
      // Disconnected client has come back to life
      return;
    }
    webView.triggerUpdate(message);
  }
  
  @SuppressWarnings("unchecked")
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
    }
    grid.setConversionMode(WebGridCell.of((int) jsRowId, (int) jsColId), mode);
  }
  
  @SuppressWarnings("unchecked")
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
  }

  public void processInitDataRequest(Client remote, Message message) {
    s_logger.info("processInitDataRequest");
    Map<String, Object> reply = new HashMap<String, Object>();
    
    List<String> availableViewNames = getViewNames();
    reply.put("viewNames", availableViewNames);
    
    Map<String, Map<String, String>> snapshotDetails = getSnapshotDetails();
    reply.put("snapshots", snapshotDetails);
    
    remote.deliver(getClient(), "/initData", reply, null);
  }

  private List<String> getViewNames() {
    Set<String> availableViewNames = _viewProcessor.getViewDefinitionRepository().getDefinitionNames();
    s_logger.debug("Available view names: " + availableViewNames);
    return new ArrayList<String>(availableViewNames);
  }

  private Map<String, Map<String, String>> getSnapshotDetails() {
    MarketDataSnapshotSearchRequest snapshotSearchRequest = new MarketDataSnapshotSearchRequest();
    snapshotSearchRequest.setIncludeData(false);
    MarketDataSnapshotSearchResult snapshotSearchResult = _snapshotMaster.search(snapshotSearchRequest);
    List<ManageableMarketDataSnapshot> snapshots = snapshotSearchResult.getMarketDataSnapshots();
    
    Map<String, Map<String, String>> snapshotsByBasisView = new HashMap<String, Map<String, String>>();
    for (ManageableMarketDataSnapshot snapshot : snapshots) {
      String basisViewName = snapshot.getBasisViewName() != null ? snapshot.getBasisViewName() : "unknown";
      Map<String, String> snapshotsForBasisView = snapshotsByBasisView.get(basisViewName);
      if (snapshotsForBasisView == null) {
        snapshotsForBasisView = new HashMap<String, String>();
        snapshotsByBasisView.put(basisViewName, snapshotsForBasisView);
      }
      snapshotsForBasisView.put(snapshot.getName(), snapshot.getUniqueId().toString());
    }
    return snapshotsByBasisView;
  }

  @SuppressWarnings("unchecked")
  public void processInitializeRequest(Client remote, Message message) {
    Map<String, Object> data = (Map<String, Object>) message.getData();
    String viewName = (String) data.get("viewName");
    String snapshotIdString = (String) data.get("snapshotId");
    UniqueIdentifier snapshotId = !StringUtils.isBlank(snapshotIdString) ? UniqueIdentifier.parse(snapshotIdString) : null;
    s_logger.info("Initializing view '{}' with snapshot '{}' for client '{}'", new Object[] {viewName, snapshotId, remote});
    initializeClientView(remote, viewName, getUser(remote));
  }
  
  public void processPauseRequest(Client remote, Message message) {
    WebView webView = getClientView(remote.getId());
    if (webView == null) {
      return;
    }
    webView.pause();
  }
  
  public void processResumeRequest(Client remote, Message message) {
    WebView webView = getClientView(remote.getId());
    if (webView == null) {
      return;
    }
    webView.resume();
  }
  
}
