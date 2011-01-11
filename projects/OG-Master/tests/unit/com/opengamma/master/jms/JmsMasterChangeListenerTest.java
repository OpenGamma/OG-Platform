/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.ConnectionFactory;

import org.fudgemsg.FudgeContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.google.common.collect.Lists;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.impl.InMemoryConfigTypeMaster;
import com.opengamma.util.test.ActiveMQTestUtil;
import com.opengamma.util.tuple.Pair;

public class JmsMasterChangeListenerTest {

  private static final long WAIT_TIMEOUT = 30000;
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(JmsMasterChangeListenerTest.class);
  private JmsTemplate _jmsTemplate;
  private FudgeContext _fudgeContext = FudgeContext.GLOBAL_DEFAULT;
  private TestMasterChangeClient _testListener;
  private JmsMasterChangeSubscriber _masterChangeSubscriber;
  private JmsMasterChangePublisher _masterChangePublisher;
  private InMemoryConfigTypeMaster<Identifier> _configMaster;
  private String _removedTopic;
  private String _addedTopic;
  private String _updatedTopic;
  private String _correctedTopic;
  private Map<String, DefaultMessageListenerContainer> _containers = new HashMap<String, DefaultMessageListenerContainer>();
  
  @Before
  public void setUp() throws Exception {
    ConnectionFactory cf = ActiveMQTestUtil.createTestConnectionFactory();
    JmsTemplate jmsTemplate = new JmsTemplate();
    jmsTemplate.setConnectionFactory(cf);
    jmsTemplate.setPubSubDomain(true);
    _jmsTemplate = jmsTemplate;
    
    //create master change client
    _testListener = new TestMasterChangeClient();
    //create the jms master change subscriber
    _masterChangeSubscriber = new JmsMasterChangeSubscriber(_fudgeContext);
    //register client with jms subscriber
    _masterChangeSubscriber.addChangeListener(_testListener);
    
    //setup topics
    long currentTimeMillis = System.currentTimeMillis();
    String user = System.getProperty("user.name");
    List<String> topics = createTopics(currentTimeMillis, user);
    
    createMessageListenerPerTopic(topics, cf);
    
    //create a config master
    _configMaster = new InMemoryConfigTypeMaster<Identifier>();
    
    //create jms master change publisher
    _masterChangePublisher = new JmsMasterChangePublisher(_fudgeContext);
    _masterChangePublisher.setJmsTemplate(_jmsTemplate);
    _masterChangePublisher.setAddedTopic(_addedTopic);
    _masterChangePublisher.setCorrectedTopic(_correctedTopic);
    _masterChangePublisher.setUpdatedTopic(_updatedTopic);
    _masterChangePublisher.setRemovedTopic(_removedTopic);
    
    //register jms publisher with config master
    _configMaster.addChangeListener(_masterChangePublisher);
  }

  private void createMessageListenerPerTopic(List<String> topics, ConnectionFactory cf) {
    for (String topic : topics) {
      DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
      container.setConnectionFactory(cf);
      container.setMessageListener(_masterChangeSubscriber);
      container.setDestinationName(_addedTopic);
      container.setPubSubDomain(true);
      container.setDestinationName(topic);
      _containers.put(topic, container);
    }
  }

  private List<String> createTopics(long currentTimeMillis, String user) {
    List<String> topics = Lists.newArrayList();
    _removedTopic = "JmsMasterChange-removed-" + user + "-" + currentTimeMillis;
    topics.add(_removedTopic);
    _addedTopic = "JmsMasterChange-added-" + user + "-" + currentTimeMillis;
    topics.add(_addedTopic);
    _updatedTopic = "JmsMasterChange-updated-" + user + "-" + currentTimeMillis;
    topics.add(_updatedTopic);
    _correctedTopic = "JmsMasterChange-corrected-" + user + "-" + currentTimeMillis;
    topics.add(_correctedTopic);
    return topics;
  }

  @After
  public void tearDown() throws Exception {
    for (DefaultMessageListenerContainer container : _containers.values()) {
      if (container != null) {
        container.stop();
        container.destroy();
      }
    }
  }

