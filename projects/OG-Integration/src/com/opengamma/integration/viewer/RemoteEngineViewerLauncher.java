/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.Executors;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.financial.view.rest.RemoteViewProcessor;
import com.opengamma.util.jms.JmsConnectorFactoryBean;
import com.opengamma.util.test.TestProperties;

/**
 * Launcher for the remote engine viewer.
 */
public class RemoteEngineViewerLauncher extends AbstractEngineViewerLauncher {

  @Override
  protected void startup() {
    final Properties props = TestProperties.getTestProperties();
    final StringBuilder uriString = new StringBuilder("http://");
    uriString.append(System.getProperty("web.host", props.getProperty("opengamma.engine.host")));
    uriString.append(':').append(System.getProperty("web.port", props.getProperty("opengamma.engine.port")));
    uriString.append(System.getProperty("web.path", props.getProperty("opengamma.engine.path")));
    uriString.append("jax/viewProcessor/0/");
    URI vpBase;
    try {
      vpBase = new URI(uriString.toString());
    } catch (URISyntaxException ex) {
      throw new OpenGammaRuntimeException("Invalid URI", ex);
    }
    
    URI uri = URI.create(props.getProperty("activeMQ.brokerURL"));
    ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(uri);
    JmsConnectorFactoryBean factory = new JmsConnectorFactoryBean();
    factory.setName(getClass().getSimpleName());
    factory.setConnectionFactory(cf);
    factory.setClientBrokerUri(uri);
    ViewProcessor vp = new RemoteViewProcessor(vpBase, factory.getObjectCreating(), Executors.newSingleThreadScheduledExecutor());
    startViewer(vp);
  }

  /**
   * Starts the demo client, connecting to a shared server.
   * 
   * @param args command line arguments
   */
  public static void main(String[] args) { // CSIGNORE
    launch(RemoteEngineViewerLauncher.class, args);
  }

}
