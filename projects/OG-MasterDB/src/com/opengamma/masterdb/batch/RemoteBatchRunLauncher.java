/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.financial.view.rest.RemoteViewProcessor;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;
import com.opengamma.util.test.TestProperties;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Launcher for the remote engine viewer.
 */
public class RemoteBatchRunLauncher {

  /**
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {

    final Properties props = TestProperties.getTestProperties();

    final StringBuilder uriString = new StringBuilder("http://");
    uriString.append(System.getProperty("web.host", props.getProperty("opengamma.engine.host")));
    uriString.append(':').append(System.getProperty("web.port", props.getProperty("opengamma.engine.port")));
    uriString.append(System.getProperty("web.path", props.getProperty("opengamma.engine.path")));
    uriString.append("jax/data/viewProcessors/Vp~0/");

    URI vpBase;
    try {
      vpBase = new URI(uriString.toString());
    } catch (URISyntaxException ex) {
      throw new OpenGammaRuntimeException("Invalid URI", ex);
    }

    ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(props.getProperty("activeMQ.brokerURL"));
    activeMQConnectionFactory.setWatchTopicAdvisories(false);

    JmsConnectorFactoryBean jmsConnectorFactoryBean = new JmsConnectorFactoryBean();
    jmsConnectorFactoryBean.setConnectionFactory(activeMQConnectionFactory);
    jmsConnectorFactoryBean.setName("Masters");

    JmsConnector jmsConnector = jmsConnectorFactoryBean.createObject();
    ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
    try {
      ViewProcessor vp = new RemoteViewProcessor(
        vpBase,
        jmsConnector,
        heartbeatScheduler);
      ViewClient vc = vp.createViewClient(new UserPrincipal(props.getProperty("opengamma.engine.username"), props.getProperty("opengamma.engine.ip-address")));

      Map<UniqueId, String> viewDefinitions = vp.getViewDefinitionRepository().getDefinitionEntries();

      UniqueId viewDefUniqueId = viewDefinitions.keySet().iterator().next();

      //com.opengamma.engine.view.ViewDefinition viewDefinition = vp.getViewDefinitionRepository().getDefinition(viewDefUniqueId);
      //System.out.println(viewDefinitions);

      ViewExecutionOptions executionOptions = ExecutionOptions.batch(null, MarketData.live(), null);

      vc.attachToViewProcess(viewDefUniqueId, executionOptions);


      vc.shutdown();
    } finally {
      heartbeatScheduler.shutdown();
    }
  }

}
