/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

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
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.marketdata.LiveMarketDataSourceRegistry;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.financial.aggregation.AggregationFunction;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.conversion.ConversionMode;
import com.opengamma.web.server.conversion.ResultConverterCache;

/**
 * The core of the back-end to the web client, providing the implementation of the Bayeux protocol.
 */
public class LiveResultsService extends BayeuxService implements ClientBayeuxListener {

  private static final String DEFAULT_LIVE_MARKET_DATA_NAME = "Automatic";
  
  private static final Logger s_logger = LoggerFactory.getLogger(LiveResultsService.class);
  private static final Pattern s_guidPattern = Pattern.compile("(\\{?([0-9a-fA-F]){8}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){4}-([0-9a-fA-F]){12}\\}?)");
  
  private final Map<String, WebView> _clientViews = new HashMap<String, WebView>();
  
  /**
   * The executor service used to call web clients back asynchronously.
   */
  private final ExecutorService _executorService;
    
  private final ViewProcessor _viewProcessor;
  private final MarketDataSnapshotMaster _snapshotMaster;
  private final UserPrincipal _user;
  private final ResultConverterCache _resultConverterCache;
  private final LiveMarketDataSourceRegistry _liveMarketDataSourceRegistry;
  private final AggregatedViewDefinitionManager _aggregatedViewDefinitionManager;
  
  public LiveResultsService(final Bayeux bayeux, final ViewProcessor viewProcessor,
      final PositionSource positionSource, final SecuritySource securitySource,
      final PortfolioMaster userPortfolioMaster, final PositionMaster userPositionMaster,
      final ManageableViewDefinitionRepository userViewDefinitionRepository,
      final MarketDataSnapshotMaster snapshotMaster, final UserPrincipal user, final ExecutorService executorService,
      final FudgeContext fudgeContext, final LiveMarketDataSourceRegistry liveMarketDataSourceRegistry,
      final List<AggregationFunction<?>> portfolioAggregators) {
    super(bayeux, "processPortfolioRequest");
    ArgumentChecker.notNull(bayeux, "bayeux");
    ArgumentChecker.notNull(viewProcessor, "viewProcessor");
    ArgumentChecker.notNull(positionSource, "positionSource");
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(userPortfolioMaster, "userPortfolioMaster");
    ArgumentChecker.notNull(userPositionMaster, "userPositionMaster");
    ArgumentChecker.notNull(userViewDefinitionRepository, "userViewDefinitionRepository");
    ArgumentChecker.notNull(snapshotMaster, "snapshotMaster");
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notNull(executorService, "executorService");
    ArgumentChecker.notNull(liveMarketDataSourceRegistry, "liveMarketDataSourceRegistry");
    ArgumentChecker.notNull(portfolioAggregators, "portfolioAggregators");
    
    _viewProcessor = viewProcessor;
    _snapshotMaster = snapshotMaster;
    _user = user;
    _executorService = executorService;
    _resultConverterCache = new ResultConverterCache(fudgeContext);
    _liveMarketDataSourceRegistry = liveMarketDataSourceRegistry;
    _aggregatedViewDefinitionManager = new AggregatedViewDefinitionManager(positionSource, securitySource,
        viewProcessor.getViewDefinitionRepository(), userViewDefinitionRepository, userPortfolioMaster, userPositionMaster,
        mapPortfolioAggregators(portfolioAggregators));
    
    viewProcessor.getViewDefinitionRepository().changeManager().addChangeListener(new ChangeListener() {

      @Override
      public void entityChanged(ChangeEvent event) {
        sendInitData(false);
      }
      
    });
    
    s_logger.info("Subscribing to services");
    subscribe("/service/getInitData", "processInitDataRequest");
    subscribe("/service/changeView", "processChangeViewRequest");
    subscribe("/service/updates", "processUpdateRequest");
    subscribe("/service/updates/mode", "processUpdateModeRequest");
    subscribe("/service/updates/depgraph", "processDepGraphRequest");
    subscribe("/service/currentview/pause", "processPauseRequest");
    subscribe("/service/currentview/resume", "processResumeRequest");
    getBayeux().addListener(this);
    s_logger.info("Finished subscribing to services");
  }
  
  private Map<String, AggregationFunction<?>> mapPortfolioAggregators(List<AggregationFunction<?>> portfolioAggregators) {
    Map<String, AggregationFunction<?>> result = new HashMap<String, AggregationFunction<?>>();
    for (AggregationFunction<?> portfolioAggregator : portfolioAggregators) {
      result.put(portfolioAggregator.getName(), portfolioAggregator);
    }
    return result;
  }
  
  @Override
  public void clientAdded(Client client) {
    s_logger.debug("Client " + client.getId() + " connected");
  }
  
  @Override
  public void clientRemoved(Client client) {
    // Tidy up
    s_logger.debug("Client " + client.getId() + " disconnected");
    if (_clientViews.containsKey(client.getId())) {
      WebView view = _clientViews.remove(client.getId());
      shutDownWebView(view);
    }
  }
  
  public WebView getClientView(String clientId) {
    synchronized (_clientViews) {
      return _clientViews.get(clientId);
    }
  }

