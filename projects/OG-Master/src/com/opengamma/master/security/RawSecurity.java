// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.master.security;
public class RawSecurity extends com.opengamma.master.security.ManageableSecurity implements java.io.Serializable {
  private static final long serialVersionUID = 29168862329l;
  private byte[] _rawData;
  public static final String RAW_DATA_KEY = "rawData";
  public RawSecurity (String securityType, byte[] rawData) {
    super (securityType);
    if (rawData == null) throw new NullPointerException ("'rawData' cannot be null");
    else {
      _rawData = java.util.Arrays.copyOf (rawData, rawData.length);
    }
  }
  protected RawSecurity (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (RAW_DATA_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a RawSecurity - field 'rawData' is not present");
    try {
      _rawData = fudgeMsg.getFieldValue (byte[].class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a RawSecurity - field 'rawData' is not byte[]", e);
    }
  }
  public RawSecurity (com.opengamma.id.UniqueId uniqueId, String name, String securityType, com.opengamma.id.ExternalIdBundle identifiers, byte[] rawData) {
    super (uniqueId, name, securityType, identifiers);
    if (rawData == null) throw new NullPointerException ("'rawData' cannot be null");
    else {
      _rawData = java.util.Arrays.copyOf (rawData, rawData.length);
    }
  }
  protected RawSecurity (final RawSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._rawData == null) _rawData = null;
    else {
      _rawData = java.util.Arrays.copyOf (source._rawData, source._rawData.length);
    }
  }
  public RawSecurity clone () {
    return new RawSecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_rawData != null)  {
      msg.add (RAW_DATA_KEY, null, _rawData);
    }
  }
  public static RawSecurity fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.master.security.RawSecurity".equals (className)) break;
      try {
        return (com.opengamma.master.security.RawSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new RawSecurity (deserializer, fudgeMsg);
  }
  public byte[] getRawData () {
    return _rawData;
  }
  public void setRawData (byte[] rawData) {
    if (rawData == null) throw new NullPointerException ("'rawData' cannot be null");
    else {
      _rawData = java.util.Arrays.copyOf (rawData, rawData.length);
    }
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof RawSecurity)) return false;
    RawSecurity msg = (RawSecurity)o;
    if (!java.util.Arrays.equals (_rawData, msg._rawData)) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_rawData != null)hc += java.util.Arrays.hashCode (_rawData);
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