  @Test
  public void testAdded() throws Exception {
    DefaultMessageListenerContainer listenerContainer = _containers.get(_addedTopic);
    listenerContainer.afterPropertiesSet();
    listenerContainer.start();
    
    while(!listenerContainer.isRunning()) {
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
    DefaultMessageListenerContainer listenerContainer = _containers.get(_removedTopic);
    listenerContainer.afterPropertiesSet();
    listenerContainer.start();
    
    while(!listenerContainer.isRunning()) {
      Thread.sleep(10l);
    }
    
    final ConfigDocument<Identifier> doc = createTestDocument();
    ConfigDocument<Identifier> added = _configMaster.add(doc);
    UniqueIdentifier uid = added.getUniqueId();
    assertNotNull(uid);
    
    _configMaster.remove(uid);
    UniqueIdentifier removedItem = uid;    
    _testListener.waitForRemovedItem(WAIT_TIMEOUT);
    assertEquals(removedItem, _testListener.getRemovedItem());
  }
  
  @Test
  public void testUpdated() throws Exception {
    DefaultMessageListenerContainer listenerContainer = _containers.get(_updatedTopic);
    listenerContainer.afterPropertiesSet();
    listenerContainer.start();
    
    while(!listenerContainer.isRunning()) {
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
  public void testCorrected() throws Exception {
    DefaultMessageListenerContainer listenerContainer = _containers.get(_correctedTopic);
    listenerContainer.afterPropertiesSet();
    listenerContainer.start();
    
    while(!listenerContainer.isRunning()) {
      Thread.sleep(10l);
    }
    
    final ConfigDocument<Identifier> doc = createTestDocument();
    ConfigDocument<Identifier> added = _configMaster.add(doc);
    UniqueIdentifier oldItem = added.getUniqueId();
    assertNotNull(oldItem);
    
    ConfigDocument<Identifier> corrected = _configMaster.correct(added);
    UniqueIdentifier newItem = corrected.getUniqueId();
    assertNotNull(newItem);
    
    _testListener.waitForCorrectedItem(WAIT_TIMEOUT);
    assertEquals(Pair.of(oldItem, newItem), _testListener.getCorrectedItem());
  }
  
  @Test
  public void testMultipleListeners() throws Exception {    
    //setup multiple master change listener
    List<TestMasterChangeClient> clients = Lists.newArrayList();
    for (int i = 0; i < 2; i++) {
      TestMasterChangeClient client = new TestMasterChangeClient();
      _masterChangeSubscriber.addChangeListener(client);
      clients.add(client);
    }
    
    for (DefaultMessageListenerContainer container : _containers.values()) {
      container.afterPropertiesSet();
      container.start();
      while(!container.isRunning()) {
        Thread.sleep(10l);
      }
    }
    
    //add, update, correct and remove doc in config master
    final ConfigDocument<Identifier> doc = createTestDocument();
    ConfigDocument<Identifier> added = _configMaster.add(doc);
    UniqueIdentifier addedItem = added.getUniqueId();
    assertNotNull(addedItem);
    
    ConfigDocument<Identifier> corrected = _configMaster.correct(added);
    UniqueIdentifier correctedItem = corrected.getUniqueId();
    assertNotNull(correctedItem);
    
    ConfigDocument<Identifier> updated = _configMaster.update(added);
    UniqueIdentifier updatedItem = updated.getUniqueId();
    
    UniqueIdentifier removedItem = addedItem;
    _configMaster.remove(removedItem);
    
    for (TestMasterChangeClient client : clients) {
      client.waitForAddedItem(WAIT_TIMEOUT);
      client.waitForCorrectedItem(WAIT_TIMEOUT);
      client.waitForRemovedItem(WAIT_TIMEOUT);
      client.waitForUpdatedItem(WAIT_TIMEOUT);
    }
    
    //assert items
    assertEquals(2, clients.size());
    for (TestMasterChangeClient client : clients) {
      assertEquals(addedItem, client.getAddedItem());
      assertEquals(removedItem, client.getRemovedItem());
      assertEquals(Pair.of(addedItem, correctedItem), client.getCorrectedItem());
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
