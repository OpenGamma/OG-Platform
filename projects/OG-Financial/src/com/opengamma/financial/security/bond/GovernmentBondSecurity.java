// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.bond;
public class GovernmentBondSecurity extends com.opengamma.financial.security.bond.BondSecurity implements java.io.Serializable {
  public <T> T accept (BondSecurityVisitor<T> visitor) { return visitor.visitGovernmentBondSecurity (this); }
  private static final long serialVersionUID = 1l;
  public GovernmentBondSecurity (String issuerName, String issuerType, String issuerDomicile, String market, com.opengamma.util.money.Currency currency, com.opengamma.financial.convention.yield.YieldConvention yieldConvention, com.opengamma.util.time.Expiry lastTradeDate, String couponType, double couponRate, com.opengamma.financial.convention.frequency.Frequency couponFrequency, com.opengamma.financial.convention.daycount.DayCount dayCountConvention, com.opengamma.financial.security.DateTimeWithZone interestAccrualDate, com.opengamma.financial.security.DateTimeWithZone settlementDate, com.opengamma.financial.security.DateTimeWithZone firstCouponDate, double issuancePrice, double totalAmountIssued, double minimumAmount, double minimumIncrement, double parAmount, double redemptionValue) {
    super (issuerName, issuerType, issuerDomicile, market, currency, yieldConvention, lastTradeDate, couponType, couponRate, couponFrequency, dayCountConvention, interestAccrualDate, settlementDate, firstCouponDate, issuancePrice, totalAmountIssued, minimumAmount, minimumIncrement, parAmount, redemptionValue);
  }
  protected GovernmentBondSecurity (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeMsg);
  }
  public GovernmentBondSecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, String issuerName, String issuerType, String issuerDomicile, String market, com.opengamma.util.money.Currency currency, com.opengamma.financial.convention.yield.YieldConvention yieldConvention, String guaranteeType, com.opengamma.util.time.Expiry lastTradeDate, String couponType, double couponRate, com.opengamma.financial.convention.frequency.Frequency couponFrequency, com.opengamma.financial.convention.daycount.DayCount dayCountConvention, com.opengamma.financial.convention.businessday.BusinessDayConvention businessDayConvention, com.opengamma.financial.security.DateTimeWithZone announcementDate, com.opengamma.financial.security.DateTimeWithZone interestAccrualDate, com.opengamma.financial.security.DateTimeWithZone settlementDate, com.opengamma.financial.security.DateTimeWithZone firstCouponDate, double issuancePrice, double totalAmountIssued, double minimumAmount, double minimumIncrement, double parAmount, double redemptionValue) {
    super (uniqueId, name, securityType, identifiers, issuerName, issuerType, issuerDomicile, market, currency, yieldConvention, guaranteeType, lastTradeDate, couponType, couponRate, couponFrequency, dayCountConvention, businessDayConvention, announcementDate, interestAccrualDate, settlementDate, firstCouponDate, issuancePrice, totalAmountIssued, minimumAmount, minimumIncrement, parAmount, redemptionValue);
  }
  protected GovernmentBondSecurity (final GovernmentBondSecurity source) {
    super (source);
  }
  public GovernmentBondSecurity clone () {
    return new GovernmentBondSecurity (this);
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
  public static GovernmentBondSecurity fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.bond.GovernmentBondSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.bond.GovernmentBondSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new GovernmentBondSecurity (fudgeMsg);
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof GovernmentBondSecurity)) return false;
    GovernmentBondSecurity msg = (GovernmentBondSecurity)o;
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
