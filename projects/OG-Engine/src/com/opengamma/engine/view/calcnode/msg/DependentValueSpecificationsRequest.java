// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.view.calcnode.msg;
public class DependentValueSpecificationsRequest extends com.opengamma.engine.view.calcnode.msg.ViewProcessorQueryMessage implements java.io.Serializable {
  private static final long serialVersionUID = 1125952727l;
  private com.opengamma.engine.view.calcnode.CalculationJobSpecification _job;
  public static final String JOB_KEY = "job";
  public DependentValueSpecificationsRequest (long correlationId, com.opengamma.engine.view.calcnode.CalculationJobSpecification job) {
    super (correlationId);
    if (job == null) throw new NullPointerException ("'job' cannot be null");
    else {
      _job = job;
    }
  }
  protected DependentValueSpecificationsRequest (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (JOB_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a DependentValueSpecificationsRequest - field 'job' is not present");
    try {
      _job = fudgeContext.fieldValueToObject (com.opengamma.engine.view.calcnode.CalculationJobSpecification.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a DependentValueSpecificationsRequest - field 'job' is not CalculationJobSpecification message", e);
    }
  }
  protected DependentValueSpecificationsRequest (final DependentValueSpecificationsRequest source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._job == null) _job = null;
    else {
      _job = source._job;
    }
  }
  public DependentValueSpecificationsRequest clone () {
    return new DependentValueSpecificationsRequest (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_job != null)  {
      fudgeContext.objectToFudgeMsgWithClassHeaders (msg, JOB_KEY, null, _job, com.opengamma.engine.view.calcnode.CalculationJobSpecification.class);
    }
  }
  public static DependentValueSpecificationsRequest fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.calcnode.msg.DependentValueSpecificationsRequest".equals (className)) break;
      try {
        return (com.opengamma.engine.view.calcnode.msg.DependentValueSpecificationsRequest)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeMsg.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new DependentValueSpecificationsRequest (fudgeContext, fudgeMsg);
  }
  public com.opengamma.engine.view.calcnode.CalculationJobSpecification getJob () {
    return _job;
  }
  public void setJob (com.opengamma.engine.view.calcnode.CalculationJobSpecification job) {
    if (job == null) throw new NullPointerException ("'job' cannot be null");
    else {
      _job = job;
    }
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
