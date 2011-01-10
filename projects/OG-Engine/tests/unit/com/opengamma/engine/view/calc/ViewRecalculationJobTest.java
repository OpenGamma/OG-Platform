/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.test.TestComputationResultListener;
import com.opengamma.engine.view.ViewImpl;
import com.opengamma.engine.view.ViewProcessorImpl;
import com.opengamma.engine.view.ViewProcessorTestEnvironment;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.util.test.Timeout;

/**
 * Tests ViewRecalculationJob
 */
public class ViewRecalculationJobTest {

  private static final long TIMEOUT = 10L * Timeout.standardTimeoutMillis();
  
  @Test
  public void testInterruptJobBetweenCycles() throws InterruptedException {
    // Due to all the dependencies between components for execution to take place, it's easiest to test it in a
    // realistic environment. In its default configuration, only live data can trigger a computation cycle (after the
    // initial cycle).
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewImpl view = (ViewImpl) vp.getView(env.getViewDefinition().getName(), ViewProcessorTestEnvironment.TEST_USER);
    view.start();
    view.init();
    
    ViewClient client = view.createClient(ViewProcessorTestEnvironment.TEST_USER);
    TestComputationResultListener resultListener = new TestComputationResultListener();
    client.setResultListener(resultListener);
    
    client.startLive();  // Performs an initial cycle
    resultListener.getResult(TIMEOUT);  // Consume the initial result
    
    Thread recalcThread = env.getCurrentRecalcThread(view);
    long startTime = System.currentTimeMillis();
    while (recalcThread.getState() != Thread.State.TIMED_WAITING) {
      // REVIEW jonathan 2010-10-01 -- I don't particularly like this, but how else can I wait for the recalc job to
      // perform another cycle and then realise it needs to go to sleep?
      Thread.sleep(50);
      if (System.currentTimeMillis() - startTime > TIMEOUT) {
        throw new OpenGammaRuntimeException("Waited longer than " + TIMEOUT + " ms for the recalc thread to go to sleep"); 
      }
    }
    
    // We're now 'between cycles', waiting for the arrival of live data.
    // Interrupting should terminate the job gracefully
    ViewRecalculationJob job = env.getCurrentRecalcJob(view);
    job.terminate();
    recalcThread.interrupt();
    
    recalcThread.join(TIMEOUT);
    assertEquals(Thread.State.TERMINATED, recalcThread.getState());
  }
  
}
