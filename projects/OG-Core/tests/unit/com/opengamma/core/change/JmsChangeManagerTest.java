/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.change;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import javax.time.Instant;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.id.UniqueId;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.test.ActiveMQTestUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Test {@link JmsChangeManager}.
 */
@Test
public class JmsChangeManagerTest {

  private static final long WAIT_TIMEOUT = 30000;
  private TestChangeClient _testListener;
  private JmsChangeManager _changeManager;

  @BeforeMethod
  public void setUp() throws Exception {
    long currentTimeMillis = System.currentTimeMillis();
    String user = System.getProperty("user.name");
    String topic = "JmsSourceChange-" + user + "-" + currentTimeMillis;
    JmsConnector connector = ActiveMQTestUtils.createJmsConnector().withTopicName(topic);
    
    _testListener = new TestChangeClient();
    _changeManager = new JmsChangeManager(connector);
    _changeManager.start();
  }

  @AfterMethod
  public void tearDown() throws Exception {
    if (_changeManager != null) {
      _changeManager.stop();
    }
  }

  private void startContainer() throws Exception {
    //No-op
  }

  //-------------------------------------------------------------------------
  public void testAdded() throws Exception {
    _changeManager.addChangeListener(_testListener);
    startContainer();
    
    UniqueId addedId = generateUniqueId();
    _changeManager.entityChanged(ChangeType.ADDED, null, addedId, Instant.now());
    _testListener.waitForAddedItem(WAIT_TIMEOUT);
    assertEquals(addedId, _testListener.getAddedItem());
  }

  public void testRemoved() throws Exception {
    _changeManager.addChangeListener(_testListener);
    startContainer();
    
    UniqueId removedId = generateUniqueId();
    _changeManager.entityChanged(ChangeType.REMOVED, removedId, null, Instant.now());
    _testListener.waitForRemovedItem(WAIT_TIMEOUT);
    assertEquals(removedId, _testListener.getRemovedItem());
  }

  public void testUpdated() throws Exception {
    _changeManager.addChangeListener(_testListener);
    startContainer();

    UniqueId oldId = generateUniqueId();
    UniqueId newId = generateUniqueId();
    _changeManager.entityChanged(ChangeType.UPDATED, oldId, newId, Instant.now());
    _testListener.waitForUpdatedItem(WAIT_TIMEOUT);
    assertEquals(Pair.of(oldId, newId), _testListener.getUpdatedItem());
  }

  public void testMultipleListeners() throws Exception {
    //setup multiple source change listener
    List<TestChangeClient> clients = Lists.newArrayList();
    for (int i = 0; i < 2; i++) {
      TestChangeClient client = new TestChangeClient();
      _changeManager.addChangeListener(client);
      clients.add(client);
    }
    startContainer();
    
    UniqueId v1Id = generateUniqueId();
    UniqueId v2Id = generateUniqueId();
    _changeManager.entityChanged(ChangeType.ADDED, null, v1Id, Instant.now());
    _changeManager.entityChanged(ChangeType.UPDATED, v1Id, v2Id, Instant.now());
    _changeManager.entityChanged(ChangeType.REMOVED, v2Id, null, Instant.now());
    
    for (TestChangeClient client : clients) {
      client.waitForAddedItem(WAIT_TIMEOUT);
      client.waitForRemovedItem(WAIT_TIMEOUT);
      client.waitForUpdatedItem(WAIT_TIMEOUT);
    }
    
    // assert items
    assertEquals(2, clients.size());
    for (TestChangeClient client : clients) {
      assertEquals(v1Id, client.getAddedItem());
      assertEquals(v2Id, client.getRemovedItem());
      assertEquals(Pair.of(v1Id, v2Id), client.getUpdatedItem());
    }
  }

  private UniqueId generateUniqueId() {
    return UniqueId.of("TestEntitySource", GUIDGenerator.generate().toString());
  }

}
