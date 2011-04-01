// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.view.cache.msg;
public class DeleteRequest extends com.opengamma.engine.view.cache.msg.CacheMessage implements java.io.Serializable {
  public CacheMessage accept (CacheMessageVisitor visitor) { return visitor.visitDeleteRequest (this); }
  private static final long serialVersionUID = 32499290648188741l;
  private String _viewName;
  public static final String VIEW_NAME_KEY = "viewName";
  private String _calculationConfigurationName;
  public static final String CALCULATION_CONFIGURATION_NAME_KEY = "calculationConfigurationName";
  private long _snapshotTimestamp;
  public static final String SNAPSHOT_TIMESTAMP_KEY = "snapshotTimestamp";
  public DeleteRequest (String viewName, String calculationConfigurationName, long snapshotTimestamp) {
    if (viewName == null) throw new NullPointerException ("viewName' cannot be null");
    _viewName = viewName;
    if (calculationConfigurationName == null) throw new NullPointerException ("calculationConfigurationName' cannot be null");
    _calculationConfigurationName = calculationConfigurationName;
    _snapshotTimestamp = snapshotTimestamp;
  }
  protected DeleteRequest (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (VIEW_NAME_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a DeleteRequest - field 'viewName' is not present");
    try {
      _viewName = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a DeleteRequest - field 'viewName' is not string", e);
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
  public DeleteRequest (Long correlationId, String viewName, String calculationConfigurationName, long snapshotTimestamp) {
    super (correlationId);
    if (viewName == null) throw new NullPointerException ("viewName' cannot be null");
    _viewName = viewName;
    if (calculationConfigurationName == null) throw new NullPointerException ("calculationConfigurationName' cannot be null");
    _calculationConfigurationName = calculationConfigurationName;
    _snapshotTimestamp = snapshotTimestamp;
  }
  protected DeleteRequest (final DeleteRequest source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _viewName = source._viewName;
    _calculationConfigurationName = source._calculationConfigurationName;
    _snapshotTimestamp = source._snapshotTimestamp;
  }
  public DeleteRequest clone () {
    return new DeleteRequest (this);
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_viewName != null)  {
      msg.add (VIEW_NAME_KEY, null, _viewName);
    }
    if (_calculationConfigurationName != null)  {
      msg.add (CALCULATION_CONFIGURATION_NAME_KEY, null, _calculationConfigurationName);
    }
    msg.add (SNAPSHOT_TIMESTAMP_KEY, null, _snapshotTimestamp);
  }
  public static DeleteRequest fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.cache.msg.DeleteRequest".equals (className)) break;
      try {
        return (com.opengamma.engine.view.cache.msg.DeleteRequest)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new DeleteRequest (fudgeMsg);
  }
  public String getViewName () {
    return _viewName;
  }
  public void setViewName (String viewName) {
    if (viewName == null) throw new NullPointerException ("viewName' cannot be null");
    _viewName = viewName;
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
