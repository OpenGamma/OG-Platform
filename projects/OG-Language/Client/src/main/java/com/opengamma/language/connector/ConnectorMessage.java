// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.language.connector;
public class ConnectorMessage implements java.io.Serializable {
  private static final long serialVersionUID = 50678589418965l;
  public enum Operation {
    HEARTBEAT (1),
    POISON (2),
    STASH (3);
    private final int _fudgeEncoding;
    private Operation (final int fudgeEncoding) {
      _fudgeEncoding = fudgeEncoding;
    }
    public int getFudgeEncoding () {
      return _fudgeEncoding;
    }
    public static Operation fromFudgeEncoding (final int fudgeEncoding) {
      switch (fudgeEncoding) {
        case 1 : return HEARTBEAT;
        case 2 : return POISON;
        case 3 : return STASH;
        default : throw new IllegalArgumentException ("field is not a Operation - invalid value '" + fudgeEncoding + "'");
      }
    }
  }
  private com.opengamma.language.connector.ConnectorMessage.Operation _operation;
  public static final int OPERATION_ORDINAL = 1;
  private org.fudgemsg.FudgeMsg _stash;
  public static final int STASH_ORDINAL = 2;
  public ConnectorMessage (com.opengamma.language.connector.ConnectorMessage.Operation operation) {
    if (operation == null) throw new NullPointerException ("operation' cannot be null");
    _operation = operation;
  }
  protected ConnectorMessage (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByOrdinal (OPERATION_ORDINAL);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ConnectorMessage - field 'operation' is not present");
    try {
      _operation = com.opengamma.language.connector.ConnectorMessage.Operation.fromFudgeEncoding (fudgeMsg.getFieldValue (Integer.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ConnectorMessage - field 'operation' is not Operation enum", e);
    }
    fudgeField = fudgeMsg.getByOrdinal (STASH_ORDINAL);
    if (fudgeField != null)  {
      try {
        final org.fudgemsg.FudgeMsg fudge1;
        fudge1 = fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField);
        setStash (fudge1);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a ConnectorMessage - field 'stash' is not anonymous/unknown message", e);
      }
    }
  }
  public ConnectorMessage (com.opengamma.language.connector.ConnectorMessage.Operation operation, org.fudgemsg.FudgeMsg stash) {
    if (operation == null) throw new NullPointerException ("operation' cannot be null");
    _operation = operation;
    _stash = stash;
  }
  protected ConnectorMessage (final ConnectorMessage source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _operation = source._operation;
    _stash = source._stash;
  }
  public ConnectorMessage clone () {
    return new ConnectorMessage (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    if (_operation != null)  {
      msg.add (null, OPERATION_ORDINAL, _operation.getFudgeEncoding ());
    }
    if (_stash != null)  {
      msg.add (null, STASH_ORDINAL, (_stash instanceof org.fudgemsg.MutableFudgeMsg) ? serializer.newMessage (_stash) : _stash);
    }
  }
  public static ConnectorMessage fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.connector.ConnectorMessage".equals (className)) break;
      try {
        return (com.opengamma.language.connector.ConnectorMessage)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new ConnectorMessage (deserializer, fudgeMsg);
  }
  public com.opengamma.language.connector.ConnectorMessage.Operation getOperation () {
    return _operation;
  }
  public void setOperation (com.opengamma.language.connector.ConnectorMessage.Operation operation) {
    if (operation == null) throw new NullPointerException ("operation' cannot be null");
    _operation = operation;
  }
  public org.fudgemsg.FudgeMsg getStash () {
    return _stash;
  }
  public void setStash (org.fudgemsg.FudgeMsg stash) {
    _stash = stash;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof ConnectorMessage)) return false;
    ConnectorMessage msg = (ConnectorMessage)o;
    if (_operation != null) {
      if (msg._operation != null) {
        if (!_operation.equals (msg._operation)) return false;
      }
      else return false;
    }
    else if (msg._operation != null) return false;
    if (_stash != null) {
      if (msg._stash != null) {
        if (!_stash.equals (msg._stash)) return false;
      }
      else return false;
    }
    else if (msg._stash != null) return false;
    return true;
  }
  public int hashCode () {
    int hc = 1;
    hc *= 31;
    if (_operation != null) hc += _operation.hashCode ();
    hc *= 31;
    if (_stash != null) hc += _stash.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
