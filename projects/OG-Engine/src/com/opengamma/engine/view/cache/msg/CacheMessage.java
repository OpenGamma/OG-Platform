// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.view.cache.msg;
public class CacheMessage implements java.io.Serializable {
  public CacheMessage accept (CacheMessageVisitor visitor) { throw new UnsupportedOperationException (); }
  private static final long serialVersionUID = -23712120896l;
  private Long _correlationId;
  public static final String CORRELATION_ID_KEY = "correlationId";
  public CacheMessage () {
  }
  protected CacheMessage (final org.fudgemsg.FudgeMsg fudgeMsg) {
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
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    if (_correlationId != null)  {
      msg.add (CORRELATION_ID_KEY, null, _correlationId);
    }
  }
  public static CacheMessage fromFudgeMsg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.cache.msg.CacheMessage".equals (className)) break;
      try {
        return (com.opengamma.engine.view.cache.msg.CacheMessage)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeMsg.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new CacheMessage (fudgeMsg);
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
///CLOVER:ON
// CSON: Generated File
