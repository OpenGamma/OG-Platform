/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * An audit logger that stores messages in-memory.
 * <p>
 * This implementation is primarily for use in testing.
 */
public class InMemoryAuditLogger extends AbstractAuditLogger {

  /**
   * The collection of log messages.
   */
  private final List<AuditLogEntry> _logMessages = new ArrayList<AuditLogEntry>();

  @Override
  public void log(String user, String originatingSystem, String object, String operation,
      String description, boolean success) {
    AuditLogEntry auditLogEntry = new AuditLogEntry(user, originatingSystem, object, operation, description, success, new Date());
    _logMessages.add(auditLogEntry);
  }

  /**
   * Gets the in-memory list of log messages.
   * @return the list of messages, not null
   */
  public List<AuditLogEntry> getMessages() {
    return Collections.unmodifiableList(_logMessages);    
  }

}
