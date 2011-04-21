/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

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
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.conversion.ConversionMode;
import com.opengamma.web.server.conversion.ResultConverterCache;

/**
 */
public class LiveResultsService extends BayeuxService implements ClientBayeuxListener {

  private static final Logger s_logger = LoggerFactory.getLogger(LiveResultsService.class);

  private Map<Client, WebView> _clientViews = new HashMap<Client, WebView>();
  
  /**
   * The executor service used to call web clients back asynchronously.
   */
  private final ExecutorService _executorService;
    
  private final ViewProcessor _viewProcessor;
  private final UserPrincipal _user;
  private final ResultConverterCache _resultConverterCache;
  
  public LiveResultsService(Bayeux bayeux, ViewProcessor viewProcessor, final UserPrincipal user, final ExecutorService executorService, final FudgeContext fudgeContext) {
    super(bayeux, "processPortfolioRequest");
    ArgumentChecker.notNull(bayeux, "bayeux");
    ArgumentChecker.notNull(viewProcessor, "viewProcessor");
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notNull(executorService, "executorService");
    
    _viewProcessor = viewProcessor;
    _user = user;
    _executorService = executorService;
    _resultConverterCache = new ResultConverterCache(fudgeContext);
    
    s_logger.info("Subscribing to services");
    subscribe("/service/views", "processViewsRequest");
    subscribe("/service/initialize", "processInitializeRequest");
    subscribe("/service/updates", "processUpdateRequest");
    subscribe("/service/updates/mode", "processUpdateModeRequest");
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
      WebView view = _clientViews.remove(client);
      view.shutdown();
    }
  }
  
  private WebView getClientView(Client remote) {
    synchronized (_clientViews) {
      return _clientViews.get(remote);
    }
  }

  private void initializeClientView(final Client remote, final String viewDefinitionName, final UserPrincipal user) {
    synchronized (_clientViews) {
      WebView webView = _clientViews.get(remote);
      if (webView != null) {
        if (webView.getViewDefinitionName().equals(viewDefinitionName)) {
          // Already initialized
          webView.reconnected();
          return;
        }
        // Existing view is different - client is switching views
        webView.shutdown();
        _clientViews.remove(remote);
      }
      
      ViewClient viewClient = getViewProcessor().createViewClient(user);
      try {
        webView = new WebView(getClient(), remote, viewClient, viewDefinitionName, user, getExecutorService(), getResultConverterCache());
      } catch (Exception e) {
        viewClient.shutdown();
        throw new OpenGammaRuntimeException("Error attaching client to view definition '" + viewDefinitionName + "'", e);
      }
      _clientViews.put(remote, webView);
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
    WebView webView = getClientView(remote);
    if (webView == null) {
      // Disconnected client has come back to life
      return;
    }
    webView.triggerUpdate(message);
  }
  
  @SuppressWarnings("unchecked")
  public void processUpdateModeRequest(Client remote, Message message) {
    WebView webView = getClientView(remote);
    if (webView == null) {
      return;
    }
    Map<String, Object> dataMap = (Map<String, Object>) message.getData();
    String gridName = (String) dataMap.get("gridName");
    long rowId = (Long) dataMap.get("rowId");
    long colId = (Long) dataMap.get("colId");
    ConversionMode mode = ConversionMode.valueOf((String) dataMap.get("mode"));
    webView.getGridByName(gridName).setConversionMode(WebGridCell.of(rowId, colId), mode);
  }

  public void processViewsRequest(Client remote, Message message) {
    s_logger.info("processViewsRequest");
    Set<String> availableViewNames = _viewProcessor.getViewDefinitionRepository().getDefinitionNames();
    s_logger.info("processViewsRequest:" + availableViewNames);
    Map<String, Object> reply = new HashMap<String, Object>();
    reply.put("availableViewNames", availableViewNames.toArray(new String[] {}));
    remote.deliver(getClient(), "/views", reply, null);
    s_logger.info("sent reply");
  }

  @SuppressWarnings("unchecked")
  public void processInitializeRequest(Client remote, Message message) {
    Map<String, Object> data = (Map<String, Object>) message.getData();
    String viewName = (String) data.get("viewName");
    s_logger.info("Initializing view '{}' for client '{}'", viewName, remote);
    initializeClientView(remote, viewName, getUser(remote));
  }
  
  public void processPauseRequest(Client remote, Message message) {
    WebView webView = getClientView(remote);
    if (webView == null) {
      return;
    }
    webView.pause();
  }
  
  public void processResumeRequest(Client remote, Message message) {
    WebView webView = getClientView(remote);
    if (webView == null) {
      return;
    }
    webView.resume();
  }
  
}
