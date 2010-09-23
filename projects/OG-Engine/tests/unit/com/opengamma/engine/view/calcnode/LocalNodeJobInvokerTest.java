/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.engine.test.TestCalculationNode;

/**
 * Tests the LocalNodeJobInvoker class
 */
public class LocalNodeJobInvokerTest implements JobInvokerRegister {

  private static final long TIMEOUT = 1000L;

  private JobInvoker _invoker;

  @Test
  public void addNodeWithCallbackPending() {
    final LocalNodeJobInvoker invoker = new LocalNodeJobInvoker();
    _invoker = null;
    assertFalse (invoker.notifyWhenAvailable(this));
    assertNull(_invoker);
    invoker.addNode(new TestCalculationNode());
    assertEquals(invoker, _invoker);
  }

  @Test
  public void addCallbackWithNodePending() {
    final LocalNodeJobInvoker invoker = new LocalNodeJobInvoker();
    _invoker = null;
    invoker.addNode(new TestCalculationNode());
    assertTrue (invoker.notifyWhenAvailable(this));
    assertNull(_invoker);
  }

  @Override
  public void registerJobInvoker(final JobInvoker invoker) {
    _invoker = invoker;
  }

  @Test
  public void invokeWithNoNodes() {
    final LocalNodeJobInvoker invoker = new LocalNodeJobInvoker();
    final TestJobInvocationReceiver receiver = new TestJobInvocationReceiver();
    assertFalse(invoker.invoke(JobDispatcherTest.createTestJob(), receiver));
    assertNull(receiver.getCompletionResult());
  }

  @Test
  public void invokeWithOneNode() {
    final LocalNodeJobInvoker invoker = new LocalNodeJobInvoker(new TestCalculationNode());
    final TestJobInvocationReceiver receiver = new TestJobInvocationReceiver();
    final CalculationJob job = JobDispatcherTest.createTestJob();
    assertTrue(invoker.invoke(job, receiver));
    final CalculationJobResult jobResult = receiver.waitForCompletionResult(TIMEOUT);
    assertNotNull(jobResult);
    assertEquals(job.getSpecification(), jobResult.getSpecification());
  }

}
