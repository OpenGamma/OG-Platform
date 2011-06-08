// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.option;
public class FutureOptionSecurity extends com.opengamma.financial.security.option.OptionSecurity implements java.io.Serializable {
  public <T> T accept (OptionSecurityVisitor<T> visitor) { return visitor.visitFutureOptionSecurity (this); }
  private static final long serialVersionUID = 2220333710060786855l;
  private double _pointValue;
  public static final String POINT_VALUE_KEY = "pointValue";
  private String _exchange;
  public static final String EXCHANGE_KEY = "exchange";
  private boolean _isMargined;
  public static final String IS_MARGINED_KEY = "isMargined";
  public static final String SECURITY_TYPE = "FUTURE_OPTION";
  public FutureOptionSecurity (com.opengamma.financial.security.option.ExerciseType exerciseType, com.opengamma.financial.security.option.PayoffStyle payoffStyle, com.opengamma.financial.security.option.OptionType optionType, double strike, com.opengamma.util.time.Expiry expiry, com.opengamma.id.Identifier underlyingIdentifier, com.opengamma.util.money.Currency currency, double pointValue, String exchange, boolean isMargined) {
    super (SECURITY_TYPE, exerciseType, payoffStyle, optionType, strike, expiry, underlyingIdentifier, currency);
    _pointValue = pointValue;
    if (exchange == null) throw new NullPointerException ("exchange' cannot be null");
    _exchange = exchange;
    _isMargined = isMargined;
  }
  protected FutureOptionSecurity (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeContext, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (POINT_VALUE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FutureOptionSecurity - field 'pointValue' is not present");
    try {
      _pointValue = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FutureOptionSecurity - field 'pointValue' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (EXCHANGE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FutureOptionSecurity - field 'exchange' is not present");
    try {
      _exchange = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FutureOptionSecurity - field 'exchange' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (IS_MARGINED_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FutureOptionSecurity - field 'isMargined' is not present");
    try {
      _isMargined = fudgeMsg.getFieldValue (Boolean.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FutureOptionSecurity - field 'isMargined' is not boolean", e);
    }
  }
  public FutureOptionSecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, com.opengamma.financial.security.option.ExerciseType exerciseType, com.opengamma.financial.security.option.PayoffStyle payoffStyle, com.opengamma.financial.security.option.OptionType optionType, double strike, com.opengamma.util.time.Expiry expiry, com.opengamma.id.Identifier underlyingIdentifier, com.opengamma.util.money.Currency currency, double pointValue, String exchange, boolean isMargined) {
    super (uniqueId, name, securityType, identifiers, exerciseType, payoffStyle, optionType, strike, expiry, underlyingIdentifier, currency);
    _pointValue = pointValue;
    if (exchange == null) throw new NullPointerException ("exchange' cannot be null");
    _exchange = exchange;
    _isMargined = isMargined;
  }
  protected FutureOptionSecurity (final FutureOptionSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _pointValue = source._pointValue;
    _exchange = source._exchange;
    _isMargined = source._isMargined;
  }
  public FutureOptionSecurity clone () {
    return new FutureOptionSecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    msg.add (POINT_VALUE_KEY, null, _pointValue);
    if (_exchange != null)  {
      msg.add (EXCHANGE_KEY, null, _exchange);
    }
    msg.add (IS_MARGINED_KEY, null, _isMargined);
  }
  public static FutureOptionSecurity fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.option.FutureOptionSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.option.FutureOptionSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeMsg.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new FutureOptionSecurity (fudgeContext, fudgeMsg);
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
  public boolean getIsMargined () {
    return _isMargined;
  }
  public void setIsMargined (boolean isMargined) {
    _isMargined = isMargined;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof FutureOptionSecurity)) return false;
    FutureOptionSecurity msg = (FutureOptionSecurity)o;
    if (_pointValue != msg._pointValue) return false;
    if (_exchange != null) {
      if (msg._exchange != null) {
        if (!_exchange.equals (msg._exchange)) return false;
      }
      else return false;
    }
    else if (msg._exchange != null) return false;
    if (_isMargined != msg._isMargined) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc = (hc * 31) + (int)_pointValue;
    hc *= 31;
    if (_exchange != null) hc += _exchange.hashCode ();
    hc *= 31;
    if (_isMargined) hc++;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
