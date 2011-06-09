// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.view.cache.msg;
public class ReleaseCacheMessage extends com.opengamma.engine.view.cache.msg.CacheMessage implements java.io.Serializable {
  public CacheMessage accept (CacheMessageVisitor visitor) { return visitor.visitReleaseCacheMessage (this); }
  private static final long serialVersionUID = -51557833422991l;
  private com.opengamma.id.UniqueIdentifier _viewProcessId;
  public static final String VIEW_PROCESS_ID_KEY = "viewProcessId";
  private long _timestamp;
  public static final String TIMESTAMP_KEY = "timestamp";
  public ReleaseCacheMessage (com.opengamma.id.UniqueIdentifier viewProcessId, long timestamp) {
    if (viewProcessId == null) throw new NullPointerException ("'viewProcessId' cannot be null");
    else {
      _viewProcessId = viewProcessId;
    }
    _timestamp = timestamp;
  }
  protected ReleaseCacheMessage (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeContext, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (VIEW_PROCESS_ID_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ReleaseCacheMessage - field 'viewProcessId' is not present");
    try {
      _viewProcessId = com.opengamma.id.UniqueIdentifier.fromFudgeMsg (fudgeContext, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ReleaseCacheMessage - field 'viewProcessId' is not UniqueIdentifier message", e);
    }
    fudgeField = fudgeMsg.getByName (TIMESTAMP_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ReleaseCacheMessage - field 'timestamp' is not present");
    try {
      _timestamp = fudgeMsg.getFieldValue (Long.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ReleaseCacheMessage - field 'timestamp' is not long", e);
    }
  }
  public ReleaseCacheMessage (Long correlationId, com.opengamma.id.UniqueIdentifier viewProcessId, long timestamp) {
    super (correlationId);
    if (viewProcessId == null) throw new NullPointerException ("'viewProcessId' cannot be null");
    else {
      _viewProcessId = viewProcessId;
    }
    _timestamp = timestamp;
  }
  protected ReleaseCacheMessage (final ReleaseCacheMessage source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._viewProcessId == null) _viewProcessId = null;
    else {
      _viewProcessId = source._viewProcessId;
    }
    _timestamp = source._timestamp;
  }
  public ReleaseCacheMessage clone () {
    return new ReleaseCacheMessage (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_viewProcessId != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _viewProcessId.getClass (), com.opengamma.id.UniqueIdentifier.class);
      _viewProcessId.toFudgeMsg (fudgeContext, fudge1);
      msg.add (VIEW_PROCESS_ID_KEY, null, fudge1);
    }
    msg.add (TIMESTAMP_KEY, null, _timestamp);
  }
  public static ReleaseCacheMessage fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.cache.msg.ReleaseCacheMessage".equals (className)) break;
      try {
        return (com.opengamma.engine.view.cache.msg.ReleaseCacheMessage)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeMsg.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new ReleaseCacheMessage (fudgeContext, fudgeMsg);
  }
  public com.opengamma.id.UniqueIdentifier getViewProcessId () {
    return _viewProcessId;
  }
  public void setViewProcessId (com.opengamma.id.UniqueIdentifier viewProcessId) {
    if (viewProcessId == null) throw new NullPointerException ("'viewProcessId' cannot be null");
    else {
      _viewProcessId = viewProcessId;
    }
  }
  public long getTimestamp () {
    return _timestamp;
  }
  public void setTimestamp (long timestamp) {
    _timestamp = timestamp;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
