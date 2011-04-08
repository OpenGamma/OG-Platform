// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
// Created from com/opengamma/engine/view/cache/msg/GetRequest.proto:9(10)
package com.opengamma.engine.view.cache.msg;
public class GetRequest extends com.opengamma.engine.view.cache.msg.CacheMessage implements java.io.Serializable {
  public CacheMessage accept (CacheMessageVisitor visitor) { return visitor.visitGetRequest (this); }
  private static final long serialVersionUID = -5661669885779352616l;
  private String _viewName;
  public static final String VIEW_NAME_KEY = "viewName";
  private String _calculationConfigurationName;
  public static final String CALCULATION_CONFIGURATION_NAME_KEY = "calculationConfigurationName";
  private long _snapshotTimestamp;
  public static final String SNAPSHOT_TIMESTAMP_KEY = "snapshotTimestamp";
  private java.util.List<Long> _identifier;
  public static final String IDENTIFIER_KEY = "identifier";
  public GetRequest (String viewName, String calculationConfigurationName, long snapshotTimestamp, java.util.Collection<? extends Long> identifier) {
    if (viewName == null) throw new NullPointerException ("viewName' cannot be null");
    _viewName = viewName;
    if (calculationConfigurationName == null) throw new NullPointerException ("calculationConfigurationName' cannot be null");
    _calculationConfigurationName = calculationConfigurationName;
    _snapshotTimestamp = snapshotTimestamp;
    if (identifier == null) throw new NullPointerException ("'identifier' cannot be null");
    else {
      final java.util.List<Long> fudge0 = new java.util.ArrayList<Long> (identifier);
      if (identifier.size () == 0) throw new IllegalArgumentException ("'identifier' cannot be an empty list");
      for (java.util.ListIterator<Long> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        Long fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'identifier' cannot be null");
      }
      _identifier = fudge0;
    }
  }
  protected GetRequest (final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeField = fudgeMsg.getByName (VIEW_NAME_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a GetRequest - field 'viewName' is not present");
    try {
      _viewName = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a GetRequest - field 'viewName' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (CALCULATION_CONFIGURATION_NAME_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a GetRequest - field 'calculationConfigurationName' is not present");
    try {
      _calculationConfigurationName = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a GetRequest - field 'calculationConfigurationName' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (SNAPSHOT_TIMESTAMP_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a GetRequest - field 'snapshotTimestamp' is not present");
    try {
      _snapshotTimestamp = fudgeMsg.getFieldValue (Long.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a GetRequest - field 'snapshotTimestamp' is not long", e);
    }
    fudgeFields = fudgeMsg.getAllByName (IDENTIFIER_KEY);
    if (fudgeFields.size () == 0) throw new IllegalArgumentException ("Fudge message is not a GetRequest - field 'identifier' is not present");
    _identifier = new java.util.ArrayList<Long> (fudgeFields.size ());
    for (org.fudgemsg.FudgeField fudge1 : fudgeFields) {
      try {
        _identifier.add (fudgeMsg.getFieldValue (Long.class, fudge1));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a GetRequest - field 'identifier' is not long", e);
      }
    }
  }
  public GetRequest (Long correlationId, String viewName, String calculationConfigurationName, long snapshotTimestamp, java.util.Collection<? extends Long> identifier) {
    super (correlationId);
    if (viewName == null) throw new NullPointerException ("viewName' cannot be null");
    _viewName = viewName;
    if (calculationConfigurationName == null) throw new NullPointerException ("calculationConfigurationName' cannot be null");
    _calculationConfigurationName = calculationConfigurationName;
    _snapshotTimestamp = snapshotTimestamp;
    if (identifier == null) throw new NullPointerException ("'identifier' cannot be null");
    else {
      final java.util.List<Long> fudge0 = new java.util.ArrayList<Long> (identifier);
      if (identifier.size () == 0) throw new IllegalArgumentException ("'identifier' cannot be an empty list");
      for (java.util.ListIterator<Long> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        Long fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'identifier' cannot be null");
      }
      _identifier = fudge0;
    }
  }
  protected GetRequest (final GetRequest source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _viewName = source._viewName;
    _calculationConfigurationName = source._calculationConfigurationName;
    _snapshotTimestamp = source._snapshotTimestamp;
    if (source._identifier == null) _identifier = null;
    else {
      _identifier = new java.util.ArrayList<Long> (source._identifier);
    }
  }
  public GetRequest clone () {
    return new GetRequest (this);
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
    if (_calculationConfigurationName != null)  {
      msg.add (CALCULATION_CONFIGURATION_NAME_KEY, null, _calculationConfigurationName);
    }
    msg.add (SNAPSHOT_TIMESTAMP_KEY, null, _snapshotTimestamp);
    if (_identifier != null)  {
      for (Long fudge1 : _identifier) {
        msg.add (IDENTIFIER_KEY, null, fudge1);
      }
    }
  }
  public static GetRequest fromFudgeMsg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.cache.msg.GetRequest".equals (className)) break;
      try {
        return (com.opengamma.engine.view.cache.msg.GetRequest)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeMsg.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new GetRequest (fudgeMsg);
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
  public java.util.List<Long> getIdentifier () {
    return java.util.Collections.unmodifiableList (_identifier);
  }
  public void setIdentifier (Long identifier) {
    if (identifier == null) throw new NullPointerException ("'identifier' cannot be null");
    else {
      _identifier = new java.util.ArrayList<Long> (1);
      addIdentifier (identifier);
    }
  }
  public void setIdentifier (java.util.Collection<? extends Long> identifier) {
    if (identifier == null) throw new NullPointerException ("'identifier' cannot be null");
    else {
      final java.util.List<Long> fudge0 = new java.util.ArrayList<Long> (identifier);
      if (identifier.size () == 0) throw new IllegalArgumentException ("'identifier' cannot be an empty list");
      for (java.util.ListIterator<Long> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        Long fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'identifier' cannot be null");
      }
      _identifier = fudge0;
    }
  }
  public void addIdentifier (Long identifier) {
    if (identifier == null) throw new NullPointerException ("'identifier' cannot be null");
    if (_identifier == null) _identifier = new java.util.ArrayList<Long> ();
    _identifier.add (identifier);
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
