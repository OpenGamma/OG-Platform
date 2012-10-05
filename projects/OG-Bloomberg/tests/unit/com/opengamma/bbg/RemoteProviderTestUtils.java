/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import java.net.URI;
import java.util.Properties;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.fudgemsg.FudgeContext;

import com.opengamma.component.ComponentServer;
import com.opengamma.component.rest.RemoteComponentServer;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.provider.historicaltimeseries.impl.RemoteHistoricalTimeSeriesProvider;
import com.opengamma.provider.livedata.LiveDataMetaDataProvider;
import com.opengamma.provider.livedata.impl.RemoteLiveDataMetaDataProvider;
import com.opengamma.provider.security.SecurityProvider;
import com.opengamma.provider.security.impl.RemoteSecurityProvider;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;
import com.opengamma.util.test.TestProperties;

/**
 * Configuration tools for remote providers.
 */
public class RemoteProviderTestUtils {

  /**
   * Static instance, that calls the server once to find the URIs.
   * Use {@link AbstractRemoteProviderTest} to wrap this correctly.
   */
  public static final RemoteProviderTestUtils INSTANCE = new RemoteProviderTestUtils();

  private final FudgeContext _fudgeContext;
  private final ComponentServer _components;
  private final JmsConnector _jmsConnector;

  public RemoteProviderTestUtils() {
    _fudgeContext = OpenGammaFudgeContext.getInstance();
    final Properties props = TestProperties.getTestProperties();
    final String baseUrl = new StringBuilder("http://")
//      .append("localhost:8090/")
      .append(System.getProperty("web.host", props.getProperty("opengamma.provider.host"))).append(':')
      .append(System.getProperty("web.port", props.getProperty("opengamma.provider.port")))
      .append(System.getProperty("web.path", props.getProperty("opengamma.provider.path")))
      .append("jax").toString();
    URI componentsUri = URI.create(baseUrl);
    RemoteComponentServer remote = new RemoteComponentServer(componentsUri);
    _components = remote.getComponentServer();
    
    URI uri = URI.create(props.getProperty("activeMQ.brokerURL"));
    ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(uri);
    JmsConnectorFactoryBean factory = new JmsConnectorFactoryBean();
    factory.setName(getClass().getSimpleName());
    factory.setConnectionFactory(cf);
    factory.setClientBrokerUri(uri);
    _jmsConnector = factory.getObjectCreating();
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public SecurityProvider getSecurityProviderBloomberg() {
    URI uri = _components.getComponentInfo(SecurityProvider.class, "bloomberg").getUri();
    return new RemoteSecurityProvider(uri);
  }

  public HistoricalTimeSeriesProvider getHistoricalTimeSeriesProviderBloomberg() {
    URI uri = _components.getComponentInfo(HistoricalTimeSeriesProvider.class, "bloomberg").getUri();
    return new RemoteHistoricalTimeSeriesProvider(uri);
  }

  public LiveDataMetaDataProvider getLiveDataMetaDataProvider(String classifier) {
    URI uri = _components.getComponentInfo(LiveDataMetaDataProvider.class, classifier).getUri();
    return new RemoteLiveDataMetaDataProvider(uri);
  }

  public JmsConnector getJmsConnector() {
    return _jmsConnector;
  }

}
