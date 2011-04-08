// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
// Created from com/opengamma/financial/security/equity/EquitySecurity.proto:12(10)
package com.opengamma.financial.security.equity;
public class EquitySecurity extends com.opengamma.financial.security.FinancialSecurity implements java.io.Serializable {
          /**
         * Override to use the company name as the display name.
         * @return the display name, not null
         */
        @Override
        protected String buildDefaultDisplayName() {
          if (getCompanyName() != null) {
            return getCompanyName();
          } else {
            return super.buildDefaultDisplayName();
          }
        }
        
        public <T> T accept(EquitySecurityVisitor<T> visitor) { return visitor.visitEquitySecurity(this); }
        public final <T> T accept(com.opengamma.financial.security.FinancialSecurityVisitor<T> visitor) { return visitor.visitEquitySecurity(this); }
  private static final long serialVersionUID = 6816777187225274132l;
  private String _shortName;
  public static final String SHORT_NAME_KEY = "shortName";
  private String _exchange;
  public static final String EXCHANGE_KEY = "exchange";
  private String _exchangeCode;
  public static final String EXCHANGE_CODE_KEY = "exchangeCode";
  private String _companyName;
  public static final String COMPANY_NAME_KEY = "companyName";
  private com.opengamma.util.money.Currency _currency;
  public static final String CURRENCY_KEY = "currency";
  private com.opengamma.financial.security.equity.GICSCode _gicsCode;
  public static final String GICS_CODE_KEY = "gicsCode";
  public static final String SECURITY_TYPE = "EQUITY";
  public EquitySecurity (String exchange, String exchangeCode, String companyName, com.opengamma.util.money.Currency currency) {
    super (SECURITY_TYPE);
    if (exchange == null) throw new NullPointerException ("exchange' cannot be null");
    _exchange = exchange;
    if (exchangeCode == null) throw new NullPointerException ("exchangeCode' cannot be null");
    _exchangeCode = exchangeCode;
    if (companyName == null) throw new NullPointerException ("companyName' cannot be null");
    _companyName = companyName;
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
  }
  protected EquitySecurity (final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (EXCHANGE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquitySecurity - field 'exchange' is not present");
    try {
      _exchange = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquitySecurity - field 'exchange' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (EXCHANGE_CODE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquitySecurity - field 'exchangeCode' is not present");
    try {
      _exchangeCode = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquitySecurity - field 'exchangeCode' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (COMPANY_NAME_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquitySecurity - field 'companyName' is not present");
    try {
      _companyName = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquitySecurity - field 'companyName' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (CURRENCY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquitySecurity - field 'currency' is not present");
    try {
      _currency = fudgeMsg.getFieldValue (com.opengamma.util.money.Currency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquitySecurity - field 'currency' is not Currency typedef", e);
    }
    fudgeField = fudgeMsg.getByName (SHORT_NAME_KEY);
    if (fudgeField != null)  {
      try {
        setShortName ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a EquitySecurity - field 'shortName' is not string", e);
      }
    }
    fudgeField = fudgeMsg.getByName (GICS_CODE_KEY);
    if (fudgeField != null)  {
      try {
        setGicsCode (fudgeMsg.getFieldValue (com.opengamma.financial.security.equity.GICSCode.class, fudgeField));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a EquitySecurity - field 'gicsCode' is not GICSCode typedef", e);
      }
    }
  }
  public EquitySecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, String shortName, String exchange, String exchangeCode, String companyName, com.opengamma.util.money.Currency currency, com.opengamma.financial.security.equity.GICSCode gicsCode) {
    super (uniqueId, name, securityType, identifiers);
    _shortName = shortName;
    if (exchange == null) throw new NullPointerException ("exchange' cannot be null");
    _exchange = exchange;
    if (exchangeCode == null) throw new NullPointerException ("exchangeCode' cannot be null");
    _exchangeCode = exchangeCode;
    if (companyName == null) throw new NullPointerException ("companyName' cannot be null");
    _companyName = companyName;
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
    _gicsCode = gicsCode;
  }
  protected EquitySecurity (final EquitySecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _shortName = source._shortName;
    _exchange = source._exchange;
    _exchangeCode = source._exchangeCode;
    _companyName = source._companyName;
    _currency = source._currency;
    _gicsCode = source._gicsCode;
  }
  public EquitySecurity clone () {
    return new EquitySecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_shortName != null)  {
      msg.add (SHORT_NAME_KEY, null, _shortName);
    }
    if (_exchange != null)  {
      msg.add (EXCHANGE_KEY, null, _exchange);
    }
    if (_exchangeCode != null)  {
      msg.add (EXCHANGE_CODE_KEY, null, _exchangeCode);
    }
    if (_companyName != null)  {
      msg.add (COMPANY_NAME_KEY, null, _companyName);
    }
    if (_currency != null)  {
      msg.add (CURRENCY_KEY, null, _currency);
    }
    if (_gicsCode != null)  {
      msg.add (GICS_CODE_KEY, null, _gicsCode);
    }
  }
  public static EquitySecurity fromFudgeMsg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.equity.EquitySecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.equity.EquitySecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeMsg.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new EquitySecurity (fudgeMsg);
  }
  public String getShortName () {
    return _shortName;
  }
  public void setShortName (String shortName) {
    _shortName = shortName;
  }
  public String getExchange () {
    return _exchange;
  }
  public void setExchange (String exchange) {
    if (exchange == null) throw new NullPointerException ("exchange' cannot be null");
    _exchange = exchange;
  }
  public String getExchangeCode () {
    return _exchangeCode;
  }
  public void setExchangeCode (String exchangeCode) {
    if (exchangeCode == null) throw new NullPointerException ("exchangeCode' cannot be null");
    _exchangeCode = exchangeCode;
  }
  public String getCompanyName () {
    return _companyName;
  }
  public void setCompanyName (String companyName) {
    if (companyName == null) throw new NullPointerException ("companyName' cannot be null");
    _companyName = companyName;
  }
  public com.opengamma.util.money.Currency getCurrency () {
    return _currency;
  }
  public void setCurrency (com.opengamma.util.money.Currency currency) {
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
  }
  public com.opengamma.financial.security.equity.GICSCode getGicsCode () {
    return _gicsCode;
  }
  public void setGicsCode (com.opengamma.financial.security.equity.GICSCode gicsCode) {
    _gicsCode = gicsCode;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof EquitySecurity)) return false;
    EquitySecurity msg = (EquitySecurity)o;
    if (_shortName != null) {
      if (msg._shortName != null) {
        if (!_shortName.equals (msg._shortName)) return false;
      }
      else return false;
    }
    else if (msg._shortName != null) return false;
    if (_exchange != null) {
      if (msg._exchange != null) {
        if (!_exchange.equals (msg._exchange)) return false;
      }
      else return false;
    }
    else if (msg._exchange != null) return false;
    if (_exchangeCode != null) {
      if (msg._exchangeCode != null) {
        if (!_exchangeCode.equals (msg._exchangeCode)) return false;
      }
      else return false;
    }
    else if (msg._exchangeCode != null) return false;
    if (_companyName != null) {
      if (msg._companyName != null) {
        if (!_companyName.equals (msg._companyName)) return false;
      }
      else return false;
    }
    else if (msg._companyName != null) return false;
    if (_currency != null) {
      if (msg._currency != null) {
        if (!_currency.equals (msg._currency)) return false;
      }
      else return false;
    }
    else if (msg._currency != null) return false;
    if (_gicsCode != null) {
      if (msg._gicsCode != null) {
        if (!_gicsCode.equals (msg._gicsCode)) return false;
      }
      else return false;
    }
    else if (msg._gicsCode != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_shortName != null) hc += _shortName.hashCode ();
    hc *= 31;
    if (_exchange != null) hc += _exchange.hashCode ();
    hc *= 31;
    if (_exchangeCode != null) hc += _exchangeCode.hashCode ();
    hc *= 31;
    if (_companyName != null) hc += _companyName.hashCode ();
    hc *= 31;
    if (_currency != null) hc += _currency.hashCode ();
    hc *= 31;
    if (_gicsCode != null) hc += _gicsCode.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
