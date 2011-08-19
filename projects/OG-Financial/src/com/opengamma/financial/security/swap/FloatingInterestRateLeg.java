// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.swap;
public class FloatingInterestRateLeg extends com.opengamma.financial.security.swap.InterestRateLeg implements java.io.Serializable {
  public <T> T accept (SwapLegVisitor<T> visitor) { return visitor.visitFloatingInterestRateLeg (this); }
  private static final long serialVersionUID = -916431872598351773l;
  private final com.opengamma.id.ExternalId _floatingReferenceRateIdentifier;
  public static final String FLOATING_REFERENCE_RATE_IDENTIFIER_KEY = "floatingReferenceRateIdentifier";
  private final Double _initialFloatingRate;
  public static final String INITIAL_FLOATING_RATE_KEY = "initialFloatingRate";
  private final double _spread;
  public static final String SPREAD_KEY = "spread";
  private final com.opengamma.financial.security.swap.FloatingRateType _floatingRateType;
  public static final String FLOATING_RATE_TYPE_KEY = "floatingRateType";
  public static class Builder {
    private com.opengamma.financial.convention.daycount.DayCount _dayCount;
    private com.opengamma.financial.convention.frequency.Frequency _frequency;
    private com.opengamma.id.ExternalId _regionIdentifier;
    private com.opengamma.financial.convention.businessday.BusinessDayConvention _businessDayConvention;
    private com.opengamma.financial.security.swap.Notional _notional;
    private boolean _isEOM;
    private org.fudgemsg.mapping.FudgeDeserializer _deserializer;
    protected org.fudgemsg.mapping.FudgeDeserializer getDeserializer () {
      return _deserializer;
    }
    private org.fudgemsg.FudgeMsg _fudgeRoot;
    protected org.fudgemsg.FudgeMsg getFudgeRoot () {
      return _fudgeRoot;
    }
    private com.opengamma.id.ExternalId _floatingReferenceRateIdentifier;
    private Double _initialFloatingRate;
    private double _spread;
    private com.opengamma.financial.security.swap.FloatingRateType _floatingRateType;
    public Builder (com.opengamma.financial.convention.daycount.DayCount dayCount, com.opengamma.financial.convention.frequency.Frequency frequency, com.opengamma.id.ExternalId regionIdentifier, com.opengamma.financial.convention.businessday.BusinessDayConvention businessDayConvention, com.opengamma.financial.security.swap.Notional notional, boolean isEOM, com.opengamma.id.ExternalId floatingReferenceRateIdentifier, double spread, com.opengamma.financial.security.swap.FloatingRateType floatingRateType) {
      _dayCount = dayCount;
      _frequency = frequency;
      _regionIdentifier = regionIdentifier;
      _businessDayConvention = businessDayConvention;
      _notional = notional;
      _isEOM = isEOM;
      floatingReferenceRateIdentifier (floatingReferenceRateIdentifier);
      spread (spread);
      floatingRateType (floatingRateType);
    }
    protected Builder (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
      _fudgeRoot = fudgeMsg;
      _deserializer = deserializer;
      org.fudgemsg.FudgeField fudgeField;
      fudgeField = fudgeMsg.getByName (FLOATING_REFERENCE_RATE_IDENTIFIER_KEY);
      if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FloatingInterestRateLeg - field 'floatingReferenceRateIdentifier' is not present");
      try {
        _floatingReferenceRateIdentifier = com.opengamma.id.ExternalId.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a FloatingInterestRateLeg - field 'floatingReferenceRateIdentifier' is not ExternalId message", e);
      }
      fudgeField = fudgeMsg.getByName (SPREAD_KEY);
      if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FloatingInterestRateLeg - field 'spread' is not present");
      try {
        _spread = fudgeMsg.getFieldValue (Double.class, fudgeField);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a FloatingInterestRateLeg - field 'spread' is not double", e);
      }
      fudgeField = fudgeMsg.getByName (FLOATING_RATE_TYPE_KEY);
      if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FloatingInterestRateLeg - field 'floatingRateType' is not present");
      try {
        _floatingRateType = fudgeMsg.getFieldValue (com.opengamma.financial.security.swap.FloatingRateType.class, fudgeField);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a FloatingInterestRateLeg - field 'floatingRateType' is not FloatingRateType enum", e);
      }
      fudgeField = fudgeMsg.getByName (INITIAL_FLOATING_RATE_KEY);
      if (fudgeField != null)  {
        try {
          initialFloatingRate (fudgeMsg.getFieldValue (Double.class, fudgeField));
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a FloatingInterestRateLeg - field 'initialFloatingRate' is not double", e);
        }
      }
    }
    public Builder floatingReferenceRateIdentifier (com.opengamma.id.ExternalId floatingReferenceRateIdentifier) {
      if (floatingReferenceRateIdentifier == null) throw new NullPointerException ("'floatingReferenceRateIdentifier' cannot be null");
      else {
        _floatingReferenceRateIdentifier = floatingReferenceRateIdentifier;
      }
      return this;
    }
    public Builder initialFloatingRate (Double initialFloatingRate) {
      _initialFloatingRate = initialFloatingRate;
      return this;
    }
    public Builder spread (double spread) {
      _spread = spread;
      return this;
    }
    public Builder floatingRateType (com.opengamma.financial.security.swap.FloatingRateType floatingRateType) {
      if (floatingRateType == null) throw new NullPointerException ("floatingRateType' cannot be null");
      _floatingRateType = floatingRateType;
      return this;
    }
    public FloatingInterestRateLeg build () {
      return (getFudgeRoot () != null) ? new FloatingInterestRateLeg (getDeserializer (), getFudgeRoot (), this) : new FloatingInterestRateLeg (this);
    }
  }
  protected FloatingInterestRateLeg (final Builder builder) {
    super (builder._dayCount, builder._frequency, builder._regionIdentifier, builder._businessDayConvention, builder._notional, builder._isEOM);
    if (builder._floatingReferenceRateIdentifier == null) _floatingReferenceRateIdentifier = null;
    else {
      _floatingReferenceRateIdentifier = builder._floatingReferenceRateIdentifier;
    }
    _initialFloatingRate = builder._initialFloatingRate;
    _spread = builder._spread;
    _floatingRateType = builder._floatingRateType;
  }
  protected FloatingInterestRateLeg (final org.fudgemsg.mapping.FudgeDeserializer serializer, final org.fudgemsg.FudgeMsg fudgeMsg, final Builder builder) {
    super (serializer, fudgeMsg);
    if (builder._floatingReferenceRateIdentifier == null) _floatingReferenceRateIdentifier = null;
    else {
      _floatingReferenceRateIdentifier = builder._floatingReferenceRateIdentifier;
    }
    _initialFloatingRate = builder._initialFloatingRate;
    _spread = builder._spread;
    _floatingRateType = builder._floatingRateType;
  }
  public FloatingInterestRateLeg (com.opengamma.financial.convention.daycount.DayCount dayCount, com.opengamma.financial.convention.frequency.Frequency frequency, com.opengamma.id.ExternalId regionIdentifier, com.opengamma.financial.convention.businessday.BusinessDayConvention businessDayConvention, com.opengamma.financial.security.swap.Notional notional, boolean isEOM, com.opengamma.id.ExternalId floatingReferenceRateIdentifier, Double initialFloatingRate, double spread, com.opengamma.financial.security.swap.FloatingRateType floatingRateType) {
    super (dayCount, frequency, regionIdentifier, businessDayConvention, notional, isEOM);
    if (floatingReferenceRateIdentifier == null) throw new NullPointerException ("'floatingReferenceRateIdentifier' cannot be null");
    else {
      _floatingReferenceRateIdentifier = floatingReferenceRateIdentifier;
    }
    _initialFloatingRate = initialFloatingRate;
    _spread = spread;
    if (floatingRateType == null) throw new NullPointerException ("floatingRateType' cannot be null");
    _floatingRateType = floatingRateType;
  }
  protected FloatingInterestRateLeg (final FloatingInterestRateLeg source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._floatingReferenceRateIdentifier == null) _floatingReferenceRateIdentifier = null;
    else {
      _floatingReferenceRateIdentifier = source._floatingReferenceRateIdentifier;
    }
    _initialFloatingRate = source._initialFloatingRate;
    _spread = source._spread;
    _floatingRateType = source._floatingRateType;
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_floatingReferenceRateIdentifier != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _floatingReferenceRateIdentifier.getClass (), com.opengamma.id.ExternalId.class);
      _floatingReferenceRateIdentifier.toFudgeMsg (serializer, fudge1);
      msg.add (FLOATING_REFERENCE_RATE_IDENTIFIER_KEY, null, fudge1);
    }
    if (_initialFloatingRate != null)  {
      msg.add (INITIAL_FLOATING_RATE_KEY, null, _initialFloatingRate);
    }
    msg.add (SPREAD_KEY, null, _spread);
    if (_floatingRateType != null)  {
      msg.add (FLOATING_RATE_TYPE_KEY, null, _floatingRateType.name ());
    }
  }
  public static FloatingInterestRateLeg fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.swap.FloatingInterestRateLeg".equals (className)) break;
      try {
        return (com.opengamma.financial.security.swap.FloatingInterestRateLeg)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new Builder (deserializer, fudgeMsg).build ();
  }
  public com.opengamma.id.ExternalId getFloatingReferenceRateIdentifier () {
    return _floatingReferenceRateIdentifier;
  }
  public Double getInitialFloatingRate () {
    return _initialFloatingRate;
  }
  public double getSpread () {
    return _spread;
  }
  public com.opengamma.financial.security.swap.FloatingRateType getFloatingRateType () {
    return _floatingRateType;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof FloatingInterestRateLeg)) return false;
    FloatingInterestRateLeg msg = (FloatingInterestRateLeg)o;
    if (_floatingReferenceRateIdentifier != null) {
      if (msg._floatingReferenceRateIdentifier != null) {
        if (!_floatingReferenceRateIdentifier.equals (msg._floatingReferenceRateIdentifier)) return false;
      }
      else return false;
    }
    else if (msg._floatingReferenceRateIdentifier != null) return false;
    if (_initialFloatingRate != null) {
      if (msg._initialFloatingRate != null) {
        if (!_initialFloatingRate.equals (msg._initialFloatingRate)) return false;
      }
      else return false;
    }
    else if (msg._initialFloatingRate != null) return false;
    if (_spread != msg._spread) return false;
    if (_floatingRateType != null) {
      if (msg._floatingRateType != null) {
        if (!_floatingRateType.equals (msg._floatingRateType)) return false;
      }
      else return false;
    }
    else if (msg._floatingRateType != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_floatingReferenceRateIdentifier != null) hc += _floatingReferenceRateIdentifier.hashCode ();
    hc *= 31;
    if (_initialFloatingRate != null) hc += _initialFloatingRate.hashCode ();
    hc = (hc * 31) + (int)_spread;
    hc *= 31;
    if (_floatingRateType != null) hc += _floatingRateType.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
