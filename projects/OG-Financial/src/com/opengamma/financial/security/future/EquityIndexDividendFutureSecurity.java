// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.future;
public class EquityIndexDividendFutureSecurity extends com.opengamma.financial.security.future.EquityFutureSecurity implements java.io.Serializable {
  public <T> T accept (FutureSecurityVisitor<T> visitor) { return visitor.visitEquityIndexDividendFutureSecurity (this); }
  private static final long serialVersionUID = 1l;
  public EquityIndexDividendFutureSecurity (com.opengamma.util.time.Expiry expiry, String tradingExchange, String settlementExchange, com.opengamma.util.money.Currency currency, double unitAmount, com.opengamma.financial.security.DateTimeWithZone settlementDate) {
    super (expiry, tradingExchange, settlementExchange, currency, unitAmount, settlementDate);
  }
  protected EquityIndexDividendFutureSecurity (final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
  }
  public EquityIndexDividendFutureSecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, com.opengamma.util.time.Expiry expiry, String tradingExchange, String settlementExchange, com.opengamma.util.money.Currency currency, double unitAmount, com.opengamma.financial.security.DateTimeWithZone settlementDate, com.opengamma.id.Identifier underlyingIdentifier) {
    super (uniqueId, name, securityType, identifiers, expiry, tradingExchange, settlementExchange, currency, unitAmount, settlementDate, underlyingIdentifier);
  }
  protected EquityIndexDividendFutureSecurity (final EquityIndexDividendFutureSecurity source) {
    super (source);
  }
  public EquityIndexDividendFutureSecurity clone () {
    return new EquityIndexDividendFutureSecurity (this);
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
  public static EquityIndexDividendFutureSecurity fromFudgeMsg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeMsg.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new EquityIndexDividendFutureSecurity (fudgeMsg);
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof EquityIndexDividendFutureSecurity)) return false;
    EquityIndexDividendFutureSecurity msg = (EquityIndexDividendFutureSecurity)o;
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
