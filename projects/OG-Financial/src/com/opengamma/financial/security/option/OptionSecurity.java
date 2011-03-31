// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.option;
public abstract class OptionSecurity extends com.opengamma.financial.security.FinancialSecurity implements java.io.Serializable {
          public abstract <T> T accept (OptionSecurityVisitor<T> visitor);
        
        public <T> T accept (com.opengamma.financial.security.FinancialSecurityVisitor<T> visitor) {
          return visitor.visitOptionSecurity (this);
        }
  private static final long serialVersionUID = 7515036772700726741l;
  private com.opengamma.financial.security.option.ExerciseType _exerciseType;
  public static final String EXERCISE_TYPE_KEY = "exerciseType";
  private com.opengamma.financial.security.option.PayoffStyle _payoffStyle;
  public static final String PAYOFF_STYLE_KEY = "payoffStyle";
  private com.opengamma.financial.security.option.OptionType _optionType;
  public static final String OPTION_TYPE_KEY = "optionType";
  private double _strike;
  public static final String STRIKE_KEY = "strike";
  private com.opengamma.util.time.Expiry _expiry;
  public static final String EXPIRY_KEY = "expiry";
  private com.opengamma.id.Identifier _underlyingIdentifier;
  public static final String UNDERLYING_IDENTIFIER_KEY = "underlyingIdentifier";
  private com.opengamma.util.money.Currency _currency;
  public static final String CURRENCY_KEY = "currency";
  public OptionSecurity (String securityType, com.opengamma.financial.security.option.ExerciseType exerciseType, com.opengamma.financial.security.option.PayoffStyle payoffStyle, com.opengamma.financial.security.option.OptionType optionType, double strike, com.opengamma.util.time.Expiry expiry, com.opengamma.id.Identifier underlyingIdentifier, com.opengamma.util.money.Currency currency) {
    super (securityType);
    if (exerciseType == null) throw new NullPointerException ("'exerciseType' cannot be null");
    else {
      _exerciseType = exerciseType;
    }
    if (payoffStyle == null) throw new NullPointerException ("'payoffStyle' cannot be null");
    else {
      _payoffStyle = payoffStyle;
    }
    if (optionType == null) throw new NullPointerException ("optionType' cannot be null");
    _optionType = optionType;
    _strike = strike;
    if (expiry == null) throw new NullPointerException ("'expiry' cannot be null");
    else {
      _expiry = expiry;
    }
    if (underlyingIdentifier == null) throw new NullPointerException ("'underlyingIdentifier' cannot be null");
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
  }
  protected OptionSecurity (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (EXERCISE_TYPE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a OptionSecurity - field 'exerciseType' is not present");
    try {
      _exerciseType = com.opengamma.financial.security.option.ExerciseType.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a OptionSecurity - field 'exerciseType' is not ExerciseType message", e);
    }
    fudgeField = fudgeMsg.getByName (PAYOFF_STYLE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a OptionSecurity - field 'payoffStyle' is not present");
    try {
      _payoffStyle = com.opengamma.financial.security.option.PayoffStyle.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a OptionSecurity - field 'payoffStyle' is not PayoffStyle message", e);
    }
    fudgeField = fudgeMsg.getByName (OPTION_TYPE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a OptionSecurity - field 'optionType' is not present");
    try {
      _optionType = fudgeMsg.getFieldValue (com.opengamma.financial.security.option.OptionType.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a OptionSecurity - field 'optionType' is not OptionType enum", e);
    }
    fudgeField = fudgeMsg.getByName (STRIKE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a OptionSecurity - field 'strike' is not present");
    try {
      _strike = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a OptionSecurity - field 'strike' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (EXPIRY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a OptionSecurity - field 'expiry' is not present");
    try {
      _expiry = com.opengamma.util.time.Expiry.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a OptionSecurity - field 'expiry' is not Expiry message", e);
    }
    fudgeField = fudgeMsg.getByName (UNDERLYING_IDENTIFIER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a OptionSecurity - field 'underlyingIdentifier' is not present");
    try {
      _underlyingIdentifier = com.opengamma.id.Identifier.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a OptionSecurity - field 'underlyingIdentifier' is not Identifier message", e);
    }
    fudgeField = fudgeMsg.getByName (CURRENCY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a OptionSecurity - field 'currency' is not present");
    try {
      _currency = fudgeMsg.getFieldValue (com.opengamma.util.money.Currency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a OptionSecurity - field 'currency' is not Currency typedef", e);
    }
  }
  public OptionSecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, com.opengamma.financial.security.option.ExerciseType exerciseType, com.opengamma.financial.security.option.PayoffStyle payoffStyle, com.opengamma.financial.security.option.OptionType optionType, double strike, com.opengamma.util.time.Expiry expiry, com.opengamma.id.Identifier underlyingIdentifier, com.opengamma.util.money.Currency currency) {
    super (uniqueId, name, securityType, identifiers);
    if (exerciseType == null) throw new NullPointerException ("'exerciseType' cannot be null");
    else {
      _exerciseType = exerciseType;
    }
    if (payoffStyle == null) throw new NullPointerException ("'payoffStyle' cannot be null");
    else {
      _payoffStyle = payoffStyle;
    }
    if (optionType == null) throw new NullPointerException ("optionType' cannot be null");
    _optionType = optionType;
    _strike = strike;
    if (expiry == null) throw new NullPointerException ("'expiry' cannot be null");
    else {
      _expiry = expiry;
    }
    if (underlyingIdentifier == null) throw new NullPointerException ("'underlyingIdentifier' cannot be null");
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
  }
  protected OptionSecurity (final OptionSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._exerciseType == null) _exerciseType = null;
    else {
      _exerciseType = source._exerciseType;
    }
    if (source._payoffStyle == null) _payoffStyle = null;
    else {
      _payoffStyle = source._payoffStyle;
    }
    _optionType = source._optionType;
    _strike = source._strike;
    if (source._expiry == null) _expiry = null;
    else {
      _expiry = source._expiry;
    }
    if (source._underlyingIdentifier == null) _underlyingIdentifier = null;
    else {
      _underlyingIdentifier = source._underlyingIdentifier;
    }
    _currency = source._currency;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_exerciseType != null)  {
      final org.fudgemsg.MutableFudgeFieldContainer fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _exerciseType.getClass (), com.opengamma.financial.security.option.ExerciseType.class);
      _exerciseType.toFudgeMsg (fudgeContext, fudge1);
      msg.add (EXERCISE_TYPE_KEY, null, fudge1);
    }
    if (_payoffStyle != null)  {
      final org.fudgemsg.MutableFudgeFieldContainer fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _payoffStyle.getClass (), com.opengamma.financial.security.option.PayoffStyle.class);
      _payoffStyle.toFudgeMsg (fudgeContext, fudge1);
      msg.add (PAYOFF_STYLE_KEY, null, fudge1);
    }
    if (_optionType != null)  {
      msg.add (OPTION_TYPE_KEY, null, _optionType.name ());
    }
    msg.add (STRIKE_KEY, null, _strike);
    if (_expiry != null)  {
      final org.fudgemsg.MutableFudgeFieldContainer fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _expiry.getClass (), com.opengamma.util.time.Expiry.class);
      _expiry.toFudgeMsg (fudgeContext, fudge1);
      msg.add (EXPIRY_KEY, null, fudge1);
    }
    if (_underlyingIdentifier != null)  {
      final org.fudgemsg.MutableFudgeFieldContainer fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _underlyingIdentifier.getClass (), com.opengamma.id.Identifier.class);
      _underlyingIdentifier.toFudgeMsg (fudgeContext, fudge1);
      msg.add (UNDERLYING_IDENTIFIER_KEY, null, fudge1);
    }
    if (_currency != null)  {
      msg.add (CURRENCY_KEY, null, _currency);
    }
  }
  public static OptionSecurity fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.option.OptionSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.option.OptionSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    throw new UnsupportedOperationException ("OptionSecurity is an abstract message");
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
  public com.opengamma.financial.security.option.PayoffStyle getPayoffStyle () {
    return _payoffStyle;
  }
  public void setPayoffStyle (com.opengamma.financial.security.option.PayoffStyle payoffStyle) {
    if (payoffStyle == null) throw new NullPointerException ("'payoffStyle' cannot be null");
    else {
      _payoffStyle = payoffStyle;
    }
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
  public com.opengamma.util.time.Expiry getExpiry () {
    return _expiry;
  }
  public void setExpiry (com.opengamma.util.time.Expiry expiry) {
    if (expiry == null) throw new NullPointerException ("'expiry' cannot be null");
    else {
      _expiry = expiry;
    }
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
  public com.opengamma.util.money.Currency getCurrency () {
    return _currency;
  }
  public void setCurrency (com.opengamma.util.money.Currency currency) {
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof OptionSecurity)) return false;
    OptionSecurity msg = (OptionSecurity)o;
    if (_exerciseType != null) {
      if (msg._exerciseType != null) {
        if (!_exerciseType.equals (msg._exerciseType)) return false;
      }
      else return false;
    }
    else if (msg._exerciseType != null) return false;
    if (_payoffStyle != null) {
      if (msg._payoffStyle != null) {
        if (!_payoffStyle.equals (msg._payoffStyle)) return false;
      }
      else return false;
    }
    else if (msg._payoffStyle != null) return false;
    if (_optionType != null) {
      if (msg._optionType != null) {
        if (!_optionType.equals (msg._optionType)) return false;
      }
      else return false;
    }
    else if (msg._optionType != null) return false;
    if (_strike != msg._strike) return false;
    if (_expiry != null) {
      if (msg._expiry != null) {
        if (!_expiry.equals (msg._expiry)) return false;
      }
      else return false;
    }
    else if (msg._expiry != null) return false;
    if (_underlyingIdentifier != null) {
      if (msg._underlyingIdentifier != null) {
        if (!_underlyingIdentifier.equals (msg._underlyingIdentifier)) return false;
      }
      else return false;
    }
    else if (msg._underlyingIdentifier != null) return false;
    if (_currency != null) {
      if (msg._currency != null) {
        if (!_currency.equals (msg._currency)) return false;
      }
      else return false;
    }
    else if (msg._currency != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_exerciseType != null) hc += _exerciseType.hashCode ();
    hc *= 31;
    if (_payoffStyle != null) hc += _payoffStyle.hashCode ();
    hc *= 31;
    if (_optionType != null) hc += _optionType.hashCode ();
    hc = (hc * 31) + (int)_strike;
    hc *= 31;
    if (_expiry != null) hc += _expiry.hashCode ();
    hc *= 31;
    if (_underlyingIdentifier != null) hc += _underlyingIdentifier.hashCode ();
    hc *= 31;
    if (_currency != null) hc += _currency.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
