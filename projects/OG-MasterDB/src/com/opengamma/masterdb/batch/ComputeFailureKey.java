/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import com.opengamma.util.ArgumentChecker;

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
    ArgumentChecker.notNull(functionId, "functionId");
    ArgumentChecker.notNull(exceptionClass, "exceptionClass");
    ArgumentChecker.notNull(stackTrace, "stackTrace");
    
    _functionId = functionId;
    _exceptionClass = exceptionClass;
    _stackTrace = stackTrace.substring(0, Math.min(stackTrace.length(), 2000));
    
    if (exceptionMsg == null) {
      _exceptionMsg = ""; // although Throwable.getMessage() can return null, our db doesn't allow nulls
    } else {
      _exceptionMsg = exceptionMsg.substring(0, Math.min(exceptionMsg.length(), 255));
    }
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
  
  public SqlParameterSource toSqlParameterSource() {
    MapSqlParameterSource source = new MapSqlParameterSource();
    source.addValue("function_id", getFunctionId());
    source.addValue("exception_class", getExceptionClass());
    source.addValue("exception_msg", getExceptionMsg());
    source.addValue("stack_trace", getStackTrace());
    return source;
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
