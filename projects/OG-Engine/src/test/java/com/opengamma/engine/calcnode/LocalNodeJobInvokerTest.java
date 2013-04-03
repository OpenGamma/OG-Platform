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

import com.opengamma.engine.calcnode.CalculationJob;
import com.opengamma.engine.calcnode.CalculationJobResult;
import com.opengamma.engine.calcnode.JobInvoker;
import com.opengamma.engine.calcnode.JobInvokerRegister;
import com.opengamma.engine.calcnode.LocalNodeJobInvoker;
import com.opengamma.engine.test.TestCalculationNode;
import com.opengamma.util.test.TestGroup;
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
    final LocalNodeJobInvoker invoker = new LocalNodeJobInvoker();
    _invoker = null;
    assertFalse(invoker.notifyWhenAvailable(_register));
    assertNull(_invoker);
    invoker.addNode(new TestCalculationNode());
    assertEquals(invoker, _invoker);
  }

  public void testAddCallbackWithNodePending() {
    final LocalNodeJobInvoker invoker = new LocalNodeJobInvoker();
    _invoker = null;
    invoker.addNode(new TestCalculationNode());
    assertTrue(invoker.notifyWhenAvailable(_register));
    assertNull(_invoker);
  }

  public void testInvokeWithNoNodes() {
    final LocalNodeJobInvoker invoker = new LocalNodeJobInvoker();
    final TestJobInvocationReceiver receiver = new TestJobInvocationReceiver();
    assertFalse(invoker.invoke(JobDispatcherTest.createTestJob(), receiver));
    assertNull(receiver.getCompletionResult());
  }

  public void testInvokeWithOneNode() {
    final LocalNodeJobInvoker invoker = new LocalNodeJobInvoker(new TestCalculationNode());
    final TestJobInvocationReceiver receiver = new TestJobInvocationReceiver();
    final CalculationJob job = JobDispatcherTest.createTestJob();
    assertTrue(invoker.invoke(job, receiver));
    final CalculationJobResult jobResult = receiver.waitForCompletionResult(TIMEOUT);
    assertNotNull(jobResult);
    assertEquals(job.getSpecification(), jobResult.getSpecification());
  }

  class Register implements JobInvokerRegister {
    @Override
    public void registerJobInvoker(final JobInvoker invoker) {
      _invoker = invoker;
    }
  }
}
