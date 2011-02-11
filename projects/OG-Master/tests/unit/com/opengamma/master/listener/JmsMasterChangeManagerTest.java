/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.jms.ConnectionFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.google.common.collect.Lists;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.impl.InMemoryConfigTypeMaster;
import com.opengamma.util.test.ActiveMQTestUtil;
import com.opengamma.util.tuple.Pair;

/**
 * Test JmsMasterChangeManager.
 */
public class JmsMasterChangeManagerTest {

  private static final long WAIT_TIMEOUT = 30000;
  private JmsTemplate _jmsTemplate;
  private TestMasterChangeClient _testListener;
  private JmsMasterChangeManager _changeManager;
  private InMemoryConfigTypeMaster<Identifier> _configMaster;
  private String _topic;
  private DefaultMessageListenerContainer _container;

  @Before
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
    _changeManager.addChangeListener(_testListener);
    
    _container = new DefaultMessageListenerContainer();
    _container.setConnectionFactory(cf);
    _container.setPubSubDomain(true);
    _container.setDestinationName(_topic);
    _container.setMessageListener(_changeManager);
    
    // create a config master
    _configMaster = new InMemoryConfigTypeMaster<Identifier>(_changeManager);
  }

  @After
  public void tearDown() throws Exception {
    if (_container != null) {
      _container.stop();
      _container.destroy();
    }
  }

  @Test
  public void testAdded() throws Exception {
    _container.afterPropertiesSet();
    _container.start();
    while (!_container.isRunning()) {
      Thread.sleep(10l);
    }
    
    final ConfigDocument<Identifier> doc = createTestDocument();
    ConfigDocument<Identifier> added = _configMaster.add(doc);
    UniqueIdentifier addedItem = added.getUniqueId();
    assertNotNull(addedItem);
    
    _testListener.waitForAddedItem(WAIT_TIMEOUT);
    assertEquals(addedItem, _testListener.getAddedItem());
  }

  @Test
  public void testRemoved() throws Exception {
    _container.afterPropertiesSet();
    _container.start();
    while (!_container.isRunning()) {
      Thread.sleep(10l);
    }
    
    final ConfigDocument<Identifier> doc = createTestDocument();
    ConfigDocument<Identifier> added = _configMaster.add(doc);
    UniqueIdentifier uniqueId = added.getUniqueId();
    assertNotNull(uniqueId);
    
    _configMaster.remove(uniqueId);
    UniqueIdentifier removedItem = uniqueId;
    _testListener.waitForRemovedItem(WAIT_TIMEOUT);
    assertEquals(removedItem, _testListener.getRemovedItem());
  }

  @Test
  public void testUpdated() throws Exception {
    _container.afterPropertiesSet();
    _container.start();
    while (!_container.isRunning()) {
      Thread.sleep(10l);
    }
    
    final ConfigDocument<Identifier> doc = createTestDocument();
    ConfigDocument<Identifier> added = _configMaster.add(doc);
    UniqueIdentifier oldItem = added.getUniqueId();
    assertNotNull(oldItem);
    
    ConfigDocument<Identifier> updated = _configMaster.update(added);
    UniqueIdentifier newItem = updated.getUniqueId();
    assertNotNull(newItem);
    
    _testListener.waitForUpdatedItem(WAIT_TIMEOUT);
    assertEquals(Pair.of(oldItem, newItem), _testListener.getUpdatedItem());
  }

  @Test
  public void testMultipleListeners() throws Exception {
    //setup multiple master change listener
    List<TestMasterChangeClient> clients = Lists.newArrayList();
    for (int i = 0; i < 2; i++) {
      TestMasterChangeClient client = new TestMasterChangeClient();
      _changeManager.addChangeListener(client);
      clients.add(client);
    }
    
    _container.afterPropertiesSet();
    _container.start();
    while (!_container.isRunning()) {
      Thread.sleep(10l);
    }
    
    // add, update and remove doc in config master
    final ConfigDocument<Identifier> doc = createTestDocument();
    ConfigDocument<Identifier> added = _configMaster.add(doc);
    UniqueIdentifier addedItem = added.getUniqueId();
    assertNotNull(addedItem);
    
    ConfigDocument<Identifier> updated = _configMaster.update(added);
    UniqueIdentifier updatedItem = updated.getUniqueId();
    
    UniqueIdentifier removedItem = addedItem;
    _configMaster.remove(removedItem);
    
    for (TestMasterChangeClient client : clients) {
      client.waitForAddedItem(WAIT_TIMEOUT);
      client.waitForRemovedItem(WAIT_TIMEOUT);
      client.waitForUpdatedItem(WAIT_TIMEOUT);
    }
    
    //assert items
    assertEquals(2, clients.size());
    for (TestMasterChangeClient client : clients) {
      assertEquals(addedItem, client.getAddedItem());
      assertEquals(removedItem, client.getRemovedItem());
      assertEquals(Pair.of(addedItem, updatedItem), client.getUpdatedItem());
    }
  }

  private ConfigDocument<Identifier> createTestDocument() {
    final ConfigDocument<Identifier> doc = new ConfigDocument<Identifier>();
    doc.setName("TEST");
    doc.setValue(Identifier.of("A", "B"));
    return doc;
  }

}
