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

/**
 * 
 */
public class LocalNodeJobInvokerTest implements JobInvokerRegister {
  
  private static final long TIMEOUT = 1000L;
  
  private JobInvoker _invoker;
  
  @Test
  public void addNodeWithCallbackPending () {
    final LocalNodeJobInvoker invoker = new LocalNodeJobInvoker ();
    _invoker = null;
    invoker.notifyWhenAvailable(this);
    assertNull (_invoker);
    invoker.addNode(new TestCalculationNode ());
    assertEquals (invoker, _invoker);
  }
  
  @Test
  public void addCallbackWithNodePending () {
    final LocalNodeJobInvoker invoker = new LocalNodeJobInvoker ();
    _invoker = null;
    invoker.addNode (new TestCalculationNode ());
    invoker.notifyWhenAvailable (this);
    assertEquals (invoker, _invoker);
  }
  
  @Override
  public void registerJobInvoker (final JobInvoker invoker) {
    _invoker = invoker;
  }
  
  @Test
  public void invokeWithNoNodes () {
    final LocalNodeJobInvoker invoker = new LocalNodeJobInvoker ();
    final TestJobResultReceiver result = new TestJobResultReceiver (); 
    assertFalse (invoker.invoke(JobDispatcherTest.createTestJobSpec (), JobDispatcherTest.createTestJobItems (), result));
    assertNull (result.getResult ());
  }
  
  @Test
  public void invokeWithOneNode () {
    final LocalNodeJobInvoker invoker = new LocalNodeJobInvoker (new TestCalculationNode ());
    final TestJobResultReceiver result = new TestJobResultReceiver ();
    final CalculationJobSpecification jobSpec = JobDispatcherTest.createTestJobSpec ();
    assertTrue (invoker.invoke(jobSpec, JobDispatcherTest.createTestJobItems (), result));
    final CalculationJobResult jobResult = result.waitForResult(TIMEOUT);
    assertNotNull (jobResult);
    assertEquals (jobSpec, jobResult.getSpecification());
  }
  
}