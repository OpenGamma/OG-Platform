/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.testng.annotations.Test;

/**
 * Tests the {@link InMemoryFunctionBlacklistProvider} class.
 */
@Test
public class InMemoryFunctionBlacklistProviderTest {

  public void testGetBlacklist() {
    final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    try {
      final InMemoryFunctionBlacklistProvider provider = new InMemoryFunctionBlacklistProvider(executor);
      final FunctionBlacklist foo = provider.getBlacklist("Foo");
      assertEquals(foo.getName(), "Foo");
      final FunctionBlacklist bar = provider.getBlacklist("Bar");
      assertEquals(bar.getName(), "Bar");
      assertNotSame(foo, bar);
      final FunctionBlacklist foo2 = provider.getBlacklist("Foo");
      assertSame(foo2, foo);
    } finally {
      executor.shutdown();
    }
  }

}
