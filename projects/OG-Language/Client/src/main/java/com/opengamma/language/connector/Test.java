// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.language.connector;
public class Test extends com.opengamma.language.connector.UserMessagePayload implements java.io.Serializable {
  public <T1,T2> T1 accept (final UserMessagePayloadVisitor<T1,T2> visitor, final T2 data) throws com.opengamma.util.async.AsynchronousExecution { return visitor.visitTest (this, data); }
  private static final long serialVersionUID = 48456185335614l;
  public enum Operation {
    ECHO_REQUEST,
    ECHO_REQUEST_A,
    ECHO_RESPONSE,
    ECHO_RESPONSE_A,
    VOID_REQUEST,
    VOID_REQUEST_A,
    VOID_RESPONSE_A,
    CRASH_REQUEST,
    PAUSE_REQUEST,
    STASH_REQUEST,
    STASH_RESPONSE;
  }
  private com.opengamma.language.connector.Test.Operation _operation;
  public static final int OPERATION_ORDINAL = 1;
  private int _nonce;
  public static final int NONCE_ORDINAL = 2;
  public Test (com.opengamma.language.connector.Test.Operation operation, int nonce) {
    if (operation == null) throw new NullPointerException ("operation' cannot be null");
    _operation = operation;
    _nonce = nonce;
  }
  protected Test (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByOrdinal (OPERATION_ORDINAL);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a Test - field 'operation' is not present");
    try {
      _operation = fudgeMsg.getFieldValue (com.opengamma.language.connector.Test.Operation.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a Test - field 'operation' is not Operation enum", e);
    }
    fudgeField = fudgeMsg.getByOrdinal (NONCE_ORDINAL);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a Test - field 'nonce' is not present");
    try {
      _nonce = fudgeMsg.getFieldValue (Integer.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a Test - field 'nonce' is not integer", e);
    }
  }
  protected Test (final Test source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _operation = source._operation;
    _nonce = source._nonce;
  }
  public Test clone () {
    return new Test (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_operation != null)  {
      msg.add (null, OPERATION_ORDINAL, _operation.name ());
    }
    msg.add (null, NONCE_ORDINAL, _nonce);
  }
  public static Test fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.connector.Test".equals (className)) break;
      try {
        return (com.opengamma.language.connector.Test)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Test (deserializer, fudgeMsg);
  }
  public com.opengamma.language.connector.Test.Operation getOperation () {
    return _operation;
  }
  public void setOperation (com.opengamma.language.connector.Test.Operation operation) {
    if (operation == null) throw new NullPointerException ("operation' cannot be null");
    _operation = operation;
  }
  public int getNonce () {
    return _nonce;
  }
  public void setNonce (int nonce) {
    _nonce = nonce;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof Test)) return false;
    Test msg = (Test)o;
    if (_operation != null) {
      if (msg._operation != null) {
        if (!_operation.equals (msg._operation)) return false;
      }
      else return false;
    }
    else if (msg._operation != null) return false;
    if (_nonce != msg._nonce) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_operation != null) hc += _operation.hashCode ();
    hc = (hc * 31) + (int)_nonce;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
