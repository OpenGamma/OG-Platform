/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Tests ViewProcessor
 */
public class ViewProcessorTest {
  
  @Test
  public void testCreateViewProcessor() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    
    assertFalse(vp.isRunning());
    
    vp.start();
    
    assertTrue(vp.isRunning());
    assertEquals(Collections.singleton(env.getViewDefinition().getName()), vp.getViewNames());
    vp.stop();
  }
  
  @Test
  public void testCreateViewProcessorLocalExecutor() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.setLocalExecutorService(true);
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    vp.stop();
  }
  
  @Test(expected = IllegalStateException.class)
  public void testAssertNotStarted() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    vp.assertNotStarted();
  }
  
  @Test
  public void testObtainKnownView() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    vp.getView(env.getViewDefinition().getName(), ViewProcessorTestEnvironment.TEST_USER);
    vp.stop();
  }
  
  @Test(expected = OpenGammaRuntimeException.class)
  public void testObtainUnknownView() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    // Exception
    vp.getView("Something random", ViewProcessorTestEnvironment.TEST_USER);
  }

}
