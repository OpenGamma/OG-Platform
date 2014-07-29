/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.spring;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.mockito.Mockito;
import org.springframework.context.Lifecycle;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link LifecycleUtils} class
 */
@Test(groups = TestGroup.UNIT)
public class LifecycleUtilsTest {

  public void testStart() {
    // No-op; not a Lifecycle
    LifecycleUtils.start("Foo");
    // Call start on implementing object
    final Lifecycle mock = Mockito.mock(Lifecycle.class);
    LifecycleUtils.start(mock);
    Mockito.verify(mock).start();
    Mockito.verifyNoMoreInteractions(mock);
  }

  public void testIsRunning() {
    // Default return value; not a Lifecycle
    assertFalse(LifecycleUtils.isRunning("Foo", false));
    assertTrue(LifecycleUtils.isRunning("Foo", true));
    // Call start on implementing object
    final Lifecycle mock = Mockito.mock(Lifecycle.class);
    Mockito.when(mock.isRunning()).thenReturn(true);
    assertTrue(LifecycleUtils.isRunning(mock, false));
  }

  public void testStop() {
    // No-op; not a Lifecycle
    LifecycleUtils.stop("Foo");
    // Call start on implementing object
    final Lifecycle mock = Mockito.mock(Lifecycle.class);
    LifecycleUtils.stop(mock);
    Mockito.verify(mock).stop();
    Mockito.verifyNoMoreInteractions(mock);
  }

}
