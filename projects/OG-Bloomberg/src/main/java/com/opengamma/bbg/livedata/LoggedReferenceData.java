/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 * 
 */
public class LoggedReferenceData {
  
  private static final String SECURITY_FIELD = "security";
  private static final String FIELD_NAME_FIELD = "fieldName";
  private static final String FIELD_VALUE_FIELD = "fieldValue";
  
  private final String _security;
  private final String _field;
  private final Object _value;
  
  /**
   * @param security  the security
   * @param field  the field
   * @param value  the field value
   */
  public LoggedReferenceData(String security, String field, Object value) {
    super();
    _security = security;
    _field = field;
    _value = value;
  }
  
  public String getSecurity() {
    return _security;
  }
  
  public String getField() {
    return _field;
  }
  
  public Object getValue() {
    return _value;
  }
  
  public FudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    msg.add(SECURITY_FIELD, getSecurity());
    msg.add(FIELD_NAME_FIELD, getField());
    serializer.addToMessage(msg, FIELD_VALUE_FIELD, null, getValue());
    return msg;
  }
  
  public static LoggedReferenceData fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg message) {
    FudgeField fieldValueField = message.getByName(FIELD_VALUE_FIELD);
    Object fieldValue = fieldValueField != null ? deserializer.fieldValueToObject(fieldValueField) : null;
    return new LoggedReferenceData(
        message.getString(SECURITY_FIELD),
        message.getString(FIELD_NAME_FIELD),
        fieldValue);
  }
  
}
