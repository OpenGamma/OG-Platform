// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.swap;
public abstract class InterestRateLeg extends com.opengamma.financial.security.swap.SwapLeg implements java.io.Serializable {
  private static final long serialVersionUID = 1l;
  public InterestRateLeg (com.opengamma.financial.convention.daycount.DayCount dayCount, com.opengamma.financial.convention.frequency.Frequency frequency, com.opengamma.id.ExternalId regionIdentifier, com.opengamma.financial.convention.businessday.BusinessDayConvention businessDayConvention, com.opengamma.financial.security.swap.Notional notional, boolean isEOM) {
    super (dayCount, frequency, regionIdentifier, businessDayConvention, notional, isEOM);
  }
  protected InterestRateLeg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
  }
  protected InterestRateLeg (final InterestRateLeg source) {
    super (source);
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
  }
  public static InterestRateLeg fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.swap.InterestRateLeg".equals (className)) break;
      try {
        return (com.opengamma.financial.security.swap.InterestRateLeg)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    throw new UnsupportedOperationException ("InterestRateLeg is an abstract message");
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof InterestRateLeg)) return false;
    InterestRateLeg msg = (InterestRateLeg)o;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
