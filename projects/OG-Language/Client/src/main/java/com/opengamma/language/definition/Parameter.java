// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.language.definition;
public class Parameter implements java.io.Serializable {
  private static final long serialVersionUID = -1626374043775451l;
  private String _name;
  public static final String NAME_KEY = "name";
  private String _description;
  public static final String DESCRIPTION_KEY = "description";
  private boolean _required;
  public static final String REQUIRED_KEY = "required";
  public Parameter (String name, boolean required) {
    if (name == null) throw new NullPointerException ("name' cannot be null");
    _name = name;
    _required = required;
  }
  protected Parameter (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (NAME_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a Parameter - field 'name' is not present");
    try {
      _name = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a Parameter - field 'name' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (REQUIRED_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a Parameter - field 'required' is not present");
    try {
      _required = fudgeMsg.getFieldValue (Boolean.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a Parameter - field 'required' is not boolean", e);
    }
    fudgeField = fudgeMsg.getByName (DESCRIPTION_KEY);
    if (fudgeField != null)  {
      try {
        setDescription ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a Parameter - field 'description' is not string", e);
      }
    }
  }
  public Parameter (String name, String description, boolean required) {
    if (name == null) throw new NullPointerException ("name' cannot be null");
    _name = name;
    _description = description;
    _required = required;
  }
  protected Parameter (final Parameter source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _name = source._name;
    _description = source._description;
    _required = source._required;
  }
  public Parameter clone () {
    return new Parameter (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    if (_name != null)  {
      msg.add (NAME_KEY, null, _name);
    }
    if (_description != null)  {
      msg.add (DESCRIPTION_KEY, null, _description);
    }
    msg.add (REQUIRED_KEY, null, _required);
  }
  public static Parameter fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.definition.Parameter".equals (className)) break;
      try {
        return (com.opengamma.language.definition.Parameter)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Parameter (deserializer, fudgeMsg);
  }
  public String getName () {
    return _name;
  }
  public void setName (String name) {
    if (name == null) throw new NullPointerException ("name' cannot be null");
    _name = name;
  }
  public String getDescription () {
    return _description;
  }
  public void setDescription (String description) {
    _description = description;
  }
  public boolean getRequired () {
    return _required;
  }
  public void setRequired (boolean required) {
    _required = required;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof Parameter)) return false;
    Parameter msg = (Parameter)o;
    if (_name != null) {
      if (msg._name != null) {
        if (!_name.equals (msg._name)) return false;
      }
      else return false;
    }
    else if (msg._name != null) return false;
    if (_description != null) {
      if (msg._description != null) {
        if (!_description.equals (msg._description)) return false;
      }
      else return false;
    }
    else if (msg._description != null) return false;
    if (_required != msg._required) return false;
    return true;
  }
  public int hashCode () {
    int hc = 1;
    hc *= 31;
    if (_name != null) hc += _name.hashCode ();
    hc *= 31;
    if (_description != null) hc += _description.hashCode ();
    hc *= 31;
    if (_required) hc++;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
