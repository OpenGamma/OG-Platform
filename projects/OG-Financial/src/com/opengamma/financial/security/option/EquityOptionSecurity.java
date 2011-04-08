// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
// Created from com/opengamma/financial/security/option/EquityOptionSecurity.proto:9(10)
package com.opengamma.financial.security.option;
public class EquityOptionSecurity extends com.opengamma.financial.security.option.OptionSecurity implements java.io.Serializable {
  public <T> T accept (OptionSecurityVisitor<T> visitor) { return visitor.visitEquityOptionSecurity (this); }
  private static final long serialVersionUID = 21505804163113270l;
  private double _pointValue;
  public static final String POINT_VALUE_KEY = "pointValue";
  private String _exchange;
  public static final String EXCHANGE_KEY = "exchange";
  public static final String SECURITY_TYPE = "EQUITY_OPTION";
  public EquityOptionSecurity (com.opengamma.financial.security.option.ExerciseType exerciseType, com.opengamma.financial.security.option.PayoffStyle payoffStyle, com.opengamma.financial.security.option.OptionType optionType, double strike, com.opengamma.util.time.Expiry expiry, com.opengamma.id.Identifier underlyingIdentifier, com.opengamma.util.money.Currency currency, double pointValue, String exchange) {
    super (SECURITY_TYPE, exerciseType, payoffStyle, optionType, strike, expiry, underlyingIdentifier, currency);
    _pointValue = pointValue;
    if (exchange == null) throw new NullPointerException ("exchange' cannot be null");
    _exchange = exchange;
  }
  protected EquityOptionSecurity (final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (POINT_VALUE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityOptionSecurity - field 'pointValue' is not present");
    try {
      _pointValue = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityOptionSecurity - field 'pointValue' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (EXCHANGE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityOptionSecurity - field 'exchange' is not present");
    try {
      _exchange = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityOptionSecurity - field 'exchange' is not string", e);
    }
  }
  public EquityOptionSecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, com.opengamma.financial.security.option.ExerciseType exerciseType, com.opengamma.financial.security.option.PayoffStyle payoffStyle, com.opengamma.financial.security.option.OptionType optionType, double strike, com.opengamma.util.time.Expiry expiry, com.opengamma.id.Identifier underlyingIdentifier, com.opengamma.util.money.Currency currency, double pointValue, String exchange) {
    super (uniqueId, name, securityType, identifiers, exerciseType, payoffStyle, optionType, strike, expiry, underlyingIdentifier, currency);
    _pointValue = pointValue;
    if (exchange == null) throw new NullPointerException ("exchange' cannot be null");
    _exchange = exchange;
  }
  protected EquityOptionSecurity (final EquityOptionSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _pointValue = source._pointValue;
    _exchange = source._exchange;
  }
  public EquityOptionSecurity clone () {
    return new EquityOptionSecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    msg.add (POINT_VALUE_KEY, null, _pointValue);
    if (_exchange != null)  {
      msg.add (EXCHANGE_KEY, null, _exchange);
    }
  }
  public static EquityOptionSecurity fromFudgeMsg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.option.EquityOptionSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.option.EquityOptionSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeMsg.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new EquityOptionSecurity (fudgeMsg);
  }
  public double getPointValue () {
    return _pointValue;
  }
  public void setPointValue (double pointValue) {
    _pointValue = pointValue;
  }
  public String getExchange () {
    return _exchange;
  }
  public void setExchange (String exchange) {
    if (exchange == null) throw new NullPointerException ("exchange' cannot be null");
    _exchange = exchange;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof EquityOptionSecurity)) return false;
    EquityOptionSecurity msg = (EquityOptionSecurity)o;
    if (_pointValue != msg._pointValue) return false;
    if (_exchange != null) {
      if (msg._exchange != null) {
        if (!_exchange.equals (msg._exchange)) return false;
      }
      else return false;
    }
    else if (msg._exchange != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc = (hc * 31) + (int)_pointValue;
    hc *= 31;
    if (_exchange != null) hc += _exchange.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
