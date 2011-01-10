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

/**
 * 
 */
public class ComputeFailure {
  
  private long _id = -1;
  private String _functionId;
  private String _exceptionClass;
  private String _exceptionMsg;
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
  
  public String getExceptionClass() {
    return _exceptionClass;
  }
  
  public void setExceptionClass(String exceptionClass) {
    _exceptionClass = exceptionClass;
  }
  
  public String getExceptionMsg() {
    return _exceptionMsg;
  }
  
  public void setExceptionMsg(String exceptionMsg) {
    _exceptionMsg = exceptionMsg;
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
  
  public SqlParameterSource toSqlParameterSource() {
    MapSqlParameterSource source = new MapSqlParameterSource();
    source.addValue("id", getId());
    source.addValue("function_id", getFunctionId());
    source.addValue("exception_class", getExceptionClass());
    source.addValue("exception_msg", getExceptionMsg());
    source.addValue("stack_trace", getStackTrace());
    return source;
  }

  
  public static String sqlInsert() {
    return "INSERT INTO " + BatchDbManagerImpl.getDatabaseSchema() + "rsk_compute_failure " +
              "(id, function_id, exception_class, exception_msg, stack_trace)" +
            "VALUES " +
              "(:id, :function_id, :exception_class, :exception_msg, :stack_trace)";
  }
  
  public static String sqlGet() {
    return "SELECT id FROM " + BatchDbManagerImpl.getDatabaseSchema() + "rsk_compute_failure WHERE " +
      "function_id = :function_id AND " +
      "exception_class = :exception_class AND " + 
      "exception_msg = :exception_msg AND " +
      "stack_trace = :stack_trace";
  }
  
  public static String sqlCount() {
    return "SELECT COUNT(*) FROM " + BatchDbManagerImpl.getDatabaseSchema() + "rsk_compute_failure";
  }

}
