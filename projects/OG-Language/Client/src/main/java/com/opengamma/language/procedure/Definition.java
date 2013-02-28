// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.language.procedure;
public class Definition extends com.opengamma.language.definition.Definition implements java.io.Serializable {
  private static final long serialVersionUID = -46879673903l;
  private int _returnCount;
  public static final String RETURN_COUNT_KEY = "returnCount";
  public static final int RETURN_COUNT = 0;
  public Definition (String name) {
    super (name);
    setReturnCount (RETURN_COUNT);
  }
  protected Definition (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (RETURN_COUNT_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a Definition - field 'returnCount' is not present");
    try {
      _returnCount = fudgeMsg.getFieldValue (Integer.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a Definition - field 'returnCount' is not integer", e);
    }
  }
  public Definition (String name, String description, java.util.Collection<? extends String> alias, String category, java.util.Collection<? extends com.opengamma.language.definition.Parameter> parameter, int returnCount) {
    super (name, description, alias, category, parameter);
    _returnCount = returnCount;
  }
  protected Definition (final Definition source) {
    super (source);
    if (source != null) {
      _returnCount = source._returnCount;
    }
    else {
      _returnCount = RETURN_COUNT;
    }
  }
  public Definition clone () {
    return new Definition (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    msg.add (RETURN_COUNT_KEY, null, _returnCount);
  }
  public static Definition fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.procedure.Definition".equals (className)) break;
      try {
        return (com.opengamma.language.procedure.Definition)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Definition (deserializer, fudgeMsg);
  }
  public int getReturnCount () {
    return _returnCount;
  }
  public void setReturnCount (int returnCount) {
    _returnCount = returnCount;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof Definition)) return false;
    Definition msg = (Definition)o;
    if (_returnCount != msg._returnCount) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc = (hc * 31) + (int)_returnCount;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
