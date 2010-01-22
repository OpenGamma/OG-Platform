/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.security.auditlog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * An audit log entry describes an operation a user attempted on an object. 
 * The entry is timestamped. Both successful and failed attempts are logged.
 *
 * @author pietari
 */
public class AuditLogEntry {
  
  private Long _id;
  private String _user;
  private String _originatingSystem;
  private String _object;
  private String _operation;
  private String _description;
  private boolean _success;
  private Date _timestamp;
  
  public AuditLogEntry(String user,
      String originatingSystem,
      String object,
      String operation,
      String description,
      boolean success,
      Date timestamp) {
    ArgumentChecker.checkNotNull(user, "User name");    
    ArgumentChecker.checkNotNull(originatingSystem, "Originating system");
    ArgumentChecker.checkNotNull(object, "Object name");
    ArgumentChecker.checkNotNull(operation, "Operation name");
    ArgumentChecker.checkNotNull(timestamp, "timestamp");
    
    _id = null;
    _user = user;
    _originatingSystem = originatingSystem;
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
  
  public String getOriginatingSystem() {
    return _originatingSystem;
  }

  public void setOriginatingSystem(String originatingSystem) {
    _originatingSystem = originatingSystem;
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
  
  public FudgeMsg toFudgeMsg(FudgeContext fudgeContext) {
    FudgeMsg msg = fudgeContext.newMessage();
    msg.add("user", getUser());
    msg.add("originatingSystem", getOriginatingSystem());
    msg.add("object", getObject());
    msg.add("operation", getOperation());
    if (getDescription() != null) {
      msg.add("description", getDescription());
    }
    msg.add("success", isSuccess());
    String yyyymmdd = new SimpleDateFormat("yyyyMMddHHmmssZ").format(getTimestamp());
    msg.add("timestamp", yyyymmdd); // change as soon as Fudge supports Date natively
    return msg;
  }
  
  public static AuditLogEntry fromFudgeMsg(FudgeMsg msg) {
    String user = msg.getString("user");
    String originatingSystem = msg.getString("originatingSystem");
    String object = msg.getString("object");
    String operation = msg.getString("operation");
    String description = msg.getString("description");
    Boolean success = msg.getBoolean("success");
    String yyyymmdd = msg.getString("timestamp"); // change as soon as Fudge supports Date natively
    Date timestamp;
    try {
      timestamp = new SimpleDateFormat("yyyyMMddHHmmssZ").parse(yyyymmdd);
    } catch (ParseException e) {
      throw new OpenGammaRuntimeException("Invalid Fudge message", e);
    }
    
    AuditLogEntry logEntry;
    try {
      logEntry = new AuditLogEntry(user, originatingSystem, object, operation, description, success, timestamp);
    } catch (NullPointerException e) {
      throw new OpenGammaRuntimeException("Invalid Fudge message", e);            
    }
    return logEntry;
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((_description == null) ? 0 : _description.hashCode());
    result = prime * result + ((_id == null) ? 0 : _id.hashCode());
    result = prime * result + ((_object == null) ? 0 : _object.hashCode());
    result = prime * result
        + ((_operation == null) ? 0 : _operation.hashCode());
    result = prime * result
        + ((_originatingSystem == null) ? 0 : _originatingSystem.hashCode());
    result = prime * result + (_success ? 1231 : 1237);
    result = prime * result
        + ((_timestamp == null) ? 0 : _timestamp.hashCode());
    result = prime * result + ((_user == null) ? 0 : _user.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AuditLogEntry other = (AuditLogEntry) obj;
    if (_description == null) {
      if (other._description != null)
        return false;
    } else if (!_description.equals(other._description))
      return false;
    if (_id == null) {
      if (other._id != null)
        return false;
    } else if (!_id.equals(other._id))
      return false;
    if (_object == null) {
      if (other._object != null)
        return false;
    } else if (!_object.equals(other._object))
      return false;
    if (_operation == null) {
      if (other._operation != null)
        return false;
    } else if (!_operation.equals(other._operation))
      return false;
    if (_originatingSystem == null) {
      if (other._originatingSystem != null)
        return false;
    } else if (!_originatingSystem.equals(other._originatingSystem))
      return false;
    if (_success != other._success)
      return false;
    if (_timestamp == null) {
      if (other._timestamp != null)
        return false;
    } else if (!_timestamp.equals(other._timestamp))
      return false;
    if (_user == null) {
      if (other._user != null)
        return false;
    } else if (!_user.equals(other._user))
      return false;
    return true;
  }

}
