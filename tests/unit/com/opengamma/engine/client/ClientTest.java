/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Test;

import com.opengamma.engine.test.MockView;
import com.opengamma.engine.test.MockViewProcessor;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.livedata.msg.UserPrincipal;

/**
 * Tests Client
 */
public class ClientTest {

  @Test
  public void testObtainClient() {
    MockViewProcessor viewProcessor = new MockViewProcessor();
    MockView view = new MockView("Test");
    viewProcessor.addView(view);
    
    ClientManager manager = new ClientManager(viewProcessor);
    Client client = manager.createClient("Test", UserPrincipal.getLocalUser());
    
    assertEquals(client.getUser(), UserPrincipal.getLocalUser());
  }
  
  @Test
  public void testSubscriptionToComputationResults() {
    MockViewProcessor viewProcessor = new MockViewProcessor();
    MockView view = new MockView("Test");
    viewProcessor.addView(view);
    
    ClientManager manager = new ClientManager(viewProcessor);
    Client client = manager.createClient("Test", UserPrincipal.getLocalUser());
    
    TestComputationResultListener resultListener = new TestComputationResultListener();
    client.setResultListener(resultListener);
    
    // Client not started - should not be listening
    view.newResult(mock(ViewComputationResultModel.class), null);
    assertEquals(0, resultListener.popResults().size());
    
    client.start();
    
    ViewComputationResultModel result1 = mock(ViewComputationResultModel.class);
    view.newResult(result1, null);
    ViewComputationResultModel result2 = mock(ViewComputationResultModel.class);
    view.newResult(result2, null);
    
    List<ViewComputationResultModel> results = resultListener.popResults();
    assertEquals(2, results.size());
    assertEquals(result1, results.get(0));
    assertEquals(result2, results.get(1));
    
    client.pause();
    
    ViewComputationResultModel result3 = mock(ViewComputationResultModel.class);
    view.newResult(result3, null);
    ViewComputationResultModel result4 = mock(ViewComputationResultModel.class);
    view.newResult(result4, null);
    
    // Should have been merging results received in the meantime
    client.start();
    
    results = resultListener.popResults();
    assertEquals(1, results.size());
    assertEquals(result4, results.get(0));
  }
  
  @Test
  public void testSubscriptionToDeltaResults() {
    MockViewProcessor viewProcessor = new MockViewProcessor();
    MockView view = new MockView("Test");
    viewProcessor.addView(view);
    
    ClientManager manager = new ClientManager(viewProcessor);
    Client client = manager.createClient("Test", UserPrincipal.getLocalUser());
    
    TestDeltaResultListener resultListener = new TestDeltaResultListener();
    client.setDeltaResultListener(resultListener);
    
    // Client stopped - should not be listening
    ViewComputationResultModel result1c = mock(ViewComputationResultModel.class);
    view.newResult(result1c, mock(ViewDeltaResultModel.class));
    assertEquals(0, resultListener.popResults().size());
    assertEquals(result1c, client.getLatestResult());
    
    client.start();
    assertEquals(result1c, client.getLatestResult());
    
    ViewDeltaResultModel result1 = mock(ViewDeltaResultModel.class);
    view.newResult(mock(ViewComputationResultModel.class), result1);
    ViewDeltaResultModel result2 = mock(ViewDeltaResultModel.class);
    view.newResult(mock(ViewComputationResultModel.class), result2);
    
    List<ViewDeltaResultModel> results = resultListener.popResults();
    assertEquals(2, results.size());
    assertEquals(result1, results.get(0));
    assertEquals(result2, results.get(1));
    
    client.pause();

    ViewDeltaResultModel result3 = mock(ViewDeltaResultModel.class);
    view.newResult(mock(ViewComputationResultModel.class), result3);
    ViewDeltaResultModel result4d = mock(ViewDeltaResultModel.class);
    ViewComputationResultModel result4c = mock(ViewComputationResultModel.class);
    view.newResult(result4c, result4d);
    
    // Should have been merging results received in the meantime
    client.start();
    
    results = resultListener.popResults();
    assertEquals(1, results.size());
    assertEquals(result4c, client.getLatestResult());
  }
  
