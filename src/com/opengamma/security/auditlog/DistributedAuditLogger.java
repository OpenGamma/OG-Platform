/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

import java.util.Date;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.ArgumentChecker;

/**
 * An <code>AuditLogger</code> that sends log messages
 * to a remote destination as Fudge messages. 
 *
 * @author pietari
 */
public class DistributedAuditLogger implements AuditLogger {
  
  private static final Logger s_logger = LoggerFactory.getLogger(DistributedAuditLogger.class);
  private final FudgeMessageSender _msgSender;
  private final FudgeContext _fudgeContext;
  
  public DistributedAuditLogger(FudgeMessageSender msgSender) {
    this(msgSender, new FudgeContext());
  }
  
  public DistributedAuditLogger(FudgeMessageSender msgSender, FudgeContext fudgeContext) {
    ArgumentChecker.checkNotNull(msgSender, "Message Sender");
    ArgumentChecker.checkNotNull(fudgeContext, "Fudge Context");
    _msgSender = msgSender;
    _fudgeContext = fudgeContext;
  }

  @Override
  public void log(String user, String object, String operation, boolean success) {
    log(user, object, operation, null, success);    
  }

  @Override
  public void log(String user, String object, String operation, String description, boolean success) {
    AuditLogEntry auditLogEntry = new AuditLogEntry(user, object, operation, description, success, new Date());
    s_logger.info("Sending message: " + auditLogEntry.toString());
    FudgeMsg logMessage = auditLogEntry.toFudgeMsg(_fudgeContext);
    _msgSender.send(logMessage);
  }
  
}
