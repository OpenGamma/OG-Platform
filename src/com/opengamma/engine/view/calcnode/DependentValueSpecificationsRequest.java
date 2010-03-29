// Automatically created - do not modify
// Created from com/opengamma/engine/view/calcnode/DependentValueSpecificationsRequest.proto:9(10)
package com.opengamma.engine.view.calcnode;
public class DependentValueSpecificationsRequest extends com.opengamma.engine.view.calcnode.ViewProcessorQueryMessage implements java.io.Serializable {
  private static final long serialVersionUID = 1l;
  public DependentValueSpecificationsRequest () {
  }
  protected DependentValueSpecificationsRequest (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeContext, fudgeMsg);
  }
  public DependentValueSpecificationsRequest (com.opengamma.engine.view.calcnode.CalculationJobSpecification jobSpec) {
    super (jobSpec);
  }
  protected DependentValueSpecificationsRequest (final DependentValueSpecificationsRequest source) {
    super (source);
  }
  public DependentValueSpecificationsRequest clone () {
    return new DependentValueSpecificationsRequest (this);
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    super.toFudgeMsg (fudgeContext, msg);
  }
  public static DependentValueSpecificationsRequest fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.calcnode.DependentValueSpecificationsRequest".equals (className)) break;
      try {
        return (com.opengamma.engine.view.calcnode.DependentValueSpecificationsRequest)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new DependentValueSpecificationsRequest (fudgeContext, fudgeMsg);
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
