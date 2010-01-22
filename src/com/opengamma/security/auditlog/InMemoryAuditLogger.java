/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * This <code>AuditLogger</code> is mainly useful for testing.
 *
 * @author pietari
 */
public class InMemoryAuditLogger extends AbstractAuditLogger {
  
  private final List<AuditLogEntry> logMessages = new ArrayList<AuditLogEntry>();

  @Override
  public void log(String user, String originatingSystem, String object, String operation,
      String description, boolean success) {
    AuditLogEntry auditLogEntry = new AuditLogEntry(user, originatingSystem, object, operation, description, success, new Date());
    logMessages.add(auditLogEntry);
  }
  
  public List<AuditLogEntry> getMessages() {
    return Collections.unmodifiableList(logMessages);    
  }
}
