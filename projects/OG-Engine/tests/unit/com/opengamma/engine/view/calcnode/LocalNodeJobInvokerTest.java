/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import com.opengamma.engine.test.TestCalculationNode;
import com.opengamma.util.test.Timeout;

/**
 * Tests the LocalNodeJobInvoker class
 */
@Test
public class LocalNodeJobInvokerTest implements JobInvokerRegister {

  private static final long TIMEOUT = Timeout.standardTimeoutMillis ();

  private JobInvoker _invoker;

  public void addNodeWithCallbackPending() {
    final LocalNodeJobInvoker invoker = new LocalNodeJobInvoker();
    _invoker = null;
    assertFalse (invoker.notifyWhenAvailable(this));
    assertNull(_invoker);
    invoker.addNode(new TestCalculationNode());
    assertEquals(invoker, _invoker);
  }

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

  public void invokeWithNoNodes() {
    final LocalNodeJobInvoker invoker = new LocalNodeJobInvoker();
    final TestJobInvocationReceiver receiver = new TestJobInvocationReceiver();
    assertFalse(invoker.invoke(JobDispatcherTest.createTestJob(), receiver));
    assertNull(receiver.getCompletionResult());
  }

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
