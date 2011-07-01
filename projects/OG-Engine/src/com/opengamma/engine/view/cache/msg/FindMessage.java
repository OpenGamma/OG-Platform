// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.view.cache.msg;
public class FindMessage extends com.opengamma.engine.view.cache.msg.CacheMessage implements java.io.Serializable {
  public CacheMessage accept (CacheMessageVisitor visitor) { return visitor.visitFindMessage (this); }
  private static final long serialVersionUID = 33855499995732320l;
  private com.opengamma.id.UniqueIdentifier _viewCycleId;
  public static final String VIEW_CYCLE_ID_KEY = "viewCycleId";
  private String _calculationConfigurationName;
  public static final String CALCULATION_CONFIGURATION_NAME_KEY = "calculationConfigurationName";
  private java.util.List<Long> _identifier;
  public static final String IDENTIFIER_KEY = "identifier";
  public FindMessage (com.opengamma.id.UniqueIdentifier viewCycleId, String calculationConfigurationName, java.util.Collection<? extends Long> identifier) {
    if (viewCycleId == null) throw new NullPointerException ("'viewCycleId' cannot be null");
    else {
      _viewCycleId = viewCycleId;
    }
    if (calculationConfigurationName == null) throw new NullPointerException ("calculationConfigurationName' cannot be null");
    _calculationConfigurationName = calculationConfigurationName;
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
  protected FindMessage (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeContext, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeField = fudgeMsg.getByName (VIEW_CYCLE_ID_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FindMessage - field 'viewCycleId' is not present");
    try {
      _viewCycleId = com.opengamma.id.UniqueIdentifier.fromFudgeMsg (fudgeContext, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FindMessage - field 'viewCycleId' is not UniqueIdentifier message", e);
    }
    fudgeField = fudgeMsg.getByName (CALCULATION_CONFIGURATION_NAME_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FindMessage - field 'calculationConfigurationName' is not present");
    try {
      _calculationConfigurationName = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FindMessage - field 'calculationConfigurationName' is not string", e);
    }
    fudgeFields = fudgeMsg.getAllByName (IDENTIFIER_KEY);
    if (fudgeFields.size () == 0) throw new IllegalArgumentException ("Fudge message is not a FindMessage - field 'identifier' is not present");
    _identifier = new java.util.ArrayList<Long> (fudgeFields.size ());
    for (org.fudgemsg.FudgeField fudge1 : fudgeFields) {
      try {
        _identifier.add (fudgeMsg.getFieldValue (Long.class, fudge1));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a FindMessage - field 'identifier' is not long", e);
      }
    }
  }
  public FindMessage (Long correlationId, com.opengamma.id.UniqueIdentifier viewCycleId, String calculationConfigurationName, java.util.Collection<? extends Long> identifier) {
    super (correlationId);
    if (viewCycleId == null) throw new NullPointerException ("'viewCycleId' cannot be null");
    else {
      _viewCycleId = viewCycleId;
    }
    if (calculationConfigurationName == null) throw new NullPointerException ("calculationConfigurationName' cannot be null");
    _calculationConfigurationName = calculationConfigurationName;
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
  protected FindMessage (final FindMessage source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._viewCycleId == null) _viewCycleId = null;
    else {
      _viewCycleId = source._viewCycleId;
    }
    _calculationConfigurationName = source._calculationConfigurationName;
    if (source._identifier == null) _identifier = null;
    else {
      _identifier = new java.util.ArrayList<Long> (source._identifier);
    }
  }
  public FindMessage clone () {
    return new FindMessage (this);
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
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _viewCycleId.getClass (), com.opengamma.id.UniqueIdentifier.class);
      _viewCycleId.toFudgeMsg (fudgeContext, fudge1);
      msg.add (VIEW_CYCLE_ID_KEY, null, fudge1);
    }
    if (_calculationConfigurationName != null)  {
      msg.add (CALCULATION_CONFIGURATION_NAME_KEY, null, _calculationConfigurationName);
    }
    if (_identifier != null)  {
      for (Long fudge1 : _identifier) {
        msg.add (IDENTIFIER_KEY, null, fudge1);
      }
    }
  }
  public static FindMessage fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.cache.msg.FindMessage".equals (className)) break;
      try {
        return (com.opengamma.engine.view.cache.msg.FindMessage)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeMsg.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new FindMessage (fudgeContext, fudgeMsg);
  }
  public com.opengamma.id.UniqueIdentifier getViewCycleId () {
    return _viewCycleId;
  }
  public void setViewCycleId (com.opengamma.id.UniqueIdentifier viewCycleId) {
    if (viewCycleId == null) throw new NullPointerException ("'viewCycleId' cannot be null");
    else {
      _viewCycleId = viewCycleId;
    }
  }
  public String getCalculationConfigurationName () {
    return _calculationConfigurationName;
  }
  public void setCalculationConfigurationName (String calculationConfigurationName) {
    if (calculationConfigurationName == null) throw new NullPointerException ("calculationConfigurationName' cannot be null");
    _calculationConfigurationName = calculationConfigurationName;
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
