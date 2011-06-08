// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.view.cache.msg;
public class PutRequest extends com.opengamma.engine.view.cache.msg.CacheMessage implements java.io.Serializable {
  public CacheMessage accept (CacheMessageVisitor visitor) { return visitor.visitPutRequest (this); }
  private static final long serialVersionUID = 5559368179933814711l;
  private com.opengamma.id.UniqueIdentifier _viewProcessId;
  public static final String VIEW_PROCESS_ID_KEY = "viewProcessId";
  private String _calculationConfigurationName;
  public static final String CALCULATION_CONFIGURATION_NAME_KEY = "calculationConfigurationName";
  private long _snapshotTimestamp;
  public static final String SNAPSHOT_TIMESTAMP_KEY = "snapshotTimestamp";
  private java.util.List<Long> _identifier;
  public static final String IDENTIFIER_KEY = "identifier";
  private java.util.List<org.fudgemsg.FudgeMsg> _data;
  public static final String DATA_KEY = "data";
  public PutRequest (com.opengamma.id.UniqueIdentifier viewProcessId, String calculationConfigurationName, long snapshotTimestamp, java.util.Collection<? extends Long> identifier, java.util.Collection<? extends org.fudgemsg.FudgeMsg> data) {
    if (viewProcessId == null) throw new NullPointerException ("'viewProcessId' cannot be null");
    else {
      _viewProcessId = viewProcessId;
    }
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
    if (data == null) throw new NullPointerException ("'data' cannot be null");
    else {
      final java.util.List<org.fudgemsg.FudgeMsg> fudge0 = new java.util.ArrayList<org.fudgemsg.FudgeMsg> (data);
      if (data.size () == 0) throw new IllegalArgumentException ("'data' cannot be an empty list");
      for (java.util.ListIterator<org.fudgemsg.FudgeMsg> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        org.fudgemsg.FudgeMsg fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'data' cannot be null");
      }
      _data = fudge0;
    }
  }
  protected PutRequest (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeContext, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeField = fudgeMsg.getByName (VIEW_PROCESS_ID_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a PutRequest - field 'viewProcessId' is not present");
    try {
      _viewProcessId = com.opengamma.id.UniqueIdentifier.fromFudgeMsg (fudgeContext, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a PutRequest - field 'viewProcessId' is not UniqueIdentifier message", e);
    }
    fudgeField = fudgeMsg.getByName (CALCULATION_CONFIGURATION_NAME_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a PutRequest - field 'calculationConfigurationName' is not present");
    try {
      _calculationConfigurationName = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a PutRequest - field 'calculationConfigurationName' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (SNAPSHOT_TIMESTAMP_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a PutRequest - field 'snapshotTimestamp' is not present");
    try {
      _snapshotTimestamp = fudgeMsg.getFieldValue (Long.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a PutRequest - field 'snapshotTimestamp' is not long", e);
    }
    fudgeFields = fudgeMsg.getAllByName (IDENTIFIER_KEY);
    if (fudgeFields.size () == 0) throw new IllegalArgumentException ("Fudge message is not a PutRequest - field 'identifier' is not present");
    _identifier = new java.util.ArrayList<Long> (fudgeFields.size ());
    for (org.fudgemsg.FudgeField fudge1 : fudgeFields) {
      try {
        _identifier.add (fudgeMsg.getFieldValue (Long.class, fudge1));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a PutRequest - field 'identifier' is not long", e);
      }
    }
    fudgeFields = fudgeMsg.getAllByName (DATA_KEY);
    if (fudgeFields.size () == 0) throw new IllegalArgumentException ("Fudge message is not a PutRequest - field 'data' is not present");
    _data = new java.util.ArrayList<org.fudgemsg.FudgeMsg> (fudgeFields.size ());
    for (org.fudgemsg.FudgeField fudge2 : fudgeFields) {
      try {
        final org.fudgemsg.FudgeMsg fudge3;
        fudge3 = fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudge2);
        _data.add (fudge3);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a PutRequest - field 'data' is not anonymous/unknown message", e);
      }
    }
  }
  public PutRequest (Long correlationId, com.opengamma.id.UniqueIdentifier viewProcessId, String calculationConfigurationName, long snapshotTimestamp, java.util.Collection<? extends Long> identifier, java.util.Collection<? extends org.fudgemsg.FudgeMsg> data) {
    super (correlationId);
    if (viewProcessId == null) throw new NullPointerException ("'viewProcessId' cannot be null");
    else {
      _viewProcessId = viewProcessId;
    }
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
    if (data == null) throw new NullPointerException ("'data' cannot be null");
    else {
      final java.util.List<org.fudgemsg.FudgeMsg> fudge0 = new java.util.ArrayList<org.fudgemsg.FudgeMsg> (data);
      if (data.size () == 0) throw new IllegalArgumentException ("'data' cannot be an empty list");
      for (java.util.ListIterator<org.fudgemsg.FudgeMsg> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        org.fudgemsg.FudgeMsg fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'data' cannot be null");
      }
      _data = fudge0;
    }
  }
  protected PutRequest (final PutRequest source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._viewProcessId == null) _viewProcessId = null;
    else {
      _viewProcessId = source._viewProcessId;
    }
    _calculationConfigurationName = source._calculationConfigurationName;
    _snapshotTimestamp = source._snapshotTimestamp;
    if (source._identifier == null) _identifier = null;
    else {
      _identifier = new java.util.ArrayList<Long> (source._identifier);
    }
    if (source._data == null) _data = null;
    else {
      _data = new java.util.ArrayList<org.fudgemsg.FudgeMsg> (source._data);
    }
  }
  public PutRequest clone () {
    return new PutRequest (this);
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
    if (_calculationConfigurationName != null)  {
      msg.add (CALCULATION_CONFIGURATION_NAME_KEY, null, _calculationConfigurationName);
    }
    msg.add (SNAPSHOT_TIMESTAMP_KEY, null, _snapshotTimestamp);
    if (_identifier != null)  {
      for (Long fudge1 : _identifier) {
        msg.add (IDENTIFIER_KEY, null, fudge1);
      }
    }
    if (_data != null)  {
      for (org.fudgemsg.FudgeMsg fudge1 : _data) {
        msg.add (DATA_KEY, null, (fudge1 instanceof org.fudgemsg.MutableFudgeMsg) ? fudgeContext.newMessage (fudge1) : fudge1);
      }
    }
  }
  public static PutRequest fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.cache.msg.PutRequest".equals (className)) break;
      try {
        return (com.opengamma.engine.view.cache.msg.PutRequest)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeMsg.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new PutRequest (fudgeContext, fudgeMsg);
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
  public java.util.List<org.fudgemsg.FudgeMsg> getData () {
    return java.util.Collections.unmodifiableList (_data);
  }
  public void setData (org.fudgemsg.FudgeMsg data) {
    if (data == null) throw new NullPointerException ("'data' cannot be null");
    else {
      _data = new java.util.ArrayList<org.fudgemsg.FudgeMsg> (1);
      addData (data);
    }
  }
  public void setData (java.util.Collection<? extends org.fudgemsg.FudgeMsg> data) {
    if (data == null) throw new NullPointerException ("'data' cannot be null");
    else {
      final java.util.List<org.fudgemsg.FudgeMsg> fudge0 = new java.util.ArrayList<org.fudgemsg.FudgeMsg> (data);
      if (data.size () == 0) throw new IllegalArgumentException ("'data' cannot be an empty list");
      for (java.util.ListIterator<org.fudgemsg.FudgeMsg> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        org.fudgemsg.FudgeMsg fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'data' cannot be null");
      }
      _data = fudge0;
    }
  }
  public void addData (org.fudgemsg.FudgeMsg data) {
    if (data == null) throw new NullPointerException ("'data' cannot be null");
    if (_data == null) _data = new java.util.ArrayList<org.fudgemsg.FudgeMsg> ();
    _data.add (data);
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
