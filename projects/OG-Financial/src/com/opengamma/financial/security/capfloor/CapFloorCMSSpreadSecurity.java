// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.capfloor;
public class CapFloorCMSSpreadSecurity extends com.opengamma.financial.security.FinancialSecurity implements java.io.Serializable {
          public <T> T accept(CapFloorCMSSpreadSecurityVisitor<T> visitor) { return visitor.visitCapFloorCMSSpreadSecurity(this); };
        public final <T> T accept(com.opengamma.financial.security.FinancialSecurityVisitor<T> visitor) { return visitor.visitCapFloorCMSSpreadSecurity(this); }
  private static final long serialVersionUID = -5326972972901251765l;
  private javax.time.calendar.ZonedDateTime _startDate;
  public static final String START_DATE_KEY = "startDate";
  private javax.time.calendar.ZonedDateTime _maturityDate;
  public static final String MATURITY_DATE_KEY = "maturityDate";
  private double _notional;
  public static final String NOTIONAL_KEY = "notional";
  private com.opengamma.id.ExternalId _longIdentifier;
  public static final String LONG_IDENTIFIER_KEY = "longIdentifier";
  private com.opengamma.id.ExternalId _shortIdentifier;
  public static final String SHORT_IDENTIFIER_KEY = "shortIdentifier";
  private double _strike;
  public static final String STRIKE_KEY = "strike";
  private com.opengamma.financial.convention.frequency.Frequency _frequency;
  public static final String FREQUENCY_KEY = "frequency";
  private com.opengamma.util.money.Currency _currency;
  public static final String CURRENCY_KEY = "currency";
  private com.opengamma.financial.convention.daycount.DayCount _dayCount;
  public static final String DAY_COUNT_KEY = "dayCount";
  private boolean _isPayer;
  public static final String IS_PAYER_KEY = "isPayer";
  private boolean _isCap;
  public static final String IS_CAP_KEY = "isCap";
  public static final String SECURITY_TYPE = "CAP/FLOOR CMS SPREAD";
  public CapFloorCMSSpreadSecurity (javax.time.calendar.ZonedDateTime startDate, javax.time.calendar.ZonedDateTime maturityDate, double notional, com.opengamma.id.ExternalId longIdentifier, com.opengamma.id.ExternalId shortIdentifier, double strike, com.opengamma.financial.convention.frequency.Frequency frequency, com.opengamma.util.money.Currency currency, com.opengamma.financial.convention.daycount.DayCount dayCount, boolean isPayer, boolean isCap) {
    super (SECURITY_TYPE);
    if (startDate == null) throw new NullPointerException ("'startDate' cannot be null");
    else {
      _startDate = startDate;
    }
    if (maturityDate == null) throw new NullPointerException ("'maturityDate' cannot be null");
    else {
      _maturityDate = maturityDate;
    }
    _notional = notional;
    if (longIdentifier == null) throw new NullPointerException ("'longIdentifier' cannot be null");
    else {
      _longIdentifier = longIdentifier;
    }
    if (shortIdentifier == null) throw new NullPointerException ("'shortIdentifier' cannot be null");
    else {
      _shortIdentifier = shortIdentifier;
    }
    _strike = strike;
    if (frequency == null) throw new NullPointerException ("frequency' cannot be null");
    _frequency = frequency;
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
    if (dayCount == null) throw new NullPointerException ("dayCount' cannot be null");
    _dayCount = dayCount;
    _isPayer = isPayer;
    _isCap = isCap;
  }
  protected CapFloorCMSSpreadSecurity (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeContext, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (START_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CapFloorCMSSpreadSecurity - field 'startDate' is not present");
    try {
      _startDate = fudgeContext.fieldValueToObject (javax.time.calendar.ZonedDateTime.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CapFloorCMSSpreadSecurity - field 'startDate' is not ZonedDateTime typedef", e);
    }
    fudgeField = fudgeMsg.getByName (MATURITY_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CapFloorCMSSpreadSecurity - field 'maturityDate' is not present");
    try {
      _maturityDate = fudgeContext.fieldValueToObject (javax.time.calendar.ZonedDateTime.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CapFloorCMSSpreadSecurity - field 'maturityDate' is not ZonedDateTime typedef", e);
    }
    fudgeField = fudgeMsg.getByName (NOTIONAL_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CapFloorCMSSpreadSecurity - field 'notional' is not present");
    try {
      _notional = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CapFloorCMSSpreadSecurity - field 'notional' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (LONG_IDENTIFIER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CapFloorCMSSpreadSecurity - field 'longIdentifier' is not present");
    try {
      _longIdentifier = com.opengamma.id.ExternalId.fromFudgeMsg (fudgeContext, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CapFloorCMSSpreadSecurity - field 'longIdentifier' is not ExternalId message", e);
    }
    fudgeField = fudgeMsg.getByName (SHORT_IDENTIFIER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CapFloorCMSSpreadSecurity - field 'shortIdentifier' is not present");
    try {
      _shortIdentifier = com.opengamma.id.ExternalId.fromFudgeMsg (fudgeContext, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CapFloorCMSSpreadSecurity - field 'shortIdentifier' is not ExternalId message", e);
    }
    fudgeField = fudgeMsg.getByName (STRIKE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CapFloorCMSSpreadSecurity - field 'strike' is not present");
    try {
      _strike = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CapFloorCMSSpreadSecurity - field 'strike' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (FREQUENCY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CapFloorCMSSpreadSecurity - field 'frequency' is not present");
    try {
      _frequency = fudgeMsg.getFieldValue (com.opengamma.financial.convention.frequency.Frequency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CapFloorCMSSpreadSecurity - field 'frequency' is not Frequency typedef", e);
    }
    fudgeField = fudgeMsg.getByName (CURRENCY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CapFloorCMSSpreadSecurity - field 'currency' is not present");
    try {
      _currency = fudgeMsg.getFieldValue (com.opengamma.util.money.Currency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CapFloorCMSSpreadSecurity - field 'currency' is not Currency typedef", e);
    }
    fudgeField = fudgeMsg.getByName (DAY_COUNT_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CapFloorCMSSpreadSecurity - field 'dayCount' is not present");
    try {
      _dayCount = fudgeMsg.getFieldValue (com.opengamma.financial.convention.daycount.DayCount.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CapFloorCMSSpreadSecurity - field 'dayCount' is not DayCount typedef", e);
    }
    fudgeField = fudgeMsg.getByName (IS_PAYER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CapFloorCMSSpreadSecurity - field 'isPayer' is not present");
    try {
      _isPayer = fudgeMsg.getFieldValue (Boolean.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CapFloorCMSSpreadSecurity - field 'isPayer' is not boolean", e);
    }
    fudgeField = fudgeMsg.getByName (IS_CAP_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a CapFloorCMSSpreadSecurity - field 'isCap' is not present");
    try {
      _isCap = fudgeMsg.getFieldValue (Boolean.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a CapFloorCMSSpreadSecurity - field 'isCap' is not boolean", e);
    }
  }
  public CapFloorCMSSpreadSecurity (com.opengamma.id.UniqueId uniqueId, String name, String securityType, com.opengamma.id.ExternalIdBundle identifiers, javax.time.calendar.ZonedDateTime startDate, javax.time.calendar.ZonedDateTime maturityDate, double notional, com.opengamma.id.ExternalId longIdentifier, com.opengamma.id.ExternalId shortIdentifier, double strike, com.opengamma.financial.convention.frequency.Frequency frequency, com.opengamma.util.money.Currency currency, com.opengamma.financial.convention.daycount.DayCount dayCount, boolean isPayer, boolean isCap) {
    super (uniqueId, name, securityType, identifiers);
    if (startDate == null) throw new NullPointerException ("'startDate' cannot be null");
    else {
      _startDate = startDate;
    }
    if (maturityDate == null) throw new NullPointerException ("'maturityDate' cannot be null");
    else {
      _maturityDate = maturityDate;
    }
    _notional = notional;
    if (longIdentifier == null) throw new NullPointerException ("'longIdentifier' cannot be null");
    else {
      _longIdentifier = longIdentifier;
    }
    if (shortIdentifier == null) throw new NullPointerException ("'shortIdentifier' cannot be null");
    else {
      _shortIdentifier = shortIdentifier;
    }
    _strike = strike;
    if (frequency == null) throw new NullPointerException ("frequency' cannot be null");
    _frequency = frequency;
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
    if (dayCount == null) throw new NullPointerException ("dayCount' cannot be null");
    _dayCount = dayCount;
    _isPayer = isPayer;
    _isCap = isCap;
  }
  protected CapFloorCMSSpreadSecurity (final CapFloorCMSSpreadSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._startDate == null) _startDate = null;
    else {
      _startDate = source._startDate;
    }
    if (source._maturityDate == null) _maturityDate = null;
    else {
      _maturityDate = source._maturityDate;
    }
    _notional = source._notional;
    if (source._longIdentifier == null) _longIdentifier = null;
    else {
      _longIdentifier = source._longIdentifier;
    }
    if (source._shortIdentifier == null) _shortIdentifier = null;
    else {
      _shortIdentifier = source._shortIdentifier;
    }
    _strike = source._strike;
    _frequency = source._frequency;
    _currency = source._currency;
    _dayCount = source._dayCount;
    _isPayer = source._isPayer;
    _isCap = source._isCap;
  }
  public CapFloorCMSSpreadSecurity clone () {
    return new CapFloorCMSSpreadSecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_startDate != null)  {
      fudgeContext.addToMessage (msg, START_DATE_KEY, null, _startDate);
    }
    if (_maturityDate != null)  {
      fudgeContext.addToMessage (msg, MATURITY_DATE_KEY, null, _maturityDate);
    }
    msg.add (NOTIONAL_KEY, null, _notional);
    if (_longIdentifier != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _longIdentifier.getClass (), com.opengamma.id.ExternalId.class);
      _longIdentifier.toFudgeMsg (fudgeContext, fudge1);
      msg.add (LONG_IDENTIFIER_KEY, null, fudge1);
    }
    if (_shortIdentifier != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _shortIdentifier.getClass (), com.opengamma.id.ExternalId.class);
      _shortIdentifier.toFudgeMsg (fudgeContext, fudge1);
      msg.add (SHORT_IDENTIFIER_KEY, null, fudge1);
    }
    msg.add (STRIKE_KEY, null, _strike);
    if (_frequency != null)  {
      msg.add (FREQUENCY_KEY, null, _frequency);
    }
    if (_currency != null)  {
      msg.add (CURRENCY_KEY, null, _currency);
    }
    if (_dayCount != null)  {
      msg.add (DAY_COUNT_KEY, null, _dayCount);
    }
    msg.add (IS_PAYER_KEY, null, _isPayer);
    msg.add (IS_CAP_KEY, null, _isCap);
  }
  public static CapFloorCMSSpreadSecurity fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeMsg.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new CapFloorCMSSpreadSecurity (fudgeContext, fudgeMsg);
  }
  public javax.time.calendar.ZonedDateTime getStartDate () {
    return _startDate;
  }
  public void setStartDate (javax.time.calendar.ZonedDateTime startDate) {
    if (startDate == null) throw new NullPointerException ("'startDate' cannot be null");
    else {
      _startDate = startDate;
    }
  }
  public javax.time.calendar.ZonedDateTime getMaturityDate () {
    return _maturityDate;
  }
  public void setMaturityDate (javax.time.calendar.ZonedDateTime maturityDate) {
    if (maturityDate == null) throw new NullPointerException ("'maturityDate' cannot be null");
    else {
      _maturityDate = maturityDate;
    }
  }
  public double getNotional () {
    return _notional;
  }
  public void setNotional (double notional) {
    _notional = notional;
  }
  public com.opengamma.id.ExternalId getLongIdentifier () {
    return _longIdentifier;
  }
  public void setLongIdentifier (com.opengamma.id.ExternalId longIdentifier) {
    if (longIdentifier == null) throw new NullPointerException ("'longIdentifier' cannot be null");
    else {
      _longIdentifier = longIdentifier;
    }
  }
  public com.opengamma.id.ExternalId getShortIdentifier () {
    return _shortIdentifier;
  }
  public void setShortIdentifier (com.opengamma.id.ExternalId shortIdentifier) {
    if (shortIdentifier == null) throw new NullPointerException ("'shortIdentifier' cannot be null");
    else {
      _shortIdentifier = shortIdentifier;
    }
  }
  public double getStrike () {
    return _strike;
  }
  public void setStrike (double strike) {
    _strike = strike;
  }
  public com.opengamma.financial.convention.frequency.Frequency getFrequency () {
    return _frequency;
  }
  public void setFrequency (com.opengamma.financial.convention.frequency.Frequency frequency) {
    if (frequency == null) throw new NullPointerException ("frequency' cannot be null");
    _frequency = frequency;
  }
  public com.opengamma.util.money.Currency getCurrency () {
    return _currency;
  }
  public void setCurrency (com.opengamma.util.money.Currency currency) {
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
  }
  public com.opengamma.financial.convention.daycount.DayCount getDayCount () {
    return _dayCount;
  }
  public void setDayCount (com.opengamma.financial.convention.daycount.DayCount dayCount) {
    if (dayCount == null) throw new NullPointerException ("dayCount' cannot be null");
    _dayCount = dayCount;
  }
  public boolean getIsPayer () {
    return _isPayer;
  }
  public void setIsPayer (boolean isPayer) {
    _isPayer = isPayer;
  }
  public boolean getIsCap () {
    return _isCap;
  }
  public void setIsCap (boolean isCap) {
    _isCap = isCap;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof CapFloorCMSSpreadSecurity)) return false;
    CapFloorCMSSpreadSecurity msg = (CapFloorCMSSpreadSecurity)o;
    if (_startDate != null) {
      if (msg._startDate != null) {
        if (!_startDate.equals (msg._startDate)) return false;
      }
      else return false;
    }
    else if (msg._startDate != null) return false;
    if (_maturityDate != null) {
      if (msg._maturityDate != null) {
        if (!_maturityDate.equals (msg._maturityDate)) return false;
      }
      else return false;
    }
    else if (msg._maturityDate != null) return false;
    if (_notional != msg._notional) return false;
    if (_longIdentifier != null) {
      if (msg._longIdentifier != null) {
        if (!_longIdentifier.equals (msg._longIdentifier)) return false;
      }
      else return false;
    }
    else if (msg._longIdentifier != null) return false;
    if (_shortIdentifier != null) {
      if (msg._shortIdentifier != null) {
        if (!_shortIdentifier.equals (msg._shortIdentifier)) return false;
      }
      else return false;
    }
    else if (msg._shortIdentifier != null) return false;
    if (_strike != msg._strike) return false;
    if (_frequency != null) {
      if (msg._frequency != null) {
        if (!_frequency.equals (msg._frequency)) return false;
      }
      else return false;
    }
    else if (msg._frequency != null) return false;
    if (_currency != null) {
      if (msg._currency != null) {
        if (!_currency.equals (msg._currency)) return false;
      }
      else return false;
    }
    else if (msg._currency != null) return false;
    if (_dayCount != null) {
      if (msg._dayCount != null) {
        if (!_dayCount.equals (msg._dayCount)) return false;
      }
      else return false;
    }
    else if (msg._dayCount != null) return false;
    if (_isPayer != msg._isPayer) return false;
    if (_isCap != msg._isCap) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_startDate != null) hc += _startDate.hashCode ();
    hc *= 31;
    if (_maturityDate != null) hc += _maturityDate.hashCode ();
    hc = (hc * 31) + (int)_notional;
    hc *= 31;
    if (_longIdentifier != null) hc += _longIdentifier.hashCode ();
    hc *= 31;
    if (_shortIdentifier != null) hc += _shortIdentifier.hashCode ();
    hc = (hc * 31) + (int)_strike;
    hc *= 31;
    if (_frequency != null) hc += _frequency.hashCode ();
    hc *= 31;
    if (_currency != null) hc += _currency.hashCode ();
    hc *= 31;
    if (_dayCount != null) hc += _dayCount.hashCode ();
    hc *= 31;
    if (_isPayer) hc++;
    hc *= 31;
    if (_isCap) hc++;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
