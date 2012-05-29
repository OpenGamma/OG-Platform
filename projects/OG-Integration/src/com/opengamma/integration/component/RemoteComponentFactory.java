/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.component;

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
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.impl.RemoteMarketDataSnapshotSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.RemotePositionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.impl.RemoteRegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.impl.RemoteSecuritySource;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
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
import com.opengamma.financial.function.rest.RemoteRepositoryConfigurationSource;
import com.opengamma.financial.security.RemoteFinancialSecuritySource;
import com.opengamma.financial.view.rest.RemoteAvailableOutputsProvider;
import com.opengamma.financial.view.rest.RemoteViewProcessor;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.RemoteConfigMaster;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.impl.RemoteExchangeMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.impl.RemoteHistoricalTimeSeriesMaster;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.impl.RemoteHolidayMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.impl.RemoteMarketDataSnapshotMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.impl.RemotePortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.impl.RemotePositionMaster;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.impl.RemoteRegionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.RemoteSecurityMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;

/**
 * Constructs components exposed by a remote component server. =
 */
public class RemoteComponentFactory {
  private final URI _baseUri;
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
  public URI getBaseUri() {
    return _baseUri;
  }
  
  private ComponentInfo getTopLevelComponent(List<String> preferenceList, Class<?> type) {
    if (preferenceList != null) {
      for (String preference : preferenceList) {
        ComponentInfo componentInfo = getComponentServer().getComponentInfo(type, preference);
        if (componentInfo != null) {
          return componentInfo;
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
  
  // Configs
  
  /**
   * @param preferredClassifiers a list of names of classifiers in order of preference (most preferred first)
   * @return the best matching master available
   */
  public ConfigMaster getConfigMaster(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, ConfigMaster.class).getUri();
    return new RemoteConfigMaster(uri);
  }

  public ConfigMaster getConfigMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(ConfigMaster.class, name).getUri();
    return new RemoteConfigMaster(uri);
  }
  
  public Map<String, ConfigMaster> getConfigMasters() {
    Map<String, ConfigMaster> result = new LinkedHashMap<String, ConfigMaster>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(ConfigMaster.class)) {
      result.put(info.getClassifier(), new RemoteConfigMaster(info.getUri()));
    }
    return result;
  }
  
  public ConfigSource getConfigSource(final List<String> preferredClassifiers) {
    ComponentInfo componentInfo = getTopLevelComponent(preferredClassifiers, ConfigSource.class);
    return new RemoteConfigSource(componentInfo.getUri());
  }
  
  public ConfigSource getConfigSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(ConfigSource.class, name).getUri();
    return new RemoteConfigSource(uri);
  }
  
