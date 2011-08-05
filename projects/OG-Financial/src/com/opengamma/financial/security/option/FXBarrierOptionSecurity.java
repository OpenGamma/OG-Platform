// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.option;
public class FXBarrierOptionSecurity extends com.opengamma.financial.security.FinancialSecurity implements java.io.Serializable {
          public <T> T accept (FXBarrierOptionSecurityVisitor<T> visitor) { return visitor.visitFXBarrierOptionSecurity(this); }
        public final <T> T accept(com.opengamma.financial.security.FinancialSecurityVisitor<T> visitor) { return visitor.visitFXBarrierOptionSecurity(this); }
  private static final long serialVersionUID = 2361078214451615862l;
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
  private com.opengamma.financial.security.option.BarrierType _barrierType;
  public static final String BARRIER_TYPE_KEY = "barrierType";
  private com.opengamma.financial.security.option.BarrierDirection _barrierDirection;
  public static final String BARRIER_DIRECTION_KEY = "barrierDirection";
  private com.opengamma.financial.security.option.MonitoringType _monitoringType;
  public static final String MONITORING_TYPE_KEY = "monitoringType";
  private com.opengamma.financial.security.option.SamplingFrequency _samplingFrequency;
  public static final String SAMPLING_FREQUENCY_KEY = "samplingFrequency";
  private double _barrierLevel;
  public static final String BARRIER_LEVEL_KEY = "barrierLevel";
  private boolean _isLong;
  public static final String IS_LONG_KEY = "isLong";
  public static final String SECURITY_TYPE = "FX_BARRIER_OPTION";
  public FXBarrierOptionSecurity (com.opengamma.util.money.Currency putCurrency, com.opengamma.util.money.Currency callCurrency, double putAmount, double callAmount, com.opengamma.util.time.Expiry expiry, javax.time.calendar.ZonedDateTime settlementDate, com.opengamma.financial.security.option.BarrierType barrierType, com.opengamma.financial.security.option.BarrierDirection barrierDirection, com.opengamma.financial.security.option.MonitoringType monitoringType, com.opengamma.financial.security.option.SamplingFrequency samplingFrequency, double barrierLevel, boolean isLong) {
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
    if (barrierType == null) throw new NullPointerException ("barrierType' cannot be null");
    _barrierType = barrierType;
    if (barrierDirection == null) throw new NullPointerException ("barrierDirection' cannot be null");
    _barrierDirection = barrierDirection;
    if (monitoringType == null) throw new NullPointerException ("monitoringType' cannot be null");
    _monitoringType = monitoringType;
    if (samplingFrequency == null) throw new NullPointerException ("samplingFrequency' cannot be null");
    _samplingFrequency = samplingFrequency;
    _barrierLevel = barrierLevel;
    _isLong = isLong;
  }
  protected FXBarrierOptionSecurity (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeContext, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (PUT_CURRENCY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'putCurrency' is not present");
    try {
      _putCurrency = fudgeMsg.getFieldValue (com.opengamma.util.money.Currency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'putCurrency' is not Currency typedef", e);
    }
    fudgeField = fudgeMsg.getByName (CALL_CURRENCY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'callCurrency' is not present");
    try {
      _callCurrency = fudgeMsg.getFieldValue (com.opengamma.util.money.Currency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'callCurrency' is not Currency typedef", e);
    }
    fudgeField = fudgeMsg.getByName (PUT_AMOUNT_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'putAmount' is not present");
    try {
      _putAmount = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'putAmount' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (CALL_AMOUNT_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'callAmount' is not present");
    try {
      _callAmount = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'callAmount' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (EXPIRY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'expiry' is not present");
    try {
      _expiry = com.opengamma.util.time.Expiry.fromFudgeMsg (fudgeContext, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'expiry' is not Expiry message", e);
    }
    fudgeField = fudgeMsg.getByName (SETTLEMENT_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'settlementDate' is not present");
    try {
      _settlementDate = fudgeContext.fieldValueToObject (javax.time.calendar.ZonedDateTime.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'settlementDate' is not ZonedDateTime typedef", e);
    }
    fudgeField = fudgeMsg.getByName (BARRIER_TYPE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'barrierType' is not present");
    try {
      _barrierType = fudgeMsg.getFieldValue (com.opengamma.financial.security.option.BarrierType.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'barrierType' is not BarrierType enum", e);
    }
    fudgeField = fudgeMsg.getByName (BARRIER_DIRECTION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'barrierDirection' is not present");
    try {
      _barrierDirection = fudgeMsg.getFieldValue (com.opengamma.financial.security.option.BarrierDirection.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'barrierDirection' is not BarrierDirection enum", e);
    }
    fudgeField = fudgeMsg.getByName (MONITORING_TYPE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'monitoringType' is not present");
    try {
      _monitoringType = fudgeMsg.getFieldValue (com.opengamma.financial.security.option.MonitoringType.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'monitoringType' is not MonitoringType enum", e);
    }
    fudgeField = fudgeMsg.getByName (SAMPLING_FREQUENCY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'samplingFrequency' is not present");
    try {
      _samplingFrequency = fudgeMsg.getFieldValue (com.opengamma.financial.security.option.SamplingFrequency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'samplingFrequency' is not SamplingFrequency enum", e);
    }
    fudgeField = fudgeMsg.getByName (BARRIER_LEVEL_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'barrierLevel' is not present");
    try {
      _barrierLevel = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'barrierLevel' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (IS_LONG_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'isLong' is not present");
    try {
      _isLong = fudgeMsg.getFieldValue (Boolean.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FXBarrierOptionSecurity - field 'isLong' is not boolean", e);
    }
  }
  public FXBarrierOptionSecurity (com.opengamma.id.UniqueId uniqueId, String name, String securityType, com.opengamma.id.ExternalIdBundle identifiers, com.opengamma.util.money.Currency putCurrency, com.opengamma.util.money.Currency callCurrency, double putAmount, double callAmount, com.opengamma.util.time.Expiry expiry, javax.time.calendar.ZonedDateTime settlementDate, com.opengamma.financial.security.option.BarrierType barrierType, com.opengamma.financial.security.option.BarrierDirection barrierDirection, com.opengamma.financial.security.option.MonitoringType monitoringType, com.opengamma.financial.security.option.SamplingFrequency samplingFrequency, double barrierLevel, boolean isLong) {
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
    if (barrierType == null) throw new NullPointerException ("barrierType' cannot be null");
    _barrierType = barrierType;
    if (barrierDirection == null) throw new NullPointerException ("barrierDirection' cannot be null");
    _barrierDirection = barrierDirection;
    if (monitoringType == null) throw new NullPointerException ("monitoringType' cannot be null");
    _monitoringType = monitoringType;
    if (samplingFrequency == null) throw new NullPointerException ("samplingFrequency' cannot be null");
    _samplingFrequency = samplingFrequency;
    _barrierLevel = barrierLevel;
    _isLong = isLong;
  }
  protected FXBarrierOptionSecurity (final FXBarrierOptionSecurity source) {
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
    _barrierType = source._barrierType;
    _barrierDirection = source._barrierDirection;
    _monitoringType = source._monitoringType;
    _samplingFrequency = source._samplingFrequency;
    _barrierLevel = source._barrierLevel;
    _isLong = source._isLong;
  }
  public FXBarrierOptionSecurity clone () {
    return new FXBarrierOptionSecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_putCurrency != null)  {
      msg.add (PUT_CURRENCY_KEY, null, _putCurrency);
    }
    if (_callCurrency != null)  {
      msg.add (CALL_CURRENCY_KEY, null, _callCurrency);
    }
    msg.add (PUT_AMOUNT_KEY, null, _putAmount);
    msg.add (CALL_AMOUNT_KEY, null, _callAmount);
    if (_expiry != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _expiry.getClass (), com.opengamma.util.time.Expiry.class);
      _expiry.toFudgeMsg (fudgeContext, fudge1);
      msg.add (EXPIRY_KEY, null, fudge1);
    }
    if (_settlementDate != null)  {
      fudgeContext.addToMessage (msg, SETTLEMENT_DATE_KEY, null, _settlementDate);
    }
    if (_barrierType != null)  {
      msg.add (BARRIER_TYPE_KEY, null, _barrierType.name ());
    }
    if (_barrierDirection != null)  {
      msg.add (BARRIER_DIRECTION_KEY, null, _barrierDirection.name ());
    }
    if (_monitoringType != null)  {
      msg.add (MONITORING_TYPE_KEY, null, _monitoringType.name ());
    }
    if (_samplingFrequency != null)  {
      msg.add (SAMPLING_FREQUENCY_KEY, null, _samplingFrequency.name ());
    }
    msg.add (BARRIER_LEVEL_KEY, null, _barrierLevel);
    msg.add (IS_LONG_KEY, null, _isLong);
  }
  public static FXBarrierOptionSecurity fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.option.FXBarrierOptionSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.option.FXBarrierOptionSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeMsg.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new FXBarrierOptionSecurity (fudgeContext, fudgeMsg);
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
  public com.opengamma.financial.security.option.BarrierType getBarrierType () {
    return _barrierType;
  }
  public void setBarrierType (com.opengamma.financial.security.option.BarrierType barrierType) {
    if (barrierType == null) throw new NullPointerException ("barrierType' cannot be null");
    _barrierType = barrierType;
  }
  public com.opengamma.financial.security.option.BarrierDirection getBarrierDirection () {
    return _barrierDirection;
  }
  public void setBarrierDirection (com.opengamma.financial.security.option.BarrierDirection barrierDirection) {
    if (barrierDirection == null) throw new NullPointerException ("barrierDirection' cannot be null");
    _barrierDirection = barrierDirection;
  }
  public com.opengamma.financial.security.option.MonitoringType getMonitoringType () {
    return _monitoringType;
  }
  public void setMonitoringType (com.opengamma.financial.security.option.MonitoringType monitoringType) {
    if (monitoringType == null) throw new NullPointerException ("monitoringType' cannot be null");
    _monitoringType = monitoringType;
  }
  public com.opengamma.financial.security.option.SamplingFrequency getSamplingFrequency () {
    return _samplingFrequency;
  }
  public void setSamplingFrequency (com.opengamma.financial.security.option.SamplingFrequency samplingFrequency) {
    if (samplingFrequency == null) throw new NullPointerException ("samplingFrequency' cannot be null");
    _samplingFrequency = samplingFrequency;
  }
  public double getBarrierLevel () {
    return _barrierLevel;
  }
  public void setBarrierLevel (double barrierLevel) {
    _barrierLevel = barrierLevel;
  }
  public boolean getIsLong () {
    return _isLong;
  }
  public void setIsLong (boolean isLong) {
    _isLong = isLong;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof FXBarrierOptionSecurity)) return false;
    FXBarrierOptionSecurity msg = (FXBarrierOptionSecurity)o;
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
    if (_barrierType != null) {
      if (msg._barrierType != null) {
        if (!_barrierType.equals (msg._barrierType)) return false;
      }
      else return false;
    }
    else if (msg._barrierType != null) return false;
    if (_barrierDirection != null) {
      if (msg._barrierDirection != null) {
        if (!_barrierDirection.equals (msg._barrierDirection)) return false;
      }
      else return false;
    }
    else if (msg._barrierDirection != null) return false;
    if (_monitoringType != null) {
      if (msg._monitoringType != null) {
        if (!_monitoringType.equals (msg._monitoringType)) return false;
      }
      else return false;
    }
    else if (msg._monitoringType != null) return false;
    if (_samplingFrequency != null) {
      if (msg._samplingFrequency != null) {
        if (!_samplingFrequency.equals (msg._samplingFrequency)) return false;
      }
      else return false;
    }
    else if (msg._samplingFrequency != null) return false;
    if (_barrierLevel != msg._barrierLevel) return false;
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
    if (_barrierType != null) hc += _barrierType.hashCode ();
    hc *= 31;
    if (_barrierDirection != null) hc += _barrierDirection.hashCode ();
    hc *= 31;
    if (_monitoringType != null) hc += _monitoringType.hashCode ();
    hc *= 31;
    if (_samplingFrequency != null) hc += _samplingFrequency.hashCode ();
    hc = (hc * 31) + (int)_barrierLevel;
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
