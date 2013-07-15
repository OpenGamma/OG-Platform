/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link FunctionBlacklistRuleSet} class.
 */
@Test(groups = TestGroup.UNIT)
public class FunctionBlacklistRuleSetTest {

  public void testEmpty() throws Exception {
    final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    try {
      final FunctionBlacklistRuleSet bl = new FunctionBlacklistRuleSet(executor);
      assertTrue(bl.isEmpty());
      assertEquals(bl.size(), 0);
      assertFalse(bl.contains(new FunctionBlacklistRule()));
      assertFalse(bl.iterator().hasNext());
      assertEquals(bl.toArray().length, 0);
      assertEquals(bl.toArray(new FunctionBlacklistRule[0]).length, 0);
      assertFalse(bl.remove(new FunctionBlacklistRule()));
      assertTrue(bl.containsAll(Collections.emptyList()));
      assertFalse(bl.containsAll(Collections.singleton(new FunctionBlacklistRule())));
      assertFalse(bl.retainAll(Collections.emptyList()));
      assertFalse(bl.removeAll(Collections.emptyList()));
    } finally {
      executor.shutdown();
    }
  }

  public void testOneEntry() {
    final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    try {
      final FunctionBlacklistRuleSet bl = new FunctionBlacklistRuleSet(executor);
      bl.add(new FunctionBlacklistRule());
      assertFalse(bl.isEmpty());
      assertEquals(bl.size(), 1);
      assertTrue(bl.contains(new FunctionBlacklistRule()));
      assertTrue(bl.iterator().hasNext());
      assertEquals(bl.toArray().length, 1);
      assertEquals(bl.toArray(new FunctionBlacklistRule[0]).length, 1);
      assertTrue(bl.remove(new FunctionBlacklistRule()));
      assertEquals(bl.size(), 0);
      bl.add(new FunctionBlacklistRule());
      assertTrue(bl.containsAll(Collections.emptyList()));
      assertTrue(bl.containsAll(Collections.singleton(new FunctionBlacklistRule())));
      assertTrue(bl.retainAll(Collections.emptyList()));
      assertEquals(bl.size(), 0);
      bl.add(new FunctionBlacklistRule());
      assertFalse(bl.removeAll(Collections.emptyList()));
      assertEquals(bl.size(), 1);
      assertTrue(bl.removeAll(Collections.singleton(new FunctionBlacklistRule())));
      assertEquals(bl.size(), 0);
    } finally {
      executor.shutdown();
    }
  }

  public void testMultipleEntries() {
    final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    try {
      final FunctionBlacklistRuleSet bl = new FunctionBlacklistRuleSet(executor);
      bl.add(new FunctionBlacklistRule());
      final List<FunctionBlacklistRule> rules = new ArrayList<FunctionBlacklistRule>(10);
      for (int i = 0; i < 10; i++) {
        final FunctionBlacklistRule rule = new FunctionBlacklistRule();
        rule.setFunctionIdentifier("F" + i);
        rules.add(rule);
      }
      bl.addAll(rules);
      assertFalse(bl.isEmpty());
      assertEquals(bl.size(), 11);
      assertTrue(bl.contains(new FunctionBlacklistRule()));
      assertTrue(bl.iterator().hasNext());
      assertEquals(bl.toArray().length, 11);
      assertEquals(bl.toArray(new FunctionBlacklistRule[0]).length, 11);
      assertTrue(bl.remove(new FunctionBlacklistRule()));
      assertEquals(bl.size(), 10);
      bl.add(new FunctionBlacklistRule());
      assertTrue(bl.containsAll(Collections.emptyList()));
      assertTrue(bl.containsAll(Collections.singleton(new FunctionBlacklistRule())));
      assertTrue(bl.retainAll(Collections.singleton(new FunctionBlacklistRule())));
      assertEquals(bl.size(), 1);
      bl.addAll(rules);
      assertFalse(bl.removeAll(Collections.emptyList()));
      assertEquals(bl.size(), 11);
      assertTrue(bl.removeAll(Collections.singleton(new FunctionBlacklistRule())));
      assertEquals(bl.size(), 10);
    } finally {
      executor.shutdown();
    }
  }

}
