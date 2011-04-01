// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.cash;
public class CashSecurity extends com.opengamma.financial.security.FinancialSecurity implements java.io.Serializable {
          public <T> T accept(CashSecurityVisitor<T> visitor) { return visitor.visitCashSecurity(this); }
        public final <T> T accept(com.opengamma.financial.security.FinancialSecurityVisitor<T> visitor) { return visitor.visitCashSecurity(this); }
  private static final long serialVersionUID = 8769249180404561583l;
  private com.opengamma.util.money.Currency _currency;
  public static final String CURRENCY_KEY = "currency";
  private com.opengamma.id.Identifier _region;
  public static final String REGION_KEY = "region";
  private com.opengamma.financial.security.DateTimeWithZone _maturity;
  public static final String MATURITY_KEY = "maturity";
  private double _rate;
  public static final String RATE_KEY = "rate";
  private double _amount;
  public static final String AMOUNT_KEY = "amount";
  public static final String SECURITY_TYPE = "Cash";
  public CashSecurity (com.opengamma.util.money.Currency currency, com.opengamma.id.Identifier region, com.opengamma.financial.security.DateTimeWithZone maturity, double rate, double amount) {
    super (SECURITY_TYPE);
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
    if (region == null) throw new NullPointerException ("'region' cannot be null");
    else {
      _region = region;
    }
    if (maturity == null) throw new NullPointerException ("'maturity' cannot be null");
    else {
      _maturity = (com.opengamma.financial.security.DateTimeWithZone)maturity.clone ();
    }
    _rate = rate;
    _amount = amount;
  }
  protected CashSecurity (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (CURRENCY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CashSecurity - field 'currency' is not present");
    try {
      _currency = fudgeMsg.getFieldValue (com.opengamma.util.money.Currency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CashSecurity - field 'currency' is not Currency typedef", e);
    }
    fudgeField = fudgeMsg.getByName (REGION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CashSecurity - field 'region' is not present");
    try {
      _region = com.opengamma.id.Identifier.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CashSecurity - field 'region' is not Identifier message", e);
    }
    fudgeField = fudgeMsg.getByName (MATURITY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CashSecurity - field 'maturity' is not present");
    try {
      _maturity = com.opengamma.financial.security.DateTimeWithZone.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CashSecurity - field 'maturity' is not DateTimeWithZone message", e);
    }
    fudgeField = fudgeMsg.getByName (RATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CashSecurity - field 'rate' is not present");
    try {
      _rate = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CashSecurity - field 'rate' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (AMOUNT_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CashSecurity - field 'amount' is not present");
    try {
      _amount = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CashSecurity - field 'amount' is not double", e);
    }
  }
  public CashSecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, com.opengamma.util.money.Currency currency, com.opengamma.id.Identifier region, com.opengamma.financial.security.DateTimeWithZone maturity, double rate, double amount) {
    super (uniqueId, name, securityType, identifiers);
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
    if (region == null) throw new NullPointerException ("'region' cannot be null");
    else {
      _region = region;
    }
    if (maturity == null) throw new NullPointerException ("'maturity' cannot be null");
    else {
      _maturity = (com.opengamma.financial.security.DateTimeWithZone)maturity.clone ();
    }
    _rate = rate;
    _amount = amount;
  }
  protected CashSecurity (final CashSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _currency = source._currency;
    if (source._region == null) _region = null;
    else {
      _region = source._region;
    }
    if (source._maturity == null) _maturity = null;
    else {
      _maturity = (com.opengamma.financial.security.DateTimeWithZone)source._maturity.clone ();
    }
    _rate = source._rate;
    _amount = source._amount;
  }
  public CashSecurity clone () {
    return new CashSecurity (this);
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_currency != null)  {
      msg.add (CURRENCY_KEY, null, _currency);
    }
    if (_region != null)  {
      final org.fudgemsg.MutableFudgeFieldContainer fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _region.getClass (), com.opengamma.id.Identifier.class);
      _region.toFudgeMsg (fudgeContext, fudge1);
      msg.add (REGION_KEY, null, fudge1);
    }
    if (_maturity != null)  {
      final org.fudgemsg.MutableFudgeFieldContainer fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _maturity.getClass (), com.opengamma.financial.security.DateTimeWithZone.class);
      _maturity.toFudgeMsg (fudgeContext, fudge1);
      msg.add (MATURITY_KEY, null, fudge1);
    }
    msg.add (RATE_KEY, null, _rate);
    msg.add (AMOUNT_KEY, null, _amount);
  }
  public static CashSecurity fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.cash.CashSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.cash.CashSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new CashSecurity (fudgeMsg);
  }
  public com.opengamma.util.money.Currency getCurrency () {
    return _currency;
  }
  public void setCurrency (com.opengamma.util.money.Currency currency) {
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
  }
  public com.opengamma.id.Identifier getRegion () {
    return _region;
  }
  public void setRegion (com.opengamma.id.Identifier region) {
    if (region == null) throw new NullPointerException ("'region' cannot be null");
    else {
      _region = region;
    }
  }
  public com.opengamma.financial.security.DateTimeWithZone getMaturity () {
    return _maturity;
  }
  public void setMaturity (com.opengamma.financial.security.DateTimeWithZone maturity) {
    if (maturity == null) throw new NullPointerException ("'maturity' cannot be null");
    else {
      _maturity = (com.opengamma.financial.security.DateTimeWithZone)maturity.clone ();
    }
  }
  public double getRate () {
    return _rate;
  }
  public void setRate (double rate) {
    _rate = rate;
  }
  public double getAmount () {
    return _amount;
  }
  public void setAmount (double amount) {
    _amount = amount;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof CashSecurity)) return false;
    CashSecurity msg = (CashSecurity)o;
    if (_currency != null) {
      if (msg._currency != null) {
        if (!_currency.equals (msg._currency)) return false;
      }
      else return false;
    }
    else if (msg._currency != null) return false;
    if (_region != null) {
      if (msg._region != null) {
        if (!_region.equals (msg._region)) return false;
      }
      else return false;
    }
    else if (msg._region != null) return false;
    if (_maturity != null) {
      if (msg._maturity != null) {
        if (!_maturity.equals (msg._maturity)) return false;
      }
      else return false;
    }
    else if (msg._maturity != null) return false;
    if (_rate != msg._rate) return false;
    if (_amount != msg._amount) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_currency != null) hc += _currency.hashCode ();
    hc *= 31;
    if (_region != null) hc += _region.hashCode ();
    hc *= 31;
    if (_maturity != null) hc += _maturity.hashCode ();
    hc = (hc * 31) + (int)_rate;
    hc = (hc * 31) + (int)_amount;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
