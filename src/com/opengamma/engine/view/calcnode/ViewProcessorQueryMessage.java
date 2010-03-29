// Automatically created - do not modify
// Created from com/opengamma/engine/view/calcnode/ViewProcessorQueryMessage.proto:11(10)
package com.opengamma.engine.view.calcnode;
public class ViewProcessorQueryMessage implements java.io.Serializable {
  private static final long serialVersionUID = -43453246756l;
  private com.opengamma.engine.view.calcnode.CalculationJobSpecification _jobSpec;
  public static final String JOBSPEC_KEY = "jobSpec";
  public ViewProcessorQueryMessage () {
  }
  protected ViewProcessorQueryMessage (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (JOBSPEC_KEY);
    if (fudgeField != null)  {
      try {
        final com.opengamma.engine.view.calcnode.CalculationJobSpecification fudge1;
        fudge1 = fudgeContext.fudgeMsgToObject (com.opengamma.engine.view.calcnode.CalculationJobSpecification.class, fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
        setJobSpec (fudge1);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a ViewProcessorQueryMessage - field 'jobSpec' is not CalculationJobSpecification message", e);
      }
    }
  }
  public ViewProcessorQueryMessage (com.opengamma.engine.view.calcnode.CalculationJobSpecification jobSpec) {
    if (jobSpec == null) _jobSpec = null;
    else {
      _jobSpec = jobSpec;
    }
  }
  protected ViewProcessorQueryMessage (final ViewProcessorQueryMessage source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._jobSpec == null) _jobSpec = null;
    else {
      _jobSpec = source._jobSpec;
    }
  }
  public ViewProcessorQueryMessage clone () {
    return new ViewProcessorQueryMessage (this);
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    if (_jobSpec != null)  {
      msg.add (JOBSPEC_KEY, null, fudgeContext.objectToFudgeMsg (_jobSpec));
    }
  }
  public static ViewProcessorQueryMessage fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.calcnode.ViewProcessorQueryMessage".equals (className)) break;
      try {
        return (com.opengamma.engine.view.calcnode.ViewProcessorQueryMessage)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new ViewProcessorQueryMessage (fudgeContext, fudgeMsg);
  }
  public com.opengamma.engine.view.calcnode.CalculationJobSpecification getJobSpec () {
    return _jobSpec;
  }
  public void setJobSpec (com.opengamma.engine.view.calcnode.CalculationJobSpecification jobSpec) {
    if (jobSpec == null) _jobSpec = null;
    else {
      _jobSpec = jobSpec;
    }
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
