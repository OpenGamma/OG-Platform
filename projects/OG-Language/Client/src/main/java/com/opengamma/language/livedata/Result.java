// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.language.livedata;
public class Result extends com.opengamma.language.connector.LiveData implements java.io.Serializable {
  private static final long serialVersionUID = -23781587995738l;
  private Integer _connection;
  public static final String CONNECTION_KEY = "connection";
  private com.opengamma.language.Data _result;
  public static final String RESULT_KEY = "result";
  public Result () {
  }
  protected Result (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (CONNECTION_KEY);
    if (fudgeField != null)  {
      try {
        setConnection (fudgeMsg.getFieldValue (Integer.class, fudgeField));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a Result - field 'connection' is not integer", e);
      }
    }
    fudgeField = fudgeMsg.getByName (RESULT_KEY);
    if (fudgeField != null)  {
      try {
        final com.opengamma.language.Data fudge1;
        fudge1 = com.opengamma.language.Data.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
        setResult (fudge1);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a Result - field 'result' is not Data message", e);
      }
    }
  }
  public Result (Integer connection, com.opengamma.language.Data result) {
    _connection = connection;
    if (result == null) _result = null;
    else {
      _result = (com.opengamma.language.Data)result.clone ();
    }
  }
  protected Result (final Result source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _connection = source._connection;
    if (source._result == null) _result = null;
    else {
      _result = (com.opengamma.language.Data)source._result.clone ();
    }
  }
  public Result clone () {
    return new Result (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_connection != null)  {
      msg.add (CONNECTION_KEY, null, _connection);
    }
    if (_result != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _result.getClass (), com.opengamma.language.Data.class);
      _result.toFudgeMsg (serializer, fudge1);
      msg.add (RESULT_KEY, null, fudge1);
    }
  }
  public static Result fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.livedata.Result".equals (className)) break;
      try {
        return (com.opengamma.language.livedata.Result)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Result (deserializer, fudgeMsg);
  }
  public Integer getConnection () {
    return _connection;
  }
  public void setConnection (Integer connection) {
    _connection = connection;
  }
  public com.opengamma.language.Data getResult () {
    return _result;
  }
  public void setResult (com.opengamma.language.Data result) {
    if (result == null) _result = null;
    else {
      _result = (com.opengamma.language.Data)result.clone ();
    }
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof Result)) return false;
    Result msg = (Result)o;
    if (_connection != null) {
      if (msg._connection != null) {
        if (!_connection.equals (msg._connection)) return false;
      }
      else return false;
    }
    else if (msg._connection != null) return false;
    if (_result != null) {
      if (msg._result != null) {
        if (!_result.equals (msg._result)) return false;
      }
      else return false;
    }
    else if (msg._result != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_connection != null) hc += _connection.hashCode ();
    hc *= 31;
    if (_result != null) hc += _result.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
