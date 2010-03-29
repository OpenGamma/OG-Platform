// Automatically created - do not modify
// Created from com/opengamma/engine/view/calcnode/DependentValueSpecificationsReply.proto:10(10)
package com.opengamma.engine.view.calcnode;
public class DependentValueSpecificationsReply extends com.opengamma.engine.view.calcnode.ViewProcessorQueryMessage implements java.io.Serializable {
  private static final long serialVersionUID = -1306548906l;
  private java.util.List<com.opengamma.engine.value.ValueSpecification> _valueSpecification;
  public static final String VALUESPECIFICATION_KEY = "valueSpecification";
  public DependentValueSpecificationsReply () {
  }
  protected DependentValueSpecificationsReply (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeContext, fudgeMsg);
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeFields = fudgeMsg.getAllByName (VALUESPECIFICATION_KEY);
    if (fudgeFields.size () > 0)  {
      final java.util.List<com.opengamma.engine.value.ValueSpecification> fudge1;
      fudge1 = new java.util.ArrayList<com.opengamma.engine.value.ValueSpecification> (fudgeFields.size ());
      for (org.fudgemsg.FudgeField fudge2 : fudgeFields) {
        try {
          final com.opengamma.engine.value.ValueSpecification fudge3;
          fudge3 = com.opengamma.engine.value.ValueSpecification.fromFudgeMsg (fudgeContext, fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudge2));
          fudge1.add (fudge3);
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a DependentValueSpecificationsReply - field 'valueSpecification' is not ValueSpecification message", e);
        }
      }
      setValueSpecification (fudge1);
    }
  }
  public DependentValueSpecificationsReply (com.opengamma.engine.view.calcnode.CalculationJobSpecification jobSpec, java.util.Collection<? extends com.opengamma.engine.value.ValueSpecification> valueSpecification) {
    super (jobSpec);
    if (valueSpecification == null) _valueSpecification = null;
    else {
      final java.util.List<com.opengamma.engine.value.ValueSpecification> fudge0 = new java.util.ArrayList<com.opengamma.engine.value.ValueSpecification> (valueSpecification);
      for (java.util.ListIterator<com.opengamma.engine.value.ValueSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.engine.value.ValueSpecification fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'valueSpecification' cannot be null");
        fudge1.set ((com.opengamma.engine.value.ValueSpecification)fudge2.clone ());
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
        fudge1.set ((com.opengamma.engine.value.ValueSpecification)fudge2.clone ());
      }
      _valueSpecification = fudge0;
    }
  }
  public DependentValueSpecificationsReply clone () {
    return new DependentValueSpecificationsReply (this);
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_valueSpecification != null)  {
      for (com.opengamma.engine.value.ValueSpecification fudge1 : _valueSpecification) {
        final org.fudgemsg.MutableFudgeFieldContainer fudge2 = fudgeContext.newMessage ();
        Class<?> fudge3 = fudge1.getClass ();
        while (!com.opengamma.engine.value.ValueSpecification.class.equals (fudge3)) {
          fudge2.add (null, 0, org.fudgemsg.types.StringFieldType.INSTANCE, fudge3.getName ());
          fudge3 = fudge3.getSuperclass ();
        }
        fudge1.toFudgeMsg (fudgeContext, fudge2);
        msg.add (VALUESPECIFICATION_KEY, null, fudge2);
      }
    }
  }
  public static DependentValueSpecificationsReply fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.calcnode.DependentValueSpecificationsReply".equals (className)) break;
      try {
        return (com.opengamma.engine.view.calcnode.DependentValueSpecificationsReply)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new DependentValueSpecificationsReply (fudgeContext, fudgeMsg);
  }
  public java.util.List<com.opengamma.engine.value.ValueSpecification> getValueSpecification () {
    return java.util.Collections.unmodifiableList (_valueSpecification);
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
        fudge1.set ((com.opengamma.engine.value.ValueSpecification)fudge2.clone ());
      }
      _valueSpecification = fudge0;
    }
  }
  public void addValueSpecification (com.opengamma.engine.value.ValueSpecification valueSpecification) {
    if (valueSpecification == null) throw new NullPointerException ("'valueSpecification' cannot be null");
    if (_valueSpecification == null) _valueSpecification = new java.util.ArrayList<com.opengamma.engine.value.ValueSpecification> ();
    _valueSpecification.add ((com.opengamma.engine.value.ValueSpecification)valueSpecification.clone ());
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
