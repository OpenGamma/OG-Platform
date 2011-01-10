/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

/**
 * An <code>AuditLogger</code> logs messages to an audit log.
 * <p>
 * Most systems require some kind of audit log.
 * Example events could be logging into the system, viewing or modifying a portfolio
 * or obtaining access to market data.
 */
public interface AuditLogger {

  /**
   * Logs a message to the audit log without a description.
   * 
   * @param user  the user name of the current user, not null
   * @param object  the Object ID of the object the user tried to access,
   *  for example /Portfolio/XYZ123, not null
   * @param operation  the operation the user tried to execute, for example View, not null
   * @param success  whether the operation was successful
   * @see #log(String, String, String, String, boolean)
   */
  void log(String user, String object, String operation, boolean success);

  /**
   * Logs a message to the audit log with a description.
   * 
   * @param user  the user name of the current user, not null
   * @param object  the Object ID of the object the user tried to access,
   *  for example /Portfolio/XYZ123, not null
   * @param operation  the operation the user tried to execute, for example View, not null
   * @param description  a description of the operation, may be null
   * @param success  whether the operation was successful
   */
  void log(String user, String object, String operation, String description, boolean success);

}
