// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.option;
public class BarrierPayoffStyle extends com.opengamma.financial.security.option.PayoffStyle implements java.io.Serializable {
  public <T> T accept (PayoffStyleVisitor<T> visitor) { return visitor.visitBarrierPayoffStyle(this); }
  private static final long serialVersionUID = 1l;
  public BarrierPayoffStyle () {
  }
  protected BarrierPayoffStyle (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeMsg);
  }
  protected BarrierPayoffStyle (final BarrierPayoffStyle source) {
    super (source);
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    super.toFudgeMsg (fudgeContext, msg);
  }
  public static BarrierPayoffStyle fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.option.BarrierPayoffStyle".equals (className)) break;
      try {
        return (com.opengamma.financial.security.option.BarrierPayoffStyle)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new BarrierPayoffStyle (fudgeMsg);
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof BarrierPayoffStyle)) return false;
    BarrierPayoffStyle msg = (BarrierPayoffStyle)o;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
