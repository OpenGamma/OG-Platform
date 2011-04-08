// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
// Created from com/opengamma/engine/view/cache/msg/SpecificationLookupResponse.proto:9(10)
package com.opengamma.engine.view.cache.msg;
public class SpecificationLookupResponse extends com.opengamma.engine.view.cache.msg.CacheMessage implements java.io.Serializable {
  public CacheMessage accept (CacheMessageVisitor visitor) { return visitor.visitSpecificationLookupResponse (this); }
  private static final long serialVersionUID = 38975840645l;
  private java.util.List<com.opengamma.engine.value.ValueSpecification> _specification;
  public static final String SPECIFICATION_KEY = "specification";
  public SpecificationLookupResponse (java.util.Collection<? extends com.opengamma.engine.value.ValueSpecification> specification) {
    if (specification == null) throw new NullPointerException ("'specification' cannot be null");
    else {
      final java.util.List<com.opengamma.engine.value.ValueSpecification> fudge0 = new java.util.ArrayList<com.opengamma.engine.value.ValueSpecification> (specification);
      if (specification.size () == 0) throw new IllegalArgumentException ("'specification' cannot be an empty list");
      for (java.util.ListIterator<com.opengamma.engine.value.ValueSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.engine.value.ValueSpecification fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'specification' cannot be null");
        fudge1.set (fudge2);
      }
      _specification = fudge0;
    }
  }
  protected SpecificationLookupResponse (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeFields = fudgeMsg.getAllByName (SPECIFICATION_KEY);
    if (fudgeFields.size () == 0) throw new IllegalArgumentException ("Fudge message is not a SpecificationLookupResponse - field 'specification' is not present");
    _specification = new java.util.ArrayList<com.opengamma.engine.value.ValueSpecification> (fudgeFields.size ());
    for (org.fudgemsg.FudgeField fudge1 : fudgeFields) {
      try {
        final com.opengamma.engine.value.ValueSpecification fudge2;
        fudge2 = fudgeContext.fieldValueToObject (com.opengamma.engine.value.ValueSpecification.class, fudge1);
        _specification.add (fudge2);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a SpecificationLookupResponse - field 'specification' is not ValueSpecification message", e);
      }
    }
  }
  public SpecificationLookupResponse (Long correlationId, java.util.Collection<? extends com.opengamma.engine.value.ValueSpecification> specification) {
    super (correlationId);
    if (specification == null) throw new NullPointerException ("'specification' cannot be null");
    else {
      final java.util.List<com.opengamma.engine.value.ValueSpecification> fudge0 = new java.util.ArrayList<com.opengamma.engine.value.ValueSpecification> (specification);
      if (specification.size () == 0) throw new IllegalArgumentException ("'specification' cannot be an empty list");
      for (java.util.ListIterator<com.opengamma.engine.value.ValueSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.engine.value.ValueSpecification fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'specification' cannot be null");
        fudge1.set (fudge2);
      }
      _specification = fudge0;
    }
  }
  protected SpecificationLookupResponse (final SpecificationLookupResponse source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._specification == null) _specification = null;
    else {
      final java.util.List<com.opengamma.engine.value.ValueSpecification> fudge0 = new java.util.ArrayList<com.opengamma.engine.value.ValueSpecification> (source._specification);
      for (java.util.ListIterator<com.opengamma.engine.value.ValueSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.engine.value.ValueSpecification fudge2 = fudge1.next ();
        fudge1.set (fudge2);
      }
      _specification = fudge0;
    }
  }
  public SpecificationLookupResponse clone () {
    return new SpecificationLookupResponse (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_specification != null)  {
      for (com.opengamma.engine.value.ValueSpecification fudge1 : _specification) {
        fudgeContext.addToMessageWithClassHeaders (msg, SPECIFICATION_KEY, null, fudge1, com.opengamma.engine.value.ValueSpecification.class);
      }
    }
  }
  public static SpecificationLookupResponse fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.cache.msg.SpecificationLookupResponse".equals (className)) break;
      try {
        return (com.opengamma.engine.view.cache.msg.SpecificationLookupResponse)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeMsg.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new SpecificationLookupResponse (fudgeContext, fudgeMsg);
  }
  public java.util.List<com.opengamma.engine.value.ValueSpecification> getSpecification () {
    return java.util.Collections.unmodifiableList (_specification);
  }
  public void setSpecification (com.opengamma.engine.value.ValueSpecification specification) {
    if (specification == null) throw new NullPointerException ("'specification' cannot be null");
    else {
      _specification = new java.util.ArrayList<com.opengamma.engine.value.ValueSpecification> (1);
      addSpecification (specification);
    }
  }
  public void setSpecification (java.util.Collection<? extends com.opengamma.engine.value.ValueSpecification> specification) {
    if (specification == null) throw new NullPointerException ("'specification' cannot be null");
    else {
      final java.util.List<com.opengamma.engine.value.ValueSpecification> fudge0 = new java.util.ArrayList<com.opengamma.engine.value.ValueSpecification> (specification);
      if (specification.size () == 0) throw new IllegalArgumentException ("'specification' cannot be an empty list");
      for (java.util.ListIterator<com.opengamma.engine.value.ValueSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.engine.value.ValueSpecification fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'specification' cannot be null");
        fudge1.set (fudge2);
      }
      _specification = fudge0;
    }
  }
  public void addSpecification (com.opengamma.engine.value.ValueSpecification specification) {
    if (specification == null) throw new NullPointerException ("'specification' cannot be null");
    if (_specification == null) _specification = new java.util.ArrayList<com.opengamma.engine.value.ValueSpecification> ();
    _specification.add (specification);
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
