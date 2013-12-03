/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.engine.test.TestCalculationNode;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestLifecycle;
import com.opengamma.util.test.Timeout;

/**
 * Tests the LocalNodeJobInvoker class
 */
@Test(groups = TestGroup.UNIT)
public class LocalNodeJobInvokerTest {

  private static final long TIMEOUT = Timeout.standardTimeoutMillis();

  private JobInvoker _invoker;
  private Register _register;

  @BeforeMethod
  public void setUp() {
    _invoker = null;
    _register = new Register();
  }

  public void testAddNodeWithCallbackPending() {
    TestLifecycle.begin();
    try {
      final LocalNodeJobInvoker invoker = new LocalNodeJobInvoker();
      _invoker = null;
      assertFalse(invoker.notifyWhenAvailable(_register));
      assertNull(_invoker);
      final TestCalculationNode node = new TestCalculationNode();
      invoker.addNode(node);
      assertEquals(invoker, _invoker);
    } finally {
      TestLifecycle.end();
    }
  }

  public void testAddCallbackWithNodePending() {
    TestLifecycle.begin();
    try {
      final LocalNodeJobInvoker invoker = new LocalNodeJobInvoker();
      _invoker = null;
      final TestCalculationNode node = new TestCalculationNode();
      TestLifecycle.register(node);
      invoker.addNode(node);
      assertTrue(invoker.notifyWhenAvailable(_register));
      assertNull(_invoker);
    } finally {
      TestLifecycle.end();
    }
  }

  public void testInvokeWithNoNodes() {
    final LocalNodeJobInvoker invoker = new LocalNodeJobInvoker();
    final TestJobInvocationReceiver receiver = new TestJobInvocationReceiver();
    assertFalse(invoker.invoke(JobDispatcherTest.createTestJob(), receiver));
    assertNull(receiver.getCompletionResult());
  }

  public void testInvokeWithOneNode() {
    TestLifecycle.begin();
    try {
      final TestCalculationNode node = new TestCalculationNode();
      TestLifecycle.register(node);
      final LocalNodeJobInvoker invoker = new LocalNodeJobInvoker(node);
      TestLifecycle.register(invoker);
      final TestJobInvocationReceiver receiver = new TestJobInvocationReceiver();
      final CalculationJob job = JobDispatcherTest.createTestJob();
      assertTrue(invoker.invoke(job, receiver));
      final CalculationJobResult jobResult = receiver.waitForCompletionResult(TIMEOUT);
      assertNotNull(jobResult);
      assertEquals(job.getSpecification(), jobResult.getSpecification());
    } finally {
      TestLifecycle.end();
    }
  }

  class Register implements JobInvokerRegister {
    @Override
    public void registerJobInvoker(final JobInvoker invoker) {
      _invoker = invoker;
    }
  }
}
