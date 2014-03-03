/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentServer;
import com.opengamma.component.rest.RemoteComponentServer;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.RemoteConfigSource;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.exchange.impl.RemoteExchangeSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.impl.RemoteHistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.impl.RemoteHolidaySource;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.legalentity.impl.RemoteLegalEntitySource;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.impl.RemoteMarketDataSnapshotSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.RemotePositionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.impl.RemoteRegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.impl.RemoteSecuritySource;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.helper.AvailableOutputsProvider;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.rest.RemoteInterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.analytics.ircurve.rest.RemoteInterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.rest.RemoteInterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.currency.CurrencyMatrixSource;
import com.opengamma.financial.currency.rest.RemoteCurrencyMatrixSource;
import com.opengamma.financial.function.rest.RemoteFunctionConfigurationSource;
import com.opengamma.financial.security.RemoteFinancialSecuritySource;
import com.opengamma.financial.view.rest.RemoteAvailableOutputsProvider;
import com.opengamma.financial.view.rest.RemoteViewProcessor;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.RemoteConfigMaster;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.impl.RemoteConventionMaster;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.impl.RemoteExchangeMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoader;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.impl.RemoteHistoricalTimeSeriesLoader;
import com.opengamma.master.historicaltimeseries.impl.RemoteHistoricalTimeSeriesMaster;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.impl.RemoteHolidayMaster;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.impl.RemoteLegalEntityMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.impl.RemoteMarketDataSnapshotMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.impl.RemoteRegionMaster;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.RemoteSecurityLoader;
import com.opengamma.masterdb.portfolio.RemoteDbPortfolioMaster;
import com.opengamma.masterdb.position.RemoteDbPositionMaster;
import com.opengamma.masterdb.security.RemoteDbSecurityMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;

/**
 * Constructs components exposed by a remote component server.
 */
public class RemoteComponentFactory {

  /**
   * The base URI.
   */
  private final URI _baseUri;
  /**
   * The component server.
   */
  private final ComponentServer _componentServer;

  /**
   * Constructs an instance.
   * 
   * @param componentServerUri  the URI of the remote component server, not null
   */
  public RemoteComponentFactory(String componentServerUri) {
    this(URI.create(componentServerUri));
  }

  /**
   * Constructs an instance.
   * 
   * @param componentServerUri  the URI of the remote component server, not null
   */
  public RemoteComponentFactory(URI componentServerUri) {
    ArgumentChecker.notNull(componentServerUri, "componentServerUri");
    RemoteComponentServer remoteComponentServer = new RemoteComponentServer(componentServerUri);
    _baseUri = componentServerUri;
    _componentServer = remoteComponentServer.getComponentServer();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the base URI.
   * 
   * @return the base URI, not null
   */
  public URI getBaseUri() {
    return _baseUri;
  }

  private ComponentInfo getTopLevelComponent(List<String> preferenceList, Class<?> type) {
    if (preferenceList != null) {
      for (String preference : preferenceList) {
        try {
          ComponentInfo componentInfo = getComponentServer().getComponentInfo(type, preference);
          if (componentInfo != null) {
            return componentInfo;
          }
        } catch (IllegalArgumentException iae) {
          // do nothing and try the next one.
        }
      }
    }
    List<ComponentInfo> componentInfos = getComponentServer().getComponentInfos();
    return componentInfos.size() == 0 ? null : componentInfos.get(0);
  }

  //-------------------------------------------------------------------------
  public RemoteViewProcessor getViewProcessor(String vpId) {
    ComponentInfo info = getComponentServer().getComponentInfo(ViewProcessor.class, "main");
    URI uri = info.getUri();
    JmsConnector jmsConnector = getJmsConnector(info);
    return new RemoteViewProcessor(uri, jmsConnector, Executors.newSingleThreadScheduledExecutor());
  }

  public List<RemoteViewProcessor> getViewProcessors() {
    List<RemoteViewProcessor> result = new ArrayList<RemoteViewProcessor>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(ViewProcessor.class)) {
      URI uri = info.getUri();
      JmsConnector jmsConnector = getJmsConnector(info);
      RemoteViewProcessor vp = new RemoteViewProcessor(uri, jmsConnector, Executors.newSingleThreadScheduledExecutor());
      result.add(vp);
    }
    return result;
  }

