/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

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
public class RemoteReferenceDataProviderWithInMemoryTransportTest extends BloombergReferenceDataProviderTestCase {

  private ConfigurableApplicationContext _appContext;
  
  private void setUpSpring() throws Exception {
    ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("/com/opengamma/bbg/test-inMemory-remoteRefDataProvider-context.xml");
    context.start();
    _appContext = context;
  }
  
  private void tearDownSpring() throws Exception {
    if (_appContext != null) {
      ConfigurableApplicationContext context = _appContext;
      _appContext = null;
      context.close();
    }    
  }
 
  private void assertSpringConfig() throws Exception {
    PropertyPlaceholderConfigurer propConfigurer= (PropertyPlaceholderConfigurer)_appContext.getBean("propertyConfigurer");
    assertNotNull(propConfigurer);
    
    BloombergReferenceDataProvider refDataProvider = (BloombergReferenceDataProvider)_appContext.getBean("refDataProvider");
    assertNotNull(refDataProvider);
    assertNotNull(refDataProvider.getSessionOptions());
    assertNotNull(refDataProvider.getSessionOptions().getServerHost());
    
    CachingReferenceDataProvider cachingRefDataProvider = (CachingReferenceDataProvider)_appContext.getBean("cachingRefDataProvider");
    assertNotNull(cachingRefDataProvider);
    
    ReferenceDataProviderRequestReceiver requestReceiver = (ReferenceDataProviderRequestReceiver)_appContext.getBean("requestReceiver");
    assertNotNull(requestReceiver);
    assertSame(cachingRefDataProvider, requestReceiver.getUnderlying());
    
    FudgeRequestDispatcher requestDispatcher = (FudgeRequestDispatcher)_appContext.getBean("requestDispatcher");
    assertNotNull(requestDispatcher);
    assertSame(requestReceiver, requestDispatcher.getUnderlying());
    
    JmsByteArrayRequestDispatcher jmsByteArrayRequestDispatcher = (JmsByteArrayRequestDispatcher)_appContext.getBean("jmsByteArrayRequestDispatcher");
    assertNotNull(jmsByteArrayRequestDispatcher);
    assertSame(requestDispatcher, jmsByteArrayRequestDispatcher.getUnderlying());
    
    
    ActiveMQConnectionFactory factory = (ActiveMQConnectionFactory)_appContext.getBean("jmsConnectionFactory");
    assertNotNull(factory);
    
    DefaultMessageListenerContainer jmsContainer = (DefaultMessageListenerContainer)_appContext.getBean("jmsContainer");
    assertNotNull(jmsContainer);
    assertSame(jmsByteArrayRequestDispatcher, jmsContainer.getMessageListener());
    assertSame(factory, jmsContainer.getConnectionFactory());
    assertFalse(jmsContainer.isPubSubDomain());
    
    JmsTemplate jmsTemplate = (JmsTemplate)_appContext.getBean("jmsTemplate");
    assertNotNull(jmsTemplate);
    assertNotNull(jmsTemplate.getConnectionFactory());
    assertFalse(jmsTemplate.isPubSubDomain());
    
    JmsByteArrayRequestSender jmsByteArrayRequestSender = (JmsByteArrayRequestSender)_appContext.getBean("jmsByteArrayRequestSender");
    assertNotNull(jmsByteArrayRequestSender);
    
    ByteArrayFudgeRequestSender byteArrayFudgeRequestSender = (ByteArrayFudgeRequestSender)_appContext.getBean("byteArrayFudgeRequestSender");
    assertNotNull(byteArrayFudgeRequestSender);
    
    ReferenceDataProvider remoteReferenceDataProvider = (RemoteReferenceDataProvider)_appContext.getBean("remoteReferenceDataProvider");
    assertNotNull(remoteReferenceDataProvider);
    
  }

  @Override
  protected ReferenceDataProvider createReferenceDataProvider(Class<?> c) throws Exception {
    setUpSpring();
    assertSpringConfig();
    DefaultMessageListenerContainer jmsContainer = (DefaultMessageListenerContainer)_appContext.getBean("jmsContainer");
    BloombergReferenceDataProvider refDataProvider = (BloombergReferenceDataProvider)_appContext.getBean("refDataProvider");
    while(!jmsContainer.isRunning() && !refDataProvider.isRunning()) {
      Thread.sleep(10l);
    }
    
    ReferenceDataProvider remoteReferenceDataProvider = (RemoteReferenceDataProvider)_appContext.getBean("remoteReferenceDataProvider");
    assertNotNull(remoteReferenceDataProvider);
    return remoteReferenceDataProvider;
  }

  @Override
  protected void stopProvider() throws Exception {
    tearDownSpring();
  }
  
}
