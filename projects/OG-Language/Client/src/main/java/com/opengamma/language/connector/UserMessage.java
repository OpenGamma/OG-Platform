// Automatically created - do not modify
///CLOVER:OFF - CSOFF
package com.opengamma.language.connector;
public class UserMessage implements java.io.Serializable {
  private static final long serialVersionUID = -37149920464748l;
  private Integer _handle;
  public static final int HANDLE_ORDINAL = 1;
  private com.opengamma.language.connector.UserMessagePayload _payload;
  public static final int PAYLOAD_ORDINAL = 2;
  public UserMessage (com.opengamma.language.connector.UserMessagePayload payload) {
    if (payload == null) throw new NullPointerException ("'payload' cannot be null");
    else {
      _payload = payload;
    }
  }
  protected UserMessage (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByOrdinal (PAYLOAD_ORDINAL);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a UserMessage - field 'payload' is not present");
    try {
      _payload = com.opengamma.language.connector.UserMessagePayload.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a UserMessage - field 'payload' is not UserMessagePayload message", e);
    }
    fudgeField = fudgeMsg.getByOrdinal (HANDLE_ORDINAL);
    if (fudgeField != null)  {
      try {
        setHandle (fudgeMsg.getFieldValue (Integer.class, fudgeField));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a UserMessage - field 'handle' is not integer", e);
      }
    }
  }
  public UserMessage (Integer handle, com.opengamma.language.connector.UserMessagePayload payload) {
    _handle = handle;
    if (payload == null) throw new NullPointerException ("'payload' cannot be null");
    else {
      _payload = payload;
    }
  }
  protected UserMessage (final UserMessage source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _handle = source._handle;
    if (source._payload == null) _payload = null;
    else {
      _payload = source._payload;
    }
  }
  public UserMessage clone () {
    return new UserMessage (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    if (_handle != null)  {
      msg.add (null, HANDLE_ORDINAL, _handle);
    }
    if (_payload != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _payload.getClass (), com.opengamma.language.connector.UserMessagePayload.class);
      _payload.toFudgeMsg (serializer, fudge1);
      msg.add (null, PAYLOAD_ORDINAL, fudge1);
    }
  }
  public static UserMessage fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.connector.UserMessage".equals (className)) break;
      try {
        return (com.opengamma.language.connector.UserMessage)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new UserMessage (deserializer, fudgeMsg);
  }
  public Integer getHandle () {
    return _handle;
  }
  public void setHandle (Integer handle) {
    _handle = handle;
  }
  public com.opengamma.language.connector.UserMessagePayload getPayload () {
    return _payload;
  }
  public void setPayload (com.opengamma.language.connector.UserMessagePayload payload) {
    if (payload == null) throw new NullPointerException ("'payload' cannot be null");
    else {
      _payload = payload;
    }
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof UserMessage)) return false;
    UserMessage msg = (UserMessage)o;
    if (_handle != null) {
      if (msg._handle != null) {
        if (!_handle.equals (msg._handle)) return false;
      }
      else return false;
    }
    else if (msg._handle != null) return false;
    if (_payload != null) {
      if (msg._payload != null) {
        if (!_payload.equals (msg._payload)) return false;
      }
      else return false;
    }
    else if (msg._payload != null) return false;
    return true;
  }
  public int hashCode () {
    int hc = 1;
    hc *= 31;
    if (_handle != null) hc += _handle.hashCode ();
    hc *= 31;
    if (_payload != null) hc += _payload.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
