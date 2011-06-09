// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.future;
public class AgricultureFutureSecurity extends com.opengamma.financial.security.future.CommodityFutureSecurity implements java.io.Serializable {
  public <T> T accept (FutureSecurityVisitor<T> visitor) { return visitor.visitAgricultureFutureSecurity (this); }
  private static final long serialVersionUID = 1l;
  public AgricultureFutureSecurity (com.opengamma.util.time.Expiry expiry, String tradingExchange, String settlementExchange, com.opengamma.util.money.Currency currency, double unitAmount, String commodityType) {
    super (expiry, tradingExchange, settlementExchange, currency, unitAmount, commodityType);
  }
  protected AgricultureFutureSecurity (final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
  }
  public AgricultureFutureSecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, com.opengamma.util.time.Expiry expiry, String tradingExchange, String settlementExchange, com.opengamma.util.money.Currency currency, double unitAmount, String commodityType, Double unitNumber, String unitName) {
    super (uniqueId, name, securityType, identifiers, expiry, tradingExchange, settlementExchange, currency, unitAmount, commodityType, unitNumber, unitName);
  }
  protected AgricultureFutureSecurity (final AgricultureFutureSecurity source) {
    super (source);
  }
  public AgricultureFutureSecurity clone () {
    return new AgricultureFutureSecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
  }
  public static AgricultureFutureSecurity fromFudgeMsg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.future.AgricultureFutureSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.future.AgricultureFutureSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeMsg.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new AgricultureFutureSecurity (fudgeMsg);
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof AgricultureFutureSecurity)) return false;
    AgricultureFutureSecurity msg = (AgricultureFutureSecurity)o;
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
