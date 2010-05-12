/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.InetAddress;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.junit.Test;

import com.opengamma.transport.CollectingFudgeMessageReceiver;

/**
 * 
 */
public class SocketFudgeMessageConduitTest {
  @Test
  public void simpleTest() throws Exception {
    CollectingFudgeMessageReceiver collectingReceiver = new CollectingFudgeMessageReceiver();
    ServerSocketFudgeMessageReceiver socketReceiver = new ServerSocketFudgeMessageReceiver(collectingReceiver);
    socketReceiver.start();
    
    SocketFudgeMessageSender sender = new SocketFudgeMessageSender();
    sender.setInetAddress(InetAddress.getLocalHost());
    sender.setPortNumber(socketReceiver.getPortNumber());
    
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("RATM", "Bombtrack");
    msg.add("You Know", "It's All Of That");
    sender.send(msg);
    
    msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("Anger", "is a gift");
    sender.send(msg);
    
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
    assertEquals("Bombtrack", envelope.getMessage().getString("RATM"));
    assertEquals("It's All Of That", envelope.getMessage().getString("You Know"));
    assertEquals(2, envelope.getMessage().getNumFields());

    envelope = collectingReceiver.getMessages().get(1);
    assertNotNull(envelope);
    assertNotNull(envelope.getMessage());
    assertEquals("is a gift", envelope.getMessage().getString("Anger"));
    assertEquals(1, envelope.getMessage().getNumFields());
    
    sender.stop();
    socketReceiver.stop();
  }

}