  public Map<String, ConfigSource> getConfigSources() {
    Map<String, ConfigSource> result = new LinkedHashMap<String, ConfigSource>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(ConfigSource.class)) {
      result.put(info.getClassifier(), new RemoteConfigSource(info.getUri()));
    }
    return result;    
  }

  // Portfolios
  
  public PortfolioMaster getPortfolioMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(PortfolioMaster.class, name).getUri();
    return new RemotePortfolioMaster(uri);
  }
  
  public PortfolioMaster getPortfolioMaster(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, PortfolioMaster.class).getUri();
    return new RemotePortfolioMaster(uri);
  }
  
  public Map<String, PortfolioMaster> getPortfolioMasters() {
    Map<String, PortfolioMaster> result = new LinkedHashMap<String, PortfolioMaster>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(PortfolioMaster.class)) {
      result.put(info.getClassifier(), new RemotePortfolioMaster(info.getUri()));
    }
    return result;    
  }

  // Positions
  
  public PositionMaster getPositionMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(PositionMaster.class, name).getUri();
    return new RemotePositionMaster(uri);
  }
  
  public PositionMaster getPositionMaster(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, PositionMaster.class).getUri();
    return new RemotePositionMaster(uri);
  }
  
  public Map<String, PositionMaster> getPositionMasters() {
    Map<String, PositionMaster> result = new LinkedHashMap<String, PositionMaster>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(PositionMaster.class)) {
      result.put(info.getClassifier(), new RemotePositionMaster(info.getUri()));
    }
    return result;
  }
  
  public PositionSource getPositionSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(PositionSource.class, name).getUri();
    return new RemotePositionSource(uri);
  }
  
  public PositionSource getPositionSource(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, PositionSource.class).getUri();
    return new RemotePositionSource(uri);
  }
  
  public Map<String, PositionSource> getPositionSources() {
    Map<String, PositionSource> result = new LinkedHashMap<String, PositionSource>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(PositionSource.class)) {
      result.put(info.getClassifier(), new RemotePositionSource(info.getUri()));
    }
    return result;    
  }
  
  // Securities
  
  public SecuritySource getSecuritySource(final String name) {
    URI uri = getComponentServer().getComponentInfo(SecuritySource.class, name).getUri();
    return new RemoteFinancialSecuritySource(uri);
  }
  
  public SecuritySource getSecuritySource(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, SecuritySource.class).getUri();
    return new RemoteSecuritySource(uri);
  }
  
  public Map<String, SecuritySource> getSecuritySources() {
    Map<String, SecuritySource> result = new LinkedHashMap<String, SecuritySource>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(SecuritySource.class)) {
      result.put(info.getClassifier(), new RemoteSecuritySource(info.getUri()));
    }
    return result;
  }
  
  public SecurityMaster getSecurityMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(SecurityMaster.class, name).getUri();
    return new RemoteSecurityMaster(uri);
  }
  
  public SecurityMaster getSecurityMaster(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, SecurityMaster.class).getUri();
    return new RemoteSecurityMaster(uri);
  }
  
  public Map<String, SecurityMaster> getSecurityMasters() {
    Map<String, SecurityMaster> result = new LinkedHashMap<String, SecurityMaster>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(SecurityMaster.class)) {
      result.put(info.getClassifier(), new RemoteSecurityMaster(info.getUri()));
    }
    return result;
  }

  // Market Data Snapshots
  
  public MarketDataSnapshotMaster getMarketDataSnapshotMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(MarketDataSnapshotMaster.class, name).getUri();
    return new RemoteMarketDataSnapshotMaster(uri);
  }
  
  public MarketDataSnapshotMaster getMarketDataSnapshotMaster(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, MarketDataSnapshotMaster.class).getUri();
    return new RemoteMarketDataSnapshotMaster(uri);
  }
  
  public Map<String, MarketDataSnapshotMaster> getMarketDataSnapshotMasters() {
    Map<String, MarketDataSnapshotMaster> result = new LinkedHashMap<String, MarketDataSnapshotMaster>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(MarketDataSnapshotMaster.class)) {
      result.put(info.getClassifier(), new RemoteMarketDataSnapshotMaster(info.getUri()));
    }
    return result;
  }
  
  public MarketDataSnapshotSource getMarketDataSnapshotSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(MarketDataSnapshotSource.class, name).getUri();
    return new RemoteMarketDataSnapshotSource(uri);
  }
  
  public MarketDataSnapshotSource getMarketDataSnapshotSource(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, MarketDataSnapshotSource.class).getUri();
    return new RemoteMarketDataSnapshotSource(uri);
  }
  
  public Map<String, MarketDataSnapshotSource> getMarketDataSnapshotSources() {
    Map<String, MarketDataSnapshotSource> result = new LinkedHashMap<String, MarketDataSnapshotSource>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(MarketDataSnapshotSource.class)) {
      result.put(info.getClassifier(), new RemoteMarketDataSnapshotSource(info.getUri()));
    }
    return result;
  }

  // Historical Time Series
  
  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(HistoricalTimeSeriesSource.class, name).getUri();
    return new RemoteHistoricalTimeSeriesSource(uri);
  }
  
  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, HistoricalTimeSeriesSource.class).getUri();
    return new RemoteHistoricalTimeSeriesSource(uri);
  }
  
  public Map<String, HistoricalTimeSeriesSource> getHistoricalTimeSeriesSources() {
    Map<String, HistoricalTimeSeriesSource> result = new LinkedHashMap<String, HistoricalTimeSeriesSource>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(HistoricalTimeSeriesSource.class)) {
      result.put(info.getClassifier(), new RemoteHistoricalTimeSeriesSource(info.getUri()));
    }
    return result;    
  }
  
  public HistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(HistoricalTimeSeriesMaster.class, name).getUri();
    return new RemoteHistoricalTimeSeriesMaster(uri);
  }
  
  public HistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, HistoricalTimeSeriesMaster.class).getUri();
    return new RemoteHistoricalTimeSeriesMaster(uri);
  }
  
  public Map<String, HistoricalTimeSeriesMaster> getHistoricalTimeSeriesMasters() {
    Map<String, HistoricalTimeSeriesMaster> result = new LinkedHashMap<String, HistoricalTimeSeriesMaster>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(HistoricalTimeSeriesMaster.class)) {
      result.put(info.getClassifier(), new RemoteHistoricalTimeSeriesMaster(info.getUri()));
    }
    return result;
  }

  // Currency Matrices
  
  public CurrencyMatrixSource getCurrencyMatrixSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(CurrencyMatrixSource.class, name).getUri();
    return new RemoteCurrencyMatrixSource(uri);
  }
  
  public CurrencyMatrixSource getCurrencyMatrixSource(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, CurrencyMatrixSource.class).getUri();
    return new RemoteCurrencyMatrixSource(uri);
  }
  
  public Map<String, CurrencyMatrixSource> getCurrencyMatrixSources() {
    Map<String, CurrencyMatrixSource> result = new LinkedHashMap<String, CurrencyMatrixSource>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(CurrencyMatrixSource.class)) {
      result.put(info.getClassifier(), new RemoteCurrencyMatrixSource(info.getUri()));
    }
    return result;    
  }
  
  // Repository Configurations

  public RepositoryConfigurationSource getRepositoryConfigurationSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(RepositoryConfigurationSource.class, name).getUri();
    return new RemoteRepositoryConfigurationSource(uri);
  }
  
  public RepositoryConfigurationSource getRepositoryConfigurationSource(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, RepositoryConfigurationSource.class).getUri();
    return new RemoteRepositoryConfigurationSource(uri);
  }
  
  public Map<String, RepositoryConfigurationSource> getRepositoryConfigurationSources() {
    Map<String, RepositoryConfigurationSource> result = new LinkedHashMap<String, RepositoryConfigurationSource>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(RepositoryConfigurationSource.class)) {
      result.put(info.getClassifier(), new RemoteRepositoryConfigurationSource(info.getUri()));
    }
    return result;    
  }
  
  // Exchanges
  
  public ExchangeSource getExchangeSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(ExchangeSource.class, name).getUri();
    return new RemoteExchangeSource(uri);
  }
  
  public ExchangeSource getExchangeSource(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, ExchangeSource.class).getUri();
    return new RemoteExchangeSource(uri);
  }
  
  public Map<String, ExchangeSource> getExchangeSources() {
    Map<String, ExchangeSource> result = new LinkedHashMap<String, ExchangeSource>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(ExchangeSource.class)) {
      result.put(info.getClassifier(), new RemoteExchangeSource(info.getUri()));
    }
    return result;    
  }
  
  public ExchangeMaster getExchangeMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(ExchangeMaster.class, name).getUri();
    return new RemoteExchangeMaster(uri);
  }
  
  public ExchangeMaster getExchangeMaster(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, ExchangeMaster.class).getUri();
    return new RemoteExchangeMaster(uri);
  }
  
  public Map<String, ExchangeMaster> getExchangeMasters() {
    Map<String, ExchangeMaster> result = new LinkedHashMap<String, ExchangeMaster>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(ExchangeMaster.class)) {
      result.put(info.getClassifier(), new RemoteExchangeMaster(info.getUri()));
    }
    return result;    
  }

  // Regions
  
  public RegionSource getRegionSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(RegionSource.class, name).getUri();
    return new RemoteRegionSource(uri);
  }
  
  public RegionSource getRegionSource(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, RegionSource.class).getUri();
    return new RemoteRegionSource(uri);
  }
  
  public Map<String, RegionSource> getRegionSources() {
    Map<String, RegionSource> result = new LinkedHashMap<String, RegionSource>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(RegionSource.class)) {
      result.put(info.getClassifier(), new RemoteRegionSource(info.getUri()));
    }
    return result;    
  }
  
  public RegionMaster getRegionMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(RegionMaster.class, name).getUri();
    return new RemoteRegionMaster(uri);
  }
  
  public RegionMaster getRegionMaster(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, RegionMaster.class).getUri();
    return new RemoteRegionMaster(uri);
  }
  
  public Map<String, RegionMaster> getRegionMasters() {
    Map<String, RegionMaster> result = new LinkedHashMap<String, RegionMaster>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(RegionMaster.class)) {
      result.put(info.getClassifier(), new RemoteRegionMaster(info.getUri()));
    }
    return result;    
  }

  // Holidays
  
  public HolidaySource getHolidaySource(final String name) {
    URI uri = getComponentServer().getComponentInfo(HolidaySource.class, name).getUri();
    return new RemoteHolidaySource(uri);
  }
  
  public HolidaySource getHolidaySource(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, HolidaySource.class).getUri();
    return new RemoteHolidaySource(uri);
  }
  
  public Map<String, HolidaySource> getHolidaySources() {
    Map<String, HolidaySource> result = new LinkedHashMap<String, HolidaySource>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(HolidaySource.class)) {
      result.put(info.getClassifier(), new RemoteHolidaySource(info.getUri()));
    }
    return result;    
  }
  
  public HolidayMaster getHolidayMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(HolidayMaster.class, name).getUri();
    return new RemoteHolidayMaster(uri);
  }
  
  public HolidayMaster getHolidayMaster(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, HolidayMaster.class).getUri();
    return new RemoteHolidayMaster(uri);
  }
  
  public Map<String, HolidayMaster> getHolidayMasters() {
    Map<String, HolidayMaster> result = new LinkedHashMap<String, HolidayMaster>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(HolidayMaster.class)) {
      result.put(info.getClassifier(), new RemoteHolidayMaster(info.getUri()));
    }
    return result;    
  }

  // Interpolated Yield Curve Definitions
  
  /* REVIEW: jim 28-May-2012 -- Why are we not just using the config source for this stuff? */
  public InterpolatedYieldCurveDefinitionSource getInterpolatedYieldCurveDefinitionSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(InterpolatedYieldCurveDefinitionSource.class, name).getUri();
    return new RemoteInterpolatedYieldCurveDefinitionSource(uri);
  }
  
  public InterpolatedYieldCurveDefinitionSource getInterpolatedYieldCurveDefinitionSource(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, InterpolatedYieldCurveDefinitionSource.class).getUri();
    return new RemoteInterpolatedYieldCurveDefinitionSource(uri);
  }
  
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

  // Interpolated Yield Curve Specification Builders
  
  /* REVIEW: jim 28-May-2012 -- What on earth is this stuff - this is a data structure, not a service! */
  public InterpolatedYieldCurveSpecificationBuilder getInterpolatedYieldCurveSpecificationBuilder(final String name) {
    URI uri = getComponentServer().getComponentInfo(InterpolatedYieldCurveSpecificationBuilder.class, name).getUri();
    return new RemoteInterpolatedYieldCurveSpecificationBuilder(uri);
  }
  
  /* REVIEW: jim 28-May-2012 -- What on earth is this stuff - this is a data structure, not a service! */
  public InterpolatedYieldCurveSpecificationBuilder getInterpolatedYieldCurveSpecificationBuilder(final List<String> preferredClassifiers) {
    URI uri = getTopLevelComponent(preferredClassifiers, InterpolatedYieldCurveSpecificationBuilder.class).getUri();
    return new RemoteInterpolatedYieldCurveSpecificationBuilder(uri);
  }
  
  /* REVIEW: jim 28-May-2012 -- What on earth is this stuff - this is a data structure, not a service! */  
  public Map<String, InterpolatedYieldCurveSpecificationBuilder> getInterpolatedYieldCurveSpecificationBuidlers() {
    Map<String, InterpolatedYieldCurveSpecificationBuilder> result = new LinkedHashMap<String, InterpolatedYieldCurveSpecificationBuilder>();
    for (ComponentInfo info : getComponentServer().getComponentInfos(InterpolatedYieldCurveSpecificationBuilder.class)) {
      result.put(info.getClassifier(), new RemoteInterpolatedYieldCurveSpecificationBuilder(info.getUri()));
    }
    return result;    
  }

  //-------------------------------------------------------------------------
  public RemoteAvailableOutputsProvider getRemoteAvailableOutputs() {
    URI uri = getComponentServer().getComponentInfo(AvailableOutputsProvider.class, "main").getUri();
    return new RemoteAvailableOutputsProvider(uri);
  }
  
  //-------------------------------------------------------------------------
  private JmsConnector getJmsConnector(URI activeMQBrokerUri) {
    ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(activeMQBrokerUri);
    JmsConnectorFactoryBean factory = new JmsConnectorFactoryBean();
    factory.setName(getClass().getSimpleName());
    factory.setConnectionFactory(cf);
    factory.setClientBrokerUri(activeMQBrokerUri);
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
