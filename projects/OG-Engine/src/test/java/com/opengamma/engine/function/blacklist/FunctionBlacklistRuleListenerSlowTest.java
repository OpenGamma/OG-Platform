/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.Timeout;

/**
 * Tests the {@link AbstractFunctionBlacklistRuleListener} class.
 */
@Test(groups = TestGroup.UNIT_SLOW)
public class FunctionBlacklistRuleListenerSlowTest {

  private final FunctionBlacklistRule RULE_1 = new FunctionBlacklistRule();
  private final FunctionBlacklistRule RULE_2 = new FunctionBlacklistRule(ComputationTargetSpecification.of(UniqueId.of("test", "foo")));
  private final FunctionBlacklistRule RULE_3 = new FunctionBlacklistRule(ComputationTargetSpecification.of(UniqueId.of("test", "bar")));

  private class MockBlacklist extends HashSet<FunctionBlacklistRule> implements FunctionBlacklist {

    private static final long serialVersionUID = 1L;

    private int _modificationCount;

    public MockBlacklist() {
      add(RULE_1);
    }

    @Override
    public String getName() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Set<FunctionBlacklistRule> getRules() {
      return this;
    }

    @Override
    public int getModificationCount() {
      return _modificationCount;
    }

    @Override
    public void addRuleListener(final FunctionBlacklistRuleListener listener) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void removeRuleListener(final FunctionBlacklistRuleListener listener) {
      throw new UnsupportedOperationException();
    }
  }

  private class Listener extends AbstractFunctionBlacklistRuleListener {

    private int _replaceRules;
    private int _addRule;
    private int _removeRule;

    protected Listener(final FunctionBlacklist blacklist) {
      super(blacklist);
    }

    @Override
    protected void replaceRules(final Collection<FunctionBlacklistRule> rules) {
      _replaceRules++;
    }

    @Override
    protected void addRule(final FunctionBlacklistRule rule) {
      _addRule++;
    }

    @Override
    protected void removeRule(final FunctionBlacklistRule rule) {
      _removeRule++;
    }
  }

  //-------------------------------------------------------------------------
  public void testAdd_badSequence() throws Exception {
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      final MockBlacklist blacklist = new MockBlacklist();
      final Listener listener = new Listener(blacklist);
      listener.init();
      assertEquals(listener._replaceRules, 1);
      assertEquals(listener._addRule, 0);
      assertEquals(listener._removeRule, 0);
      final CountDownLatch latch = new CountDownLatch(2);
      blacklist._modificationCount = 5;
      blacklist.add(RULE_2);
      listener.ruleAdded(5, RULE_2, executor);
      executor.submit(new Runnable() {
        @Override
        public void run() {
          latch.countDown();
        }
      });
      latch.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      assertEquals(listener._replaceRules, 2);
      assertEquals(listener._addRule, 0);
      assertEquals(listener._removeRule, 0);
    } finally {
      executor.shutdown();
    }
  }

  public void testAddMultiple_badSequence() throws Exception {
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      final MockBlacklist blacklist = new MockBlacklist();
      final Listener listener = new Listener(blacklist);
      listener.init();
      assertEquals(listener._replaceRules, 1);
      assertEquals(listener._addRule, 0);
      assertEquals(listener._removeRule, 0);
      final CountDownLatch latch = new CountDownLatch(2);
      blacklist._modificationCount = 5;
      blacklist.add(RULE_2);
      blacklist.add(RULE_3);
      listener.rulesAdded(5, Arrays.asList(RULE_2, RULE_3), executor);
      executor.submit(new Runnable() {
        @Override
        public void run() {
          latch.countDown();
        }
      });
      latch.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      assertEquals(listener._replaceRules, 2);
      assertEquals(listener._addRule, 0);
      assertEquals(listener._removeRule, 0);
    } finally {
      executor.shutdown();
    }
  }

  public void testRemove_badSequence() throws Exception {
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      final MockBlacklist blacklist = new MockBlacklist();
      final Listener listener = new Listener(blacklist);
      listener.init();
      assertEquals(listener._replaceRules, 1);
      assertEquals(listener._addRule, 0);
      assertEquals(listener._removeRule, 0);
      final CountDownLatch latch = new CountDownLatch(2);
      blacklist._modificationCount = 5;
      listener.ruleRemoved(5, RULE_2, executor);
      executor.submit(new Runnable() {
        @Override
        public void run() {
          latch.countDown();
        }
      });
      latch.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      assertEquals(listener._replaceRules, 2);
      assertEquals(listener._addRule, 0);
      assertEquals(listener._removeRule, 0);
    } finally {
      executor.shutdown();
    }
  }

  public void testRemoveMultiple_badSequence() throws Exception {
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      final MockBlacklist blacklist = new MockBlacklist();
      final Listener listener = new Listener(blacklist);
      listener.init();
      assertEquals(listener._replaceRules, 1);
      assertEquals(listener._addRule, 0);
      assertEquals(listener._removeRule, 0);
      final CountDownLatch latch = new CountDownLatch(2);
      blacklist._modificationCount = 5;
      listener.rulesRemoved(5, Arrays.asList(RULE_2, RULE_3), executor);
      executor.submit(new Runnable() {
        @Override
        public void run() {
          latch.countDown();
        }
      });
      latch.await(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      assertEquals(listener._replaceRules, 2);
      assertEquals(listener._addRule, 0);
      assertEquals(listener._removeRule, 0);
    } finally {
      executor.shutdown();
    }
  }

}
