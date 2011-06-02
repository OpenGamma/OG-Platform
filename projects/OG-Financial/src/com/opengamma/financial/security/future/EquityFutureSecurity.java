// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.future;
public class EquityFutureSecurity extends com.opengamma.financial.security.future.FutureSecurity implements java.io.Serializable {
  public <T> T accept (FutureSecurityVisitor<T> visitor) { return visitor.visitEquityFutureSecurity (this); }
  private static final long serialVersionUID = -7584290899270l;
  private com.opengamma.financial.security.DateTimeWithZone _settlementDate;
  public static final String SETTLEMENT_DATE_KEY = "settlementDate";
  private com.opengamma.id.Identifier _underlyingIdentifier;
  public static final String UNDERLYING_IDENTIFIER_KEY = "underlyingIdentifier";
  public EquityFutureSecurity (com.opengamma.util.time.Expiry expiry, String tradingExchange, String settlementExchange, com.opengamma.util.money.Currency currency, double unitAmount, com.opengamma.financial.security.DateTimeWithZone settlementDate) {
    super (expiry, tradingExchange, settlementExchange, currency, unitAmount);
    if (settlementDate == null) throw new NullPointerException ("'settlementDate' cannot be null");
    else {
      _settlementDate = (com.opengamma.financial.security.DateTimeWithZone)settlementDate.clone ();
    }
  }
  protected EquityFutureSecurity (final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (SETTLEMENT_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityFutureSecurity - field 'settlementDate' is not present");
    try {
      _settlementDate = com.opengamma.financial.security.DateTimeWithZone.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityFutureSecurity - field 'settlementDate' is not DateTimeWithZone message", e);
    }
    fudgeField = fudgeMsg.getByName (UNDERLYING_IDENTIFIER_KEY);
    if (fudgeField != null)  {
      try {
        final com.opengamma.id.Identifier fudge1;
        fudge1 = com.opengamma.id.Identifier.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
        setUnderlyingIdentifier (fudge1);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a EquityFutureSecurity - field 'underlyingIdentifier' is not Identifier message", e);
      }
    }
  }
  public EquityFutureSecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, com.opengamma.util.time.Expiry expiry, String tradingExchange, String settlementExchange, com.opengamma.util.money.Currency currency, String settlementType, double unitAmount, com.opengamma.financial.security.DateTimeWithZone settlementDate, com.opengamma.id.Identifier underlyingIdentifier) {
    super (uniqueId, name, securityType, identifiers, expiry, tradingExchange, settlementExchange, currency, settlementType, unitAmount);
    if (settlementDate == null) throw new NullPointerException ("'settlementDate' cannot be null");
    else {
      _settlementDate = (com.opengamma.financial.security.DateTimeWithZone)settlementDate.clone ();
    }
    if (underlyingIdentifier == null) _underlyingIdentifier = null;
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
  }
  protected EquityFutureSecurity (final EquityFutureSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._settlementDate == null) _settlementDate = null;
    else {
      _settlementDate = (com.opengamma.financial.security.DateTimeWithZone)source._settlementDate.clone ();
    }
    if (source._underlyingIdentifier == null) _underlyingIdentifier = null;
    else {
      _underlyingIdentifier = source._underlyingIdentifier;
    }
  }
  public EquityFutureSecurity clone () {
    return new EquityFutureSecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_settlementDate != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _settlementDate.getClass (), com.opengamma.financial.security.DateTimeWithZone.class);
      _settlementDate.toFudgeMsg (fudgeContext, fudge1);
      msg.add (SETTLEMENT_DATE_KEY, null, fudge1);
    }
    if (_underlyingIdentifier != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _underlyingIdentifier.getClass (), com.opengamma.id.Identifier.class);
      _underlyingIdentifier.toFudgeMsg (fudgeContext, fudge1);
      msg.add (UNDERLYING_IDENTIFIER_KEY, null, fudge1);
    }
  }
  public static EquityFutureSecurity fromFudgeMsg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.future.EquityFutureSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.future.EquityFutureSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeMsg.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new EquityFutureSecurity (fudgeMsg);
  }
  public com.opengamma.financial.security.DateTimeWithZone getSettlementDate () {
    return _settlementDate;
  }
  public void setSettlementDate (com.opengamma.financial.security.DateTimeWithZone settlementDate) {
    if (settlementDate == null) throw new NullPointerException ("'settlementDate' cannot be null");
    else {
      _settlementDate = (com.opengamma.financial.security.DateTimeWithZone)settlementDate.clone ();
    }
  }
  public com.opengamma.id.Identifier getUnderlyingIdentifier () {
    return _underlyingIdentifier;
  }
  public void setUnderlyingIdentifier (com.opengamma.id.Identifier underlyingIdentifier) {
    if (underlyingIdentifier == null) _underlyingIdentifier = null;
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof EquityFutureSecurity)) return false;
    EquityFutureSecurity msg = (EquityFutureSecurity)o;
    if (_settlementDate != null) {
      if (msg._settlementDate != null) {
        if (!_settlementDate.equals (msg._settlementDate)) return false;
      }
      else return false;
    }
    else if (msg._settlementDate != null) return false;
    if (_underlyingIdentifier != null) {
      if (msg._underlyingIdentifier != null) {
        if (!_underlyingIdentifier.equals (msg._underlyingIdentifier)) return false;
      }
      else return false;
    }
    else if (msg._underlyingIdentifier != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_settlementDate != null) hc += _settlementDate.hashCode ();
    hc *= 31;
    if (_underlyingIdentifier != null) hc += _underlyingIdentifier.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
