// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.view.calcnode.msg;
public class RemoteCalcNodeMessage implements java.io.Serializable {
  public void accept (RemoteCalcNodeMessageVisitor visitor) { throw new UnsupportedOperationException (); }
  private static final long serialVersionUID = 1l;
  public RemoteCalcNodeMessage () {
  }
  protected RemoteCalcNodeMessage (final org.fudgemsg.FudgeMsg fudgeMsg) {
  }
  protected RemoteCalcNodeMessage (final RemoteCalcNodeMessage source) {
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
  }
  public static RemoteCalcNodeMessage fromFudgeMsg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeMessage".equals (className)) break;
      try {
        return (com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeMessage)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeMsg.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new RemoteCalcNodeMessage (fudgeMsg);
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
