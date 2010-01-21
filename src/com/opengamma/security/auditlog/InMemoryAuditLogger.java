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
public class InMemoryAuditLogger implements AuditLogger {
  
  private final List<AuditLogEntry> logMessages = new ArrayList<AuditLogEntry>();

  @Override
  public void log(String user, String object, String operation, boolean success) {
    log(user, object, operation, null, success); 
  }

  @Override
  public void log(String user, String object, String operation,
      String description, boolean success) {
    AuditLogEntry auditLogEntry = new AuditLogEntry(user, object, operation, description, success, new Date());
    logMessages.add(auditLogEntry);
  }
  
  public List<AuditLogEntry> getMessages() {
    return Collections.unmodifiableList(logMessages);    
  }
}
