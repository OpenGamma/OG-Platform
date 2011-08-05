/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.listener;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.List;

import javax.jms.ConnectionFactory;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.util.test.ActiveMQTestUtil;
import com.opengamma.util.tuple.Pair;

/**
 * Test JmsMasterChangeManager.
 */
@Test
public class JmsMasterChangeManagerTest {

  private static final long WAIT_TIMEOUT = 30000;
  private JmsTemplate _jmsTemplate;
  private TestMasterChangeClient _testListener;
  private JmsMasterChangeManager _changeManager;
  private InMemoryConfigMaster _configMaster;
  private String _topic;
  private DefaultMessageListenerContainer _container;

  @BeforeMethod
  public void setUp() throws Exception {
    ConnectionFactory cf = ActiveMQTestUtil.createTestConnectionFactory();
    JmsTemplate jmsTemplate = new JmsTemplate();
    jmsTemplate.setConnectionFactory(cf);
    jmsTemplate.setPubSubDomain(true);
    _jmsTemplate = jmsTemplate;
    
    // setup topic
    long currentTimeMillis = System.currentTimeMillis();
    String user = System.getProperty("user.name");
    _topic = "JmsMasterChange-" + user + "-" + currentTimeMillis;
    
    _testListener = new TestMasterChangeClient();
    _changeManager = new JmsMasterChangeManager();
    _changeManager.setJmsTemplate(_jmsTemplate);
    _changeManager.setTopic(_topic);
    
    _container = new DefaultMessageListenerContainer();
    _container.setConnectionFactory(cf);
    _container.setPubSubDomain(true);
    _container.setDestinationName(_topic);
    _container.setMessageListener(_changeManager);
    
    // create a config master
    _configMaster = new InMemoryConfigMaster(_changeManager);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    if (_container != null) {
      _container.stop();
      _container.destroy();
    }
  }

  private void startContainer() throws Exception {
    _container.afterPropertiesSet();
    _container.start();
    while (!_container.isRunning()) {
      Thread.sleep(10l);
    }
  }

  //-------------------------------------------------------------------------
  public void testAdded() throws Exception {
    _changeManager.addChangeListener(_testListener);
    startContainer();
    
    final ConfigDocument<ExternalId> doc = createTestDocument();
    ConfigDocument<?> added = _configMaster.add(doc);
    UniqueId addedItem = added.getUniqueId();
    assertNotNull(addedItem);
    
    _testListener.waitForAddedItem(WAIT_TIMEOUT);
    assertEquals(addedItem, _testListener.getAddedItem());
  }

  public void testRemoved() throws Exception {
    _changeManager.addChangeListener(_testListener);
    startContainer();
    
    final ConfigDocument<ExternalId> doc = createTestDocument();
    ConfigDocument<?> added = _configMaster.add(doc);
    UniqueId uniqueId = added.getUniqueId();
    assertNotNull(uniqueId);
    
    _configMaster.remove(uniqueId);
    UniqueId removedItem = uniqueId;
    _testListener.waitForRemovedItem(WAIT_TIMEOUT);
    assertEquals(removedItem, _testListener.getRemovedItem());
  }

  public void testUpdated() throws Exception {
    _changeManager.addChangeListener(_testListener);
    startContainer();
    
    final ConfigDocument<ExternalId> doc = createTestDocument();
    ConfigDocument<?> added = _configMaster.add(doc);
    UniqueId oldItem = added.getUniqueId();
    assertNotNull(oldItem);
    
    ConfigDocument<?> updated = _configMaster.update(added);
    UniqueId newItem = updated.getUniqueId();
    assertNotNull(newItem);
    
    _testListener.waitForUpdatedItem(WAIT_TIMEOUT);
    assertEquals(Pair.of(oldItem, newItem), _testListener.getUpdatedItem());
  }

  public void testMultipleListeners() throws Exception {
    //setup multiple master change listener
    List<TestMasterChangeClient> clients = Lists.newArrayList();
    for (int i = 0; i < 2; i++) {
      TestMasterChangeClient client = new TestMasterChangeClient();
      _changeManager.addChangeListener(client);
      clients.add(client);
    }
    startContainer();
    
    // add, update and remove doc in config master
    final ConfigDocument<ExternalId> doc = createTestDocument();
    ConfigDocument<?> added = _configMaster.add(doc);
    UniqueId addedItem = added.getUniqueId();
    assertNotNull(addedItem);
    
    ConfigDocument<?> updated = _configMaster.update(added);
    UniqueId updatedItem = updated.getUniqueId();
    
    UniqueId removedItem = addedItem;
    _configMaster.remove(removedItem);
    
    for (TestMasterChangeClient client : clients) {
      client.waitForAddedItem(WAIT_TIMEOUT);
      client.waitForRemovedItem(WAIT_TIMEOUT);
      client.waitForUpdatedItem(WAIT_TIMEOUT);
    }
    
    // assert items
    assertEquals(2, clients.size());
    for (TestMasterChangeClient client : clients) {
      assertEquals(addedItem, client.getAddedItem());
      assertEquals(removedItem, client.getRemovedItem());
      assertEquals(Pair.of(addedItem, updatedItem), client.getUpdatedItem());
    }
  }

  private ConfigDocument<ExternalId> createTestDocument() {
    final ConfigDocument<ExternalId> doc = new ConfigDocument<ExternalId>(ExternalId.class);
    doc.setName("TEST");
    doc.setValue(ExternalId.of("A", "B"));
    return doc;
  }

}
