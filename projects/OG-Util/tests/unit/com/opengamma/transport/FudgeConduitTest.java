/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.junit.Test;

/**
 * Tests the various Fudge-specific conduit forms.
 *
 * @author kirk
 */
public class FudgeConduitTest {

  @Test
  public void oneWayTest() {
    FudgeContext context = new FudgeContext();
    CollectingFudgeMessageReceiver collectingReceiver = new CollectingFudgeMessageReceiver();
    ByteArrayFudgeMessageReceiver fudgeReceiver = new ByteArrayFudgeMessageReceiver(collectingReceiver);
    DirectInvocationByteArrayMessageSender byteArraySender = new DirectInvocationByteArrayMessageSender(fudgeReceiver);
    ByteArrayFudgeMessageSender fudgeSender = new ByteArrayFudgeMessageSender(byteArraySender, context);
    
    MutableFudgeFieldContainer msg = context.newMessage();
    msg.add("Foo", "Bar");
    msg.add("Number Problems", 99);
    
    fudgeSender.send(msg);
    
    List<FudgeMsgEnvelope> receivedMessages = collectingReceiver.getMessages();
    assertEquals(1, receivedMessages.size());
    FudgeMsgEnvelope receivedEnvelope = receivedMessages.get(0);
    assertNotNull(receivedEnvelope.getMessage());
    FudgeFieldContainer receivedMsg = receivedEnvelope.getMessage();
    assertEquals(2, receivedMsg.getNumFields());
    assertEquals("Bar", receivedMsg.getString("Foo"));
    assertEquals(new Integer(99), receivedMsg.getInt("Number Problems"));
  }
  
  @Test
  public void requestResponseTest() {
    FudgeContext context = new FudgeContext();
    FudgeRequestReceiver requestReceiver = new FudgeRequestReceiver() {
      @Override
      public FudgeFieldContainer requestReceived(
          FudgeDeserializationContext context, FudgeMsgEnvelope requestEnvelope) {
        MutableFudgeFieldContainer response = context.getFudgeContext().newMessage();
        response.add("Killing", "In The Name Of");
        return response;
      }
    };
    
    FudgeRequestSender sender = InMemoryRequestConduit.create(requestReceiver);
    
    MutableFudgeFieldContainer request = context.newMessage();
    request.add("Rage", "Against The Machine");
    
    CollectingFudgeMessageReceiver responseReceiver = new CollectingFudgeMessageReceiver();
    sender.sendRequest(request, responseReceiver);
    List<FudgeMsgEnvelope> receivedMessages = responseReceiver.getMessages();
    assertEquals(1, receivedMessages.size());
    
    FudgeMsgEnvelope receivedEnvelope = receivedMessages.get(0);
    assertNotNull(receivedEnvelope.getMessage());
    FudgeFieldContainer receivedMsg = receivedEnvelope.getMessage();
    assertEquals(1, receivedMsg.getNumFields());
    assertEquals("In The Name Of", receivedMsg.getString("Killing"));
  }
}
