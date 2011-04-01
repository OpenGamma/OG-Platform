// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.view.calcnode.msg;
public class Scaling extends com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeMessage implements java.io.Serializable {
  public void accept (RemoteCalcNodeMessageVisitor visitor) { visitor.visitScalingMessage (this); }
  private static final long serialVersionUID = 52573772034l;
  private double _invocation;
  public static final String INVOCATION_KEY = "invocation";
  public Scaling (double invocation) {
    _invocation = invocation;
  }
  protected Scaling (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (INVOCATION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a Scaling - field 'invocation' is not present");
    try {
      _invocation = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a Scaling - field 'invocation' is not double", e);
    }
  }
  protected Scaling (final Scaling source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _invocation = source._invocation;
  }
  public Scaling clone () {
    return new Scaling (this);
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    super.toFudgeMsg (fudgeContext, msg);
    msg.add (INVOCATION_KEY, null, _invocation);
  }
  public static Scaling fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.calcnode.msg.Scaling".equals (className)) break;
      try {
        return (com.opengamma.engine.view.calcnode.msg.Scaling)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Scaling (fudgeMsg);
  }
  public double getInvocation () {
    return _invocation;
  }
  public void setInvocation (double invocation) {
    _invocation = invocation;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
