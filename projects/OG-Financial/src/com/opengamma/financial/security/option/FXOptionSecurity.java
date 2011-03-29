// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.option;
public class FXOptionSecurity extends com.opengamma.financial.security.option.OptionSecurity implements java.io.Serializable {
  public <T> T accept (OptionSecurityVisitor<T> visitor) { return visitor.visitFXOptionSecurity (this); }
  private static final long serialVersionUID = 2136571652531828490l;
  private String _counterparty;
  public static final String COUNTERPARTY_KEY = "counterparty";
  private com.opengamma.util.money.Currency _putCurrency;
  public static final String PUT_CURRENCY_KEY = "putCurrency";
  private com.opengamma.util.money.Currency _callCurrency;
  public static final String CALL_CURRENCY_KEY = "callCurrency";
  public static final String SECURITY_TYPE = "FX_OPTION";
  public FXOptionSecurity (com.opengamma.financial.security.option.ExerciseType exerciseType, com.opengamma.financial.security.option.PayoffStyle payoffStyle, com.opengamma.financial.security.option.OptionType optionType, double strike, com.opengamma.util.time.Expiry expiry, com.opengamma.id.Identifier underlyingIdentifier, com.opengamma.util.money.Currency currency, String counterparty, com.opengamma.util.money.Currency putCurrency, com.opengamma.util.money.Currency callCurrency) {
    super (SECURITY_TYPE, exerciseType, payoffStyle, optionType, strike, expiry, underlyingIdentifier, currency);
    if (counterparty == null) throw new NullPointerException ("counterparty' cannot be null");
    _counterparty = counterparty;
    if (putCurrency == null) throw new NullPointerException ("putCurrency' cannot be null");
    _putCurrency = putCurrency;
    if (callCurrency == null) throw new NullPointerException ("callCurrency' cannot be null");
    _callCurrency = callCurrency;
  }
  protected FXOptionSecurity (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (COUNTERPARTY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXOptionSecurity - field 'counterparty' is not present");
    try {
      _counterparty = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXOptionSecurity - field 'counterparty' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (PUT_CURRENCY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXOptionSecurity - field 'putCurrency' is not present");
    try {
      _putCurrency = fudgeMsg.getFieldValue (com.opengamma.util.money.Currency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXOptionSecurity - field 'putCurrency' is not Currency typedef", e);
    }
    fudgeField = fudgeMsg.getByName (CALL_CURRENCY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXOptionSecurity - field 'callCurrency' is not present");
    try {
      _callCurrency = fudgeMsg.getFieldValue (com.opengamma.util.money.Currency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXOptionSecurity - field 'callCurrency' is not Currency typedef", e);
    }
  }
  public FXOptionSecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, com.opengamma.financial.security.option.ExerciseType exerciseType, com.opengamma.financial.security.option.PayoffStyle payoffStyle, com.opengamma.financial.security.option.OptionType optionType, double strike, com.opengamma.util.time.Expiry expiry, com.opengamma.id.Identifier underlyingIdentifier, com.opengamma.util.money.Currency currency, String counterparty, com.opengamma.util.money.Currency putCurrency, com.opengamma.util.money.Currency callCurrency) {
    super (uniqueId, name, securityType, identifiers, exerciseType, payoffStyle, optionType, strike, expiry, underlyingIdentifier, currency);
    if (counterparty == null) throw new NullPointerException ("counterparty' cannot be null");
    _counterparty = counterparty;
    if (putCurrency == null) throw new NullPointerException ("putCurrency' cannot be null");
    _putCurrency = putCurrency;
    if (callCurrency == null) throw new NullPointerException ("callCurrency' cannot be null");
    _callCurrency = callCurrency;
  }
  protected FXOptionSecurity (final FXOptionSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _counterparty = source._counterparty;
    _putCurrency = source._putCurrency;
    _callCurrency = source._callCurrency;
  }
  public FXOptionSecurity clone () {
    return new FXOptionSecurity (this);
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_counterparty != null)  {
      msg.add (COUNTERPARTY_KEY, null, _counterparty);
    }
    if (_putCurrency != null)  {
      msg.add (PUT_CURRENCY_KEY, null, _putCurrency);
    }
    if (_callCurrency != null)  {
      msg.add (CALL_CURRENCY_KEY, null, _callCurrency);
    }
  }
  public static FXOptionSecurity fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.option.FXOptionSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.option.FXOptionSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new FXOptionSecurity (fudgeMsg);
  }
  public String getCounterparty () {
    return _counterparty;
  }
  public void setCounterparty (String counterparty) {
    if (counterparty == null) throw new NullPointerException ("counterparty' cannot be null");
    _counterparty = counterparty;
  }
  public com.opengamma.util.money.Currency getPutCurrency () {
    return _putCurrency;
  }
  public void setPutCurrency (com.opengamma.util.money.Currency putCurrency) {
    if (putCurrency == null) throw new NullPointerException ("putCurrency' cannot be null");
    _putCurrency = putCurrency;
  }
  public com.opengamma.util.money.Currency getCallCurrency () {
    return _callCurrency;
  }
  public void setCallCurrency (com.opengamma.util.money.Currency callCurrency) {
    if (callCurrency == null) throw new NullPointerException ("callCurrency' cannot be null");
    _callCurrency = callCurrency;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof FXOptionSecurity)) return false;
    FXOptionSecurity msg = (FXOptionSecurity)o;
    if (_counterparty != null) {
      if (msg._counterparty != null) {
        if (!_counterparty.equals (msg._counterparty)) return false;
      }
      else return false;
    }
    else if (msg._counterparty != null) return false;
    if (_putCurrency != null) {
      if (msg._putCurrency != null) {
        if (!_putCurrency.equals (msg._putCurrency)) return false;
      }
      else return false;
    }
    else if (msg._putCurrency != null) return false;
    if (_callCurrency != null) {
      if (msg._callCurrency != null) {
        if (!_callCurrency.equals (msg._callCurrency)) return false;
      }
      else return false;
    }
    else if (msg._callCurrency != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_counterparty != null) hc += _counterparty.hashCode ();
    hc *= 31;
    if (_putCurrency != null) hc += _putCurrency.hashCode ();
    hc *= 31;
    if (_callCurrency != null) hc += _callCurrency.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
