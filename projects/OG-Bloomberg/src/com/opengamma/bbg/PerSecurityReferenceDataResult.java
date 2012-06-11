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
 * Holder for the reference data results for a single security.
 */
public class PerSecurityReferenceDataResult {

  /**
   * The security key.
   */
  private final String _security;
  /**
   * The Fudge field data.
   */
  private FudgeMsg _fieldData;
  /**
   * The EID data.
   */
  private Element _eidData;
  /**
   * The exceptions.
   */
  private final List<String> _exceptions;
  /**
   * The field exceptions.
   */
  private final Map<String, ErrorInfo> _fieldExceptions;

  /**
   * Creates an instance.
   * 
   * @param securityKey  the security key, not null
   */
  public PerSecurityReferenceDataResult(String securityKey) {
    ArgumentChecker.notNull(securityKey, "securityKey");
    _security = securityKey;
    _exceptions = new ArrayList<String>();
    _fieldExceptions = new LinkedHashMap<String, ErrorInfo>();
  }

  /**
   * Creates an instance by copying another instance.
   * 
   * @param baseToCopy  the result to copy, not null
   */
  public PerSecurityReferenceDataResult(PerSecurityReferenceDataResult baseToCopy) {
    _security = baseToCopy.getSecurity();
    _fieldData = baseToCopy.getFieldData();
    _eidData = baseToCopy.getEidData();
    _exceptions = baseToCopy.getExceptions();
    _fieldExceptions = baseToCopy.getFieldExceptions();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security key.
   * 
   * @return the security, not null
   */
  public String getSecurity() {
    return _security;
  }

  /**
   * Gets the Fudge field data.
   * 
   * @return the field data, may be null
   */
  public FudgeMsg getFieldData() {
    return _fieldData;
  }

  /**
   * Sets the Fudge field data.
   * 
   * @param fieldData  the field data, may be null
   */
  public void setFieldData(FudgeMsg fieldData) {
    _fieldData = fieldData;
  }

  /**
   * Gets the EID data.
   * 
   * @return the EID data, may be null
   */
  public Element getEidData() {
    return _eidData;
  }

  /**
   * Sets the EID data.
   * 
   * @param eidData  the EID data, may be null
   */
  public void setEidData(Element eidData) {
    _eidData = eidData;
  }

  /**
   * Gets the exceptions.
   * 
   * @return the connected modifiable list of exceptions, not null
   */
  public List<String> getExceptions() {
    return _exceptions;
  }

  /**
   * Adds a field exception to the list.
   * 
   * @param fieldId  the field id, may be null
   * @param errorInfo  the error, may be null
   */
  public void addFieldException(String fieldId, ErrorInfo errorInfo) {
    _fieldExceptions.put(fieldId, errorInfo);
  } 

  /**
   * Gets the field exceptions.
   * 
   * @return the connected modifiable list of field exceptions, not null
   */
  public Map<String, ErrorInfo> getFieldExceptions() {
    return _fieldExceptions;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
