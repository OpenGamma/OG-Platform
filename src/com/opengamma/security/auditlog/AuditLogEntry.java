/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

import java.util.Date;

/**
 * An audit log entry describes an operation a user attempted on an object. 
 * The entry is timestamped. Both successful and failed attempts are logged.
 *
 * @author pietari
 */
public class AuditLogEntry {
  
  private Long _id;
  private String _user;
  private String _object;
  private String _operation;
  private String _description;
  private boolean _success;
  private Date _timestamp;
  
  public AuditLogEntry(String user,
      String object,
      String operation,
      String description,
      boolean success,
      Date timestamp) {
    _id = null;
    _user = user;
    _object = object;
    _operation = operation;
    _description = description;
    _success = success;
    _timestamp = timestamp;
  }
  
  protected AuditLogEntry() {
  }
  
  public Long getId() {
    return _id;
  }

  public void setId(Long id) {
    _id = id;
  }

  public String getUser() {
    return _user;
  }

  public void setUser(String user) {
    _user = user;
  }

  public String getObject() {
    return _object;
  }

  public void setObject(String object) {
    _object = object;
  }

  public String getOperation() {
    return _operation;
  }

  public void setOperation(String operation) {
    _operation = operation;
  }
  
  public String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public boolean isSuccess() {
    return _success;
  }

  public void setSuccess(boolean success) {
    _success = success;
  }

  public Date getTimestamp() {
    return _timestamp;
  }

  public void setTimestamp(Date timestamp) {
    _timestamp = timestamp;
  }
}
