/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeMsg;

import com.bloomberglp.blpapi.Element;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class PerSecurityReferenceDataResult {
  private final String _security;
  private FudgeMsg _fieldData;
  private Element _eidData;
  private final List<String> _exceptions;
  private final Map<String, ErrorInfo> _fieldExceptions;
  
  public PerSecurityReferenceDataResult(String security) {
    ArgumentChecker.notNull(security, "Security Description");
    _security = security;
    _exceptions = new ArrayList<String>();
    _fieldExceptions = new LinkedHashMap<String, ErrorInfo>();
  }
  
  public PerSecurityReferenceDataResult(PerSecurityReferenceDataResult from) {
    _security = from.getSecurity();
    _fieldData = from.getFieldData();
    _eidData = from.getEidData();
    _exceptions = from.getExceptions();
    _fieldExceptions = from.getFieldExceptions();
  }

  /**
   * @return the security
   */
  public String getSecurity() {
    return _security;
  }

  /**
   * @return the fieldData
   */
  public FudgeMsg getFieldData() {
    return _fieldData;
  }
  /**
   * @param fieldData the fieldData to set
   */
  public void setFieldData(FudgeMsg fieldData) {
    _fieldData = fieldData;
  }
  
  public Element getEidData() {
    return _eidData;
  }

  public void setEidData(Element eidData) {
    _eidData = eidData;
  }

  /**
   * @return the exceptions
   */
  public List<String> getExceptions() {
    return _exceptions;
  }

  public void addFieldException(String fieldId, ErrorInfo errorInfo) {
    _fieldExceptions.put(fieldId, errorInfo);
  } 
  
  public Map<String, ErrorInfo> getFieldExceptions() {
    return _fieldExceptions;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