  @Test
  public void testSubscriptionToComputationAndDeltaResults() {
    MockViewProcessor viewProcessor = new MockViewProcessor();
    MockView view = new MockView("Test");
    viewProcessor.addView(view);
    
    ClientManager manager = new ClientManager(viewProcessor);
    Client client = manager.createClient("Test", UserPrincipal.getLocalUser());
    
    TestDeltaResultListener deltaListener = new TestDeltaResultListener();
    client.setDeltaResultListener(deltaListener);
    
    TestComputationResultListener computationListener = new TestComputationResultListener();
    client.setResultListener(computationListener);
    
    // Client stopped - should not be listening
    view.newResult(mock(ViewComputationResultModel.class), mock(ViewDeltaResultModel.class));
    assertEquals(0, deltaListener.popResults().size());
    assertEquals(0, computationListener.popResults().size());
    
    client.start();
    
    ViewComputationResultModel result1c = mock(ViewComputationResultModel.class);
    ViewDeltaResultModel result1d = mock(ViewDeltaResultModel.class);
    view.newResult(result1c, result1d);
    ViewComputationResultModel result2c = mock(ViewComputationResultModel.class);
    ViewDeltaResultModel result2d = mock(ViewDeltaResultModel.class);
    view.newResult(result2c, result2d);
    
    List<ViewDeltaResultModel> deltaResults = deltaListener.popResults();
    assertEquals(2, deltaResults.size());
    assertEquals(result1d, deltaResults.get(0));
    assertEquals(result2d, deltaResults.get(1));
    
    List<ViewComputationResultModel> computationResults = computationListener.popResults();
    assertEquals(2, computationResults.size());
    assertEquals(result1c, computationResults.get(0));
    assertEquals(result2c, computationResults.get(1));    
    
    client.pause();

    ViewComputationResultModel result3c = mock(ViewComputationResultModel.class);
    ViewDeltaResultModel result3d = mock(ViewDeltaResultModel.class);
    view.newResult(result3c, result3d);
    ViewComputationResultModel result4c = mock(ViewComputationResultModel.class);
    ViewDeltaResultModel result4d = mock(ViewDeltaResultModel.class);
    view.newResult(result4c, result4d);
    
    // Should have been merging results received in the meantime
    client.start();
    
    deltaResults = deltaListener.popResults();
    assertEquals(1, deltaResults.size());
    
    computationResults = computationListener.popResults();
    assertEquals(1, computationResults.size());
    assertEquals(result4c, computationResults.get(0));
  }
  
  @Test
  public void testStates() {
    MockViewProcessor viewProcessor = new MockViewProcessor();
    MockView view = new MockView("Test");
    viewProcessor.addView(view);
    
    ClientManager manager = new ClientManager(viewProcessor);
    Client client = manager.createClient("Test", UserPrincipal.getLocalUser());
    
    TestComputationResultListener resultListener = new TestComputationResultListener();
    client.setResultListener(resultListener);

    view.newResult(mock(ViewComputationResultModel.class), null);
    view.newResult(mock(ViewComputationResultModel.class), null);
    assertEquals(0, resultListener.popResults().size());
    
    client.start();
    
    view.newResult(mock(ViewComputationResultModel.class), null);
    view.newResult(mock(ViewComputationResultModel.class), null);
    assertEquals(2, resultListener.popResults().size());
    
    client.pause();
    
    view.newResult(mock(ViewComputationResultModel.class), null);
    view.newResult(mock(ViewComputationResultModel.class), null);
    assertEquals(0, resultListener.popResults().size());
    
    // Should have been merging results received in the meantime
    client.start();
    assertEquals(1, resultListener.popResults().size());
    
    view.newResult(mock(ViewComputationResultModel.class), null);
    view.newResult(mock(ViewComputationResultModel.class), null);
    assertEquals(2, resultListener.popResults().size());
    
    client.pause();
    
    view.newResult(mock(ViewComputationResultModel.class), null);
    view.newResult(mock(ViewComputationResultModel.class), null);
    assertEquals(0, resultListener.popResults().size());
    
    client.disconnect();
    assertEquals(0, resultListener.popResults().size());
    
    client.disconnect();
  }
  
