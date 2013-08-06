// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.engine.calcnode.msg;
public class Cancel extends com.opengamma.engine.calcnode.msg.RemoteCalcNodeMessage implements java.io.Serializable {
  public void accept (RemoteCalcNodeMessageVisitor visitor) { visitor.visitCancelMessage (this); }
  private static final long serialVersionUID = 597340290l;
  private java.util.List<com.opengamma.engine.calcnode.CalculationJobSpecification> _job;
  public static final String JOB_KEY = "job";
  public Cancel (java.util.Collection<? extends com.opengamma.engine.calcnode.CalculationJobSpecification> job) {
    if (job == null) throw new NullPointerException ("'job' cannot be null");
    else {
      final java.util.List<com.opengamma.engine.calcnode.CalculationJobSpecification> fudge0 = new java.util.ArrayList<com.opengamma.engine.calcnode.CalculationJobSpecification> (job);
      if (job.size () == 0) throw new IllegalArgumentException ("'job' cannot be an empty list");
      for (java.util.ListIterator<com.opengamma.engine.calcnode.CalculationJobSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.engine.calcnode.CalculationJobSpecification fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'job' cannot be null");
        fudge1.set (fudge2);
      }
      _job = fudge0;
    }
  }
  protected Cancel (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeFields = fudgeMsg.getAllByName (JOB_KEY);
    if (fudgeFields.size () == 0) throw new IllegalArgumentException ("Fudge message is not a Cancel - field 'job' is not present");
    _job = new java.util.ArrayList<com.opengamma.engine.calcnode.CalculationJobSpecification> (fudgeFields.size ());
    for (org.fudgemsg.FudgeField fudge1 : fudgeFields) {
      try {
        final com.opengamma.engine.calcnode.CalculationJobSpecification fudge2;
        fudge2 = deserializer.fieldValueToObject (com.opengamma.engine.calcnode.CalculationJobSpecification.class, fudge1);
        _job.add (fudge2);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a Cancel - field 'job' is not CalculationJobSpecification message", e);
      }
    }
  }
  protected Cancel (final Cancel source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._job == null) _job = null;
    else {
      final java.util.List<com.opengamma.engine.calcnode.CalculationJobSpecification> fudge0 = new java.util.ArrayList<com.opengamma.engine.calcnode.CalculationJobSpecification> (source._job);
      for (java.util.ListIterator<com.opengamma.engine.calcnode.CalculationJobSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.engine.calcnode.CalculationJobSpecification fudge2 = fudge1.next ();
        fudge1.set (fudge2);
      }
      _job = fudge0;
    }
  }
  public Cancel clone () {
    return new Cancel (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_job != null)  {
      for (com.opengamma.engine.calcnode.CalculationJobSpecification fudge1 : _job) {
        serializer.addToMessageWithClassHeaders (msg, JOB_KEY, null, fudge1, com.opengamma.engine.calcnode.CalculationJobSpecification.class);
      }
    }
  }
  public static Cancel fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.calcnode.msg.Cancel".equals (className)) break;
      try {
        return (com.opengamma.engine.calcnode.msg.Cancel)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Cancel (deserializer, fudgeMsg);
  }
  public java.util.List<com.opengamma.engine.calcnode.CalculationJobSpecification> getJob () {
    return java.util.Collections.unmodifiableList (_job);
  }
  public void setJob (com.opengamma.engine.calcnode.CalculationJobSpecification job) {
    if (job == null) throw new NullPointerException ("'job' cannot be null");
    else {
      _job = new java.util.ArrayList<com.opengamma.engine.calcnode.CalculationJobSpecification> (1);
      addJob (job);
    }
  }
  public void setJob (java.util.Collection<? extends com.opengamma.engine.calcnode.CalculationJobSpecification> job) {
    if (job == null) throw new NullPointerException ("'job' cannot be null");
    else {
      final java.util.List<com.opengamma.engine.calcnode.CalculationJobSpecification> fudge0 = new java.util.ArrayList<com.opengamma.engine.calcnode.CalculationJobSpecification> (job);
      if (job.size () == 0) throw new IllegalArgumentException ("'job' cannot be an empty list");
      for (java.util.ListIterator<com.opengamma.engine.calcnode.CalculationJobSpecification> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        com.opengamma.engine.calcnode.CalculationJobSpecification fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'job' cannot be null");
        fudge1.set (fudge2);
      }
      _job = fudge0;
    }
  }
  public void addJob (com.opengamma.engine.calcnode.CalculationJobSpecification job) {
    if (job == null) throw new NullPointerException ("'job' cannot be null");
    if (_job == null) _job = new java.util.ArrayList<com.opengamma.engine.calcnode.CalculationJobSpecification> ();
    _job.add (job);
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
