// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.swap;
public class SwapSecurity extends com.opengamma.financial.security.FinancialSecurity implements java.io.Serializable {
          public <T> T accept (SwapSecurityVisitor<T> visitor) { return visitor.visitSwapSecurity (this); }
        public <T> T accept (com.opengamma.financial.security.FinancialSecurityVisitor<T> visitor) { return visitor.visitSwapSecurity (this); }
  private static final long serialVersionUID = 4210989868692014018l;
  private javax.time.calendar.ZonedDateTime _tradeDate;
  public static final String TRADE_DATE_KEY = "tradeDate";
  private javax.time.calendar.ZonedDateTime _effectiveDate;
  public static final String EFFECTIVE_DATE_KEY = "effectiveDate";
  private javax.time.calendar.ZonedDateTime _maturityDate;
  public static final String MATURITY_DATE_KEY = "maturityDate";
  private String _counterparty;
  public static final String COUNTERPARTY_KEY = "counterparty";
  private com.opengamma.financial.security.swap.SwapLeg _payLeg;
  public static final String PAY_LEG_KEY = "payLeg";
  private com.opengamma.financial.security.swap.SwapLeg _receiveLeg;
  public static final String RECEIVE_LEG_KEY = "receiveLeg";
  public static final String SECURITY_TYPE = "SWAP";
  public SwapSecurity (javax.time.calendar.ZonedDateTime tradeDate, javax.time.calendar.ZonedDateTime effectiveDate, javax.time.calendar.ZonedDateTime maturityDate, String counterparty, com.opengamma.financial.security.swap.SwapLeg payLeg, com.opengamma.financial.security.swap.SwapLeg receiveLeg) {
    super (SECURITY_TYPE);
    if (tradeDate == null) throw new NullPointerException ("'tradeDate' cannot be null");
    else {
      _tradeDate = tradeDate;
    }
    if (effectiveDate == null) throw new NullPointerException ("'effectiveDate' cannot be null");
    else {
      _effectiveDate = effectiveDate;
    }
    if (maturityDate == null) throw new NullPointerException ("'maturityDate' cannot be null");
    else {
      _maturityDate = maturityDate;
    }
    if (counterparty == null) throw new NullPointerException ("counterparty' cannot be null");
    _counterparty = counterparty;
    if (payLeg == null) throw new NullPointerException ("'payLeg' cannot be null");
    else {
      _payLeg = payLeg;
    }
    if (receiveLeg == null) throw new NullPointerException ("'receiveLeg' cannot be null");
    else {
      _receiveLeg = receiveLeg;
    }
  }
  protected SwapSecurity (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeContext, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (TRADE_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SwapSecurity - field 'tradeDate' is not present");
    try {
      _tradeDate = fudgeContext.fieldValueToObject (javax.time.calendar.ZonedDateTime.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SwapSecurity - field 'tradeDate' is not ZonedDateTime typedef", e);
    }
    fudgeField = fudgeMsg.getByName (EFFECTIVE_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SwapSecurity - field 'effectiveDate' is not present");
    try {
      _effectiveDate = fudgeContext.fieldValueToObject (javax.time.calendar.ZonedDateTime.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SwapSecurity - field 'effectiveDate' is not ZonedDateTime typedef", e);
    }
    fudgeField = fudgeMsg.getByName (MATURITY_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SwapSecurity - field 'maturityDate' is not present");
    try {
      _maturityDate = fudgeContext.fieldValueToObject (javax.time.calendar.ZonedDateTime.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SwapSecurity - field 'maturityDate' is not ZonedDateTime typedef", e);
    }
    fudgeField = fudgeMsg.getByName (COUNTERPARTY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SwapSecurity - field 'counterparty' is not present");
    try {
      _counterparty = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SwapSecurity - field 'counterparty' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (PAY_LEG_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SwapSecurity - field 'payLeg' is not present");
    try {
      _payLeg = com.opengamma.financial.security.swap.SwapLeg.fromFudgeMsg (fudgeContext, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SwapSecurity - field 'payLeg' is not SwapLeg message", e);
    }
    fudgeField = fudgeMsg.getByName (RECEIVE_LEG_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SwapSecurity - field 'receiveLeg' is not present");
    try {
      _receiveLeg = com.opengamma.financial.security.swap.SwapLeg.fromFudgeMsg (fudgeContext, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SwapSecurity - field 'receiveLeg' is not SwapLeg message", e);
    }
  }
  public SwapSecurity (com.opengamma.id.UniqueId uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, javax.time.calendar.ZonedDateTime tradeDate, javax.time.calendar.ZonedDateTime effectiveDate, javax.time.calendar.ZonedDateTime maturityDate, String counterparty, com.opengamma.financial.security.swap.SwapLeg payLeg, com.opengamma.financial.security.swap.SwapLeg receiveLeg) {
    super (uniqueId, name, securityType, identifiers);
    if (tradeDate == null) throw new NullPointerException ("'tradeDate' cannot be null");
    else {
      _tradeDate = tradeDate;
    }
    if (effectiveDate == null) throw new NullPointerException ("'effectiveDate' cannot be null");
    else {
      _effectiveDate = effectiveDate;
    }
    if (maturityDate == null) throw new NullPointerException ("'maturityDate' cannot be null");
    else {
      _maturityDate = maturityDate;
    }
    if (counterparty == null) throw new NullPointerException ("counterparty' cannot be null");
    _counterparty = counterparty;
    if (payLeg == null) throw new NullPointerException ("'payLeg' cannot be null");
    else {
      _payLeg = payLeg;
    }
    if (receiveLeg == null) throw new NullPointerException ("'receiveLeg' cannot be null");
    else {
      _receiveLeg = receiveLeg;
    }
  }
  protected SwapSecurity (final SwapSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._tradeDate == null) _tradeDate = null;
    else {
      _tradeDate = source._tradeDate;
    }
    if (source._effectiveDate == null) _effectiveDate = null;
    else {
      _effectiveDate = source._effectiveDate;
    }
    if (source._maturityDate == null) _maturityDate = null;
    else {
      _maturityDate = source._maturityDate;
    }
    _counterparty = source._counterparty;
    if (source._payLeg == null) _payLeg = null;
    else {
      _payLeg = source._payLeg;
    }
    if (source._receiveLeg == null) _receiveLeg = null;
    else {
      _receiveLeg = source._receiveLeg;
    }
  }
  public SwapSecurity clone () {
    return new SwapSecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_tradeDate != null)  {
      fudgeContext.addToMessage (msg, TRADE_DATE_KEY, null, _tradeDate);
    }
    if (_effectiveDate != null)  {
      fudgeContext.addToMessage (msg, EFFECTIVE_DATE_KEY, null, _effectiveDate);
    }
    if (_maturityDate != null)  {
      fudgeContext.addToMessage (msg, MATURITY_DATE_KEY, null, _maturityDate);
    }
    if (_counterparty != null)  {
      msg.add (COUNTERPARTY_KEY, null, _counterparty);
    }
    if (_payLeg != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _payLeg.getClass (), com.opengamma.financial.security.swap.SwapLeg.class);
      _payLeg.toFudgeMsg (fudgeContext, fudge1);
      msg.add (PAY_LEG_KEY, null, fudge1);
    }
    if (_receiveLeg != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _receiveLeg.getClass (), com.opengamma.financial.security.swap.SwapLeg.class);
      _receiveLeg.toFudgeMsg (fudgeContext, fudge1);
      msg.add (RECEIVE_LEG_KEY, null, fudge1);
    }
  }
  public static SwapSecurity fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.swap.SwapSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.swap.SwapSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeMsg.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new SwapSecurity (fudgeContext, fudgeMsg);
  }
  public javax.time.calendar.ZonedDateTime getTradeDate () {
    return _tradeDate;
  }
  public void setTradeDate (javax.time.calendar.ZonedDateTime tradeDate) {
    if (tradeDate == null) throw new NullPointerException ("'tradeDate' cannot be null");
    else {
      _tradeDate = tradeDate;
    }
  }
  public javax.time.calendar.ZonedDateTime getEffectiveDate () {
    return _effectiveDate;
  }
  public void setEffectiveDate (javax.time.calendar.ZonedDateTime effectiveDate) {
    if (effectiveDate == null) throw new NullPointerException ("'effectiveDate' cannot be null");
    else {
      _effectiveDate = effectiveDate;
    }
  }
  public javax.time.calendar.ZonedDateTime getMaturityDate () {
    return _maturityDate;
  }
  public void setMaturityDate (javax.time.calendar.ZonedDateTime maturityDate) {
    if (maturityDate == null) throw new NullPointerException ("'maturityDate' cannot be null");
    else {
      _maturityDate = maturityDate;
    }
  }
  public String getCounterparty () {
    return _counterparty;
  }
  public void setCounterparty (String counterparty) {
    if (counterparty == null) throw new NullPointerException ("counterparty' cannot be null");
    _counterparty = counterparty;
  }
  public com.opengamma.financial.security.swap.SwapLeg getPayLeg () {
    return _payLeg;
  }
  public void setPayLeg (com.opengamma.financial.security.swap.SwapLeg payLeg) {
    if (payLeg == null) throw new NullPointerException ("'payLeg' cannot be null");
    else {
      _payLeg = payLeg;
    }
  }
  public com.opengamma.financial.security.swap.SwapLeg getReceiveLeg () {
    return _receiveLeg;
  }
  public void setReceiveLeg (com.opengamma.financial.security.swap.SwapLeg receiveLeg) {
    if (receiveLeg == null) throw new NullPointerException ("'receiveLeg' cannot be null");
    else {
      _receiveLeg = receiveLeg;
    }
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof SwapSecurity)) return false;
    SwapSecurity msg = (SwapSecurity)o;
    if (_tradeDate != null) {
      if (msg._tradeDate != null) {
        if (!_tradeDate.equals (msg._tradeDate)) return false;
      }
      else return false;
    }
    else if (msg._tradeDate != null) return false;
    if (_effectiveDate != null) {
      if (msg._effectiveDate != null) {
        if (!_effectiveDate.equals (msg._effectiveDate)) return false;
      }
      else return false;
    }
    else if (msg._effectiveDate != null) return false;
    if (_maturityDate != null) {
      if (msg._maturityDate != null) {
        if (!_maturityDate.equals (msg._maturityDate)) return false;
      }
      else return false;
    }
    else if (msg._maturityDate != null) return false;
    if (_counterparty != null) {
      if (msg._counterparty != null) {
        if (!_counterparty.equals (msg._counterparty)) return false;
      }
      else return false;
    }
    else if (msg._counterparty != null) return false;
    if (_payLeg != null) {
      if (msg._payLeg != null) {
        if (!_payLeg.equals (msg._payLeg)) return false;
      }
      else return false;
    }
    else if (msg._payLeg != null) return false;
    if (_receiveLeg != null) {
      if (msg._receiveLeg != null) {
        if (!_receiveLeg.equals (msg._receiveLeg)) return false;
      }
      else return false;
    }
    else if (msg._receiveLeg != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_tradeDate != null) hc += _tradeDate.hashCode ();
    hc *= 31;
    if (_effectiveDate != null) hc += _effectiveDate.hashCode ();
    hc *= 31;
    if (_maturityDate != null) hc += _maturityDate.hashCode ();
    hc *= 31;
    if (_counterparty != null) hc += _counterparty.hashCode ();
    hc *= 31;
    if (_payLeg != null) hc += _payLeg.hashCode ();
    hc *= 31;
    if (_receiveLeg != null) hc += _receiveLeg.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
