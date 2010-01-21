/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

import static org.junit.Assert.*;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.junit.Test;

import com.opengamma.transport.ByteArrayFudgeMessageSender;
import com.opengamma.transport.CollectingByteArrayMessageSender;

/**
 * 
 *
 * @author pietari
 */
public class DistributedAuditLoggerTest {
  
  @Test
  public void testClientServerAuditLogging() {
    
    CollectingByteArrayMessageSender msgStore = new CollectingByteArrayMessageSender();
    assertEquals(0, msgStore.getMessages().size());
    
    DistributedAuditLogger client = new DistributedAuditLogger(new ByteArrayFudgeMessageSender(msgStore));
    client.log("lisa", "testobject", "testop", "testdescription", true);
    assertEquals(1, msgStore.getMessages().size());
    
    FudgeMsg fudgeMsg = new FudgeMsg(msgStore.getMessages().get(0), new FudgeContext());
    
    InMemoryAuditLogger memoryAuditLogger = new InMemoryAuditLogger();
    assertEquals(0, memoryAuditLogger.getMessages().size());
    
    DistributedAuditLoggerServer server = new DistributedAuditLoggerServer(memoryAuditLogger);
    server.messageReceived(new FudgeContext(), new FudgeMsgEnvelope(fudgeMsg));
    assertEquals(1, memoryAuditLogger.getMessages().size());
    
    AuditLogEntry entry = memoryAuditLogger.getMessages().get(0);
    assertEquals("lisa", entry.getUser());
    assertEquals("testobject", entry.getObject());
    assertEquals("testop", entry.getOperation());
    assertEquals("testdescription", entry.getDescription());
    assertTrue(entry.isSuccess());
  }

}
