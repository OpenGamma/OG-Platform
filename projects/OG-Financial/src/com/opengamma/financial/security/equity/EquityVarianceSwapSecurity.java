// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.equity;
public class EquityVarianceSwapSecurity extends com.opengamma.financial.security.FinancialSecurity implements java.io.Serializable {
          
        public <T> T accept(EquityVarianceSwapSecurityVisitor<T> visitor) { return visitor.visitEquityVarianceSwapSecurity(this); }
        public final <T> T accept(com.opengamma.financial.security.FinancialSecurityVisitor<T> visitor) { return visitor.visitEquityVarianceSwapSecurity(this); }
  private static final long serialVersionUID = -156858993943404659l;
  private com.opengamma.id.ExternalId _spotUnderlyingIdentifier;
  public static final String SPOT_UNDERLYING_IDENTIFIER_KEY = "spotUnderlyingIdentifier";
  private com.opengamma.util.money.Currency _currency;
  public static final String CURRENCY_KEY = "currency";
  private double _strike;
  public static final String STRIKE_KEY = "strike";
  private double _notional;
  public static final String NOTIONAL_KEY = "notional";
  private boolean _parameterisedAsVariance;
  public static final String PARAMETERISED_AS_VARIANCE_KEY = "parameterisedAsVariance";
  private double _annualizationFactor;
  public static final String ANNUALIZATION_FACTOR_KEY = "annualizationFactor";
  private javax.time.calendar.ZonedDateTime _firstObservationDate;
  public static final String FIRST_OBSERVATION_DATE_KEY = "firstObservationDate";
  private javax.time.calendar.ZonedDateTime _lastObservationDate;
  public static final String LAST_OBSERVATION_DATE_KEY = "lastObservationDate";
  private javax.time.calendar.ZonedDateTime _settlementDate;
  public static final String SETTLEMENT_DATE_KEY = "settlementDate";
  private com.opengamma.id.ExternalId _region;
  public static final String REGION_KEY = "region";
  private com.opengamma.financial.convention.frequency.Frequency _observationFrequency;
  public static final String OBSERVATION_FREQUENCY_KEY = "observationFrequency";
  public static final String SECURITY_TYPE = "EQUITY VARIANCE SWAP";
  public EquityVarianceSwapSecurity (com.opengamma.id.ExternalId spotUnderlyingIdentifier, com.opengamma.util.money.Currency currency, double strike, double notional, boolean parameterisedAsVariance, double annualizationFactor, javax.time.calendar.ZonedDateTime firstObservationDate, javax.time.calendar.ZonedDateTime lastObservationDate, javax.time.calendar.ZonedDateTime settlementDate, com.opengamma.id.ExternalId region, com.opengamma.financial.convention.frequency.Frequency observationFrequency) {
    super (SECURITY_TYPE);
    if (spotUnderlyingIdentifier == null) throw new NullPointerException ("'spotUnderlyingIdentifier' cannot be null");
    else {
      _spotUnderlyingIdentifier = spotUnderlyingIdentifier;
    }
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
    _strike = strike;
    _notional = notional;
    _parameterisedAsVariance = parameterisedAsVariance;
    _annualizationFactor = annualizationFactor;
    if (firstObservationDate == null) throw new NullPointerException ("'firstObservationDate' cannot be null");
    else {
      _firstObservationDate = firstObservationDate;
    }
    if (lastObservationDate == null) throw new NullPointerException ("'lastObservationDate' cannot be null");
    else {
      _lastObservationDate = lastObservationDate;
    }
    if (settlementDate == null) throw new NullPointerException ("'settlementDate' cannot be null");
    else {
      _settlementDate = settlementDate;
    }
    if (region == null) throw new NullPointerException ("'region' cannot be null");
    else {
      _region = region;
    }
    if (observationFrequency == null) throw new NullPointerException ("observationFrequency' cannot be null");
    _observationFrequency = observationFrequency;
  }
  protected EquityVarianceSwapSecurity (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (SPOT_UNDERLYING_IDENTIFIER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityVarianceSwapSecurity - field 'spotUnderlyingIdentifier' is not present");
    try {
      _spotUnderlyingIdentifier = com.opengamma.id.ExternalId.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityVarianceSwapSecurity - field 'spotUnderlyingIdentifier' is not ExternalId message", e);
    }
    fudgeField = fudgeMsg.getByName (CURRENCY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityVarianceSwapSecurity - field 'currency' is not present");
    try {
      _currency = fudgeMsg.getFieldValue (com.opengamma.util.money.Currency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityVarianceSwapSecurity - field 'currency' is not Currency typedef", e);
    }
    fudgeField = fudgeMsg.getByName (STRIKE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityVarianceSwapSecurity - field 'strike' is not present");
    try {
      _strike = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityVarianceSwapSecurity - field 'strike' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (NOTIONAL_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityVarianceSwapSecurity - field 'notional' is not present");
    try {
      _notional = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityVarianceSwapSecurity - field 'notional' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (PARAMETERISED_AS_VARIANCE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityVarianceSwapSecurity - field 'parameterisedAsVariance' is not present");
    try {
      _parameterisedAsVariance = fudgeMsg.getFieldValue (Boolean.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityVarianceSwapSecurity - field 'parameterisedAsVariance' is not boolean", e);
    }
    fudgeField = fudgeMsg.getByName (ANNUALIZATION_FACTOR_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityVarianceSwapSecurity - field 'annualizationFactor' is not present");
    try {
      _annualizationFactor = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityVarianceSwapSecurity - field 'annualizationFactor' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (FIRST_OBSERVATION_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityVarianceSwapSecurity - field 'firstObservationDate' is not present");
    try {
      _firstObservationDate = deserializer.fieldValueToObject (javax.time.calendar.ZonedDateTime.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityVarianceSwapSecurity - field 'firstObservationDate' is not ZonedDateTime typedef", e);
    }
    fudgeField = fudgeMsg.getByName (LAST_OBSERVATION_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityVarianceSwapSecurity - field 'lastObservationDate' is not present");
    try {
      _lastObservationDate = deserializer.fieldValueToObject (javax.time.calendar.ZonedDateTime.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityVarianceSwapSecurity - field 'lastObservationDate' is not ZonedDateTime typedef", e);
    }
    fudgeField = fudgeMsg.getByName (SETTLEMENT_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityVarianceSwapSecurity - field 'settlementDate' is not present");
    try {
      _settlementDate = deserializer.fieldValueToObject (javax.time.calendar.ZonedDateTime.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityVarianceSwapSecurity - field 'settlementDate' is not ZonedDateTime typedef", e);
    }
    fudgeField = fudgeMsg.getByName (REGION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityVarianceSwapSecurity - field 'region' is not present");
    try {
      _region = com.opengamma.id.ExternalId.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityVarianceSwapSecurity - field 'region' is not ExternalId message", e);
    }
    fudgeField = fudgeMsg.getByName (OBSERVATION_FREQUENCY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a EquityVarianceSwapSecurity - field 'observationFrequency' is not present");
    try {
      _observationFrequency = fudgeMsg.getFieldValue (com.opengamma.financial.convention.frequency.Frequency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a EquityVarianceSwapSecurity - field 'observationFrequency' is not Frequency typedef", e);
    }
  }
  public EquityVarianceSwapSecurity (com.opengamma.id.UniqueId uniqueId, String name, String securityType, com.opengamma.id.ExternalIdBundle identifiers, com.opengamma.id.ExternalId spotUnderlyingIdentifier, com.opengamma.util.money.Currency currency, double strike, double notional, boolean parameterisedAsVariance, double annualizationFactor, javax.time.calendar.ZonedDateTime firstObservationDate, javax.time.calendar.ZonedDateTime lastObservationDate, javax.time.calendar.ZonedDateTime settlementDate, com.opengamma.id.ExternalId region, com.opengamma.financial.convention.frequency.Frequency observationFrequency) {
    super (uniqueId, name, securityType, identifiers);
    if (spotUnderlyingIdentifier == null) throw new NullPointerException ("'spotUnderlyingIdentifier' cannot be null");
    else {
      _spotUnderlyingIdentifier = spotUnderlyingIdentifier;
    }
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
    _strike = strike;
    _notional = notional;
    _parameterisedAsVariance = parameterisedAsVariance;
    _annualizationFactor = annualizationFactor;
    if (firstObservationDate == null) throw new NullPointerException ("'firstObservationDate' cannot be null");
    else {
      _firstObservationDate = firstObservationDate;
    }
    if (lastObservationDate == null) throw new NullPointerException ("'lastObservationDate' cannot be null");
    else {
      _lastObservationDate = lastObservationDate;
    }
    if (settlementDate == null) throw new NullPointerException ("'settlementDate' cannot be null");
    else {
      _settlementDate = settlementDate;
    }
    if (region == null) throw new NullPointerException ("'region' cannot be null");
    else {
      _region = region;
    }
    if (observationFrequency == null) throw new NullPointerException ("observationFrequency' cannot be null");
    _observationFrequency = observationFrequency;
  }
  protected EquityVarianceSwapSecurity (final EquityVarianceSwapSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._spotUnderlyingIdentifier == null) _spotUnderlyingIdentifier = null;
    else {
      _spotUnderlyingIdentifier = source._spotUnderlyingIdentifier;
    }
    _currency = source._currency;
    _strike = source._strike;
    _notional = source._notional;
    _parameterisedAsVariance = source._parameterisedAsVariance;
    _annualizationFactor = source._annualizationFactor;
    if (source._firstObservationDate == null) _firstObservationDate = null;
    else {
      _firstObservationDate = source._firstObservationDate;
    }
    if (source._lastObservationDate == null) _lastObservationDate = null;
    else {
      _lastObservationDate = source._lastObservationDate;
    }
    if (source._settlementDate == null) _settlementDate = null;
    else {
      _settlementDate = source._settlementDate;
    }
    if (source._region == null) _region = null;
    else {
      _region = source._region;
    }
    _observationFrequency = source._observationFrequency;
  }
  public EquityVarianceSwapSecurity clone () {
    return new EquityVarianceSwapSecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_spotUnderlyingIdentifier != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _spotUnderlyingIdentifier.getClass (), com.opengamma.id.ExternalId.class);
      _spotUnderlyingIdentifier.toFudgeMsg (serializer, fudge1);
      msg.add (SPOT_UNDERLYING_IDENTIFIER_KEY, null, fudge1);
    }
    if (_currency != null)  {
      msg.add (CURRENCY_KEY, null, _currency);
    }
    msg.add (STRIKE_KEY, null, _strike);
    msg.add (NOTIONAL_KEY, null, _notional);
    msg.add (PARAMETERISED_AS_VARIANCE_KEY, null, _parameterisedAsVariance);
    msg.add (ANNUALIZATION_FACTOR_KEY, null, _annualizationFactor);
    if (_firstObservationDate != null)  {
      serializer.addToMessage (msg, FIRST_OBSERVATION_DATE_KEY, null, _firstObservationDate);
    }
    if (_lastObservationDate != null)  {
      serializer.addToMessage (msg, LAST_OBSERVATION_DATE_KEY, null, _lastObservationDate);
    }
    if (_settlementDate != null)  {
      serializer.addToMessage (msg, SETTLEMENT_DATE_KEY, null, _settlementDate);
    }
    if (_region != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _region.getClass (), com.opengamma.id.ExternalId.class);
      _region.toFudgeMsg (serializer, fudge1);
      msg.add (REGION_KEY, null, fudge1);
    }
    if (_observationFrequency != null)  {
      msg.add (OBSERVATION_FREQUENCY_KEY, null, _observationFrequency);
    }
  }
  public static EquityVarianceSwapSecurity fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.equity.EquityVarianceSwapSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.equity.EquityVarianceSwapSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new EquityVarianceSwapSecurity (deserializer, fudgeMsg);
  }
  public com.opengamma.id.ExternalId getSpotUnderlyingIdentifier () {
    return _spotUnderlyingIdentifier;
  }
  public void setSpotUnderlyingIdentifier (com.opengamma.id.ExternalId spotUnderlyingIdentifier) {
    if (spotUnderlyingIdentifier == null) throw new NullPointerException ("'spotUnderlyingIdentifier' cannot be null");
    else {
      _spotUnderlyingIdentifier = spotUnderlyingIdentifier;
    }
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
  public double getNotional () {
    return _notional;
  }
  public void setNotional (double notional) {
    _notional = notional;
  }
  public boolean getParameterisedAsVariance () {
    return _parameterisedAsVariance;
  }
  public void setParameterisedAsVariance (boolean parameterisedAsVariance) {
    _parameterisedAsVariance = parameterisedAsVariance;
  }
  public double getAnnualizationFactor () {
    return _annualizationFactor;
  }
  public void setAnnualizationFactor (double annualizationFactor) {
    _annualizationFactor = annualizationFactor;
  }
  public javax.time.calendar.ZonedDateTime getFirstObservationDate () {
    return _firstObservationDate;
  }
  public void setFirstObservationDate (javax.time.calendar.ZonedDateTime firstObservationDate) {
    if (firstObservationDate == null) throw new NullPointerException ("'firstObservationDate' cannot be null");
    else {
      _firstObservationDate = firstObservationDate;
    }
  }
  public javax.time.calendar.ZonedDateTime getLastObservationDate () {
    return _lastObservationDate;
  }
  public void setLastObservationDate (javax.time.calendar.ZonedDateTime lastObservationDate) {
    if (lastObservationDate == null) throw new NullPointerException ("'lastObservationDate' cannot be null");
    else {
      _lastObservationDate = lastObservationDate;
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
  public com.opengamma.id.ExternalId getRegion () {
    return _region;
  }
  public void setRegion (com.opengamma.id.ExternalId region) {
    if (region == null) throw new NullPointerException ("'region' cannot be null");
    else {
      _region = region;
    }
  }
  public com.opengamma.financial.convention.frequency.Frequency getObservationFrequency () {
    return _observationFrequency;
  }
  public void setObservationFrequency (com.opengamma.financial.convention.frequency.Frequency observationFrequency) {
    if (observationFrequency == null) throw new NullPointerException ("observationFrequency' cannot be null");
    _observationFrequency = observationFrequency;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof EquityVarianceSwapSecurity)) return false;
    EquityVarianceSwapSecurity msg = (EquityVarianceSwapSecurity)o;
    if (_spotUnderlyingIdentifier != null) {
      if (msg._spotUnderlyingIdentifier != null) {
        if (!_spotUnderlyingIdentifier.equals (msg._spotUnderlyingIdentifier)) return false;
      }
      else return false;
    }
    else if (msg._spotUnderlyingIdentifier != null) return false;
    if (_currency != null) {
      if (msg._currency != null) {
        if (!_currency.equals (msg._currency)) return false;
      }
      else return false;
    }
    else if (msg._currency != null) return false;
    if (_strike != msg._strike) return false;
    if (_notional != msg._notional) return false;
    if (_parameterisedAsVariance != msg._parameterisedAsVariance) return false;
    if (_annualizationFactor != msg._annualizationFactor) return false;
    if (_firstObservationDate != null) {
      if (msg._firstObservationDate != null) {
        if (!_firstObservationDate.equals (msg._firstObservationDate)) return false;
      }
      else return false;
    }
    else if (msg._firstObservationDate != null) return false;
    if (_lastObservationDate != null) {
      if (msg._lastObservationDate != null) {
        if (!_lastObservationDate.equals (msg._lastObservationDate)) return false;
      }
      else return false;
    }
    else if (msg._lastObservationDate != null) return false;
    if (_settlementDate != null) {
      if (msg._settlementDate != null) {
        if (!_settlementDate.equals (msg._settlementDate)) return false;
      }
      else return false;
    }
    else if (msg._settlementDate != null) return false;
    if (_region != null) {
      if (msg._region != null) {
        if (!_region.equals (msg._region)) return false;
      }
      else return false;
    }
    else if (msg._region != null) return false;
    if (_observationFrequency != null) {
      if (msg._observationFrequency != null) {
        if (!_observationFrequency.equals (msg._observationFrequency)) return false;
      }
      else return false;
    }
    else if (msg._observationFrequency != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_spotUnderlyingIdentifier != null) hc += _spotUnderlyingIdentifier.hashCode ();
    hc *= 31;
    if (_currency != null) hc += _currency.hashCode ();
    hc = (hc * 31) + (int)_strike;
    hc = (hc * 31) + (int)_notional;
    hc *= 31;
    if (_parameterisedAsVariance) hc++;
    hc = (hc * 31) + (int)_annualizationFactor;
    hc *= 31;
    if (_firstObservationDate != null) hc += _firstObservationDate.hashCode ();
    hc *= 31;
    if (_lastObservationDate != null) hc += _lastObservationDate.hashCode ();
    hc *= 31;
    if (_settlementDate != null) hc += _settlementDate.hashCode ();
    hc *= 31;
    if (_region != null) hc += _region.hashCode ();
    hc *= 31;
    if (_observationFrequency != null) hc += _observationFrequency.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
