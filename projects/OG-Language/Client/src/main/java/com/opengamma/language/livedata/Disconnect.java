// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.language.livedata;
public class Disconnect extends com.opengamma.language.connector.LiveData implements java.io.Serializable {
  public <T1,T2> T1 accept (final LiveDataVisitor<T1,T2> visitor, final T2 data) throws com.opengamma.util.async.AsynchronousExecution { return visitor.visitDisconnect (this, data); }
  private static final long serialVersionUID = -24045094766l;
  private int _connection;
  public static final String CONNECTION_KEY = "connection";
  public Disconnect (int connection) {
    _connection = connection;
  }
  protected Disconnect (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (CONNECTION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a Disconnect - field 'connection' is not present");
    try {
      _connection = fudgeMsg.getFieldValue (Integer.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a Disconnect - field 'connection' is not integer", e);
    }
  }
  protected Disconnect (final Disconnect source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _connection = source._connection;
  }
  public Disconnect clone () {
    return new Disconnect (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    msg.add (CONNECTION_KEY, null, _connection);
  }
  public static Disconnect fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.livedata.Disconnect".equals (className)) break;
      try {
        return (com.opengamma.language.livedata.Disconnect)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Disconnect (deserializer, fudgeMsg);
  }
  public int getConnection () {
    return _connection;
  }
  public void setConnection (int connection) {
    _connection = connection;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof Disconnect)) return false;
    Disconnect msg = (Disconnect)o;
    if (_connection != msg._connection) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc = (hc * 31) + (int)_connection;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
