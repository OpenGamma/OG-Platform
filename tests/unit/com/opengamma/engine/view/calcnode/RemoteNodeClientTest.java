/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.junit.Test;

import com.opengamma.transport.CollectingFudgeMessageReceiver;
import com.opengamma.transport.DirectFudgeConnection;

/**
 * Tests RemoteNodeClient
 */
public class RemoteNodeClientTest {

  private static final long TIMEOUT = 1000L;

  protected static CalculationJob createTestCalculationJob() {
    return new CalculationJob(JobDispatcherTest.createTestJobSpec(), JobDispatcherTest.createTestJobItems());
  }

  @Test
  public void simpleInvocation() {
    final DirectFudgeConnection conduit = new DirectFudgeConnection(FudgeContext.GLOBAL_DEFAULT);
    final CollectingFudgeMessageReceiver messages = new CollectingFudgeMessageReceiver();
    conduit.getEnd2().setFudgeMessageReceiver(messages);
    final RemoteNodeClient client = new RemoteNodeClient(conduit.getEnd1());
    final AbstractCalculationNode node = new TestCalculationNode();
    assertEquals(0, messages.getMessages().size());
    client.addNode(node);
    assertEquals(0, messages.getMessages().size());
    client.start();
    assertEquals(1, messages.getMessages().size());
    final FudgeMsgEnvelope readyMsgEnvelope = messages.getMessages().get(0);
    messages.clear();
    final FudgeDeserializationContext dcontext = new FudgeDeserializationContext(FudgeContext.GLOBAL_DEFAULT);
    final FudgeSerializationContext scontext = new FudgeSerializationContext(FudgeContext.GLOBAL_DEFAULT);
    final RemoteCalcNodeMessage readyMessage = dcontext.fudgeMsgToObject(RemoteCalcNodeMessage.class, readyMsgEnvelope.getMessage());
    assertTrue(readyMessage instanceof RemoteCalcNodeReadyMessage);
    final RemoteCalcNodeReadyMessage ready = (RemoteCalcNodeReadyMessage) readyMessage;
    assertEquals(1, ready.getCapacity());
    final CalculationJob job = createTestCalculationJob();
    conduit.getEnd2().getFudgeMessageSender().send(
        FudgeSerializationContext.addClassHeader(scontext.objectToFudgeMsg(new RemoteCalcNodeJobMessage(job)), RemoteCalcNodeJobMessage.class, RemoteCalcNodeMessage.class));
    final FudgeMsgEnvelope resultMsgEnvelope = messages.waitForMessage(TIMEOUT);
    assertNotNull(resultMsgEnvelope);
    final RemoteCalcNodeMessage resultMessage = dcontext.fudgeMsgToObject(RemoteCalcNodeMessage.class, resultMsgEnvelope.getMessage());
    assertTrue(resultMessage instanceof RemoteCalcNodeResultMessage);
    final RemoteCalcNodeResultMessage result = (RemoteCalcNodeResultMessage) resultMessage;
    assertEquals(job.getSpecification(), result.getResult().getSpecification());
  }

}
