/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.component;

import static com.google.common.collect.Lists.newArrayList;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Executors;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.fudgemsg.FudgeContext;

import com.opengamma.component.ComponentInfo;
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
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.marketdata.snapshot.MarketDataSnapshotter;
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
import com.opengamma.financial.view.rest.RemoteMarketDataSnapshotter;
import com.opengamma.financial.view.rest.RemoteViewProcessor;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.RemoteConfigMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.impl.RemotePortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.impl.RemotePositionMaster;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;

/**
 * Configuration tools for remote processors
 */
public class RemoteEngineUtils {

  public static final RemoteEngineUtils INSTANCE = new RemoteEngineUtils();

  private final FudgeContext _fudgeContext;


  public RemoteEngineUtils() {
    _fudgeContext = OpenGammaFudgeContext.getInstance();
  }

  private URI _baseURI;
  private URI _activeMQBrokerURI;

  public void setBaseUri(String baseUrl) {
    _baseURI = URI.create(baseUrl);
  }

  public URI getBaseURI() {
    return _baseURI;
  }

  public URI getActiveMQBrokerURI() {
    return _activeMQBrokerURI;
  }

  public void setActiveMQBrokerUrl(String activeMQBrokerUrl) {
    _activeMQBrokerURI = URI.create(activeMQBrokerUrl);
  }

  public JmsConnector getJmsConnector() {
    ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(_activeMQBrokerURI);
    JmsConnectorFactoryBean factory = new JmsConnectorFactoryBean();
    factory.setName(getClass().getSimpleName());
    factory.setConnectionFactory(cf);
    factory.setClientBrokerUri(_activeMQBrokerURI);
    return factory.getObjectCreating();
  }

  public RemoteComponentServer getRemoteComponentServer() {
    return new RemoteComponentServer(_baseURI);
  }

  private ComponentServer getComponentServer() {
    return getRemoteComponentServer().getComponentServer();
  }


  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public RemoteViewProcessor getViewProcessor(String vpId) {
    URI uri = getComponentServer().getComponentInfo(ViewProcessor.class, "main").getUri();
    return new RemoteViewProcessor(uri, getJmsConnector(), Executors.newSingleThreadScheduledExecutor());
  }

  public List<RemoteViewProcessor> getViewProcessors() {
    List<RemoteViewProcessor> viewProcessors = newArrayList();
    for (ComponentInfo o : getRemoteComponentServer().getComponentServer().getComponentInfos()) {
      if (ViewProcessor.class.isAssignableFrom(o.getType())) {
        RemoteViewProcessor vp = new RemoteViewProcessor(
          o.getUri(), getJmsConnector(), Executors.newSingleThreadScheduledExecutor());
        viewProcessors.add(vp);
      }
    }
    return viewProcessors;
  }

  public ConfigMaster getConfigMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(ConfigMaster.class, name).getUri();
    return new RemoteConfigMaster(uri);
  }

  public PortfolioMaster getPortfolioMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(PortfolioMaster.class, name).getUri();
    return new RemotePortfolioMaster(uri);
  }

  public PositionMaster getPositionMaster(final String name) {
    URI uri = getComponentServer().getComponentInfo(PositionMaster.class, name).getUri();
    return new RemotePositionMaster(uri);
  }

  public SecuritySource getSecuritySource(final String name) {
    URI uri = getComponentServer().getComponentInfo(SecuritySource.class, name).getUri();
    return new RemoteFinancialSecuritySource(uri);
  }


  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(HistoricalTimeSeriesSource.class, name).getUri();
    return new RemoteHistoricalTimeSeriesSource(uri);
  }

  public CurrencyMatrixSource getCurrencyMatrixSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(CurrencyMatrixSource.class, name).getUri();
    return new RemoteCurrencyMatrixSource(uri);
  }

  public RepositoryConfigurationSource getRepositoryConfigurationSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(RepositoryConfigurationSource.class, name).getUri();
    return new RemoteRepositoryConfigurationSource(uri);
  }

  public PositionSource getPositionSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(PositionSource.class, name).getUri();
    return new RemotePositionSource(uri);
  }

  public ExchangeSource getExchangeSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(ExchangeSource.class, name).getUri();
    return new RemoteExchangeSource(uri);
  }

  public RegionSource getRegionSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(RegionSource.class, name).getUri();
    return new RemoteRegionSource(uri);
  }

  public HolidaySource getHolidaySource(final String name) {
    URI uri = getComponentServer().getComponentInfo(HolidaySource.class, name).getUri();
    return new RemoteHolidaySource(uri);
  }

  public InterpolatedYieldCurveDefinitionSource getInterpolatedYieldCurveDefinitionSource(final String name) {
    URI uri = getComponentServer().getComponentInfo(InterpolatedYieldCurveDefinitionSource.class, name).getUri();
    return new RemoteInterpolatedYieldCurveDefinitionSource(uri);
  }

  public InterpolatedYieldCurveDefinitionMaster getTestInterpolatedYieldCurveDefinitionMaster() {
    URI uri = getComponentServer().getComponentInfo(InterpolatedYieldCurveDefinitionMaster.class, "test").getUri();
    return new RemoteInterpolatedYieldCurveDefinitionMaster(uri);
  }

  public InterpolatedYieldCurveSpecificationBuilder getInterpolatedYieldCurveSpecificationBuilder(final String name) {
    URI uri = getComponentServer().getComponentInfo(InterpolatedYieldCurveSpecificationBuilder.class, name).getUri();
    return new RemoteInterpolatedYieldCurveSpecificationBuilder(uri);
  }

  //-------------------------------------------------------------------------
  public RemoteAvailableOutputsProvider getRemoteAvailableOutputs() {
    URI uri = getComponentServer().getComponentInfo(AvailableOutputsProvider.class, "main").getUri();
    return new RemoteAvailableOutputsProvider(uri);
  }

}
