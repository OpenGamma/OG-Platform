/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import net.sf.ehcache.util.NamedThreadFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;

import com.opengamma.component.ComponentServer;
import com.opengamma.component.rest.RemoteComponentServer;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.exchange.impl.RemoteExchangeSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.impl.RemoteHistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.impl.RemoteHolidaySource;
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
import com.opengamma.financial.user.FinancialUserManager;
import com.opengamma.financial.user.rest.RemoteClient;
import com.opengamma.financial.view.rest.RemoteAvailableOutputsProvider;
import com.opengamma.financial.view.rest.RemoteViewProcessor;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.RemoteConfigMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoader;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.impl.RemoteHistoricalTimeSeriesLoader;
import com.opengamma.master.historicaltimeseries.impl.RemoteHistoricalTimeSeriesMaster;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.impl.RemoteHolidayMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.impl.RemotePortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.impl.RemotePositionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.RemoteSecurityMaster;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.provider.historicaltimeseries.impl.RemoteHistoricalTimeSeriesProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;
import com.opengamma.util.rest.FudgeRestClient;

/**
 * Configuration tools for remote engine components.
 */
public class RemoteEngine {

  private FudgeContext _fudgeContext;
  private ComponentServer _components;
  private JmsConnector _jmsConnector;
  private URI _configurationURI;
  private FudgeMsg _configuration;
  private final ScheduledExecutorService _scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("rvp"));
 
  public RemoteEngine(String baseUrl) {
    ArgumentChecker.notNull(StringUtils.trimToNull(baseUrl), "baseUrl");
    init(baseUrl);
  }
  
  private void init(String baseUrl) {
    baseUrl = StringUtils.stripEnd(baseUrl, "/");
    if (baseUrl.endsWith("/jax") == false) {
      baseUrl += "/jax";
    }
    
    _fudgeContext = OpenGammaFudgeContext.getInstance();
    final URI componentsUri = URI.create(baseUrl);
    final RemoteComponentServer remote = new RemoteComponentServer(componentsUri);
    _components = remote.getComponentServer();
    _configurationURI = URI.create(baseUrl + "/configuration/0");
    _configuration = FudgeRestClient.create().accessFudge(_configurationURI).get(FudgeMsg.class);
    final String activeMQBroker = _configuration.getString("activeMQ");
    final JmsConnectorFactoryBean factory = new JmsConnectorFactoryBean();
    factory.setName(getClass().getSimpleName());
    factory.setClientBrokerUri(URI.create(activeMQBroker));
    factory.setConnectionFactory(new ActiveMQConnectionFactory(factory.getClientBrokerUri()));
    _jmsConnector = factory.getObjectCreating();    
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public JmsConnector getJmsConnector() {
    return _jmsConnector;
  }

  public FudgeMsg getConfiguration() {
    return _configuration;
  }

  public URI getConfigurationURI() {
    return _configurationURI;
  }
  
  /**
   * Gets the components.
   * @return the components
   */
  public ComponentServer getComponents() {
    return _components;
  }

  public RemoteViewProcessor getViewProcessor(final String name) {
    final URI uri = _components.getComponentInfo(ViewProcessor.class, name).getUri();
    return new RemoteViewProcessor(uri, _jmsConnector, _scheduler);
  }

  public RemoteConfigMaster getConfigMaster(final String name) {
    final URI uri = _components.getComponentInfo(ConfigMaster.class, name).getUri();
    return new RemoteConfigMaster(uri);
  }

  public RemotePortfolioMaster getPortfolioMaster(final String name) {
    final URI uri = _components.getComponentInfo(PortfolioMaster.class, name).getUri();
    return new RemotePortfolioMaster(uri);
  }

  public RemotePositionMaster getPositionMaster(final String name) {
    final URI uri = _components.getComponentInfo(PositionMaster.class, name).getUri();
    return new RemotePositionMaster(uri);
  }

  public RemoteSecuritySource getSecuritySource(final String name) {
    final URI uri = _components.getComponentInfo(SecuritySource.class, name).getUri();
    return new RemoteFinancialSecuritySource(uri);
  }

  public RemoteSecurityMaster getSecurityMaster(final String name) {
    final URI uri = _components.getComponentInfo(SecurityMaster.class, name).getUri();
    return new RemoteSecurityMaster(uri);
  }

  public RemoteHistoricalTimeSeriesSource getHistoricalTimeSeriesSource(final String name) {
    final URI uri = _components.getComponentInfo(HistoricalTimeSeriesSource.class, name).getUri();
    return new RemoteHistoricalTimeSeriesSource(uri);
  }

  public RemoteCurrencyMatrixSource getCurrencyMatrixSource(final String name) {
    final URI uri = _components.getComponentInfo(CurrencyMatrixSource.class, name).getUri();
    return new RemoteCurrencyMatrixSource(uri);
  }

  public RemoteFunctionConfigurationSource getRepositoryConfigurationSource(final String name) {
    final URI uri = _components.getComponentInfo(FunctionConfigurationSource.class, name).getUri();
    return new RemoteFunctionConfigurationSource(uri);
  }

  public RemotePositionSource getPositionSource(final String name) {
    final URI uri = _components.getComponentInfo(PositionSource.class, name).getUri();
    return new RemotePositionSource(uri);
  }

  public RemoteExchangeSource getExchangeSource(final String name) {
    final URI uri = _components.getComponentInfo(ExchangeSource.class, name).getUri();
    return new RemoteExchangeSource(uri);
  }

  public RemoteRegionSource getRegionSource(final String name) {
    final URI uri = _components.getComponentInfo(RegionSource.class, name).getUri();
    return new RemoteRegionSource(uri);
  }

  public RemoteHolidaySource getHolidaySource(final String name) {
    final URI uri = _components.getComponentInfo(HolidaySource.class, name).getUri();
    return new RemoteHolidaySource(uri);
  }
  
  public RemoteHolidayMaster getHolidayMaster(final String name) {
    final URI uri = _components.getComponentInfo(HolidayMaster.class, name).getUri();
    return new RemoteHolidayMaster(uri);
  }

  public RemoteInterpolatedYieldCurveDefinitionSource getInterpolatedYieldCurveDefinitionSource(final String name) {
    final URI uri = _components.getComponentInfo(InterpolatedYieldCurveDefinitionSource.class, name).getUri();
    return new RemoteInterpolatedYieldCurveDefinitionSource(uri);
  }

  public RemoteInterpolatedYieldCurveDefinitionMaster getInterpolatedYieldCurveDefinitionMaster(final String name) {
    final URI uri = _components.getComponentInfo(InterpolatedYieldCurveDefinitionMaster.class, name).getUri();
    return new RemoteInterpolatedYieldCurveDefinitionMaster(uri);
  }

  public RemoteInterpolatedYieldCurveSpecificationBuilder getInterpolatedYieldCurveSpecificationBuilder(final String name) {
    final URI uri = _components.getComponentInfo(InterpolatedYieldCurveSpecificationBuilder.class, name).getUri();
    return new RemoteInterpolatedYieldCurveSpecificationBuilder(uri);
  }

  //-------------------------------------------------------------------------
  public RemoteAvailableOutputsProvider getAvailableOutputsProvider(String name) {
    final URI uri = _components.getComponentInfo(AvailableOutputsProvider.class, name).getUri();
    return new RemoteAvailableOutputsProvider(uri);
  }

  public RemoteClient getUserClient(final String finUserManagerComponentName, final String username, final String clientId) {
    final URI uri = _components.getComponentInfo(FinancialUserManager.class, finUserManagerComponentName).getUri();
    return RemoteClient.forClient(_fudgeContext, uri, username, clientId);
  }
  
  public RemoteHistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster(final String name) {
    final URI uri = _components.getComponentInfo(HistoricalTimeSeriesMaster.class, name).getUri();
    return new RemoteHistoricalTimeSeriesMaster(uri);
  }
  
  public RemoteHistoricalTimeSeriesProvider getHistoricalTimeSeriesProvider(final String name) {
    final URI uri = _components.getComponentInfo(HistoricalTimeSeriesProvider.class, name).getUri();
    return new RemoteHistoricalTimeSeriesProvider(uri);
  }
  
  public RemoteHistoricalTimeSeriesLoader getHistoricalTimeSeriesLoader(final String name) {
    final URI uri = _components.getComponentInfo(HistoricalTimeSeriesLoader.class, name).getUri();
    return new RemoteHistoricalTimeSeriesLoader(uri);
  }
  
  public RemoteFunctionConfigurationSource getFunctionConfigurationSource(final String name) {
    final URI uri = _components.getComponentInfo(FunctionConfigurationSource.class, name).getUri();
    return new RemoteFunctionConfigurationSource(uri);
  }
  
  public void shutDown() {
    if (_scheduler != null) {
      _scheduler.shutdownNow();
    }
    if (_jmsConnector != null) {
      _jmsConnector.close();
    }
  }

}
