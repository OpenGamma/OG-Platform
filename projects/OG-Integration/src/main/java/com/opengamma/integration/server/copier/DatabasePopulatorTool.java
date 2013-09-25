/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.server.copier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.config.FunctionConfigurationDefinition;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.view.compilation.PortfolioCompiler;
import com.opengamma.financial.portfolio.save.SavePortfolio;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.tool.config.ConfigLoader;
import com.opengamma.integration.tool.config.ConfigSaver;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesInfoSearchIterator;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.impl.MarketDataSnapshotSearchIterator;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.impl.PortfolioSearchIterator;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.impl.SecuritySearchIterator;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class DatabasePopulatorTool extends AbstractTool<ToolContext> {
  
  private static final Logger s_logger = LoggerFactory.getLogger(DatabasePopulatorTool.class);
  /**
   * Demo function configuration object name.
   */
  public static final String DEMO_FUNCTION = "DEMO_FUNCTIONS";
  /**
   * URL of opengamma server to copy data from
   */
  private final String _serverUrl;
  private final ExecutorService _executorService = Executors.newCachedThreadPool();
  
  public DatabasePopulatorTool(final String serverUrl) {
    ArgumentChecker.notNull(serverUrl, "serverUrl");
    _serverUrl = serverUrl;
  }

  @Override
  protected void doRun() throws Exception {
    ToolContext toolContext = getToolContext();
    loadSecurity(toolContext.getSecurityMaster());
    loadPortfolio(toolContext.getPortfolioMaster(), toolContext.getPositionMaster(), toolContext.getSecurityMaster(), toolContext.getSecuritySource());
    loadConfig(toolContext.getConfigMaster(), toolContext.getPortfolioMaster());
    loadHistoricalTimeSeries(toolContext.getHistoricalTimeSeriesMaster());
    loadSnapshot(toolContext.getMarketDataSnapshotMaster());
    loadFunctionConfiguration(toolContext.getConfigMaster());
  }
  
  protected void loadFunctionConfiguration(final ConfigMaster configMaster) {
    AbstractTool<ToolContext> remoteServerTool = new AbstractTool<ToolContext>() {

      @Override
      protected void doRun() throws Exception {
        FunctionConfigurationSource functionConfigSource = getToolContext().getFunctionConfigSource();
        FunctionConfigurationDefinition definition = FunctionConfigurationDefinition.of(DEMO_FUNCTION, functionConfigSource);
        final ConfigItem<FunctionConfigurationDefinition> config = ConfigItem.of(definition, DEMO_FUNCTION, FunctionConfigurationDefinition.class);
        ConfigMasterUtils.storeByName(getToolContext().getConfigMaster(), config);
      }
    };
    String[] args = {"-c", _serverUrl};
    remoteServerTool.initAndRun(args, ToolContext.class);
  }

  protected void loadSecurity(final SecurityMaster demoSecurityMaster) {
    s_logger.info("loading securities");
    AbstractTool<ToolContext> remoteServerTool = new AbstractTool<ToolContext>() {

      @Override
      protected void doRun() throws Exception {
        SecurityMaster remotesecurityMaster = getToolContext().getSecurityMaster();
        for (SecurityDocument securityDocument : SecuritySearchIterator.iterable(remotesecurityMaster, new SecuritySearchRequest())) {
          securityDocument.setUniqueId(null);
          demoSecurityMaster.add(securityDocument);
        }
      }
    };
    String[] args = {"-c", _serverUrl};
    remoteServerTool.initAndRun(args, ToolContext.class);
  }
  
  protected void loadPortfolio(final PortfolioMaster demoPortfolioMaster, final PositionMaster demoPositionMaster,
      final SecurityMaster demoSecurityMaster, final SecuritySource demoSecuritySource) {
    s_logger.info("loading portfolios");
    AbstractTool<ToolContext> remoteServerTool = new AbstractTool<ToolContext>() {

      @Override
      protected void doRun() throws Exception {
        PortfolioMaster remotePortfolioMaster = getToolContext().getPortfolioMaster();
        PositionSource remotePositionSource = getToolContext().getPositionSource();
        
        PortfolioSearchRequest request = new PortfolioSearchRequest();
        request.setDepth(0);
        for (PortfolioDocument portfolioDocument : PortfolioSearchIterator.iterable(remotePortfolioMaster, request)) {
          Portfolio portfolio = remotePositionSource.getPortfolio(portfolioDocument.getUniqueId(), VersionCorrection.LATEST);
          Portfolio resolvePortfolio = null;
          try {
            resolvePortfolio = PortfolioCompiler.resolvePortfolio(portfolio, _executorService, getToolContext().getSecuritySource());
          } catch (Exception ex) {
            s_logger.warn(String.format("Error resolving porfolio %s", portfolio.getName()), ex);
            continue;
          }
          SavePortfolio savePortfolio = new SavePortfolio(_executorService, demoPortfolioMaster, demoPositionMaster);
          savePortfolio.savePortfolio(resolvePortfolio, true);
        }
      }
    };
    String[] args = {"-c", _serverUrl };
    remoteServerTool.initAndRun(args, ToolContext.class);
  }
  
  protected void loadConfig(final ConfigMaster configMaster, final PortfolioMaster portfolioMaster) {
    s_logger.info("loading configs");
    AbstractTool<ToolContext> remoteServerTool = new AbstractTool<ToolContext>() {

      @Override
      protected void doRun() throws Exception {
        final ConfigMaster remoteConfigMaster = getToolContext().getConfigMaster();
        final PortfolioMaster remotePortfolioMaster = getToolContext().getPortfolioMaster();
        ConfigSaver configSaver = new ConfigSaver(remoteConfigMaster, remotePortfolioMaster, new ArrayList<String>(), new ArrayList<String>(), true, true);
        ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
        PrintStream outputStream = new PrintStream(byteArrayOutput);
        configSaver.saveConfigs(outputStream);
        ConfigLoader configLoader = new ConfigLoader(configMaster, portfolioMaster, true, true, true);
        configLoader.loadConfig(new ByteArrayInputStream(byteArrayOutput.toByteArray()));            
      }
    };
    String[] args = {"-c", _serverUrl };
    remoteServerTool.initAndRun(args, ToolContext.class);
  }
  
  protected void loadHistoricalTimeSeries(final HistoricalTimeSeriesMaster htsMaster) {
    s_logger.info("loading timeseries");
    AbstractTool<ToolContext> remoteServerTool = new AbstractTool<ToolContext>() {

      @Override
      protected void doRun() throws Exception {
        final HistoricalTimeSeriesMaster remoteHtsMaster = getToolContext().getHistoricalTimeSeriesMaster();
        HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
        for (HistoricalTimeSeriesInfoDocument historicalTimeSeriesInfoDocument : HistoricalTimeSeriesInfoSearchIterator.iterable(remoteHtsMaster, request)) {
          ObjectId timeSeriesObjectId = historicalTimeSeriesInfoDocument.getInfo().getTimeSeriesObjectId();
          ManageableHistoricalTimeSeries timeSeries = remoteHtsMaster.getTimeSeries(timeSeriesObjectId, VersionCorrection.LATEST);            
          HistoricalTimeSeriesInfoDocument addedDoc = htsMaster.add(historicalTimeSeriesInfoDocument);
          htsMaster.updateTimeSeriesDataPoints(addedDoc.getInfo().getTimeSeriesObjectId(), timeSeries.getTimeSeries());
        }
      }
    };
    String[] args = {"-c", _serverUrl };
    remoteServerTool.initAndRun(args, ToolContext.class);
  }

  protected void loadSnapshot(final MarketDataSnapshotMaster marketDataSnapshotMaster) {
    s_logger.info("loading market data snapshots");
    AbstractTool<ToolContext> remoteServerTool = new AbstractTool<ToolContext>() {

      @Override
      protected void doRun() throws Exception {
        MarketDataSnapshotMaster remoteSnapshotMaster = getToolContext().getMarketDataSnapshotMaster();
        MarketDataSnapshotSearchRequest request = new MarketDataSnapshotSearchRequest();
        for (MarketDataSnapshotDocument snapshotDocument : MarketDataSnapshotSearchIterator.iterable(remoteSnapshotMaster, request)) {
          marketDataSnapshotMaster.add(snapshotDocument);
        }
      }
    };
    String[] args = {"-c", _serverUrl };
    remoteServerTool.initAndRun(args, ToolContext.class);
  }

  /**
   * Gets the serverUrl.
   * @return the serverUrl
   */
  public String getServerUrl() {
    return _serverUrl;
  }
 
}
