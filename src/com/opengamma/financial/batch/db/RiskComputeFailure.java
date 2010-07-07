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
public class RiskComputeFailure {
  
  private long _id;
  private String _functionId;
  private String _functionName;
  private String _exceptionClass;
  private String _stackTrace;
  
  public long getId() {
    return _id;
  }
  
  public void setId(long id) {
    _id = id;
  }
  
  public String getFunctionId() {
    return _functionId;
  }
  
  public void setFunctionId(String functionId) {
    _functionId = functionId;
  }
  
  public String getFunctionName() {
    return _functionName;
  }
  
  public void setFunctionName(String functionName) {
    _functionName = functionName;
  }
  
  public String getExceptionClass() {
    return _exceptionClass;
  }
  
  public void setExceptionClass(String exceptionClass) {
    _exceptionClass = exceptionClass;
  }
  
  public String getStackTrace() {
    return _stackTrace;
  }
  
  public void setStackTrace(String stackTrace) {
    _stackTrace = stackTrace;
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