  //-------------------------------------------------------------------------
  // Configs
  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public ConfigMaster getConfigMaster(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, ConfigMaster.class).getUri();
    return new RemoteConfigMaster(uri);
  }

  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public ConfigMaster getConfigMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(ConfigMaster.class, name).getUri();
    return new RemoteConfigMaster(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, ConfigMaster> getConfigMasters() {
    Map<String, ConfigMaster> result = new LinkedHashMap<String, ConfigMaster>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(ConfigMaster.class)) {
      result.put(info.getClassifier(), new RemoteConfigMaster(info.getUri()));
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching master available
   */  
  public ConfigSource getConfigSource(final List<String> preferredClassifiers) {
    ComponentInfo componentInfo = getTopLevelComponent(preferredClassifiers, ConfigSource.class);
    return new RemoteConfigSource(componentInfo.getUri());
  }

  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public ConfigSource getConfigSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(ConfigSource.class, name).getUri();
    return new RemoteConfigSource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, ConfigSource> getConfigSources() {
    Map<String, ConfigSource> result = new LinkedHashMap<String, ConfigSource>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(ConfigSource.class)) {
      result.put(info.getClassifier(), new RemoteConfigSource(info.getUri()));
    }
    return result;    
  }

  //-------------------------------------------------------------------------
  // Portfolios
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public PortfolioMaster getPortfolioMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(PortfolioMaster.class, name).getUri();
    return new RemoteDbPortfolioMaster(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public PortfolioMaster getPortfolioMaster(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, PortfolioMaster.class).getUri();
    return new RemoteDbPortfolioMaster(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, PortfolioMaster> getPortfolioMasters() {
    Map<String, PortfolioMaster> result = new LinkedHashMap<String, PortfolioMaster>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(PortfolioMaster.class)) {
      result.put(info.getClassifier(), new RemoteDbPortfolioMaster(info.getUri()));
    }
    return result;    
  }

  //-------------------------------------------------------------------------
  // Positions
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public PositionMaster getPositionMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(PositionMaster.class, name).getUri();
    return new RemoteDbPositionMaster(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public PositionMaster getPositionMaster(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, PositionMaster.class).getUri();
    return new RemoteDbPositionMaster(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, PositionMaster> getPositionMasters() {
    Map<String, PositionMaster> result = new LinkedHashMap<String, PositionMaster>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(PositionMaster.class)) {
      result.put(info.getClassifier(), new RemoteDbPositionMaster(info.getUri()));
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public PositionSource getPositionSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(PositionSource.class, name).getUri();
    return new RemotePositionSource(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public PositionSource getPositionSource(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, PositionSource.class).getUri();
    return new RemotePositionSource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, PositionSource> getPositionSources() {
    Map<String, PositionSource> result = new LinkedHashMap<String, PositionSource>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(PositionSource.class)) {
      result.put(info.getClassifier(), new RemotePositionSource(info.getUri()));
    }
    return result;    
  }

  //-------------------------------------------------------------------------
  // Securities
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public SecuritySource getSecuritySource(final String name) {
    URI uri = getComponentServer().getComponentInfo(SecuritySource.class, name).getUri();
    return new RemoteFinancialSecuritySource(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public SecuritySource getSecuritySource(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, SecuritySource.class).getUri();
    return new RemoteSecuritySource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, SecuritySource> getSecuritySources() {
    Map<String, SecuritySource> result = new LinkedHashMap<String, SecuritySource>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(SecuritySource.class)) {
      result.put(info.getClassifier(), new RemoteSecuritySource(info.getUri()));
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public SecurityMaster getSecurityMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(SecurityMaster.class, name).getUri();
    return new RemoteDbSecurityMaster(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public SecurityMaster getSecurityMaster(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, SecurityMaster.class).getUri();
    return new RemoteDbSecurityMaster(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, SecurityMaster> getSecurityMasters() {
    Map<String, SecurityMaster> result = new LinkedHashMap<String, SecurityMaster>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(SecurityMaster.class)) {
      result.put(info.getClassifier(), new RemoteDbSecurityMaster(info.getUri()));
    }
    return result;
  }

  //-------------------------------------------------------------------------
  // Conventions
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public ConventionMaster getConventionMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(ConventionMaster.class, name).getUri();
    return new RemoteConventionMaster(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public ConventionMaster getConventionMaster(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, ConventionMaster.class).getUri();
    return new RemoteConventionMaster(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, ConventionMaster> getConventionMasters() {
    Map<String, ConventionMaster> result = new LinkedHashMap<String, ConventionMaster>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(ConventionMaster.class)) {
      result.put(info.getClassifier(), new RemoteConventionMaster(info.getUri()));
    }
    return result;
  }

  //-------------------------------------------------------------------------
  // Organizations/Obligors
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public LegalEntitySource getLegalEntitySource(final String name) {
    URI uri = getComponentServer().getComponentInfo(LegalEntitySource.class, name).getUri();
    return new RemoteLegalEntitySource(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public LegalEntitySource getLegalEntitySource(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, LegalEntitySource.class).getUri();
    return new RemoteLegalEntitySource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, LegalEntitySource> getLegalEntitySources() {
    Map<String, LegalEntitySource> result = new LinkedHashMap<>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(LegalEntitySource.class)) {
      result.put(info.getClassifier(), new RemoteLegalEntitySource(info.getUri()));
    }
    return result;
  }
  //-------------------------------------------------------------------------

  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public LegalEntityMaster getLegalEntityMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(LegalEntityMaster.class, name).getUri();
    return new RemoteLegalEntityMaster(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public LegalEntityMaster getLegalEntityMaster(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, LegalEntityMaster.class).getUri();
    return new RemoteLegalEntityMaster(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, LegalEntityMaster> getLegalEntityMasters() {
    Map<String, LegalEntityMaster> result = new LinkedHashMap<>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(LegalEntityMaster.class)) {
      result.put(info.getClassifier(), new RemoteLegalEntityMaster(info.getUri()));
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public SecurityLoader getSecurityLoader(final String name) {
    URI uri = getComponentServer().getComponentInfo(SecurityLoader.class, name).getUri();
    return new RemoteSecurityLoader(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public SecurityLoader getSecurityLoader(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, SecurityLoader.class).getUri();
    return new RemoteSecurityLoader(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, SecurityLoader> getSecurityLoaders() {
    Map<String, SecurityLoader> result = new LinkedHashMap<String, SecurityLoader>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(SecurityLoader.class)) {
      result.put(info.getClassifier(), new RemoteSecurityLoader(info.getUri()));
    }
    return result;
  }

  //-------------------------------------------------------------------------
  // Market Data Snapshots
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public MarketDataSnapshotMaster getMarketDataSnapshotMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(MarketDataSnapshotMaster.class, name).getUri();
    return new RemoteMarketDataSnapshotMaster(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public MarketDataSnapshotMaster getMarketDataSnapshotMaster(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, MarketDataSnapshotMaster.class).getUri();
    return new RemoteMarketDataSnapshotMaster(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, MarketDataSnapshotMaster> getMarketDataSnapshotMasters() {
    Map<String, MarketDataSnapshotMaster> result = new LinkedHashMap<String, MarketDataSnapshotMaster>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(MarketDataSnapshotMaster.class)) {
      result.put(info.getClassifier(), new RemoteMarketDataSnapshotMaster(info.getUri()));
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public MarketDataSnapshotSource getMarketDataSnapshotSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(MarketDataSnapshotSource.class, name).getUri();
    return new RemoteMarketDataSnapshotSource(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public MarketDataSnapshotSource getMarketDataSnapshotSource(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, MarketDataSnapshotSource.class).getUri();
    return new RemoteMarketDataSnapshotSource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, MarketDataSnapshotSource> getMarketDataSnapshotSources() {
    Map<String, MarketDataSnapshotSource> result = new LinkedHashMap<String, MarketDataSnapshotSource>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(MarketDataSnapshotSource.class)) {
      result.put(info.getClassifier(), new RemoteMarketDataSnapshotSource(info.getUri()));
    }
    return result;
  }

  //-------------------------------------------------------------------------
  // Historical Time Series
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(HistoricalTimeSeriesSource.class, name).getUri();
    return new RemoteHistoricalTimeSeriesSource(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, HistoricalTimeSeriesSource.class).getUri();
    return new RemoteHistoricalTimeSeriesSource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, HistoricalTimeSeriesSource> getHistoricalTimeSeriesSources() {
    Map<String, HistoricalTimeSeriesSource> result = new LinkedHashMap<String, HistoricalTimeSeriesSource>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(HistoricalTimeSeriesSource.class)) {
      result.put(info.getClassifier(), new RemoteHistoricalTimeSeriesSource(info.getUri()));
    }
    return result;    
  }

  //-------------------------------------------------------------------------
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public HistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(HistoricalTimeSeriesMaster.class, name).getUri();
    return new RemoteHistoricalTimeSeriesMaster(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public HistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, HistoricalTimeSeriesMaster.class).getUri();
    return new RemoteHistoricalTimeSeriesMaster(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, HistoricalTimeSeriesMaster> getHistoricalTimeSeriesMasters() {
    Map<String, HistoricalTimeSeriesMaster> result = new LinkedHashMap<String, HistoricalTimeSeriesMaster>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(HistoricalTimeSeriesMaster.class)) {
      result.put(info.getClassifier(), new RemoteHistoricalTimeSeriesMaster(info.getUri()));
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public HistoricalTimeSeriesLoader getHistoricalTimeSeriesLoader(final String name) {
    URI uri = getComponentServer().getComponentInfo(HistoricalTimeSeriesLoader.class, name).getUri();
    return new RemoteHistoricalTimeSeriesLoader(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public HistoricalTimeSeriesLoader getHistoricalTimeSeriesLoader(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, HistoricalTimeSeriesLoader.class).getUri();
    return new RemoteHistoricalTimeSeriesLoader(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, HistoricalTimeSeriesLoader> getHistoricalTimeSeriesLoaders() {
    Map<String, HistoricalTimeSeriesLoader> result = new LinkedHashMap<String, HistoricalTimeSeriesLoader>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(HistoricalTimeSeriesLoader.class)) {
      result.put(info.getClassifier(), new RemoteHistoricalTimeSeriesLoader(info.getUri()));
    }
    return result;
  }

  //-------------------------------------------------------------------------
  // Currency Matrices
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public CurrencyMatrixSource getCurrencyMatrixSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(CurrencyMatrixSource.class, name).getUri();
    return new RemoteCurrencyMatrixSource(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public CurrencyMatrixSource getCurrencyMatrixSource(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, CurrencyMatrixSource.class).getUri();
    return new RemoteCurrencyMatrixSource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, CurrencyMatrixSource> getCurrencyMatrixSources() {
    Map<String, CurrencyMatrixSource> result = new LinkedHashMap<String, CurrencyMatrixSource>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(CurrencyMatrixSource.class)) {
      result.put(info.getClassifier(), new RemoteCurrencyMatrixSource(info.getUri()));
    }
    return result;    
  }

  //-------------------------------------------------------------------------
  // Function Configurations
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public FunctionConfigurationSource getFunctionConfigurationSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(FunctionConfigurationSource.class, name).getUri();
    return new RemoteFunctionConfigurationSource(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public FunctionConfigurationSource getFunctionConfigurationSource(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, FunctionConfigurationSource.class).getUri();
    return new RemoteFunctionConfigurationSource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, FunctionConfigurationSource> getFunctionConfigurationSources() {
    Map<String, FunctionConfigurationSource> result = new LinkedHashMap<String, FunctionConfigurationSource>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(FunctionConfigurationSource.class)) {
      result.put(info.getClassifier(), new RemoteFunctionConfigurationSource(info.getUri()));
    }
    return result;    
  }

  //-------------------------------------------------------------------------
  // Exchanges
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public ExchangeSource getExchangeSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(ExchangeSource.class, name).getUri();
    return new RemoteExchangeSource(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public ExchangeSource getExchangeSource(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, ExchangeSource.class).getUri();
    return new RemoteExchangeSource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, ExchangeSource> getExchangeSources() {
    Map<String, ExchangeSource> result = new LinkedHashMap<String, ExchangeSource>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(ExchangeSource.class)) {
      result.put(info.getClassifier(), new RemoteExchangeSource(info.getUri()));
    }
    return result;    
  }

  //-------------------------------------------------------------------------
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public ExchangeMaster getExchangeMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(ExchangeMaster.class, name).getUri();
    return new RemoteExchangeMaster(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public ExchangeMaster getExchangeMaster(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, ExchangeMaster.class).getUri();
    return new RemoteExchangeMaster(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, ExchangeMaster> getExchangeMasters() {
    Map<String, ExchangeMaster> result = new LinkedHashMap<String, ExchangeMaster>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(ExchangeMaster.class)) {
      result.put(info.getClassifier(), new RemoteExchangeMaster(info.getUri()));
    }
    return result;    
  }

  //-------------------------------------------------------------------------
  // Regions
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public RegionSource getRegionSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(RegionSource.class, name).getUri();
    return new RemoteRegionSource(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public RegionSource getRegionSource(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, RegionSource.class).getUri();
    return new RemoteRegionSource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, RegionSource> getRegionSources() {
    Map<String, RegionSource> result = new LinkedHashMap<String, RegionSource>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(RegionSource.class)) {
      result.put(info.getClassifier(), new RemoteRegionSource(info.getUri()));
    }
    return result;    
  }

  //-------------------------------------------------------------------------
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public RegionMaster getRegionMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(RegionMaster.class, name).getUri();
    return new RemoteRegionMaster(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public RegionMaster getRegionMaster(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, RegionMaster.class).getUri();
    return new RemoteRegionMaster(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, RegionMaster> getRegionMasters() {
    Map<String, RegionMaster> result = new LinkedHashMap<String, RegionMaster>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(RegionMaster.class)) {
      result.put(info.getClassifier(), new RemoteRegionMaster(info.getUri()));
    }
    return result;    
  }

  //-------------------------------------------------------------------------
  // Holidays
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public HolidaySource getHolidaySource(final String name) {
    URI uri = getComponentServer().getComponentInfo(HolidaySource.class, name).getUri();
    return new RemoteHolidaySource(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public HolidaySource getHolidaySource(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, HolidaySource.class).getUri();
    return new RemoteHolidaySource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, HolidaySource> getHolidaySources() {
    Map<String, HolidaySource> result = new LinkedHashMap<String, HolidaySource>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(HolidaySource.class)) {
      result.put(info.getClassifier(), new RemoteHolidaySource(info.getUri()));
    }
    return result;    
  }

  //-------------------------------------------------------------------------
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public HolidayMaster getHolidayMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(HolidayMaster.class, name).getUri();
    return new RemoteHolidayMaster(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public HolidayMaster getHolidayMaster(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, HolidayMaster.class).getUri();
    return new RemoteHolidayMaster(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, HolidayMaster> getHolidayMasters() {
    Map<String, HolidayMaster> result = new LinkedHashMap<String, HolidayMaster>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(HolidayMaster.class)) {
      result.put(info.getClassifier(), new RemoteHolidayMaster(info.getUri()));
    }
    return result;    
  }

  //-------------------------------------------------------------------------
  // Interpolated Yield Curve Definitions
  /* REVIEW: jim 28-May-2012 -- Why are we not just using the config source for this stuff? */
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public InterpolatedYieldCurveDefinitionSource getInterpolatedYieldCurveDefinitionSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(InterpolatedYieldCurveDefinitionSource.class, name).getUri();
    return new RemoteInterpolatedYieldCurveDefinitionSource(uri);
  }

  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public InterpolatedYieldCurveDefinitionSource getInterpolatedYieldCurveDefinitionSource(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, InterpolatedYieldCurveDefinitionSource.class).getUri();
    return new RemoteInterpolatedYieldCurveDefinitionSource(uri);
  }

  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, InterpolatedYieldCurveDefinitionSource> getInterpolatedYieldCurveDefinitionSources() {
    Map<String, InterpolatedYieldCurveDefinitionSource> result = new LinkedHashMap<String, InterpolatedYieldCurveDefinitionSource>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(InterpolatedYieldCurveDefinitionSource.class)) {
      result.put(info.getClassifier(), new RemoteInterpolatedYieldCurveDefinitionSource(info.getUri()));
    }
    return result;    
  }

  public InterpolatedYieldCurveDefinitionMaster getTestInterpolatedYieldCurveDefinitionMaster() {
    URI uri = getComponentServer().getComponentInfo(InterpolatedYieldCurveDefinitionMaster.class, "test").getUri();
    return new RemoteInterpolatedYieldCurveDefinitionMaster(uri);
  }

  //-------------------------------------------------------------------------
  // Interpolated Yield Curve Specification Builders
  /* REVIEW: jim 28-May-2012 -- What on earth is this stuff - this is a data structure, not a service! */
  /**
   * @param name the classifier name of the object you want to retrieve
   * @return the interface requested, or null if not present
   */
  public InterpolatedYieldCurveSpecificationBuilder getInterpolatedYieldCurveSpecificationBuilder(final String name) {
    URI uri = getComponentServer().getComponentInfo(InterpolatedYieldCurveSpecificationBuilder.class, name).getUri();
    return new RemoteInterpolatedYieldCurveSpecificationBuilder(uri);
  }

  /* REVIEW: jim 28-May-2012 -- What on earth is this stuff - this is a data structure, not a service! */
  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first), or null
   * @return the best matching interface available
   */
  public InterpolatedYieldCurveSpecificationBuilder getInterpolatedYieldCurveSpecificationBuilder(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, InterpolatedYieldCurveSpecificationBuilder.class).getUri();
    return new RemoteInterpolatedYieldCurveSpecificationBuilder(uri);
  }

  /* REVIEW: jim 28-May-2012 -- What on earth is this stuff - this is a data structure, not a service! */
  /**
   * @return a map of classifier names to requested interface type
   */
  public Map<String, InterpolatedYieldCurveSpecificationBuilder> getInterpolatedYieldCurveSpecificationBuidlers() {
    Map<String, InterpolatedYieldCurveSpecificationBuilder> result = new LinkedHashMap<String, InterpolatedYieldCurveSpecificationBuilder>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(InterpolatedYieldCurveSpecificationBuilder.class)) {
      result.put(info.getClassifier(), new RemoteInterpolatedYieldCurveSpecificationBuilder(info.getUri()));
    }
    return result;    
  }

  //-------------------------------------------------------------------------
  public AvailableOutputsProvider getAvailableOutputs(final String name) {
    URI uri = getComponentServer().getComponentInfo(AvailableOutputsProvider.class, name).getUri();
    return new RemoteAvailableOutputsProvider(uri);
  }
  
  //-------------------------------------------------------------------------
  private JmsConnector getJmsConnector(URI activeMQBrokerUri) {
    ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(activeMQBrokerUri);
    JmsConnectorFactoryBean factory = new JmsConnectorFactoryBean();
    factory.setName(getClass().getSimpleName());
    factory.setConnectionFactory(cf);
    factory.setClientBrokerUri(activeMQBrokerUri);
    factory.setTopicName(getClass().getSimpleName());
    return factory.getObjectCreating();
  }

  private JmsConnector getJmsConnector(ComponentInfo info) {
    URI jmsBrokerUri = URI.create(info.getAttribute("jmsBrokerUri"));
    JmsConnector jmsConnector = getJmsConnector(jmsBrokerUri);
    return jmsConnector;
  }

  private ComponentServer getComponentServer() {
    return _componentServer;
  }

}
