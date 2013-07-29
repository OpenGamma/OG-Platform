// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.bbg.replay;
public class BloombergTick implements java.io.Serializable {
  private static final long serialVersionUID = 5758590467687970460l;
  private long _receivedTS;
  public static final String RECEIVED_TS_KEY = "receivedTS";
  private String _buid;
  public static final String BUID_KEY = "buid";
  private String _security;
  public static final String SECURITY_KEY = "security";
  private org.fudgemsg.FudgeMsg _fields;
  public static final String FIELDS_KEY = "fields";
  public BloombergTick (long receivedTS, String buid, String security, org.fudgemsg.FudgeMsg fields) {
    _receivedTS = receivedTS;
    if (buid == null) throw new NullPointerException ("buid' cannot be null");
    _buid = buid;
    if (security == null) throw new NullPointerException ("security' cannot be null");
    _security = security;
    if (fields == null) throw new NullPointerException ("fields' cannot be null");
    _fields = fields;
  }
  protected BloombergTick (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (RECEIVED_TS_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BloombergTick - field 'receivedTS' is not present");
    try {
      _receivedTS = fudgeMsg.getFieldValue (Long.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BloombergTick - field 'receivedTS' is not long", e);
    }
    fudgeField = fudgeMsg.getByName (BUID_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BloombergTick - field 'buid' is not present");
    try {
      _buid = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BloombergTick - field 'buid' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (SECURITY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BloombergTick - field 'security' is not present");
    try {
      _security = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BloombergTick - field 'security' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (FIELDS_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a BloombergTick - field 'fields' is not present");
    try {
      _fields = fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a BloombergTick - field 'fields' is not anonymous/unknown message", e);
    }
  }
  protected BloombergTick (final BloombergTick source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _receivedTS = source._receivedTS;
    _buid = source._buid;
    _security = source._security;
    _fields = source._fields;
  }
  public BloombergTick clone () {
    return new BloombergTick (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    msg.add (RECEIVED_TS_KEY, null, _receivedTS);
    if (_buid != null)  {
      msg.add (BUID_KEY, null, _buid);
    }
    if (_security != null)  {
      msg.add (SECURITY_KEY, null, _security);
    }
    if (_fields != null)  {
      msg.add (FIELDS_KEY, null, (_fields instanceof org.fudgemsg.MutableFudgeMsg) ? serializer.newMessage (_fields) : _fields);
    }
  }
  public static BloombergTick fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.bbg.replay.BloombergTick".equals (className)) break;
      try {
        return (com.opengamma.bbg.replay.BloombergTick)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new BloombergTick (deserializer, fudgeMsg);
  }
  public long getReceivedTS () {
    return _receivedTS;
  }
  public void setReceivedTS (long receivedTS) {
    _receivedTS = receivedTS;
  }
  public String getBuid () {
    return _buid;
  }
  public void setBuid (String buid) {
    if (buid == null) throw new NullPointerException ("buid' cannot be null");
    _buid = buid;
  }
  public String getSecurity () {
    return _security;
  }
  public void setSecurity (String security) {
    if (security == null) throw new NullPointerException ("security' cannot be null");
    _security = security;
  }
  public org.fudgemsg.FudgeMsg getFields () {
    return _fields;
  }
  public void setFields (org.fudgemsg.FudgeMsg fields) {
    if (fields == null) throw new NullPointerException ("fields' cannot be null");
    _fields = fields;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
