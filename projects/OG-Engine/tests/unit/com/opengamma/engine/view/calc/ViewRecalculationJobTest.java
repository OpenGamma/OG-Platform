/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.test.TestComputationResultListener;
import com.opengamma.engine.test.ViewProcessorTestEnvironment;
import com.opengamma.engine.view.ViewProcessImpl;
import com.opengamma.engine.view.ViewProcessorImpl;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.util.test.Timeout;

/**
 * Tests ViewRecalculationJob
 */
@Test
public class ViewRecalculationJobTest {

  private static final long TIMEOUT = 10L * Timeout.standardTimeoutMillis();
  
  public void testInterruptJobBetweenCycles() throws InterruptedException {
    // Due to all the dependencies between components for execution to take place, it's easiest to test it in a
    // realistic environment. In its default configuration, only live data can trigger a computation cycle (after the
    // initial cycle).
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    TestComputationResultListener resultListener = new TestComputationResultListener();
    client.setResultListener(resultListener);
    client.attachToViewProcess(env.getViewDefinition().getName(), ExecutionOptions.realTime());
    
    resultListener.getResult(TIMEOUT);  // Consume the initial result
    
    ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    Thread recalcThread = env.getCurrentComputationThread(viewProcess);
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
    ViewComputationJob job = env.getCurrentComputationJob(viewProcess);
    job.terminate();
    recalcThread.interrupt();
    
    recalcThread.join(TIMEOUT);
    assertEquals(Thread.State.TERMINATED, recalcThread.getState());
  }
  
}
