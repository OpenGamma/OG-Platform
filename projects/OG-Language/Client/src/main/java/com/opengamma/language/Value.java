// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.language;
public class Value implements java.io.Serializable {
  private static final long serialVersionUID = -3384800892304866449l;
  private Boolean _boolValue;
  public static final int BOOL_VALUE_ORDINAL = 1;
  private Integer _intValue;
  public static final int INT_VALUE_ORDINAL = 2;
  private Double _doubleValue;
  public static final int DOUBLE_VALUE_ORDINAL = 3;
  private String _stringValue;
  public static final int STRING_VALUE_ORDINAL = 4;
  private org.fudgemsg.FudgeMsg _messageValue;
  public static final int MESSAGE_VALUE_ORDINAL = 5;
  private Integer _errorValue;
  public static final int ERROR_VALUE_ORDINAL = 6;
  public Value () {
  }
  protected Value (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByOrdinal (BOOL_VALUE_ORDINAL);
    if (fudgeField != null)  {
      try {
        setBoolValue (fudgeMsg.getFieldValue (Boolean.class, fudgeField));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a Value - field 'boolValue' is not boolean", e);
      }
    }
    fudgeField = fudgeMsg.getByOrdinal (INT_VALUE_ORDINAL);
    if (fudgeField != null)  {
      try {
        setIntValue (fudgeMsg.getFieldValue (Integer.class, fudgeField));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a Value - field 'intValue' is not integer", e);
      }
    }
    fudgeField = fudgeMsg.getByOrdinal (DOUBLE_VALUE_ORDINAL);
    if (fudgeField != null)  {
      try {
        setDoubleValue (fudgeMsg.getFieldValue (Double.class, fudgeField));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a Value - field 'doubleValue' is not double", e);
      }
    }
    fudgeField = fudgeMsg.getByOrdinal (STRING_VALUE_ORDINAL);
    if (fudgeField != null)  {
      try {
        setStringValue ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a Value - field 'stringValue' is not string", e);
      }
    }
    fudgeField = fudgeMsg.getByOrdinal (MESSAGE_VALUE_ORDINAL);
    if (fudgeField != null)  {
      try {
        final org.fudgemsg.FudgeMsg fudge1;
        fudge1 = fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField);
        setMessageValue (fudge1);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a Value - field 'messageValue' is not anonymous/unknown message", e);
      }
    }
    fudgeField = fudgeMsg.getByOrdinal (ERROR_VALUE_ORDINAL);
    if (fudgeField != null)  {
      try {
        setErrorValue (fudgeMsg.getFieldValue (Integer.class, fudgeField));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a Value - field 'errorValue' is not integer", e);
      }
    }
  }
  public Value (Boolean boolValue, Integer intValue, Double doubleValue, String stringValue, org.fudgemsg.FudgeMsg messageValue, Integer errorValue) {
    _boolValue = boolValue;
    _intValue = intValue;
    _doubleValue = doubleValue;
    _stringValue = stringValue;
    _messageValue = messageValue;
    _errorValue = errorValue;
  }
  protected Value (final Value source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _boolValue = source._boolValue;
    _intValue = source._intValue;
    _doubleValue = source._doubleValue;
    _stringValue = source._stringValue;
    _messageValue = source._messageValue;
    _errorValue = source._errorValue;
  }
  public Value clone () {
    return new Value (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    if (_boolValue != null)  {
      msg.add (null, BOOL_VALUE_ORDINAL, _boolValue);
    }
    if (_intValue != null)  {
      msg.add (null, INT_VALUE_ORDINAL, _intValue);
    }
    if (_doubleValue != null)  {
      msg.add (null, DOUBLE_VALUE_ORDINAL, _doubleValue);
    }
    if (_stringValue != null)  {
      msg.add (null, STRING_VALUE_ORDINAL, _stringValue);
    }
    if (_messageValue != null)  {
      msg.add (null, MESSAGE_VALUE_ORDINAL, (_messageValue instanceof org.fudgemsg.MutableFudgeMsg) ? serializer.newMessage (_messageValue) : _messageValue);
    }
    if (_errorValue != null)  {
      msg.add (null, ERROR_VALUE_ORDINAL, _errorValue);
    }
  }
  public static Value fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.Value".equals (className)) break;
      try {
        return (com.opengamma.language.Value)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Value (deserializer, fudgeMsg);
  }
  public Boolean getBoolValue () {
    return _boolValue;
  }
  public void setBoolValue (Boolean boolValue) {
    _boolValue = boolValue;
  }
  public Integer getIntValue () {
    return _intValue;
  }
  public void setIntValue (Integer intValue) {
    _intValue = intValue;
  }
  public Double getDoubleValue () {
    return _doubleValue;
  }
  public void setDoubleValue (Double doubleValue) {
    _doubleValue = doubleValue;
  }
  public String getStringValue () {
    return _stringValue;
  }
  public void setStringValue (String stringValue) {
    _stringValue = stringValue;
  }
  public org.fudgemsg.FudgeMsg getMessageValue () {
    return _messageValue;
  }
  public void setMessageValue (org.fudgemsg.FudgeMsg messageValue) {
    _messageValue = messageValue;
  }
  public Integer getErrorValue () {
    return _errorValue;
  }
  public void setErrorValue (Integer errorValue) {
    _errorValue = errorValue;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof Value)) return false;
    Value msg = (Value)o;
    if (_boolValue != null) {
      if (msg._boolValue != null) {
        if (!_boolValue.equals (msg._boolValue)) return false;
      }
      else return false;
    }
    else if (msg._boolValue != null) return false;
    if (_intValue != null) {
      if (msg._intValue != null) {
        if (!_intValue.equals (msg._intValue)) return false;
      }
      else return false;
    }
    else if (msg._intValue != null) return false;
    if (_doubleValue != null) {
      if (msg._doubleValue != null) {
        if (!_doubleValue.equals (msg._doubleValue)) return false;
      }
      else return false;
    }
    else if (msg._doubleValue != null) return false;
    if (_stringValue != null) {
      if (msg._stringValue != null) {
        if (!_stringValue.equals (msg._stringValue)) return false;
      }
      else return false;
    }
    else if (msg._stringValue != null) return false;
    if (_messageValue != null) {
      if (msg._messageValue != null) {
        if (!_messageValue.equals (msg._messageValue)) return false;
      }
      else return false;
    }
    else if (msg._messageValue != null) return false;
    if (_errorValue != null) {
      if (msg._errorValue != null) {
        if (!_errorValue.equals (msg._errorValue)) return false;
      }
      else return false;
    }
    else if (msg._errorValue != null) return false;
    return true;
  }
  public int hashCode () {
    int hc = 1;
    hc *= 31;
    if (_boolValue != null) hc += _boolValue.hashCode ();
    hc *= 31;
    if (_intValue != null) hc += _intValue.hashCode ();
    hc *= 31;
    if (_doubleValue != null) hc += _doubleValue.hashCode ();
    hc *= 31;
    if (_stringValue != null) hc += _stringValue.hashCode ();
    hc *= 31;
    if (_messageValue != null) hc += _messageValue.hashCode ();
    hc *= 31;
    if (_errorValue != null) hc += _errorValue.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
