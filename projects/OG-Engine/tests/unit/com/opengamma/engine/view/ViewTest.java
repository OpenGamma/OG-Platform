/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

import com.opengamma.engine.view.client.ViewClientImpl;
import com.opengamma.engine.view.client.ViewClientState;
import com.opengamma.engine.view.compilation.ViewEvaluationModel;

/**
 * Tests View
 */
public class ViewTest {

  @Test
  public void testLifecycle() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewImpl view = (ViewImpl) vp.getView(env.getViewDefinition().getName(), ViewProcessorTestEnvironment.TEST_USER);
    view.start();
    
    assertEquals(env.getViewDefinition().getName(), view.getName());
    
    view.init();
    
    // Repeated call should be ignored
    view.init();
    
    view.stop();
  }
  
  @Test
  public void testLifecycleWithoutIniting() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewImpl view = (ViewImpl) vp.getView(env.getViewDefinition().getName(), ViewProcessorTestEnvironment.TEST_USER);
    assertTrue(view.isRunning());
    view.start();
    assertTrue(view.isRunning());
    view.stop();
    assertFalse(view.isRunning());
  }
  
  @Test
  public void testViewAccessors() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewImpl view = (ViewImpl) vp.getView(env.getViewDefinition().getName(), ViewProcessorTestEnvironment.TEST_USER);
    view.init();
    
    view.assertAccessToLiveDataRequirements(ViewProcessorTestEnvironment.TEST_USER);
    assertNull(view.getLatestResult());
    assertEquals(env.getViewDefinition(), view.getDefinition());
    assertEquals(Collections.emptySet(), view.getAllSecurityTypes());
  }
  
  @Test
  public void testCreateClient() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewImpl view = (ViewImpl) vp.getView(env.getViewDefinition().getName(), ViewProcessorTestEnvironment.TEST_USER);
    view.start();
    view.init();
    
    ViewClientImpl client = (ViewClientImpl) view.createClient(ViewProcessorTestEnvironment.TEST_USER);
    assertEquals(ViewClientState.STOPPED, client.getState());
    
    assertNotNull(client.getUniqueId());
    assertEquals(client, view.getClient(client.getUniqueId()));
    
    view.stop();
    
    // Should automatically shut down the client
    assertEquals(ViewClientState.TERMINATED, client.getState());
  }
  
  @Test
  public void testGraphRebuild () {
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment ();
    env.init ();
    final ViewProcessorImpl vp = env.getViewProcessor ();
    vp.start ();
    final ViewImpl view = (ViewImpl)vp.getView(env.getViewDefinition ().getName (), ViewProcessorTestEnvironment.TEST_USER);
    view.init ();
    ViewEvaluationModel originalModel = view.getViewEvaluationModel();
    assertNotNull (originalModel);
    final long time0 = System.currentTimeMillis ();
    view.runOneCycle (time0);
    view.runOneCycle (time0 + 10);
    // The test graph doesn't refer to functions which expire, so there should have been no re-build of the graphs
    assertSame (originalModel, view.getViewEvaluationModel ());
    // Trick the view into thinking it needs to rebuild after time0 + 20
    ViewEvaluationModel dummy = new ViewEvaluationModel (originalModel.getDependencyGraphsByConfiguration(), originalModel.getPortfolio(), originalModel.getFunctionInitId()) {
      @Override
      public boolean isValidFor (final long timestamp) {
        return (timestamp <= time0 + 20);
      }
    };
    view.setViewEvaluationModel(dummy);
    // Running at time0 + 20 doesn't require a rebuild - should still use our dummy
    view.runOneCycle (time0 + 20);
    assertSame (dummy, view.getViewEvaluationModel ());
    // time0 + 30 requires a rebuild
    view.runOneCycle (time0 + 30);
    assertNotSame (dummy, view.getViewEvaluationModel ());
    
    // run a BATCH cycle. Even though a new evaluation model is built internally,
    // it should not change the (live) evaluation model of the view 
    dummy = new ViewEvaluationModel (originalModel.getDependencyGraphsByConfiguration(), originalModel.getPortfolio(), originalModel.getFunctionInitId()) {
      @Override
      public boolean isValidFor (final long timestamp) {
        return false;
      }
    };
    view.setViewEvaluationModel(dummy);
    view.runOneCycle(time0, view.getLiveDataSnapshotProvider(), null);
    assertSame(dummy, view.getViewEvaluationModel());
  }
  
}
