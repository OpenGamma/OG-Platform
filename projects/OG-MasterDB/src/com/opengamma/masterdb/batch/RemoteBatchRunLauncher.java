/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentServer;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.financial.view.rest.RemoteViewProcessor;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.holiday.HolidayMetaDataResult;
import com.opengamma.master.impl.AbstractRemoteMaster;
import com.opengamma.transport.jaxrs.UriEndPointUriFactoryBean;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;
import com.opengamma.util.rest.AbstractRemoteClient;
import com.opengamma.util.test.TestProperties;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.fudgemsg.FudgeMsgEnvelope;

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
    
    String ogConfigurationURL = System.getProperty("config.uri", props.getProperty("opengamma.config.uri"));

    class ConfigRemoteClient extends AbstractRemoteClient {
      ConfigRemoteClient(URI baseUri) {
        super(baseUri);
      }
      
      public ComponentServer getComponentServer(){
        return accessRemote(getBaseUri()).get(ComponentServer.class);
      }
    }
    ConfigRemoteClient configRemoteClient = new ConfigRemoteClient(URI.create(ogConfigurationURL));        
    
    ComponentInfo viewProcessorInfo = configRemoteClient.getComponentServer().getComponentInfo(ViewProcessor.class, "main");
    
    URI viewProcessorUri = URI.create(ogConfigurationURL).resolve(viewProcessorInfo.getUri());
   
    ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(props.getProperty("activeMQ.brokerURL"));
    activeMQConnectionFactory.setWatchTopicAdvisories(false);

    JmsConnectorFactoryBean jmsConnectorFactoryBean = new JmsConnectorFactoryBean();
    jmsConnectorFactoryBean.setConnectionFactory(activeMQConnectionFactory);
    jmsConnectorFactoryBean.setName("Masters");

    JmsConnector jmsConnector = jmsConnectorFactoryBean.getObjectCreating();
    ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
    try {
      ViewProcessor vp = new RemoteViewProcessor(
        viewProcessorUri,
        jmsConnector,
        heartbeatScheduler);
      ViewClient vc = vp.createViewClient(UserPrincipal.getLocalUser());

     
      UniqueId viewDefUniqueId = UniqueId.parse("DbCfg~990266");

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
