// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.view.cache.msg;
public class PutRequest extends com.opengamma.engine.view.cache.msg.CacheMessage implements java.io.Serializable {
  public CacheMessage accept (CacheMessageVisitor visitor) { return visitor.visitPutRequest (this); }
  private static final long serialVersionUID = 924741509978470460l;
  private String _viewName;
  public static final String VIEW_NAME_KEY = "viewName";
  private String _calculationConfigurationName;
  public static final String CALCULATION_CONFIGURATION_NAME_KEY = "calculationConfigurationName";
  private long _snapshotTimestamp;
  public static final String SNAPSHOT_TIMESTAMP_KEY = "snapshotTimestamp";
  private java.util.List<Long> _identifier;
  public static final String IDENTIFIER_KEY = "identifier";
  private java.util.List<byte[]> _data;
  public static final String DATA_KEY = "data";
  public PutRequest (String viewName, String calculationConfigurationName, long snapshotTimestamp, java.util.Collection<? extends Long> identifier, java.util.Collection<? extends byte[]> data) {
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
    if (data == null) throw new NullPointerException ("'data' cannot be null");
    else {
      final java.util.List<byte[]> fudge0 = new java.util.ArrayList<byte[]> (data);
      if (data.size () == 0) throw new IllegalArgumentException ("'data' cannot be an empty list");
      for (java.util.ListIterator<byte[]> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        byte[] fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'data' cannot be null");
        fudge1.set (java.util.Arrays.copyOf (fudge2, fudge2.length));
      }
      _data = fudge0;
    }
  }
  protected PutRequest (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeField = fudgeMsg.getByName (VIEW_NAME_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a PutRequest - field 'viewName' is not present");
    try {
      _viewName = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a PutRequest - field 'viewName' is not string", e);
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
    _data = new java.util.ArrayList<byte[]> (fudgeFields.size ());
    for (org.fudgemsg.FudgeField fudge2 : fudgeFields) {
      try {
        _data.add (fudgeMsg.getFieldValue (byte[].class, fudge2));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a PutRequest - field 'data' is not byte[]", e);
      }
    }
  }
  public PutRequest (Long correlationId, String viewName, String calculationConfigurationName, long snapshotTimestamp, java.util.Collection<? extends Long> identifier, java.util.Collection<? extends byte[]> data) {
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
    if (data == null) throw new NullPointerException ("'data' cannot be null");
    else {
      final java.util.List<byte[]> fudge0 = new java.util.ArrayList<byte[]> (data);
      if (data.size () == 0) throw new IllegalArgumentException ("'data' cannot be an empty list");
      for (java.util.ListIterator<byte[]> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        byte[] fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'data' cannot be null");
        fudge1.set (java.util.Arrays.copyOf (fudge2, fudge2.length));
      }
      _data = fudge0;
    }
  }
  protected PutRequest (final PutRequest source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _viewName = source._viewName;
    _calculationConfigurationName = source._calculationConfigurationName;
    _snapshotTimestamp = source._snapshotTimestamp;
    if (source._identifier == null) _identifier = null;
    else {
      _identifier = new java.util.ArrayList<Long> (source._identifier);
    }
    if (source._data == null) _data = null;
    else {
      final java.util.List<byte[]> fudge0 = new java.util.ArrayList<byte[]> (source._data);
      for (java.util.ListIterator<byte[]> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        byte[] fudge2 = fudge1.next ();
        fudge1.set (java.util.Arrays.copyOf (fudge2, fudge2.length));
      }
      _data = fudge0;
    }
  }
  public PutRequest clone () {
    return new PutRequest (this);
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
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
    if (_data != null)  {
      for (byte[] fudge1 : _data) {
        msg.add (DATA_KEY, null, fudge1);
      }
    }
  }
  public static PutRequest fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.cache.msg.PutRequest".equals (className)) break;
      try {
        return (com.opengamma.engine.view.cache.msg.PutRequest)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new PutRequest (fudgeMsg);
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
  public java.util.List<byte[]> getData () {
    return java.util.Collections.unmodifiableList (_data);
  }
  public void setData (byte[] data) {
    if (data == null) throw new NullPointerException ("'data' cannot be null");
    else {
      _data = new java.util.ArrayList<byte[]> (1);
      addData (data);
    }
  }
  public void setData (java.util.Collection<? extends byte[]> data) {
    if (data == null) throw new NullPointerException ("'data' cannot be null");
    else {
      final java.util.List<byte[]> fudge0 = new java.util.ArrayList<byte[]> (data);
      if (data.size () == 0) throw new IllegalArgumentException ("'data' cannot be an empty list");
      for (java.util.ListIterator<byte[]> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        byte[] fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'data' cannot be null");
        fudge1.set (java.util.Arrays.copyOf (fudge2, fudge2.length));
      }
      _data = fudge0;
    }
  }
  public void addData (byte[] data) {
    if (data == null) throw new NullPointerException ("'data' cannot be null");
    if (_data == null) _data = new java.util.ArrayList<byte[]> ();
    _data.add (java.util.Arrays.copyOf (data, data.length));
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
