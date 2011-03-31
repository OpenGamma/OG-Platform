// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.swap;
public class CommodityNotional extends com.opengamma.financial.security.swap.Notional implements java.io.Serializable {
  public <T> T accept (NotionalVisitor<T> visitor) { return visitor.visitCommodityNotional (this); }
  private static final long serialVersionUID = 1l;
  public CommodityNotional () {
  }
  protected CommodityNotional (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeMsg);
  }
  protected CommodityNotional (final CommodityNotional source) {
    super (source);
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    super.toFudgeMsg (fudgeContext, msg);
  }
  public static CommodityNotional fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.swap.CommodityNotional".equals (className)) break;
      try {
        return (com.opengamma.financial.security.swap.CommodityNotional)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new CommodityNotional (fudgeMsg);
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof CommodityNotional)) return false;
    CommodityNotional msg = (CommodityNotional)o;
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
