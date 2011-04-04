// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
// Created from com/opengamma/engine/view/cache/msg/DeleteRequest.proto:11(10)
package com.opengamma.engine.view.cache.msg;
public class DeleteRequest extends com.opengamma.engine.view.cache.msg.CacheMessage implements java.io.Serializable {
  public CacheMessage accept (CacheMessageVisitor visitor) { return visitor.visitDeleteRequest (this); }
  private static final long serialVersionUID = -49610298912146002l;
  private com.opengamma.id.UniqueIdentifier _viewProcessId;
  public static final String VIEW_PROCESS_ID_KEY = "viewProcessId";
  private String _calculationConfigurationName;
  public static final String CALCULATION_CONFIGURATION_NAME_KEY = "calculationConfigurationName";
  private long _snapshotTimestamp;
  public static final String SNAPSHOT_TIMESTAMP_KEY = "snapshotTimestamp";
  public DeleteRequest (com.opengamma.id.UniqueIdentifier viewProcessId, String calculationConfigurationName, long snapshotTimestamp) {
    if (viewProcessId == null) throw new NullPointerException ("'viewProcessId' cannot be null");
    else {
      _viewProcessId = viewProcessId;
    }
    if (calculationConfigurationName == null) throw new NullPointerException ("calculationConfigurationName' cannot be null");
    _calculationConfigurationName = calculationConfigurationName;
    _snapshotTimestamp = snapshotTimestamp;
  }
  protected DeleteRequest (final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (VIEW_PROCESS_ID_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a DeleteRequest - field 'viewProcessId' is not present");
    try {
      _viewProcessId = com.opengamma.id.UniqueIdentifier.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a DeleteRequest - field 'viewProcessId' is not UniqueIdentifier message", e);
    }
    fudgeField = fudgeMsg.getByName (CALCULATION_CONFIGURATION_NAME_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a DeleteRequest - field 'calculationConfigurationName' is not present");
    try {
      _calculationConfigurationName = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a DeleteRequest - field 'calculationConfigurationName' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (SNAPSHOT_TIMESTAMP_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a DeleteRequest - field 'snapshotTimestamp' is not present");
    try {
      _snapshotTimestamp = fudgeMsg.getFieldValue (Long.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a DeleteRequest - field 'snapshotTimestamp' is not long", e);
    }
  }
  public DeleteRequest (Long correlationId, com.opengamma.id.UniqueIdentifier viewProcessId, String calculationConfigurationName, long snapshotTimestamp) {
    super (correlationId);
    if (viewProcessId == null) throw new NullPointerException ("'viewProcessId' cannot be null");
    else {
      _viewProcessId = viewProcessId;
    }
    if (calculationConfigurationName == null) throw new NullPointerException ("calculationConfigurationName' cannot be null");
    _calculationConfigurationName = calculationConfigurationName;
    _snapshotTimestamp = snapshotTimestamp;
  }
  protected DeleteRequest (final DeleteRequest source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._viewProcessId == null) _viewProcessId = null;
    else {
      _viewProcessId = source._viewProcessId;
    }
    _calculationConfigurationName = source._calculationConfigurationName;
    _snapshotTimestamp = source._snapshotTimestamp;
  }
  public DeleteRequest clone () {
    return new DeleteRequest (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_viewProcessId != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _viewProcessId.getClass (), com.opengamma.id.UniqueIdentifier.class);
      _viewProcessId.toFudgeMsg (fudgeContext, fudge1);
      msg.add (VIEW_PROCESS_ID_KEY, null, fudge1);
    }
    if (_calculationConfigurationName != null)  {
      msg.add (CALCULATION_CONFIGURATION_NAME_KEY, null, _calculationConfigurationName);
    }
    msg.add (SNAPSHOT_TIMESTAMP_KEY, null, _snapshotTimestamp);
  }
  public static DeleteRequest fromFudgeMsg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.cache.msg.DeleteRequest".equals (className)) break;
      try {
        return (com.opengamma.engine.view.cache.msg.DeleteRequest)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeMsg.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new DeleteRequest (fudgeMsg);
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
  public String getCalculationConfigurationName () {
    return _calculationConfigurationName;
  }
  public void setCalculationConfigurationName (String calculationConfigurationName) {
    if (calculationConfigurationName == null) throw new NullPointerException ("calculationConfigurationName' cannot be null");
    _calculationConfigurationName = calculationConfigurationName;
  }
  public long getSnapshotTimestamp () {
    return _snapshotTimestamp;
  }
  public void setSnapshotTimestamp (long snapshotTimestamp) {
    _snapshotTimestamp = snapshotTimestamp;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
