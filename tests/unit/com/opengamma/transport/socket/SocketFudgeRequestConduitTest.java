/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetAddress;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.junit.Test;

import com.opengamma.transport.CollectingFudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestReceiver;

/**
 * 
 */
public class SocketFudgeRequestConduitTest {

  @Test
  public void simpleTest() throws Exception {
    CollectingFudgeMessageReceiver collectingReceiver = new CollectingFudgeMessageReceiver();
    FudgeRequestReceiver requestReceiver = new FudgeRequestReceiver() {
      @Override
      public FudgeFieldContainer requestReceived(FudgeDeserializationContext context, FudgeMsgEnvelope requestEnvelope) {
        MutableFudgeFieldContainer response = context.getFudgeContext().newMessage();
        response.add("TheTime", System.nanoTime());
        return response;
      }
    };
    ServerSocketFudgeRequestDispatcher requestDispatcher = new ServerSocketFudgeRequestDispatcher(requestReceiver);
    requestDispatcher.start();
    
    SocketFudgeRequestSender sender = new SocketFudgeRequestSender();
    sender.setInetAddress(InetAddress.getLocalHost());
    sender.setPortNumber(requestDispatcher.getPortNumber());
    
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("RATM", "Bombtrack");
    msg.add("You Know", "It's All Of That");
    sender.sendRequest(msg, collectingReceiver);
    
    msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("Anger", "is a gift");
    sender.sendRequest(msg, collectingReceiver);
    
    int nChecks = 0;
    while(collectingReceiver.getMessages().size() < 2) {
      Thread.sleep(100);
      nChecks++;
      if(nChecks > 20) {
        fail("Didn't receive messages in 2 seconds");
      }
    }
    
    FudgeMsgEnvelope envelope = null;
    envelope = collectingReceiver.getMessages().get(0);
    assertNotNull(envelope);
    assertNotNull(envelope.getMessage());
    Long firstTimestamp = envelope.getMessage().getLong("TheTime");
    assertNotNull(firstTimestamp);
    assertEquals(1, envelope.getMessage().getNumFields());

    envelope = collectingReceiver.getMessages().get(1);
    assertNotNull(envelope);
    assertNotNull(envelope.getMessage());
    Long secondTimestamp = envelope.getMessage().getLong("TheTime");
    assertNotNull(firstTimestamp);
    assertEquals(1, envelope.getMessage().getNumFields());
    
    assertTrue(secondTimestamp > firstTimestamp);
    
    sender.stop();
    requestDispatcher.stop();
  }
}
