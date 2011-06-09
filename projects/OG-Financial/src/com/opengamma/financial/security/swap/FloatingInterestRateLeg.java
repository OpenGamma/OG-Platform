// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.swap;
public class FloatingInterestRateLeg extends com.opengamma.financial.security.swap.InterestRateLeg implements java.io.Serializable {
  public <T> T accept (SwapLegVisitor<T> visitor) { return visitor.visitFloatingInterestRateLeg (this); }
  private static final long serialVersionUID = 55830238432243367l;
  private final com.opengamma.id.UniqueIdentifier _floatingReferenceRateIdentifier;
  public static final String FLOATING_REFERENCE_RATE_IDENTIFIER_KEY = "floatingReferenceRateIdentifier";
  private final double _initialFloatingRate;
  public static final String INITIAL_FLOATING_RATE_KEY = "initialFloatingRate";
  private final double _spread;
  public static final String SPREAD_KEY = "spread";
  public FloatingInterestRateLeg (com.opengamma.financial.convention.daycount.DayCount dayCount, com.opengamma.financial.convention.frequency.Frequency frequency, com.opengamma.id.Identifier regionIdentifier, com.opengamma.financial.convention.businessday.BusinessDayConvention businessDayConvention, com.opengamma.financial.security.swap.Notional notional, com.opengamma.id.UniqueIdentifier floatingReferenceRateIdentifier, double initialFloatingRate, double spread) {
    super (dayCount, frequency, regionIdentifier, businessDayConvention, notional);
    if (floatingReferenceRateIdentifier == null) throw new NullPointerException ("'floatingReferenceRateIdentifier' cannot be null");
    else {
      _floatingReferenceRateIdentifier = floatingReferenceRateIdentifier;
    }
    _initialFloatingRate = initialFloatingRate;
    _spread = spread;
  }
  protected FloatingInterestRateLeg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeContext, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (FLOATING_REFERENCE_RATE_IDENTIFIER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FloatingInterestRateLeg - field 'floatingReferenceRateIdentifier' is not present");
    try {
      _floatingReferenceRateIdentifier = com.opengamma.id.UniqueIdentifier.fromFudgeMsg (fudgeContext, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FloatingInterestRateLeg - field 'floatingReferenceRateIdentifier' is not UniqueIdentifier message", e);
    }
    fudgeField = fudgeMsg.getByName (INITIAL_FLOATING_RATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FloatingInterestRateLeg - field 'initialFloatingRate' is not present");
    try {
      _initialFloatingRate = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FloatingInterestRateLeg - field 'initialFloatingRate' is not double", e);
    }
    fudgeField = fudgeMsg.getByName (SPREAD_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FloatingInterestRateLeg - field 'spread' is not present");
    try {
      _spread = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FloatingInterestRateLeg - field 'spread' is not double", e);
    }
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
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_floatingReferenceRateIdentifier != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _floatingReferenceRateIdentifier.getClass (), com.opengamma.id.UniqueIdentifier.class);
      _floatingReferenceRateIdentifier.toFudgeMsg (fudgeContext, fudge1);
      msg.add (FLOATING_REFERENCE_RATE_IDENTIFIER_KEY, null, fudge1);
    }
    msg.add (INITIAL_FLOATING_RATE_KEY, null, _initialFloatingRate);
    msg.add (SPREAD_KEY, null, _spread);
  }
  public static FloatingInterestRateLeg fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.swap.FloatingInterestRateLeg".equals (className)) break;
      try {
        return (com.opengamma.financial.security.swap.FloatingInterestRateLeg)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeMsg.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new FloatingInterestRateLeg (fudgeContext, fudgeMsg);
  }
  public com.opengamma.id.UniqueIdentifier getFloatingReferenceRateIdentifier () {
    return _floatingReferenceRateIdentifier;
  }
  public double getInitialFloatingRate () {
    return _initialFloatingRate;
  }
  public double getSpread () {
    return _spread;
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
    if (_initialFloatingRate != msg._initialFloatingRate) return false;
    if (_spread != msg._spread) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_floatingReferenceRateIdentifier != null) hc += _floatingReferenceRateIdentifier.hashCode ();
    hc = (hc * 31) + (int)_initialFloatingRate;
    hc = (hc * 31) + (int)_spread;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
