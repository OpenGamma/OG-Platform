/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
 * An {@code AuditLogger} that sends log messages
 * to a remote destination via Fudge. The messages are consumed by {@link DistributedAuditLoggerServer}.
 */
public class DistributedAuditLogger extends AbstractAuditLogger {
  
  private static final Logger s_logger = LoggerFactory.getLogger(DistributedAuditLogger.class);
  private final FudgeMessageSender _msgSender;
  private final FudgeContext _fudgeContext;
  
  public DistributedAuditLogger(FudgeMessageSender msgSender) {
    this(getDefaultOriginatingSystem(), msgSender);    
  }
  
  public DistributedAuditLogger(String originatingSystem, FudgeMessageSender msgSender) {
    this(originatingSystem, msgSender, new FudgeContext());
  }
  
  public DistributedAuditLogger(String originatingSystem, FudgeMessageSender msgSender, FudgeContext fudgeContext) {
    super(originatingSystem);
    ArgumentChecker.notNull(msgSender, "Message Sender");
    ArgumentChecker.notNull(fudgeContext, "Fudge Context");
    _msgSender = msgSender;
    _fudgeContext = fudgeContext;
  }

  @Override
  public void log(String user, String originatingSystem, String object, String operation, String description, boolean success) {
    AuditLogEntry auditLogEntry = new AuditLogEntry(user, originatingSystem, object, operation, description, success, new Date());
    s_logger.info("Sending message: " + auditLogEntry.toString());
    FudgeMsg logMessage = auditLogEntry.toFudgeMsg(_fudgeContext);
    _msgSender.send(logMessage);
  }
  
}
