// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.option;
public class EquityIndexOptionSecurity extends com.opengamma.financial.security.FinancialSecurity implements java.io.Serializable {
          
        public <T> T accept(EquityIndexOptionSecurityVisitor<T> visitor) { return visitor.visitEquityIndexOptionSecurity(this); }
        public final <T> T accept(com.opengamma.financial.security.FinancialSecurityVisitor<T> visitor) { return visitor.visitEquityIndexOptionSecurity(this); }
  private static final long serialVersionUID = 3443342415334005848l;
  private com.opengamma.financial.security.option.OptionType _optionType;
  public static final String OPTION_TYPE_KEY = "optionType";
  private double _strike;
  public static final String STRIKE_KEY = "strike";
  private com.opengamma.util.money.Currency _currency;
  public static final String CURRENCY_KEY = "currency";
  private com.opengamma.id.Identifier _underlyingIdentifier;
  public static final String UNDERLYING_IDENTIFIER_KEY = "underlyingIdentifier";
  private com.opengamma.financial.security.option.ExerciseType _exerciseType;
  public static final String EXERCISE_TYPE_KEY = "exerciseType";
  private com.opengamma.util.time.Expiry _expiry;
  public static final String EXPIRY_KEY = "expiry";
  private double _pointValue;
  public static final String POINT_VALUE_KEY = "pointValue";
  private String _exchange;
  public static final String EXCHANGE_KEY = "exchange";
  public static final String SECURITY_TYPE = "EQUITY_INDEX_OPTION";
  public EquityIndexOptionSecurity (com.opengamma.financial.security.option.OptionType optionType, double strike, com.opengamma.util.money.Currency currency, com.opengamma.id.Identifier underlyingIdentifier, com.opengamma.financial.security.option.ExerciseType exerciseType, com.opengamma.util.time.Expiry expiry, double pointValue, String exchange) {
    super (SECURITY_TYPE);
    if (optionType == null) throw new NullPointerException ("optionType' cannot be null");
    _optionType = optionType;
    _strike = strike;
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
    if (underlyingIdentifier == null) throw new NullPointerException ("'underlyingIdentifier' cannot be null");
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
    if (exerciseType == null) throw new NullPointerException ("'exerciseType' cannot be null");
    else {
      _exerciseType = exerciseType;
    }
    if (expiry == null) throw new NullPointerException ("'expiry' cannot be null");
    else {
      _expiry = expiry;
    }
    _pointValue = pointValue;
    if (exchange == null) throw new NullPointerException ("exchange' cannot be null");
    _exchange = exchange;
  }
  protected EquityIndexOptionSecurity (final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (OPTION_TYPE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityIndexOptionSecurity - field 'optionType' is not present");
    try {
      _optionType = fudgeMsg.getFieldValue (com.opengamma.financial.security.option.OptionType.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityIndexOptionSecurity - field 'optionType' is not OptionType enum", e);
    }
    fudgeField = fudgeMsg.getByName (STRIKE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityIndexOptionSecurity - field 'strike' is not present");
    try {
      _strike = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityIndexOptionSecurity - field 'strike' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (CURRENCY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityIndexOptionSecurity - field 'currency' is not present");
    try {
      _currency = fudgeMsg.getFieldValue (com.opengamma.util.money.Currency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityIndexOptionSecurity - field 'currency' is not Currency typedef", e);
    }
    fudgeField = fudgeMsg.getByName (UNDERLYING_IDENTIFIER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityIndexOptionSecurity - field 'underlyingIdentifier' is not present");
    try {
      _underlyingIdentifier = com.opengamma.id.Identifier.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityIndexOptionSecurity - field 'underlyingIdentifier' is not Identifier message", e);
    }
    fudgeField = fudgeMsg.getByName (EXERCISE_TYPE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityIndexOptionSecurity - field 'exerciseType' is not present");
    try {
      _exerciseType = com.opengamma.financial.security.option.ExerciseType.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityIndexOptionSecurity - field 'exerciseType' is not ExerciseType message", e);
    }
    fudgeField = fudgeMsg.getByName (EXPIRY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityIndexOptionSecurity - field 'expiry' is not present");
    try {
      _expiry = com.opengamma.util.time.Expiry.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityIndexOptionSecurity - field 'expiry' is not Expiry message", e);
    }
    fudgeField = fudgeMsg.getByName (POINT_VALUE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityIndexOptionSecurity - field 'pointValue' is not present");
    try {
      _pointValue = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityIndexOptionSecurity - field 'pointValue' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (EXCHANGE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityIndexOptionSecurity - field 'exchange' is not present");
    try {
      _exchange = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityIndexOptionSecurity - field 'exchange' is not string", e);
    }
  }
  public EquityIndexOptionSecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, com.opengamma.financial.security.option.OptionType optionType, double strike, com.opengamma.util.money.Currency currency, com.opengamma.id.Identifier underlyingIdentifier, com.opengamma.financial.security.option.ExerciseType exerciseType, com.opengamma.util.time.Expiry expiry, double pointValue, String exchange) {
    super (uniqueId, name, securityType, identifiers);
    if (optionType == null) throw new NullPointerException ("optionType' cannot be null");
    _optionType = optionType;
    _strike = strike;
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
    if (underlyingIdentifier == null) throw new NullPointerException ("'underlyingIdentifier' cannot be null");
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
    if (exerciseType == null) throw new NullPointerException ("'exerciseType' cannot be null");
    else {
      _exerciseType = exerciseType;
    }
    if (expiry == null) throw new NullPointerException ("'expiry' cannot be null");
    else {
      _expiry = expiry;
    }
    _pointValue = pointValue;
    if (exchange == null) throw new NullPointerException ("exchange' cannot be null");
    _exchange = exchange;
  }
  protected EquityIndexOptionSecurity (final EquityIndexOptionSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _optionType = source._optionType;
    _strike = source._strike;
    _currency = source._currency;
    if (source._underlyingIdentifier == null) _underlyingIdentifier = null;
    else {
      _underlyingIdentifier = source._underlyingIdentifier;
    }
    if (source._exerciseType == null) _exerciseType = null;
    else {
      _exerciseType = source._exerciseType;
    }
    if (source._expiry == null) _expiry = null;
    else {
      _expiry = source._expiry;
    }
    _pointValue = source._pointValue;
    _exchange = source._exchange;
  }
  public EquityIndexOptionSecurity clone () {
    return new EquityIndexOptionSecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_optionType != null)  {
      msg.add (OPTION_TYPE_KEY, null, _optionType.name ());
    }
    msg.add (STRIKE_KEY, null, _strike);
    if (_currency != null)  {
      msg.add (CURRENCY_KEY, null, _currency);
    }
    if (_underlyingIdentifier != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _underlyingIdentifier.getClass (), com.opengamma.id.Identifier.class);
      _underlyingIdentifier.toFudgeMsg (fudgeContext, fudge1);
      msg.add (UNDERLYING_IDENTIFIER_KEY, null, fudge1);
    }
    if (_exerciseType != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _exerciseType.getClass (), com.opengamma.financial.security.option.ExerciseType.class);
      _exerciseType.toFudgeMsg (fudgeContext, fudge1);
      msg.add (EXERCISE_TYPE_KEY, null, fudge1);
    }
    if (_expiry != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _expiry.getClass (), com.opengamma.util.time.Expiry.class);
      _expiry.toFudgeMsg (fudgeContext, fudge1);
      msg.add (EXPIRY_KEY, null, fudge1);
    }
    msg.add (POINT_VALUE_KEY, null, _pointValue);
    if (_exchange != null)  {
      msg.add (EXCHANGE_KEY, null, _exchange);
    }
  }
  public static EquityIndexOptionSecurity fromFudgeMsg (final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.option.EquityIndexOptionSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.option.EquityIndexOptionSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeMsg.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new EquityIndexOptionSecurity (fudgeMsg);
  }
  public com.opengamma.financial.security.option.OptionType getOptionType () {
    return _optionType;
  }
  public void setOptionType (com.opengamma.financial.security.option.OptionType optionType) {
    if (optionType == null) throw new NullPointerException ("optionType' cannot be null");
    _optionType = optionType;
  }
  public double getStrike () {
    return _strike;
  }
  public void setStrike (double strike) {
    _strike = strike;
  }
  public com.opengamma.util.money.Currency getCurrency () {
    return _currency;
  }
  public void setCurrency (com.opengamma.util.money.Currency currency) {
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
  }
  public com.opengamma.id.Identifier getUnderlyingIdentifier () {
    return _underlyingIdentifier;
  }
  public void setUnderlyingIdentifier (com.opengamma.id.Identifier underlyingIdentifier) {
    if (underlyingIdentifier == null) throw new NullPointerException ("'underlyingIdentifier' cannot be null");
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
  }
  public com.opengamma.financial.security.option.ExerciseType getExerciseType () {
    return _exerciseType;
  }
  public void setExerciseType (com.opengamma.financial.security.option.ExerciseType exerciseType) {
    if (exerciseType == null) throw new NullPointerException ("'exerciseType' cannot be null");
    else {
      _exerciseType = exerciseType;
    }
  }
  public com.opengamma.util.time.Expiry getExpiry () {
    return _expiry;
  }
  public void setExpiry (com.opengamma.util.time.Expiry expiry) {
    if (expiry == null) throw new NullPointerException ("'expiry' cannot be null");
    else {
      _expiry = expiry;
    }
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
    if (!(o instanceof EquityIndexOptionSecurity)) return false;
    EquityIndexOptionSecurity msg = (EquityIndexOptionSecurity)o;
    if (_optionType != null) {
      if (msg._optionType != null) {
        if (!_optionType.equals (msg._optionType)) return false;
      }
      else return false;
    }
    else if (msg._optionType != null) return false;
    if (_strike != msg._strike) return false;
    if (_currency != null) {
      if (msg._currency != null) {
        if (!_currency.equals (msg._currency)) return false;
      }
      else return false;
    }
    else if (msg._currency != null) return false;
    if (_underlyingIdentifier != null) {
      if (msg._underlyingIdentifier != null) {
        if (!_underlyingIdentifier.equals (msg._underlyingIdentifier)) return false;
      }
      else return false;
    }
    else if (msg._underlyingIdentifier != null) return false;
    if (_exerciseType != null) {
      if (msg._exerciseType != null) {
        if (!_exerciseType.equals (msg._exerciseType)) return false;
      }
      else return false;
    }
    else if (msg._exerciseType != null) return false;
    if (_expiry != null) {
      if (msg._expiry != null) {
        if (!_expiry.equals (msg._expiry)) return false;
      }
      else return false;
    }
    else if (msg._expiry != null) return false;
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
    hc *= 31;
    if (_optionType != null) hc += _optionType.hashCode ();
    hc = (hc * 31) + (int)_strike;
    hc *= 31;
    if (_currency != null) hc += _currency.hashCode ();
    hc *= 31;
    if (_underlyingIdentifier != null) hc += _underlyingIdentifier.hashCode ();
    hc *= 31;
    if (_exerciseType != null) hc += _exerciseType.hashCode ();
    hc *= 31;
    if (_expiry != null) hc += _expiry.hashCode ();
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
