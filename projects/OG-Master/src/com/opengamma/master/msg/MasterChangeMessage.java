// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
// Created from com/opengamma/master/msg/MasterChangeMessage.proto:9(10)
package com.opengamma.master.msg;
public class MasterChangeMessage implements java.io.Serializable {
  public void accept (MasterChangeMessageVisitor visitor) { throw new UnsupportedOperationException (); }
  private static final long serialVersionUID = 1l;
  public MasterChangeMessage () {
  }
  protected MasterChangeMessage (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
  }
  protected MasterChangeMessage (final MasterChangeMessage source) {
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
  }
  public static MasterChangeMessage fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.master.msg.MasterChangeMessage".equals (className)) break;
      try {
        return (com.opengamma.master.msg.MasterChangeMessage)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new MasterChangeMessage (fudgeMsg);
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof MasterChangeMessage)) return false;
    MasterChangeMessage msg = (MasterChangeMessage)o;
    return true;
  }
  public int hashCode () {
    int hc = 1;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