  @Test(expected = IllegalStateException.class)
  public void testUseDisconnectedClient() {
    MockViewProcessor viewProcessor = new MockViewProcessor();
    MockView view = new MockView("Test");
    viewProcessor.addView(view);
    
    ClientManager manager = new ClientManager(viewProcessor);
    Client client = manager.createClient("Test", UserPrincipal.getLocalUser());
    
    client.disconnect();
    
    client.start();
  }
  
  @Test
  public void testChangeOfListeners() {
    MockViewProcessor viewProcessor = new MockViewProcessor();
    MockView view = new MockView("Test");
    viewProcessor.addView(view);
    
    ClientManager manager = new ClientManager(viewProcessor);
    Client client = manager.createClient("Test", UserPrincipal.getLocalUser());
    
    TestDeltaResultListener deltaListener1 = new TestDeltaResultListener();
    client.setDeltaResultListener(deltaListener1);
    TestComputationResultListener computationListener1 = new TestComputationResultListener();
    client.setResultListener(computationListener1);
    assertFalse(view.isRunning());
    
    view.newResult(mock(ViewComputationResultModel.class), mock(ViewDeltaResultModel.class));
    view.newResult(mock(ViewComputationResultModel.class), mock(ViewDeltaResultModel.class));
    assertEquals(0, computationListener1.popResults().size());
    assertEquals(0, deltaListener1.popResults().size());
    
    client.start();
    assertTrue(view.isRunning());
    
    view.newResult(mock(ViewComputationResultModel.class), mock(ViewDeltaResultModel.class));
    view.newResult(mock(ViewComputationResultModel.class), mock(ViewDeltaResultModel.class));
    assertEquals(2, computationListener1.popResults().size());
    assertEquals(2, deltaListener1.popResults().size());
    
    TestDeltaResultListener deltaListener2 = new TestDeltaResultListener();
    client.setDeltaResultListener(deltaListener2);
    TestComputationResultListener computationListener2 = new TestComputationResultListener();
    client.setResultListener(computationListener2);
    assertTrue(view.isRunning());
    
    view.newResult(mock(ViewComputationResultModel.class), mock(ViewDeltaResultModel.class));
    view.newResult(mock(ViewComputationResultModel.class), mock(ViewDeltaResultModel.class));
    assertEquals(0, computationListener1.popResults().size());
    assertEquals(2, computationListener2.popResults().size());
    assertEquals(0, deltaListener1.popResults().size());
    assertEquals(2, deltaListener2.popResults().size());
    
    client.pause();
    assertTrue(view.isRunning());
    
    view.newResult(mock(ViewComputationResultModel.class), mock(ViewDeltaResultModel.class));
    view.newResult(mock(ViewComputationResultModel.class), mock(ViewDeltaResultModel.class));
    assertEquals(0, computationListener1.popResults().size());
    assertEquals(0, computationListener2.popResults().size());
    assertEquals(0, deltaListener1.popResults().size());
    assertEquals(0, deltaListener2.popResults().size());
    
    TestDeltaResultListener deltaListener3 = new TestDeltaResultListener();
    client.setDeltaResultListener(deltaListener3);
    TestComputationResultListener computationListener3 = new TestComputationResultListener();
    client.setResultListener(computationListener3);
    assertTrue(view.isRunning());
    
    view.newResult(mock(ViewComputationResultModel.class), mock(ViewDeltaResultModel.class));
    view.newResult(mock(ViewComputationResultModel.class), mock(ViewDeltaResultModel.class));
    assertEquals(0, computationListener1.popResults().size());
    assertEquals(0, computationListener2.popResults().size());
    assertEquals(0, computationListener3.popResults().size());
    assertEquals(0, deltaListener1.popResults().size());
    assertEquals(0, deltaListener2.popResults().size());
    assertEquals(0, deltaListener3.popResults().size());
    
    client.start();
    assertTrue(view.isRunning());
    
    assertEquals(0, computationListener1.popResults().size());
    assertEquals(0, computationListener2.popResults().size());
    assertEquals(1, computationListener3.popResults().size());
    assertEquals(0, deltaListener1.popResults().size());
    assertEquals(0, deltaListener2.popResults().size());
    assertEquals(1, deltaListener3.popResults().size());
    
    client.setResultListener(null);
    client.setDeltaResultListener(null);
    assertFalse(view.isRunning());
  }
  

  

  

  
}
