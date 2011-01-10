/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

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
  
  public void testObtainUnknownView() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    View view = vp.getView("Something random", ViewProcessorTestEnvironment.TEST_USER);
    assertNull(view);
  }

}
