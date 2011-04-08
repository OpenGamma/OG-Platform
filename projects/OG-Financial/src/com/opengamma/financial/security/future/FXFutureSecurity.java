// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
// Created from com/opengamma/financial/security/future/FXFutureSecurity.proto:12(10)
package com.opengamma.financial.security.future;
public class FXFutureSecurity extends com.opengamma.financial.security.future.FutureSecurity implements java.io.Serializable {
  public <T> T accept (FutureSecurityVisitor<T> visitor) { return visitor.visitFXFutureSecurity (this); }
  private static final long serialVersionUID = 48781265047866760l;
  private com.opengamma.util.money.Currency _numerator;
  public static final String NUMERATOR_KEY = "numerator";
  private com.opengamma.util.money.Currency _denominator;
  public static final String DENOMINATOR_KEY = "denominator";
  private double _multiplicationFactor;
  public static final String MULTIPLICATION_FACTOR_KEY = "multiplicationFactor";
  public static final double MULTIPLICATION_FACTOR = 1.0;
  public FXFutureSecurity (com.opengamma.util.time.Expiry expiry, String tradingExchange, String settlementExchange, com.opengamma.util.money.Currency currency, com.opengamma.util.money.Currency numerator, com.opengamma.util.money.Currency denominator) {
    super (expiry, tradingExchange, settlementExchange, currency);
    if (numerator == null) throw new NullPointerException ("numerator' cannot be null");
    _numerator = numerator;
    if (denominator == null) throw new NullPointerException ("denominator' cannot be null");
    _denominator = denominator;
    setMultiplicationFactor (MULTIPLICATION_FACTOR);
  }
  protected FXFutureSecurity (final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (NUMERATOR_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXFutureSecurity - field 'numerator' is not present");
    try {
      _numerator = fudgeMsg.getFieldValue (com.opengamma.util.money.Currency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXFutureSecurity - field 'numerator' is not Currency typedef", e);
    }
    fudgeField = fudgeMsg.getByName (DENOMINATOR_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXFutureSecurity - field 'denominator' is not present");
    try {
      _denominator = fudgeMsg.getFieldValue (com.opengamma.util.money.Currency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXFutureSecurity - field 'denominator' is not Currency typedef", e);
    }
    fudgeField = fudgeMsg.getByName (MULTIPLICATION_FACTOR_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXFutureSecurity - field 'multiplicationFactor' is not present");
    try {
      _multiplicationFactor = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXFutureSecurity - field 'multiplicationFactor' is not double", e);
    }
  }
  public FXFutureSecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, com.opengamma.util.time.Expiry expiry, String tradingExchange, String settlementExchange, com.opengamma.util.money.Currency currency, com.opengamma.util.money.Currency numerator, com.opengamma.util.money.Currency denominator, double multiplicationFactor) {
    super (uniqueId, name, securityType, identifiers, expiry, tradingExchange, settlementExchange, currency);
    if (numerator == null) throw new NullPointerException ("numerator' cannot be null");
    _numerator = numerator;
    if (denominator == null) throw new NullPointerException ("denominator' cannot be null");
    _denominator = denominator;
    _multiplicationFactor = multiplicationFactor;
  }
  protected FXFutureSecurity (final FXFutureSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _numerator = source._numerator;
    _denominator = source._denominator;
    _multiplicationFactor = source._multiplicationFactor;
  }
  public FXFutureSecurity clone () {
    return new FXFutureSecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_numerator != null)  {
      msg.add (NUMERATOR_KEY, null, _numerator);
    }
    if (_denominator != null)  {
      msg.add (DENOMINATOR_KEY, null, _denominator);
    }
    msg.add (MULTIPLICATION_FACTOR_KEY, null, _multiplicationFactor);
  }
  public static FXFutureSecurity fromFudgeMsg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.future.FXFutureSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.future.FXFutureSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeMsg.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new FXFutureSecurity (fudgeMsg);
  }
  public com.opengamma.util.money.Currency getNumerator () {
    return _numerator;
  }
  public void setNumerator (com.opengamma.util.money.Currency numerator) {
    if (numerator == null) throw new NullPointerException ("numerator' cannot be null");
    _numerator = numerator;
  }
  public com.opengamma.util.money.Currency getDenominator () {
    return _denominator;
  }
  public void setDenominator (com.opengamma.util.money.Currency denominator) {
    if (denominator == null) throw new NullPointerException ("denominator' cannot be null");
    _denominator = denominator;
  }
  public double getMultiplicationFactor () {
    return _multiplicationFactor;
  }
  public void setMultiplicationFactor (double multiplicationFactor) {
    _multiplicationFactor = multiplicationFactor;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof FXFutureSecurity)) return false;
    FXFutureSecurity msg = (FXFutureSecurity)o;
    if (_numerator != null) {
      if (msg._numerator != null) {
        if (!_numerator.equals (msg._numerator)) return false;
      }
      else return false;
    }
    else if (msg._numerator != null) return false;
    if (_denominator != null) {
      if (msg._denominator != null) {
        if (!_denominator.equals (msg._denominator)) return false;
      }
      else return false;
    }
    else if (msg._denominator != null) return false;
    if (_multiplicationFactor != msg._multiplicationFactor) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_numerator != null) hc += _numerator.hashCode ();
    hc *= 31;
    if (_denominator != null) hc += _denominator.hashCode ();
    hc = (hc * 31) + (int)_multiplicationFactor;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
