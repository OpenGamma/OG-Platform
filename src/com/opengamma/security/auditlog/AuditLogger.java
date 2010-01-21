/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;


/**
 *  
 *
 * @author pietari
 */
public interface AuditLogger {
  
  /**
   * Logs a message to an audit log.
   * 
   * @param user User name of the current user. May not be null.
   * @param object Object ID of the object the user tried to access, for example /Portfolio/XYZ123. May not be null.
   * @param operation Operation the user tried to execute, for example View. May not be null.
   * @param description A description of the operation. Optional: may be null.
   * @param success Whether the operation was successful.
   * @throws NullPointerException If any of the required parameters is null.
   */
  public void log(String user, 
      String object, 
      String operation, 
      String description,
      boolean success);
  
  /**
   * Convenience method to use if the log entry has no description.
   * 
   * @see #log(String, String, String, String, boolean) 
   */
  public void log(String user,
      String object,
      String operation,
      boolean success);

}
