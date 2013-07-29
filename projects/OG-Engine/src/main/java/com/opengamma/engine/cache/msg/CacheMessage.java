// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.engine.cache.msg;
public class CacheMessage implements java.io.Serializable {
  public CacheMessage accept (CacheMessageVisitor visitor) { throw new UnsupportedOperationException (); }
  private static final long serialVersionUID = -23712120896l;
  private Long _correlationId;
  public static final String CORRELATION_ID_KEY = "correlationId";
  public CacheMessage () {
  }
  protected CacheMessage (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (CORRELATION_ID_KEY);
    if (fudgeField != null)  {
      try {
        setCorrelationId (fudgeMsg.getFieldValue (Long.class, fudgeField));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a CacheMessage - field 'correlationId' is not long", e);
      }
    }
  }
  public CacheMessage (Long correlationId) {
    _correlationId = correlationId;
  }
  protected CacheMessage (final CacheMessage source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _correlationId = source._correlationId;
  }
  public CacheMessage clone () {
    return new CacheMessage (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    if (_correlationId != null)  {
      msg.add (CORRELATION_ID_KEY, null, _correlationId);
    }
  }
  public static CacheMessage fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.cache.msg.CacheMessage".equals (className)) break;
      try {
        return (com.opengamma.engine.cache.msg.CacheMessage)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new CacheMessage (deserializer, fudgeMsg);
  }
  public Long getCorrelationId () {
    return _correlationId;
  }
  public void setCorrelationId (Long correlationId) {
    _correlationId = correlationId;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
