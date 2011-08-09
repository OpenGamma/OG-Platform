// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.option;
public class IRFutureOptionSecurity extends com.opengamma.financial.security.FinancialSecurity implements java.io.Serializable {
          public <T> T accept (IRFutureOptionSecurityVisitor<T> visitor) { return visitor.visitIRFutureOptionSecurity(this); }
        public final <T> T accept(com.opengamma.financial.security.FinancialSecurityVisitor<T> visitor) { return visitor.visitIRFutureOptionSecurity(this); }
  private static final long serialVersionUID = 4260132724852642790l;
  private String _exchange;
  public static final String EXCHANGE_KEY = "exchange";
  private com.opengamma.util.time.Expiry _expiry;
  public static final String EXPIRY_KEY = "expiry";
  private com.opengamma.financial.security.option.ExerciseType _exerciseType;
  public static final String EXERCISE_TYPE_KEY = "exerciseType";
  private com.opengamma.id.ExternalId _underlyingIdentifier;
  public static final String UNDERLYING_IDENTIFIER_KEY = "underlyingIdentifier";
  private double _pointValue;
  public static final String POINT_VALUE_KEY = "pointValue";
  private boolean _isMargined;
  public static final String IS_MARGINED_KEY = "isMargined";
  private com.opengamma.util.money.Currency _currency;
  public static final String CURRENCY_KEY = "currency";
  private double _strike;
  public static final String STRIKE_KEY = "strike";
  private com.opengamma.financial.security.option.OptionType _optionType;
  public static final String OPTION_TYPE_KEY = "optionType";
  public static final String SECURITY_TYPE = "IRFUTURE_OPTION";
  public IRFutureOptionSecurity (String exchange, com.opengamma.util.time.Expiry expiry, com.opengamma.financial.security.option.ExerciseType exerciseType, com.opengamma.id.ExternalId underlyingIdentifier, double pointValue, boolean isMargined, com.opengamma.util.money.Currency currency, double strike, com.opengamma.financial.security.option.OptionType optionType) {
    super (SECURITY_TYPE);
    if (exchange == null) throw new NullPointerException ("exchange' cannot be null");
    _exchange = exchange;
    if (expiry == null) throw new NullPointerException ("'expiry' cannot be null");
    else {
      _expiry = expiry;
    }
    if (exerciseType == null) throw new NullPointerException ("'exerciseType' cannot be null");
    else {
      _exerciseType = exerciseType;
    }
    if (underlyingIdentifier == null) throw new NullPointerException ("'underlyingIdentifier' cannot be null");
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
    _pointValue = pointValue;
    _isMargined = isMargined;
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
    _strike = strike;
    if (optionType == null) throw new NullPointerException ("optionType' cannot be null");
    _optionType = optionType;
  }
  protected IRFutureOptionSecurity (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (EXCHANGE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a IRFutureOptionSecurity - field 'exchange' is not present");
    try {
      _exchange = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a IRFutureOptionSecurity - field 'exchange' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (EXPIRY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a IRFutureOptionSecurity - field 'expiry' is not present");
    try {
      _expiry = com.opengamma.util.time.Expiry.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a IRFutureOptionSecurity - field 'expiry' is not Expiry message", e);
    }
    fudgeField = fudgeMsg.getByName (EXERCISE_TYPE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a IRFutureOptionSecurity - field 'exerciseType' is not present");
    try {
      _exerciseType = com.opengamma.financial.security.option.ExerciseType.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a IRFutureOptionSecurity - field 'exerciseType' is not ExerciseType message", e);
    }
    fudgeField = fudgeMsg.getByName (UNDERLYING_IDENTIFIER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a IRFutureOptionSecurity - field 'underlyingIdentifier' is not present");
    try {
      _underlyingIdentifier = com.opengamma.id.ExternalId.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a IRFutureOptionSecurity - field 'underlyingIdentifier' is not ExternalId message", e);
    }
    fudgeField = fudgeMsg.getByName (POINT_VALUE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a IRFutureOptionSecurity - field 'pointValue' is not present");
    try {
      _pointValue = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a IRFutureOptionSecurity - field 'pointValue' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (IS_MARGINED_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a IRFutureOptionSecurity - field 'isMargined' is not present");
    try {
      _isMargined = fudgeMsg.getFieldValue (Boolean.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a IRFutureOptionSecurity - field 'isMargined' is not boolean", e);
    }
    fudgeField = fudgeMsg.getByName (CURRENCY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a IRFutureOptionSecurity - field 'currency' is not present");
    try {
      _currency = fudgeMsg.getFieldValue (com.opengamma.util.money.Currency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a IRFutureOptionSecurity - field 'currency' is not Currency typedef", e);
    }
    fudgeField = fudgeMsg.getByName (STRIKE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a IRFutureOptionSecurity - field 'strike' is not present");
    try {
      _strike = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a IRFutureOptionSecurity - field 'strike' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (OPTION_TYPE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a IRFutureOptionSecurity - field 'optionType' is not present");
    try {
      _optionType = fudgeMsg.getFieldValue (com.opengamma.financial.security.option.OptionType.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a IRFutureOptionSecurity - field 'optionType' is not OptionType enum", e);
    }
  }
  public IRFutureOptionSecurity (com.opengamma.id.UniqueId uniqueId, String name, String securityType, com.opengamma.id.ExternalIdBundle identifiers, String exchange, com.opengamma.util.time.Expiry expiry, com.opengamma.financial.security.option.ExerciseType exerciseType, com.opengamma.id.ExternalId underlyingIdentifier, double pointValue, boolean isMargined, com.opengamma.util.money.Currency currency, double strike, com.opengamma.financial.security.option.OptionType optionType) {
    super (uniqueId, name, securityType, identifiers);
    if (exchange == null) throw new NullPointerException ("exchange' cannot be null");
    _exchange = exchange;
    if (expiry == null) throw new NullPointerException ("'expiry' cannot be null");
    else {
      _expiry = expiry;
    }
    if (exerciseType == null) throw new NullPointerException ("'exerciseType' cannot be null");
    else {
      _exerciseType = exerciseType;
    }
    if (underlyingIdentifier == null) throw new NullPointerException ("'underlyingIdentifier' cannot be null");
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
    _pointValue = pointValue;
    _isMargined = isMargined;
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
    _strike = strike;
    if (optionType == null) throw new NullPointerException ("optionType' cannot be null");
    _optionType = optionType;
  }
  protected IRFutureOptionSecurity (final IRFutureOptionSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _exchange = source._exchange;
    if (source._expiry == null) _expiry = null;
    else {
      _expiry = source._expiry;
    }
    if (source._exerciseType == null) _exerciseType = null;
    else {
      _exerciseType = source._exerciseType;
    }
    if (source._underlyingIdentifier == null) _underlyingIdentifier = null;
    else {
      _underlyingIdentifier = source._underlyingIdentifier;
    }
    _pointValue = source._pointValue;
    _isMargined = source._isMargined;
    _currency = source._currency;
    _strike = source._strike;
    _optionType = source._optionType;
  }
  public IRFutureOptionSecurity clone () {
    return new IRFutureOptionSecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_exchange != null)  {
      msg.add (EXCHANGE_KEY, null, _exchange);
    }
    if (_expiry != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _expiry.getClass (), com.opengamma.util.time.Expiry.class);
      _expiry.toFudgeMsg (serializer, fudge1);
      msg.add (EXPIRY_KEY, null, fudge1);
    }
    if (_exerciseType != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _exerciseType.getClass (), com.opengamma.financial.security.option.ExerciseType.class);
      _exerciseType.toFudgeMsg (serializer, fudge1);
      msg.add (EXERCISE_TYPE_KEY, null, fudge1);
    }
    if (_underlyingIdentifier != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _underlyingIdentifier.getClass (), com.opengamma.id.ExternalId.class);
      _underlyingIdentifier.toFudgeMsg (serializer, fudge1);
      msg.add (UNDERLYING_IDENTIFIER_KEY, null, fudge1);
    }
    msg.add (POINT_VALUE_KEY, null, _pointValue);
    msg.add (IS_MARGINED_KEY, null, _isMargined);
    if (_currency != null)  {
      msg.add (CURRENCY_KEY, null, _currency);
    }
    msg.add (STRIKE_KEY, null, _strike);
    if (_optionType != null)  {
      msg.add (OPTION_TYPE_KEY, null, _optionType.name ());
    }
  }
  public static IRFutureOptionSecurity fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.option.IRFutureOptionSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.option.IRFutureOptionSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new IRFutureOptionSecurity (deserializer, fudgeMsg);
  }
  public String getExchange () {
    return _exchange;
  }
  public void setExchange (String exchange) {
    if (exchange == null) throw new NullPointerException ("exchange' cannot be null");
    _exchange = exchange;
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
  public com.opengamma.financial.security.option.ExerciseType getExerciseType () {
    return _exerciseType;
  }
  public void setExerciseType (com.opengamma.financial.security.option.ExerciseType exerciseType) {
    if (exerciseType == null) throw new NullPointerException ("'exerciseType' cannot be null");
    else {
      _exerciseType = exerciseType;
    }
  }
  public com.opengamma.id.ExternalId getUnderlyingIdentifier () {
    return _underlyingIdentifier;
  }
  public void setUnderlyingIdentifier (com.opengamma.id.ExternalId underlyingIdentifier) {
    if (underlyingIdentifier == null) throw new NullPointerException ("'underlyingIdentifier' cannot be null");
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
  }
  public double getPointValue () {
    return _pointValue;
  }
  public void setPointValue (double pointValue) {
    _pointValue = pointValue;
  }
  public boolean getIsMargined () {
    return _isMargined;
  }
  public void setIsMargined (boolean isMargined) {
    _isMargined = isMargined;
  }
  public com.opengamma.util.money.Currency getCurrency () {
    return _currency;
  }
  public void setCurrency (com.opengamma.util.money.Currency currency) {
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
  }
  public double getStrike () {
    return _strike;
  }
  public void setStrike (double strike) {
    _strike = strike;
  }
  public com.opengamma.financial.security.option.OptionType getOptionType () {
    return _optionType;
  }
  public void setOptionType (com.opengamma.financial.security.option.OptionType optionType) {
    if (optionType == null) throw new NullPointerException ("optionType' cannot be null");
    _optionType = optionType;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof IRFutureOptionSecurity)) return false;
    IRFutureOptionSecurity msg = (IRFutureOptionSecurity)o;
    if (_exchange != null) {
      if (msg._exchange != null) {
        if (!_exchange.equals (msg._exchange)) return false;
      }
      else return false;
    }
    else if (msg._exchange != null) return false;
    if (_expiry != null) {
      if (msg._expiry != null) {
        if (!_expiry.equals (msg._expiry)) return false;
      }
      else return false;
    }
    else if (msg._expiry != null) return false;
    if (_exerciseType != null) {
      if (msg._exerciseType != null) {
        if (!_exerciseType.equals (msg._exerciseType)) return false;
      }
      else return false;
    }
    else if (msg._exerciseType != null) return false;
    if (_underlyingIdentifier != null) {
      if (msg._underlyingIdentifier != null) {
        if (!_underlyingIdentifier.equals (msg._underlyingIdentifier)) return false;
      }
      else return false;
    }
    else if (msg._underlyingIdentifier != null) return false;
    if (_pointValue != msg._pointValue) return false;
    if (_isMargined != msg._isMargined) return false;
    if (_currency != null) {
      if (msg._currency != null) {
        if (!_currency.equals (msg._currency)) return false;
      }
      else return false;
    }
    else if (msg._currency != null) return false;
    if (_strike != msg._strike) return false;
    if (_optionType != null) {
      if (msg._optionType != null) {
        if (!_optionType.equals (msg._optionType)) return false;
      }
      else return false;
    }
    else if (msg._optionType != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_exchange != null) hc += _exchange.hashCode ();
    hc *= 31;
    if (_expiry != null) hc += _expiry.hashCode ();
    hc *= 31;
    if (_exerciseType != null) hc += _exerciseType.hashCode ();
    hc *= 31;
    if (_underlyingIdentifier != null) hc += _underlyingIdentifier.hashCode ();
    hc = (hc * 31) + (int)_pointValue;
    hc *= 31;
    if (_isMargined) hc++;
    hc *= 31;
    if (_currency != null) hc += _currency.hashCode ();
    hc = (hc * 31) + (int)_strike;
    hc *= 31;
    if (_optionType != null) hc += _optionType.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
