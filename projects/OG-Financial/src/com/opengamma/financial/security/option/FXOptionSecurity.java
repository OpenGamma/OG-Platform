// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.option;
public class FXOptionSecurity extends com.opengamma.financial.security.FinancialSecurity implements java.io.Serializable {
          public <T> T accept (FXOptionSecurityVisitor<T> visitor) { return visitor.visitFXOptionSecurity(this); }
        public final <T> T accept(com.opengamma.financial.security.FinancialSecurityVisitor<T> visitor) { return visitor.visitFXOptionSecurity(this); }
  private static final long serialVersionUID = -7446774431750652014l;
  private com.opengamma.util.money.Currency _putCurrency;
  public static final String PUT_CURRENCY_KEY = "putCurrency";
  private com.opengamma.util.money.Currency _callCurrency;
  public static final String CALL_CURRENCY_KEY = "callCurrency";
  private double _putAmount;
  public static final String PUT_AMOUNT_KEY = "putAmount";
  private double _callAmount;
  public static final String CALL_AMOUNT_KEY = "callAmount";
  private com.opengamma.util.time.Expiry _expiry;
  public static final String EXPIRY_KEY = "expiry";
  private javax.time.calendar.ZonedDateTime _settlementDate;
  public static final String SETTLEMENT_DATE_KEY = "settlementDate";
  private boolean _isLong;
  public static final String IS_LONG_KEY = "isLong";
  public static final String SECURITY_TYPE = "FX_OPTION";
  public FXOptionSecurity (com.opengamma.util.money.Currency putCurrency, com.opengamma.util.money.Currency callCurrency, double putAmount, double callAmount, com.opengamma.util.time.Expiry expiry, javax.time.calendar.ZonedDateTime settlementDate, boolean isLong) {
    super (SECURITY_TYPE);
    if (putCurrency == null) throw new NullPointerException ("putCurrency' cannot be null");
    _putCurrency = putCurrency;
    if (callCurrency == null) throw new NullPointerException ("callCurrency' cannot be null");
    _callCurrency = callCurrency;
    _putAmount = putAmount;
    _callAmount = callAmount;
    if (expiry == null) throw new NullPointerException ("'expiry' cannot be null");
    else {
      _expiry = expiry;
    }
    if (settlementDate == null) throw new NullPointerException ("'settlementDate' cannot be null");
    else {
      _settlementDate = settlementDate;
    }
    _isLong = isLong;
  }
  protected FXOptionSecurity (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
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
    fudgeField = fudgeMsg.getByName (PUT_AMOUNT_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXOptionSecurity - field 'putAmount' is not present");
    try {
      _putAmount = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXOptionSecurity - field 'putAmount' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (CALL_AMOUNT_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXOptionSecurity - field 'callAmount' is not present");
    try {
      _callAmount = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXOptionSecurity - field 'callAmount' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (EXPIRY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXOptionSecurity - field 'expiry' is not present");
    try {
      _expiry = com.opengamma.util.time.Expiry.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXOptionSecurity - field 'expiry' is not Expiry message", e);
    }
    fudgeField = fudgeMsg.getByName (SETTLEMENT_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXOptionSecurity - field 'settlementDate' is not present");
    try {
      _settlementDate = deserializer.fieldValueToObject (javax.time.calendar.ZonedDateTime.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXOptionSecurity - field 'settlementDate' is not ZonedDateTime typedef", e);
    }
    fudgeField = fudgeMsg.getByName (IS_LONG_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXOptionSecurity - field 'isLong' is not present");
    try {
      _isLong = fudgeMsg.getFieldValue (Boolean.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXOptionSecurity - field 'isLong' is not boolean", e);
    }
  }
  public FXOptionSecurity (com.opengamma.id.UniqueId uniqueId, String name, String securityType, com.opengamma.id.ExternalIdBundle identifiers, com.opengamma.util.money.Currency putCurrency, com.opengamma.util.money.Currency callCurrency, double putAmount, double callAmount, com.opengamma.util.time.Expiry expiry, javax.time.calendar.ZonedDateTime settlementDate, boolean isLong) {
    super (uniqueId, name, securityType, identifiers);
    if (putCurrency == null) throw new NullPointerException ("putCurrency' cannot be null");
    _putCurrency = putCurrency;
    if (callCurrency == null) throw new NullPointerException ("callCurrency' cannot be null");
    _callCurrency = callCurrency;
    _putAmount = putAmount;
    _callAmount = callAmount;
    if (expiry == null) throw new NullPointerException ("'expiry' cannot be null");
    else {
      _expiry = expiry;
    }
    if (settlementDate == null) throw new NullPointerException ("'settlementDate' cannot be null");
    else {
      _settlementDate = settlementDate;
    }
    _isLong = isLong;
  }
  protected FXOptionSecurity (final FXOptionSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _putCurrency = source._putCurrency;
    _callCurrency = source._callCurrency;
    _putAmount = source._putAmount;
    _callAmount = source._callAmount;
    if (source._expiry == null) _expiry = null;
    else {
      _expiry = source._expiry;
    }
    if (source._settlementDate == null) _settlementDate = null;
    else {
      _settlementDate = source._settlementDate;
    }
    _isLong = source._isLong;
  }
  public FXOptionSecurity clone () {
    return new FXOptionSecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_putCurrency != null)  {
      msg.add (PUT_CURRENCY_KEY, null, _putCurrency);
    }
    if (_callCurrency != null)  {
      msg.add (CALL_CURRENCY_KEY, null, _callCurrency);
    }
    msg.add (PUT_AMOUNT_KEY, null, _putAmount);
    msg.add (CALL_AMOUNT_KEY, null, _callAmount);
    if (_expiry != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _expiry.getClass (), com.opengamma.util.time.Expiry.class);
      _expiry.toFudgeMsg (serializer, fudge1);
      msg.add (EXPIRY_KEY, null, fudge1);
    }
    if (_settlementDate != null)  {
      serializer.addToMessage (msg, SETTLEMENT_DATE_KEY, null, _settlementDate);
    }
    msg.add (IS_LONG_KEY, null, _isLong);
  }
  public static FXOptionSecurity fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.option.FXOptionSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.option.FXOptionSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new FXOptionSecurity (deserializer, fudgeMsg);
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
  public double getPutAmount () {
    return _putAmount;
  }
  public void setPutAmount (double putAmount) {
    _putAmount = putAmount;
  }
  public double getCallAmount () {
    return _callAmount;
  }
  public void setCallAmount (double callAmount) {
    _callAmount = callAmount;
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
  public javax.time.calendar.ZonedDateTime getSettlementDate () {
    return _settlementDate;
  }
  public void setSettlementDate (javax.time.calendar.ZonedDateTime settlementDate) {
    if (settlementDate == null) throw new NullPointerException ("'settlementDate' cannot be null");
    else {
      _settlementDate = settlementDate;
    }
  }
  public boolean getIsLong () {
    return _isLong;
  }
  public void setIsLong (boolean isLong) {
    _isLong = isLong;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof FXOptionSecurity)) return false;
    FXOptionSecurity msg = (FXOptionSecurity)o;
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
    if (_putAmount != msg._putAmount) return false;
    if (_callAmount != msg._callAmount) return false;
    if (_expiry != null) {
      if (msg._expiry != null) {
        if (!_expiry.equals (msg._expiry)) return false;
      }
      else return false;
    }
    else if (msg._expiry != null) return false;
    if (_settlementDate != null) {
      if (msg._settlementDate != null) {
        if (!_settlementDate.equals (msg._settlementDate)) return false;
      }
      else return false;
    }
    else if (msg._settlementDate != null) return false;
    if (_isLong != msg._isLong) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_putCurrency != null) hc += _putCurrency.hashCode ();
    hc *= 31;
    if (_callCurrency != null) hc += _callCurrency.hashCode ();
    hc = (hc * 31) + (int)_putAmount;
    hc = (hc * 31) + (int)_callAmount;
    hc *= 31;
    if (_expiry != null) hc += _expiry.hashCode ();
    hc *= 31;
    if (_settlementDate != null) hc += _settlementDate.hashCode ();
    hc *= 31;
    if (_isLong) hc++;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
