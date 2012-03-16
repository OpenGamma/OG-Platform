/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertSame;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.testng.annotations.Test;

import com.opengamma.bbg.server.ReferenceDataProviderRequestReceiver;
import com.opengamma.transport.ByteArrayFudgeRequestSender;
import com.opengamma.transport.FudgeRequestDispatcher;
import com.opengamma.transport.jms.JmsByteArrayRequestDispatcher;
import com.opengamma.transport.jms.JmsByteArrayRequestSender;

/**
 * 
 */
@Test
public class RemoteRefDataProviderWithActiveMQTest extends BloombergReferenceDataProviderTestCase {

  private ConfigurableApplicationContext _clientContext;
  private ConfigurableApplicationContext _serverContext;
  
  private void setUpSpring() throws Exception {
    ConfigurableApplicationContext clientContext = new ClassPathXmlApplicationContext("/com/opengamma/bbg/test-client-remoteRefDataProvider-context.xml");
    clientContext.start();
    _clientContext = clientContext;
    ConfigurableApplicationContext serverContext = new ClassPathXmlApplicationContext("/com/opengamma/bbg/test-server-remoteRefDataProvider-context.xml");
    serverContext.start();
    _serverContext = serverContext;
  }
  
  private void tearDownSpring() throws Exception {
    if (_clientContext != null) {
      ConfigurableApplicationContext clientContext = _clientContext;
      _clientContext = null;
      clientContext.close();
    }
    if (_serverContext != null) {
      ConfigurableApplicationContext serverContext = _serverContext;
      _serverContext = null;
      serverContext.close();
    }
  }

  //-------------------------------------------------------------------------
  @Override
  protected ReferenceDataProvider createReferenceDataProvider(Class<?> c)
  throws Exception {
    setUpSpring();
    assertClientSpringConfig();
    assertServerSpringConfig();
    DefaultMessageListenerContainer jmsContainer = (DefaultMessageListenerContainer)_serverContext.getBean("jmsContainer");
    BloombergReferenceDataProvider refDataProvider = (BloombergReferenceDataProvider)_serverContext.getBean("refDataProvider");
    while(!jmsContainer.isRunning() && !refDataProvider.isRunning()) {
      Thread.sleep(10l);
    }
    
    ReferenceDataProvider remoteReferenceDataProvider = (RemoteReferenceDataProvider)_clientContext.getBean("remoteReferenceDataProvider");
    assertNotNull(remoteReferenceDataProvider);
    return remoteReferenceDataProvider;
  }

  /**
   * 
   */
  private void assertServerSpringConfig() {
    PropertyPlaceholderConfigurer propConfigurer= (PropertyPlaceholderConfigurer)_serverContext.getBean("propertyConfigurer");
    assertNotNull(propConfigurer);
    
    BloombergReferenceDataProvider refDataProvider = (BloombergReferenceDataProvider)_serverContext.getBean("refDataProvider");
    assertNotNull(refDataProvider);
    assertNotNull(refDataProvider.getSessionOptions());
    assertNotNull(refDataProvider.getSessionOptions().getServerHost());
    
    CachingReferenceDataProvider cachingRefDataProvider = (CachingReferenceDataProvider)_serverContext.getBean("cachingRefDataProvider");
    assertNotNull(cachingRefDataProvider);
    
    ReferenceDataProviderRequestReceiver requestReceiver = (ReferenceDataProviderRequestReceiver)_serverContext.getBean("requestReceiver");
    assertNotNull(requestReceiver);
    assertSame(cachingRefDataProvider, requestReceiver.getUnderlying());
    
    FudgeRequestDispatcher requestDispatcher = (FudgeRequestDispatcher)_serverContext.getBean("requestDispatcher");
    assertNotNull(requestDispatcher);
    assertSame(requestReceiver, requestDispatcher.getUnderlying());
    
    JmsByteArrayRequestDispatcher jmsByteArrayRequestDispatcher = (JmsByteArrayRequestDispatcher)_serverContext.getBean("jmsByteArrayRequestDispatcher");
    assertNotNull(jmsByteArrayRequestDispatcher);
    assertSame(requestDispatcher, jmsByteArrayRequestDispatcher.getUnderlying());
    
    
    ActiveMQConnectionFactory factory = (ActiveMQConnectionFactory)_serverContext.getBean("jmsConnectionFactory");
    assertNotNull(factory);
    
    DefaultMessageListenerContainer jmsContainer = (DefaultMessageListenerContainer)_serverContext.getBean("jmsContainer");
    assertNotNull(jmsContainer);
    assertSame(jmsByteArrayRequestDispatcher, jmsContainer.getMessageListener());
    assertSame(factory, jmsContainer.getConnectionFactory());
    assertEquals("refDataRequestQueue", jmsContainer.getDestinationName());
    assertFalse(jmsContainer.isPubSubDomain());
    
  }

  /**
   * 
   */
  private void assertClientSpringConfig() { 
    PropertyPlaceholderConfigurer propConfigurer= (PropertyPlaceholderConfigurer)_serverContext.getBean("propertyConfigurer");
    assertNotNull(propConfigurer);
    
    ActiveMQConnectionFactory connFactory = (ActiveMQConnectionFactory)_serverContext.getBean("jmsConnectionFactory");
    assertNotNull(connFactory);
    
    JmsTemplate jmsTemplate = (JmsTemplate)_clientContext.getBean("jmsTemplate");
    assertNotNull(jmsTemplate);
    assertNotNull(jmsTemplate.getConnectionFactory());
    assertFalse(jmsTemplate.isPubSubDomain());
    
    JmsByteArrayRequestSender jmsByteArrayRequestSender = (JmsByteArrayRequestSender)_clientContext.getBean("jmsByteArrayRequestSender");
    assertNotNull(jmsByteArrayRequestSender);
    
    ByteArrayFudgeRequestSender byteArrayFudgeRequestSender = (ByteArrayFudgeRequestSender)_clientContext.getBean("byteArrayFudgeRequestSender");
    assertNotNull(byteArrayFudgeRequestSender);
    
    ReferenceDataProvider remoteReferenceDataProvider = (RemoteReferenceDataProvider)_clientContext.getBean("remoteReferenceDataProvider");
    assertNotNull(remoteReferenceDataProvider);
  }

  @Override
  protected void stopProvider() throws Exception {
    tearDownSpring();
  }
  
}
