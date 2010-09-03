/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.engine.test.MockView;
import com.opengamma.engine.test.MockViewProcessor;
import com.opengamma.livedata.msg.UserPrincipal;

/**
 * Tests ClientManager
 */
public class ClientManagerTest {

  @Test
  public void testAutomaticStartStopProcessingMultipleClientsSingleView() {
    MockViewProcessor viewProcessor = new MockViewProcessor();
    MockView view = new MockView("Test");
    viewProcessor.addView(view);
    
    ClientManager manager = new ClientManager(viewProcessor);
    Client client1 = manager.createClient("Test", UserPrincipal.getLocalUser());
    
    TestDeltaResultListener deltaListener1 = new TestDeltaResultListener();
    client1.start();
    
    client1.setDeltaResultListener(deltaListener1);
    assertTrue(view.isRunning());
    
    client1.setDeltaResultListener(null);
    assertFalse(view.isRunning());
    
    Client client2 = manager.createClient("Test", UserPrincipal.getLocalUser());
    TestDeltaResultListener deltaListener2 = new TestDeltaResultListener();
    client2.start();
    
    client2.setDeltaResultListener(deltaListener2);
    assertTrue(view.isRunning());
    
    client1.setDeltaResultListener(deltaListener1);
    assertTrue(view.isRunning());
    
    client1.setDeltaResultListener(null);
    client2.setDeltaResultListener(null);
    assertFalse(view.isRunning());
    
    TestComputationResultListener resultListener1 = new TestComputationResultListener();
    client1.setResultListener(resultListener1);
    assertTrue(view.isRunning());
    
    client1.setDeltaResultListener(deltaListener1);
    assertTrue(view.isRunning());
    
    client2.setDeltaResultListener(deltaListener2);
    client1.disconnect();
    assertTrue(view.isRunning());
    
    client2.disconnect();
    assertFalse(view.isRunning());
  }
  
}
