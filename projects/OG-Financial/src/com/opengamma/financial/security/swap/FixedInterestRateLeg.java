// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.swap;
public class FixedInterestRateLeg extends com.opengamma.financial.security.swap.InterestRateLeg implements java.io.Serializable {
  public <T> T accept (SwapLegVisitor<T> visitor) { return visitor.visitFixedInterestRateLeg (this); }
  private static final long serialVersionUID = -1217671502l;
  private final double _rate;
  public static final String RATE_KEY = "rate";
  public FixedInterestRateLeg (com.opengamma.financial.convention.daycount.DayCount dayCount, com.opengamma.financial.convention.frequency.Frequency frequency, com.opengamma.id.ExternalId regionIdentifier, com.opengamma.financial.convention.businessday.BusinessDayConvention businessDayConvention, com.opengamma.financial.security.swap.Notional notional, boolean isEOM, double rate) {
    super (dayCount, frequency, regionIdentifier, businessDayConvention, notional, isEOM);
    _rate = rate;
  }
  protected FixedInterestRateLeg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (RATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FixedInterestRateLeg - field 'rate' is not present");
    try {
      _rate = fudgeMsg.getFieldValue (Double.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FixedInterestRateLeg - field 'rate' is not double", e);
    }
  }
  protected FixedInterestRateLeg (final FixedInterestRateLeg source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _rate = source._rate;
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    msg.add (RATE_KEY, null, _rate);
  }
  public static FixedInterestRateLeg fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.swap.FixedInterestRateLeg".equals (className)) break;
      try {
        return (com.opengamma.financial.security.swap.FixedInterestRateLeg)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new FixedInterestRateLeg (deserializer, fudgeMsg);
  }
  public double getRate () {
    return _rate;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof FixedInterestRateLeg)) return false;
    FixedInterestRateLeg msg = (FixedInterestRateLeg)o;
    if (_rate != msg._rate) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc = (hc * 31) + (int)_rate;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
