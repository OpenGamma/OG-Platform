// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.view.cache.msg;
public class ReleaseCacheMessage extends com.opengamma.engine.view.cache.msg.CacheMessage implements java.io.Serializable {
  public CacheMessage accept (CacheMessageVisitor visitor) { return visitor.visitReleaseCacheMessage (this); }
  private static final long serialVersionUID = 36674150374l;
  private com.opengamma.id.UniqueId _viewCycleId;
  public static final String VIEW_CYCLE_ID_KEY = "viewCycleId";
  public ReleaseCacheMessage (com.opengamma.id.UniqueId viewCycleId) {
    if (viewCycleId == null) throw new NullPointerException ("'viewCycleId' cannot be null");
    else {
      _viewCycleId = viewCycleId;
    }
  }
  protected ReleaseCacheMessage (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeContext, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (VIEW_CYCLE_ID_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ReleaseCacheMessage - field 'viewCycleId' is not present");
    try {
      _viewCycleId = com.opengamma.id.UniqueId.fromFudgeMsg (fudgeContext, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ReleaseCacheMessage - field 'viewCycleId' is not UniqueId message", e);
    }
  }
  public ReleaseCacheMessage (Long correlationId, com.opengamma.id.UniqueId viewCycleId) {
    super (correlationId);
    if (viewCycleId == null) throw new NullPointerException ("'viewCycleId' cannot be null");
    else {
      _viewCycleId = viewCycleId;
    }
  }
  protected ReleaseCacheMessage (final ReleaseCacheMessage source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._viewCycleId == null) _viewCycleId = null;
    else {
      _viewCycleId = source._viewCycleId;
    }
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
    if (_viewCycleId != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _viewCycleId.getClass (), com.opengamma.id.UniqueId.class);
      _viewCycleId.toFudgeMsg (fudgeContext, fudge1);
      msg.add (VIEW_CYCLE_ID_KEY, null, fudge1);
    }
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
  public com.opengamma.id.UniqueId getViewCycleId () {
    return _viewCycleId;
  }
  public void setViewCycleId (com.opengamma.id.UniqueId viewCycleId) {
    if (viewCycleId == null) throw new NullPointerException ("'viewCycleId' cannot be null");
    else {
      _viewCycleId = viewCycleId;
    }
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
