/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode.stats;

import static org.testng.AssertJUnit.assertEquals;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.threeten.bp.Duration;

import com.opengamma.engine.calcnode.stats.FunctionCosts;
import com.opengamma.engine.calcnode.stats.FunctionInvocationStatisticsReceiver;
import com.opengamma.engine.calcnode.stats.FunctionInvocationStatisticsSender;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the function statistics sender.
 */
@Test(groups = TestGroup.INTEGRATION)
public class FunctionInvocationStatisticsSenderTest {
  
  private static final Logger s_logger = LoggerFactory.getLogger(FunctionInvocationStatisticsSenderTest.class);
  
  private FunctionCosts _cost = new FunctionCosts ();
  
  @Test(timeOut = 3_000L)
  public void testBasicBehaviour () {
    final AtomicInteger messages = new AtomicInteger ();
    final FunctionInvocationStatisticsSender sender = new FunctionInvocationStatisticsSender ();
    final FunctionInvocationStatisticsReceiver receiver = new FunctionInvocationStatisticsReceiver (_cost);
    sender.setExecutorService(Executors.newCachedThreadPool ());
    sender.setFudgeMessageSender(new FudgeMessageSender () {

      @Override
      public FudgeContext getFudgeContext() {
        return FudgeContext.GLOBAL_DEFAULT;
      }

      @Override
      public void send(final FudgeMsg message) {
        messages.incrementAndGet ();
        s_logger.debug ("Received {}", message);
        receiver.messageReceived(getFudgeContext (), new FudgeMsgEnvelope (message));
      }
      
    });
    sender.setUpdatePeriod(Duration.ofSeconds(1));
    long t = System.nanoTime ();
    for (int i = 0; i < 100; i++) {
      sender.functionInvoked("A", "1", 1, 2.0, 3.0, 4.0);
      sender.functionInvoked("A", "2", 1, 2.0, 3.0, 4.0);
      sender.functionInvoked("A", "1", 1, 2.0, 3.0, 4.0);
      try {
        Thread.sleep (20);
      } catch (InterruptedException e) {
      }
    }
    sender.functionInvoked ("A", "3", 300, 300 * 4.0, 300 * 5.0, 300 * 6.0);
    t = (System.nanoTime () - t) / 1000000000;
    sender.flush ();
    if ((messages.get () < t) || (messages.get () > t + 2)) {
      Assert.fail ("Unexpected number of messages (" + messages.get () + ") from " + t + "s execution");
    }
    assertEquals (2.0, _cost.getStatistics ("A", "1").getInvocationCost (), 1e-5);
    assertEquals (3.0, _cost.getStatistics ("A", "1").getDataInputCost (), 1e-5);
    assertEquals (4.0, _cost.getStatistics ("A", "1").getDataOutputCost (), 1e-5);
    assertEquals (2.0, _cost.getStatistics ("A", "2").getInvocationCost (), 1e-5);
    assertEquals (3.0, _cost.getStatistics ("A", "2").getDataInputCost (), 1e-5);
    assertEquals (4.0, _cost.getStatistics ("A", "2").getDataOutputCost (), 1e-5);
    assertEquals (4.0, _cost.getStatistics ("A", "3").getInvocationCost (), 1e-5);
    assertEquals (5.0, _cost.getStatistics ("A", "3").getDataInputCost (), 1e-5);
    assertEquals (6.0, _cost.getStatistics ("A", "3").getDataOutputCost (), 1e-5);
  }
  
}