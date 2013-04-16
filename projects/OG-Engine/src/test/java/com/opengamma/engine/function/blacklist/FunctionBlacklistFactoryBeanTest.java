/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import static org.testng.Assert.assertEquals;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link FunctionBlacklistFactoryBean} class.
 */
@Test(groups = TestGroup.UNIT)
public class FunctionBlacklistFactoryBeanTest {

  public void test() {
    final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    try {
      final FunctionBlacklistFactoryBean bean = new FunctionBlacklistFactoryBean();
      bean.setIdentifier("Foo");
      bean.setProvider(new InMemoryFunctionBlacklistProvider(executor));
      bean.afterPropertiesSet();
      final FunctionBlacklist blacklist = bean.createObject();
      assertEquals(blacklist.getName(), "Foo");
    } finally {
      executor.shutdown();
    }
  }

}
