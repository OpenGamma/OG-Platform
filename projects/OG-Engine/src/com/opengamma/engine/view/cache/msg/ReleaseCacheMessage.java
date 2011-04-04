// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.view.cache.msg;
public class ReleaseCacheMessage extends com.opengamma.engine.view.cache.msg.CacheMessage implements java.io.Serializable {
  public CacheMessage accept (CacheMessageVisitor visitor) { return visitor.visitReleaseCacheMessage (this); }
  private static final long serialVersionUID = 33883987139272l;
  private String _viewName;
  public static final String VIEW_NAME_KEY = "viewName";
  private long _timestamp;
  public static final String TIMESTAMP_KEY = "timestamp";
  public ReleaseCacheMessage (String viewName, long timestamp) {
    if (viewName == null) throw new NullPointerException ("viewName' cannot be null");
    _viewName = viewName;
    _timestamp = timestamp;
  }
  protected ReleaseCacheMessage (final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (VIEW_NAME_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ReleaseCacheMessage - field 'viewName' is not present");
    try {
      _viewName = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ReleaseCacheMessage - field 'viewName' is not string", e);
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
  public ReleaseCacheMessage (Long correlationId, String viewName, long timestamp) {
    super (correlationId);
    if (viewName == null) throw new NullPointerException ("viewName' cannot be null");
    _viewName = viewName;
    _timestamp = timestamp;
  }
  protected ReleaseCacheMessage (final ReleaseCacheMessage source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _viewName = source._viewName;
    _timestamp = source._timestamp;
  }
  public ReleaseCacheMessage clone () {
    return new ReleaseCacheMessage (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_viewName != null)  {
      msg.add (VIEW_NAME_KEY, null, _viewName);
    }
    msg.add (TIMESTAMP_KEY, null, _timestamp);
  }
  public static ReleaseCacheMessage fromFudgeMsg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.cache.msg.ReleaseCacheMessage".equals (className)) break;
      try {
        return (com.opengamma.engine.view.cache.msg.ReleaseCacheMessage)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeMsg.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new ReleaseCacheMessage (fudgeMsg);
  }
  public String getViewName () {
    return _viewName;
  }
  public void setViewName (String viewName) {
    if (viewName == null) throw new NullPointerException ("viewName' cannot be null");
    _viewName = viewName;
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
