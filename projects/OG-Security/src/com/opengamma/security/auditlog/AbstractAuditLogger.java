/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * A default implementation of <code>AuditLogger</code>. It implements the
 * short-form
 * convenience logging methods such as
 * {@link #log(String, String, String, boolean)}.
 * As a result, implementors of new <code>AuditLoggers</code> can focus on just
 * one <code>log()</code> method in subclasses.
 * <p>
 * We store the name of the system that emitted the log message in the database.
 * Hence, an originating system name can be given at construction time. If this
 * is not given, the host name of the local host is used.
 */
public abstract class AbstractAuditLogger implements AuditLogger {
  
  private final String _originatingSystem;

  public AbstractAuditLogger() {
    _originatingSystem = getDefaultOriginatingSystem();
  }

  /**
   * @param originatingSystem
   *          Name of the system that emitted the log message.
   *          Examples might be "view-processor-5" or "bloomberg-server".
   */
  public AbstractAuditLogger(String originatingSystem) {
    ArgumentChecker.notNull(originatingSystem,
        "Name of originating system");
    _originatingSystem = originatingSystem;
  }
  
  public static String getDefaultOriginatingSystem() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      throw new OpenGammaRuntimeException("Cannot obtain local host name", e);
    }
  }

  @Override
  public final void log(String user, String object, String operation,
      String description, boolean success) {
    log(user, _originatingSystem, object, operation, description, success);
  }

  @Override
  public final void log(String user, String object, String operation,
      boolean success) {
    log(user, object, operation, null, success);
  }

  /**
   * Logs a message to an audit log.
   * 
   * @param user
   *          User name of the current user. May not be null.
   * @param originatingSystem
   *          Name of the system that emitted the log message.
   *          Examples might be "view-processor-5" or "bloomberg-server".
   * @param object
   *          Object ID of the object the user tried to access, for example
   *          /Portfolio/XYZ123. May not be null.
   * @param operation
   *          Operation the user tried to execute, for example View. May not be
   *          null.
   * @param description
   *          A description of the operation. Optional: may be null.
   * @param success
   *          Whether the operation was successful.
   * @throws NullPointerException
   *           If any of the required parameters is null.
   */
  public abstract void log(String user, String originatingSystem,
      String object, String operation, String description, boolean success);
  
  /** 
   * Flushes log entries stored in memory into the database.
   * Only relevant for loggers with an in-memory cache.
   */
  public void flushCache() {
  }

}
