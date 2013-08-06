// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.livedata.msg;
public class EntitlementResponse implements java.io.Serializable {
  private static final long serialVersionUID = 45419841239170816l;
  private com.opengamma.livedata.LiveDataSpecification _liveDataSpecification;
  public static final String LIVE_DATA_SPECIFICATION_KEY = "liveDataSpecification";
  private boolean _isEntitled;
  public static final String IS_ENTITLED_KEY = "isEntitled";
  private String _msg;
  public static final String MSG_KEY = "msg";
  public EntitlementResponse (com.opengamma.livedata.LiveDataSpecification liveDataSpecification, boolean isEntitled) {
    if (liveDataSpecification == null) throw new NullPointerException ("'liveDataSpecification' cannot be null");
    else {
      _liveDataSpecification = liveDataSpecification;
    }
    _isEntitled = isEntitled;
  }
  protected EntitlementResponse (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (LIVE_DATA_SPECIFICATION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EntitlementResponse - field 'liveDataSpecification' is not present");
    try {
      _liveDataSpecification = deserializer.fieldValueToObject (com.opengamma.livedata.LiveDataSpecification.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EntitlementResponse - field 'liveDataSpecification' is not LiveDataSpecification message", e);
    }
    fudgeField = fudgeMsg.getByName (IS_ENTITLED_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EntitlementResponse - field 'isEntitled' is not present");
    try {
      _isEntitled = fudgeMsg.getFieldValue (Boolean.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EntitlementResponse - field 'isEntitled' is not boolean", e);
    }
    fudgeField = fudgeMsg.getByName (MSG_KEY);
    if (fudgeField != null)  {
      try {
        setMsg ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a EntitlementResponse - field 'msg' is not string", e);
      }
    }
  }
  public EntitlementResponse (com.opengamma.livedata.LiveDataSpecification liveDataSpecification, boolean isEntitled, String msg) {
    if (liveDataSpecification == null) throw new NullPointerException ("'liveDataSpecification' cannot be null");
    else {
      _liveDataSpecification = liveDataSpecification;
    }
    _isEntitled = isEntitled;
    _msg = msg;
  }
  protected EntitlementResponse (final EntitlementResponse source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._liveDataSpecification == null) _liveDataSpecification = null;
    else {
      _liveDataSpecification = source._liveDataSpecification;
    }
    _isEntitled = source._isEntitled;
    _msg = source._msg;
  }
  public EntitlementResponse clone () {
    return new EntitlementResponse (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    if (_liveDataSpecification != null)  {
      serializer.addToMessageWithClassHeaders (msg, LIVE_DATA_SPECIFICATION_KEY, null, _liveDataSpecification, com.opengamma.livedata.LiveDataSpecification.class);
    }
    msg.add (IS_ENTITLED_KEY, null, _isEntitled);
    if (_msg != null)  {
      msg.add (MSG_KEY, null, _msg);
    }
  }
  public static EntitlementResponse fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.livedata.msg.EntitlementResponse".equals (className)) break;
      try {
        return (com.opengamma.livedata.msg.EntitlementResponse)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new EntitlementResponse (deserializer, fudgeMsg);
  }
  public com.opengamma.livedata.LiveDataSpecification getLiveDataSpecification () {
    return _liveDataSpecification;
  }
  public void setLiveDataSpecification (com.opengamma.livedata.LiveDataSpecification liveDataSpecification) {
    if (liveDataSpecification == null) throw new NullPointerException ("'liveDataSpecification' cannot be null");
    else {
      _liveDataSpecification = liveDataSpecification;
    }
  }
  public boolean getIsEntitled () {
    return _isEntitled;
  }
  public void setIsEntitled (boolean isEntitled) {
    _isEntitled = isEntitled;
  }
  public String getMsg () {
    return _msg;
  }
  public void setMsg (String msg) {
    _msg = msg;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
