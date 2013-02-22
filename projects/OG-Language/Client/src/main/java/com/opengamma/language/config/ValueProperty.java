// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.language.config;
public class ValueProperty extends com.opengamma.language.config.ConfigurationItem implements java.io.Serializable {
          @Override
          public <T> T accept (ConfigurationItemVisitor<T> visitor) { return visitor.visitValueProperty (this); }
  private static final long serialVersionUID = -3771451130679827296l;
  private String _configuration;
  public static final int CONFIGURATION_ORDINAL = 1;
  private String _name;
  public static final int NAME_ORDINAL = 2;
  private java.util.List<String> _value;
  public static final int VALUE_ORDINAL = 3;
  private boolean _optional;
  public static final int OPTIONAL_ORDINAL = 4;
  public ValueProperty (String name, boolean optional) {
    if (name == null) throw new NullPointerException ("name' cannot be null");
    _name = name;
    _optional = optional;
  }
  protected ValueProperty (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeField = fudgeMsg.getByOrdinal (NAME_ORDINAL);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ValueProperty - field 'name' is not present");
    try {
      _name = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ValueProperty - field 'name' is not string", e);
    }
    fudgeField = fudgeMsg.getByOrdinal (OPTIONAL_ORDINAL);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ValueProperty - field 'optional' is not present");
    try {
      _optional = fudgeMsg.getFieldValue (Boolean.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ValueProperty - field 'optional' is not boolean", e);
    }
    fudgeField = fudgeMsg.getByOrdinal (CONFIGURATION_ORDINAL);
    if (fudgeField != null)  {
      try {
        setConfiguration ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a ValueProperty - field 'configuration' is not string", e);
      }
    }
    fudgeFields = fudgeMsg.getAllByOrdinal (VALUE_ORDINAL);
    if (fudgeFields.size () > 0)  {
      final java.util.List<String> fudge1;
      fudge1 = new java.util.ArrayList<String> (fudgeFields.size ());
      for (org.fudgemsg.FudgeField fudge2 : fudgeFields) {
        try {
          fudge1.add (fudge2.getValue ().toString ());
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a ValueProperty - field 'value' is not string", e);
        }
      }
      setValue (fudge1);
    }
  }
  public ValueProperty (String configuration, String name, java.util.Collection<? extends String> value, boolean optional) {
    _configuration = configuration;
    if (name == null) throw new NullPointerException ("name' cannot be null");
    _name = name;
    if (value == null) _value = null;
    else {
      final java.util.List<String> fudge0 = new java.util.ArrayList<String> (value);
      for (java.util.ListIterator<String> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        String fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'value' cannot be null");
      }
      _value = fudge0;
    }
    _optional = optional;
  }
  protected ValueProperty (final ValueProperty source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _configuration = source._configuration;
    _name = source._name;
    if (source._value == null) _value = null;
    else {
      _value = new java.util.ArrayList<String> (source._value);
    }
    _optional = source._optional;
  }
  public ValueProperty clone () {
    return new ValueProperty (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_configuration != null)  {
      msg.add (null, CONFIGURATION_ORDINAL, _configuration);
    }
    if (_name != null)  {
      msg.add (null, NAME_ORDINAL, _name);
    }
    if (_value != null)  {
      for (String fudge1 : _value) {
        msg.add (null, VALUE_ORDINAL, fudge1);
      }
    }
    msg.add (null, OPTIONAL_ORDINAL, _optional);
  }
  public static ValueProperty fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.config.ValueProperty".equals (className)) break;
      try {
        return (com.opengamma.language.config.ValueProperty)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new ValueProperty (deserializer, fudgeMsg);
  }
  public String getConfiguration () {
    return _configuration;
  }
  public void setConfiguration (String configuration) {
    _configuration = configuration;
  }
  public String getName () {
    return _name;
  }
  public void setName (String name) {
    if (name == null) throw new NullPointerException ("name' cannot be null");
    _name = name;
  }
  public java.util.List<String> getValue () {
    if (_value != null) {
      return java.util.Collections.unmodifiableList (_value);
    }
    else return null;
  }
  public void setValue (String value) {
    if (value == null) _value = null;
    else {
      _value = new java.util.ArrayList<String> (1);
      addValue (value);
    }
  }
  public void setValue (java.util.Collection<? extends String> value) {
    if (value == null) _value = null;
    else {
      final java.util.List<String> fudge0 = new java.util.ArrayList<String> (value);
      for (java.util.ListIterator<String> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        String fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'value' cannot be null");
      }
      _value = fudge0;
    }
  }
  public void addValue (String value) {
    if (value == null) throw new NullPointerException ("'value' cannot be null");
    if (_value == null) _value = new java.util.ArrayList<String> ();
    _value.add (value);
  }
  public boolean getOptional () {
    return _optional;
  }
  public void setOptional (boolean optional) {
    _optional = optional;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof ValueProperty)) return false;
    ValueProperty msg = (ValueProperty)o;
    if (_configuration != null) {
      if (msg._configuration != null) {
        if (!_configuration.equals (msg._configuration)) return false;
      }
      else return false;
    }
    else if (msg._configuration != null) return false;
    if (_name != null) {
      if (msg._name != null) {
        if (!_name.equals (msg._name)) return false;
      }
      else return false;
    }
    else if (msg._name != null) return false;
    if (_value != null) {
      if (msg._value != null) {
        if (!_value.equals (msg._value)) return false;
      }
      else return false;
    }
    else if (msg._value != null) return false;
    if (_optional != msg._optional) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_configuration != null) hc += _configuration.hashCode ();
    hc *= 31;
    if (_name != null) hc += _name.hashCode ();
    hc *= 31;
    if (_value != null) hc += _value.hashCode ();
    hc *= 31;
    if (_optional) hc++;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
