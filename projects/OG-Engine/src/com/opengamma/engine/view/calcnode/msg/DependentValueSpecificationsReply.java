// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.view.calcnode.msg;
public class DependentValueSpecificationsReply extends com.opengamma.engine.view.calcnode.msg.ViewProcessorQueryMessage implements java.io.Serializable {
  private static final long serialVersionUID = -1306548906l;
  private java.util.List<com.opengamma.engine.value.ValueSpecification> _valueSpecification;
  public static final String VALUE_SPECIFICATION_KEY = "valueSpecification";
  public DependentValueSpecificationsReply (long correlationId) {
    super (correlationId);
  }
  protected DependentValueSpecificationsReply (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeContext, fudgeMsg);
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeFields = fudgeMsg.getAllByName (VALUE_SPECIFICATION_KEY);
    if (fudgeFields.size () > 0)  {
      final java.util.List<com.opengamma.engine.value.ValueSpecification> fudge1;
      fudge1 = new java.util.ArrayList<com.opengamma.engine.value.ValueSpecification> (fudgeFields.size ());
      for (org.fudgemsg.FudgeField fudge2 : fudgeFields) {
        try {
          final com.opengamma.engine.value.ValueSpecification fudge3;
          fudge3 = fudgeContext.fieldValueToObject (com.opengamma.engine.value.ValueSpecification.class, fudge2);
          fudge1.add (fudge3);
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a DependentValueSpecificationsReply - field 'valueSpecification' is not ValueSpecification message", e);
        }
      }
      setValueSpecification (fudge1);
    }
  }
  public DependentValueSpecificationsReply (long correlationId, java.util.Collection<? extends com.opengamma.engine.value.ValueSpecification> valueSpecification) {
    super (correlationId);
    if (valueSpecification == null) _valueSpecification = null;
    else {
      final java.util.List<com.opengamma.engine.value.ValueSpecification> fudge0 = new java.util.ArrayList<com.opengamma.engine.value.ValueSpecification> (valueSpecification);
      for (java.util.ListIterator<com.opengamma.engine.value.ValueSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.engine.value.ValueSpecification fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'valueSpecification' cannot be null");
        fudge1.set (fudge2);
      }
      _valueSpecification = fudge0;
    }
  }
  protected DependentValueSpecificationsReply (final DependentValueSpecificationsReply source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._valueSpecification == null) _valueSpecification = null;
    else {
      final java.util.List<com.opengamma.engine.value.ValueSpecification> fudge0 = new java.util.ArrayList<com.opengamma.engine.value.ValueSpecification> (source._valueSpecification);
      for (java.util.ListIterator<com.opengamma.engine.value.ValueSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.engine.value.ValueSpecification fudge2 = fudge1.next ();
        fudge1.set (fudge2);
      }
      _valueSpecification = fudge0;
    }
  }
  public DependentValueSpecificationsReply clone () {
    return new DependentValueSpecificationsReply (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_valueSpecification != null)  {
      for (com.opengamma.engine.value.ValueSpecification fudge1 : _valueSpecification) {
        fudgeContext.addToMessageWithClassHeaders (msg, VALUE_SPECIFICATION_KEY, null, fudge1, com.opengamma.engine.value.ValueSpecification.class);
      }
    }
  }
  public static DependentValueSpecificationsReply fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.calcnode.msg.DependentValueSpecificationsReply".equals (className)) break;
      try {
        return (com.opengamma.engine.view.calcnode.msg.DependentValueSpecificationsReply)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeMsg.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new DependentValueSpecificationsReply (fudgeContext, fudgeMsg);
  }
  public java.util.List<com.opengamma.engine.value.ValueSpecification> getValueSpecification () {
    if (_valueSpecification != null) {
      return java.util.Collections.unmodifiableList (_valueSpecification);
    }
    else return null;
  }
  public void setValueSpecification (com.opengamma.engine.value.ValueSpecification valueSpecification) {
    if (valueSpecification == null) _valueSpecification = null;
    else {
      _valueSpecification = new java.util.ArrayList<com.opengamma.engine.value.ValueSpecification> (1);
      addValueSpecification (valueSpecification);
    }
  }
  public void setValueSpecification (java.util.Collection<? extends com.opengamma.engine.value.ValueSpecification> valueSpecification) {
    if (valueSpecification == null) _valueSpecification = null;
    else {
      final java.util.List<com.opengamma.engine.value.ValueSpecification> fudge0 = new java.util.ArrayList<com.opengamma.engine.value.ValueSpecification> (valueSpecification);
      for (java.util.ListIterator<com.opengamma.engine.value.ValueSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.engine.value.ValueSpecification fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'valueSpecification' cannot be null");
        fudge1.set (fudge2);
      }
      _valueSpecification = fudge0;
    }
  }
  public void addValueSpecification (com.opengamma.engine.value.ValueSpecification valueSpecification) {
    if (valueSpecification == null) throw new NullPointerException ("'valueSpecification' cannot be null");
    if (_valueSpecification == null) _valueSpecification = new java.util.ArrayList<com.opengamma.engine.value.ValueSpecification> ();
    _valueSpecification.add (valueSpecification);
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
