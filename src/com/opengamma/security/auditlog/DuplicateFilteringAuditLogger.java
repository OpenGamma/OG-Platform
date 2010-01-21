/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

import java.util.Date;

import com.opengamma.util.ArgumentChecker;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * A decorator <code>AuditLogger</code> that only sends the message
 * onwards to its delegate if the message is not a duplicate (within
 * a given time period). 
 * <p>
 * This implementation is thread-safe.
 *
 * @author pietari
 */
public class DuplicateFilteringAuditLogger implements AuditLogger {
  
  private final AuditLogger _delegate;
  private final Cache _cache;
  private final Date _creationTime;
  
  public DuplicateFilteringAuditLogger(AuditLogger delegate, int maxElementsInMemory, int secondsToKeepInMemory) {
    ArgumentChecker.checkNotNull(delegate, "Delegate logger");
    
    _delegate = delegate;    
    _cache = new Cache("audit_log_entry_cache", maxElementsInMemory, false, false, secondsToKeepInMemory, secondsToKeepInMemory);
    _cache.initialise();
    _creationTime = new Date();
  }

  @Override
  public synchronized void log(String user, String object, String operation, boolean success) {
    log(user, object, operation, null, success);
  }

  @Override
  public synchronized void log(String user, String object, String operation,
      String description, boolean success) {
    
    // Note how we standardise timestamp here
    AuditLogEntry auditLogEntry = new AuditLogEntry(user, object, operation, description, success, _creationTime);    
    
    if (_cache.get(auditLogEntry) == null) {
      Element element = new Element(auditLogEntry, new Object());
      _cache.put(element);
      
      _delegate.log(user, object, operation, description, success);                              
    }
  }
}
