// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.language.livedata;
public class QueryValue extends com.opengamma.language.connector.LiveData implements java.io.Serializable {
  public <T1,T2> T1 accept (final LiveDataVisitor<T1,T2> visitor, final T2 data) throws com.opengamma.util.async.AsynchronousExecution { return visitor.visitQueryValue (this, data); }
  private static final long serialVersionUID = -50171313113l;
  private int _identifier;
  public static final String IDENTIFIER_KEY = "identifier";
  public QueryValue (int identifier) {
    _identifier = identifier;
  }
  protected QueryValue (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (IDENTIFIER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a QueryValue - field 'identifier' is not present");
    try {
      _identifier = fudgeMsg.getFieldValue (Integer.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a QueryValue - field 'identifier' is not integer", e);
    }
  }
  protected QueryValue (final QueryValue source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _identifier = source._identifier;
  }
  public QueryValue clone () {
    return new QueryValue (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    msg.add (IDENTIFIER_KEY, null, _identifier);
  }
  public static QueryValue fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.livedata.QueryValue".equals (className)) break;
      try {
        return (com.opengamma.language.livedata.QueryValue)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new QueryValue (deserializer, fudgeMsg);
  }
  public int getIdentifier () {
    return _identifier;
  }
  public void setIdentifier (int identifier) {
    _identifier = identifier;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof QueryValue)) return false;
    QueryValue msg = (QueryValue)o;
    if (_identifier != msg._identifier) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc = (hc * 31) + (int)_identifier;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
