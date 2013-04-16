// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.language.config;
public class MarketDataOverride extends com.opengamma.language.config.ConfigurationItem implements java.io.Serializable {
          @Override
          public <T> T accept (ConfigurationItemVisitor<T> visitor) { return visitor.visitMarketDataOverride (this); }
  private static final long serialVersionUID = -20420490360656398l;
  public enum Operation {
    ADD,
    MULTIPLY;
  }
  private com.opengamma.engine.value.ValueRequirement _valueRequirement;
  public static final String VALUE_REQUIREMENT_KEY = "valueRequirement";
  private Object _value;
  public static final String VALUE_KEY = "value";
  private com.opengamma.language.config.MarketDataOverride.Operation _operation;
  public static final String OPERATION_KEY = "operation";
  public MarketDataOverride (com.opengamma.engine.value.ValueRequirement valueRequirement) {
    if (valueRequirement == null) throw new NullPointerException ("'valueRequirement' cannot be null");
    else {
      _valueRequirement = valueRequirement;
    }
  }
  protected MarketDataOverride (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (VALUE_REQUIREMENT_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a MarketDataOverride - field 'valueRequirement' is not present");
    try {
      _valueRequirement = deserializer.fieldValueToObject (com.opengamma.engine.value.ValueRequirement.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a MarketDataOverride - field 'valueRequirement' is not ValueRequirement message", e);
    }
    fudgeField = fudgeMsg.getByName (VALUE_KEY);
    if (fudgeField != null)  {
      try {
        final Object fudge1;
        fudge1 = deserializer.fieldValueToObject (Object.class, fudgeField);
        setValue (fudge1);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a MarketDataOverride - field 'value' is not Object message", e);
      }
    }
    fudgeField = fudgeMsg.getByName (OPERATION_KEY);
    if (fudgeField != null)  {
      try {
        setOperation (fudgeMsg.getFieldValue (com.opengamma.language.config.MarketDataOverride.Operation.class, fudgeField));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a MarketDataOverride - field 'operation' is not Operation enum", e);
      }
    }
  }
  public MarketDataOverride (com.opengamma.engine.value.ValueRequirement valueRequirement, Object value, com.opengamma.language.config.MarketDataOverride.Operation operation) {
    if (valueRequirement == null) throw new NullPointerException ("'valueRequirement' cannot be null");
    else {
      _valueRequirement = valueRequirement;
    }
    if (value == null) _value = null;
    else {
      _value = value;
    }
    _operation = operation;
  }
  protected MarketDataOverride (final MarketDataOverride source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._valueRequirement == null) _valueRequirement = null;
    else {
      _valueRequirement = source._valueRequirement;
    }
    if (source._value == null) _value = null;
    else {
      _value = source._value;
    }
    _operation = source._operation;
  }
  public MarketDataOverride clone () {
    return new MarketDataOverride (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_valueRequirement != null)  {
      serializer.addToMessageWithClassHeaders (msg, VALUE_REQUIREMENT_KEY, null, _valueRequirement, com.opengamma.engine.value.ValueRequirement.class);
    }
    if (_value != null)  {
      serializer.addToMessageWithClassHeaders (msg, VALUE_KEY, null, _value, Object.class);
    }
    if (_operation != null)  {
      msg.add (OPERATION_KEY, null, _operation.name ());
    }
  }
  public static MarketDataOverride fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.config.MarketDataOverride".equals (className)) break;
      try {
        return (com.opengamma.language.config.MarketDataOverride)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new MarketDataOverride (deserializer, fudgeMsg);
  }
  public com.opengamma.engine.value.ValueRequirement getValueRequirement () {
    return _valueRequirement;
  }
  public void setValueRequirement (com.opengamma.engine.value.ValueRequirement valueRequirement) {
    if (valueRequirement == null) throw new NullPointerException ("'valueRequirement' cannot be null");
    else {
      _valueRequirement = valueRequirement;
    }
  }
  public Object getValue () {
    return _value;
  }
  public void setValue (Object value) {
    if (value == null) _value = null;
    else {
      _value = value;
    }
  }
  public com.opengamma.language.config.MarketDataOverride.Operation getOperation () {
    return _operation;
  }
  public void setOperation (com.opengamma.language.config.MarketDataOverride.Operation operation) {
    _operation = operation;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof MarketDataOverride)) return false;
    MarketDataOverride msg = (MarketDataOverride)o;
    if (_valueRequirement != null) {
      if (msg._valueRequirement != null) {
        if (!_valueRequirement.equals (msg._valueRequirement)) return false;
      }
      else return false;
    }
    else if (msg._valueRequirement != null) return false;
    if (_value != null) {
      if (msg._value != null) {
        if (!_value.equals (msg._value)) return false;
      }
      else return false;
    }
    else if (msg._value != null) return false;
    if (_operation != null) {
      if (msg._operation != null) {
        if (!_operation.equals (msg._operation)) return false;
      }
      else return false;
    }
    else if (msg._operation != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_valueRequirement != null) hc += _valueRequirement.hashCode ();
    hc *= 31;
    if (_value != null) hc += _value.hashCode ();
    hc *= 31;
    if (_operation != null) hc += _operation.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
