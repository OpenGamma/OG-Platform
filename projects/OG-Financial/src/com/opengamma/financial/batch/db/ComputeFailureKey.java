/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch.db;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 */
public class ComputeFailureKey {
  
  private final String _functionId;
  private final String _exceptionClass;
  private final String _exceptionMsg;
  private final String _stackTrace;
  
  public ComputeFailureKey(String functionId,
      String exceptionClass,
      String exceptionMsg,
      String stackTrace) {
    _functionId = functionId;
    _exceptionClass = exceptionClass;
    _exceptionMsg = exceptionMsg.substring(0, Math.min(exceptionMsg.length(), 255));
    _stackTrace = stackTrace.substring(0, Math.min(stackTrace.length(), 2000));
  }
  
  public String getFunctionId() {
    return _functionId;
  }
  
  public String getExceptionClass() {
    return _exceptionClass;
  }
  
  public String getExceptionMsg() {
    return _exceptionMsg;
  }
  
  public String getStackTrace() {
    return _stackTrace;
  }
  
  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
  
}