  private void initializeClientView(final Client remote, final UniqueId baseViewDefinitionId, final String aggregatorName, final ViewExecutionOptions executionOptions, final UserPrincipal user) {
    synchronized (_clientViews) {
      WebView webView = _clientViews.get(remote.getId());
      
      if (webView != null) {
        if (webView.matches(baseViewDefinitionId, aggregatorName, executionOptions)) {
          // Already initialized
          webView.reconnected();
          return;
        }
        // Existing view is different - client is switching views
        shutDownWebView(webView);
        _clientViews.remove(remote.getId());
      }
      
      ViewClient viewClient = getViewProcessor().createViewClient(user);
      try {
        UniqueId viewDefinitionId = _aggregatedViewDefinitionManager.getViewDefinitionId(baseViewDefinitionId, aggregatorName);
        webView = new WebView(getClient(), remote, viewClient, baseViewDefinitionId, aggregatorName, viewDefinitionId,
            executionOptions, user, getExecutorService(), getResultConverterCache());
      } catch (Exception e) {
        _aggregatedViewDefinitionManager.releaseViewDefinition(baseViewDefinitionId, aggregatorName);
        viewClient.shutdown();
        throw new OpenGammaRuntimeException("Error attaching client to view definition '" + baseViewDefinitionId + "'", e);
      }
      _clientViews.put(remote.getId(), webView);
    }
  }

  private void shutDownWebView(WebView webView) {
    webView.shutdown();
    _aggregatedViewDefinitionManager.releaseViewDefinition(webView.getBaseViewDefinitionId(), webView.getAggregatorName());
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
    sendInitData(true);
  }
  
  private void sendInitData(boolean includeSnapshots) {
    Map<String, Object> reply = new HashMap<String, Object>();
    
    List<String> availableViewNames = getViewNames();
    reply.put("viewNames", availableViewNames);
    
    List<String> aggregatorNames = getAggregatorNames();
    reply.put("aggregatorNames", aggregatorNames);
    
    if (includeSnapshots) {
      List<String> liveMarketDataSourceDetails = getLiveMarketDataSourceDetails();
      reply.put("liveSources", liveMarketDataSourceDetails);
      Map<String, Map<String, String>> snapshotDetails = getSnapshotDetails();
      reply.put("snapshots", snapshotDetails);
    }
    
    getBayeux().getChannel("/initData", true).publish(getClient(), reply, null);
  }

  private List<String> getViewNames() {
    List<String> result = new ArrayList<String>();
    Map<UniqueId, String> availableViewEntries = _viewProcessor.getViewDefinitionRepository().getDefinitionEntries();
    s_logger.debug("Available view entries: " + availableViewEntries);
    for (Map.Entry<UniqueId, String> entry : availableViewEntries.entrySet()) {
      result.add(entry.getValue());
    }
    Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
    return result;
  }
  
  private List<String> getAggregatorNames() {
    List<String> result = new ArrayList<String>();
    result.addAll(_aggregatedViewDefinitionManager.getAggregatorNames());
    Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
    return result;
  }

  private List<String> getLiveMarketDataSourceDetails() {
    Collection<String> allDataSources = _liveMarketDataSourceRegistry.getDataSources();
    List<String> filteredDataSources = new ArrayList<String>();
    filteredDataSources.add(DEFAULT_LIVE_MARKET_DATA_NAME);
    for (String dataSource : allDataSources) {
      if (StringUtils.isBlank(dataSource)) {
        continue;
      }
      filteredDataSources.add(dataSource);
    }
    return filteredDataSources;
  }

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

  @SuppressWarnings("unchecked")
  public void processChangeViewRequest(Client remote, Message message) {
    Map<String, Object> data = (Map<String, Object>) message.getData();
    
    String viewDefinitionName = (String) data.get("viewName");
    UniqueId baseViewDefinitionId = getViewDefinitionId(viewDefinitionName);
    String aggregatorName = (String) data.get("aggregatorName");
    
    String marketDataType = (String) data.get("marketDataType");
    MarketDataSpecification marketDataSpec;
    EnumSet<ViewExecutionFlags> flags;
    if ("snapshot".equals(marketDataType)) {
      String snapshotIdString = (String) data.get("snapshotId");
      UniqueId snapshotId = !StringUtils.isBlank(snapshotIdString) ? UniqueId.parse(snapshotIdString) : null;
      marketDataSpec = MarketData.user(snapshotId.toLatest());
      flags = ExecutionFlags.none().triggerOnMarketData().get();
    } else if ("live".equals(marketDataType)) {
      String liveMarketDataProvider = (String) data.get("provider");
      if (StringUtils.isBlank(liveMarketDataProvider) || DEFAULT_LIVE_MARKET_DATA_NAME.equals(liveMarketDataProvider)) {
        marketDataSpec = MarketData.live();
      } else {
        marketDataSpec = MarketData.live(liveMarketDataProvider);
      }
      flags = ExecutionFlags.triggersEnabled().get();
    } else {
      throw new OpenGammaRuntimeException("Unknown market data type: " + marketDataType);
    }
    ViewExecutionOptions executionOptions = ExecutionOptions.infinite(marketDataSpec, flags);
    s_logger.info("Initializing view '{}', aggregated by '{}' with execution options '{}' for client '{}'", new Object[] {viewDefinitionName, aggregatorName, executionOptions, remote});
    initializeClientView(remote, baseViewDefinitionId, aggregatorName, executionOptions, getUser(remote));
  }
  
  private UniqueId getViewDefinitionId(String viewDefinitionName) {
    ViewDefinition view = _viewProcessor.getViewDefinitionRepository().getDefinition(viewDefinitionName);
    return view.getUniqueId().toLatest();
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
