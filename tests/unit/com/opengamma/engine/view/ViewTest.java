/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

import com.opengamma.engine.view.client.ViewClientImpl;
import com.opengamma.engine.view.client.ViewClientState;

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
    
    assertNotNull(client.getUniqueIdentifier());
    assertEquals(client, view.getClient(client.getUniqueIdentifier()));
    
    view.stop();
    
    // Should automatically shut down the client
    assertEquals(ViewClientState.TERMINATED, client.getState());
  }
  
}
