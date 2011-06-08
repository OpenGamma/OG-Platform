// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.future;
public class InterestRateFutureSecurity extends com.opengamma.financial.security.future.FutureSecurity implements java.io.Serializable {
  public <T> T accept (FutureSecurityVisitor<T> visitor) { return visitor.visitInterestRateFutureSecurity (this); }
  private static final long serialVersionUID = 61732422917l;
  private String _cashRateType;
  public static final String CASH_RATE_TYPE_KEY = "cashRateType";
  public InterestRateFutureSecurity (com.opengamma.util.time.Expiry expiry, String tradingExchange, String settlementExchange, com.opengamma.util.money.Currency currency, String cashRateType) {
    super (expiry, tradingExchange, settlementExchange, currency);
    if (cashRateType == null) throw new NullPointerException ("cashRateType' cannot be null");
    _cashRateType = cashRateType;
  }
  protected InterestRateFutureSecurity (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeContext, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (CASH_RATE_TYPE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a InterestRateFutureSecurity - field 'cashRateType' is not present");
    try {
      _cashRateType = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a InterestRateFutureSecurity - field 'cashRateType' is not string", e);
    }
  }
  public InterestRateFutureSecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, com.opengamma.util.time.Expiry expiry, String tradingExchange, String settlementExchange, com.opengamma.util.money.Currency currency, String cashRateType) {
    super (uniqueId, name, securityType, identifiers, expiry, tradingExchange, settlementExchange, currency);
    if (cashRateType == null) throw new NullPointerException ("cashRateType' cannot be null");
    _cashRateType = cashRateType;
  }
  protected InterestRateFutureSecurity (final InterestRateFutureSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _cashRateType = source._cashRateType;
  }
  public InterestRateFutureSecurity clone () {
    return new InterestRateFutureSecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_cashRateType != null)  {
      msg.add (CASH_RATE_TYPE_KEY, null, _cashRateType);
    }
  }
  public static InterestRateFutureSecurity fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.future.InterestRateFutureSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.future.InterestRateFutureSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeMsg.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new InterestRateFutureSecurity (fudgeContext, fudgeMsg);
  }
  public String getCashRateType () {
    return _cashRateType;
  }
  public void setCashRateType (String cashRateType) {
    if (cashRateType == null) throw new NullPointerException ("cashRateType' cannot be null");
    _cashRateType = cashRateType;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof InterestRateFutureSecurity)) return false;
    InterestRateFutureSecurity msg = (InterestRateFutureSecurity)o;
    if (_cashRateType != null) {
      if (msg._cashRateType != null) {
        if (!_cashRateType.equals (msg._cashRateType)) return false;
      }
      else return false;
    }
    else if (msg._cashRateType != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_cashRateType != null) hc += _cashRateType.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
