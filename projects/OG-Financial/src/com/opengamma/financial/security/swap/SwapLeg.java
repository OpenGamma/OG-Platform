// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.swap;
public abstract class SwapLeg implements java.io.Serializable {
  public abstract <T> T accept (SwapLegVisitor<T> visitor);
  private static final long serialVersionUID = 2567515945211926296l;
  private final com.opengamma.financial.convention.daycount.DayCount _dayCount;
  public static final String DAY_COUNT_KEY = "dayCount";
  private final com.opengamma.financial.convention.frequency.Frequency _frequency;
  public static final String FREQUENCY_KEY = "frequency";
  private final com.opengamma.id.ExternalId _regionIdentifier;
  public static final String REGION_IDENTIFIER_KEY = "regionIdentifier";
  private final com.opengamma.financial.convention.businessday.BusinessDayConvention _businessDayConvention;
  public static final String BUSINESS_DAY_CONVENTION_KEY = "businessDayConvention";
  private final com.opengamma.financial.security.swap.Notional _notional;
  public static final String NOTIONAL_KEY = "notional";
  public SwapLeg (com.opengamma.financial.convention.daycount.DayCount dayCount, com.opengamma.financial.convention.frequency.Frequency frequency, com.opengamma.id.ExternalId regionIdentifier, com.opengamma.financial.convention.businessday.BusinessDayConvention businessDayConvention, com.opengamma.financial.security.swap.Notional notional) {
    if (dayCount == null) throw new NullPointerException ("dayCount' cannot be null");
    _dayCount = dayCount;
    if (frequency == null) throw new NullPointerException ("frequency' cannot be null");
    _frequency = frequency;
    if (regionIdentifier == null) throw new NullPointerException ("'regionIdentifier' cannot be null");
    else {
      _regionIdentifier = regionIdentifier;
    }
    if (businessDayConvention == null) throw new NullPointerException ("businessDayConvention' cannot be null");
    _businessDayConvention = businessDayConvention;
    if (notional == null) throw new NullPointerException ("'notional' cannot be null");
    else {
      _notional = notional;
    }
  }
  protected SwapLeg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (DAY_COUNT_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SwapLeg - field 'dayCount' is not present");
    try {
      _dayCount = fudgeMsg.getFieldValue (com.opengamma.financial.convention.daycount.DayCount.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SwapLeg - field 'dayCount' is not DayCount typedef", e);
    }
    fudgeField = fudgeMsg.getByName (FREQUENCY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SwapLeg - field 'frequency' is not present");
    try {
      _frequency = fudgeMsg.getFieldValue (com.opengamma.financial.convention.frequency.Frequency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SwapLeg - field 'frequency' is not Frequency typedef", e);
    }
    fudgeField = fudgeMsg.getByName (REGION_IDENTIFIER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SwapLeg - field 'regionIdentifier' is not present");
    try {
      _regionIdentifier = com.opengamma.id.ExternalId.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SwapLeg - field 'regionIdentifier' is not ExternalId message", e);
    }
    fudgeField = fudgeMsg.getByName (BUSINESS_DAY_CONVENTION_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SwapLeg - field 'businessDayConvention' is not present");
    try {
      _businessDayConvention = fudgeMsg.getFieldValue (com.opengamma.financial.convention.businessday.BusinessDayConvention.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SwapLeg - field 'businessDayConvention' is not BusinessDayConvention typedef", e);
    }
    fudgeField = fudgeMsg.getByName (NOTIONAL_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SwapLeg - field 'notional' is not present");
    try {
      _notional = com.opengamma.financial.security.swap.Notional.fromFudgeMsg (deserializer, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SwapLeg - field 'notional' is not Notional message", e);
    }
  }
  protected SwapLeg (final SwapLeg source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _dayCount = source._dayCount;
    _frequency = source._frequency;
    if (source._regionIdentifier == null) _regionIdentifier = null;
    else {
      _regionIdentifier = source._regionIdentifier;
    }
    _businessDayConvention = source._businessDayConvention;
    if (source._notional == null) _notional = null;
    else {
      _notional = source._notional;
    }
  }
  public abstract org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer);
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    if (_dayCount != null)  {
      msg.add (DAY_COUNT_KEY, null, _dayCount);
    }
    if (_frequency != null)  {
      msg.add (FREQUENCY_KEY, null, _frequency);
    }
    if (_regionIdentifier != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _regionIdentifier.getClass (), com.opengamma.id.ExternalId.class);
      _regionIdentifier.toFudgeMsg (serializer, fudge1);
      msg.add (REGION_IDENTIFIER_KEY, null, fudge1);
    }
    if (_businessDayConvention != null)  {
      msg.add (BUSINESS_DAY_CONVENTION_KEY, null, _businessDayConvention);
    }
    if (_notional != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializer.addClassHeader (serializer.newMessage (), _notional.getClass (), com.opengamma.financial.security.swap.Notional.class);
      _notional.toFudgeMsg (serializer, fudge1);
      msg.add (NOTIONAL_KEY, null, fudge1);
    }
  }
  public static SwapLeg fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.swap.SwapLeg".equals (className)) break;
      try {
        return (com.opengamma.financial.security.swap.SwapLeg)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    throw new UnsupportedOperationException ("SwapLeg is an abstract message");
  }
  public com.opengamma.financial.convention.daycount.DayCount getDayCount () {
    return _dayCount;
  }
  public com.opengamma.financial.convention.frequency.Frequency getFrequency () {
    return _frequency;
  }
  public com.opengamma.id.ExternalId getRegionIdentifier () {
    return _regionIdentifier;
  }
  public com.opengamma.financial.convention.businessday.BusinessDayConvention getBusinessDayConvention () {
    return _businessDayConvention;
  }
  public com.opengamma.financial.security.swap.Notional getNotional () {
    return _notional;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof SwapLeg)) return false;
    SwapLeg msg = (SwapLeg)o;
    if (_dayCount != null) {
      if (msg._dayCount != null) {
        if (!_dayCount.equals (msg._dayCount)) return false;
      }
      else return false;
    }
    else if (msg._dayCount != null) return false;
    if (_frequency != null) {
      if (msg._frequency != null) {
        if (!_frequency.equals (msg._frequency)) return false;
      }
      else return false;
    }
    else if (msg._frequency != null) return false;
    if (_regionIdentifier != null) {
      if (msg._regionIdentifier != null) {
        if (!_regionIdentifier.equals (msg._regionIdentifier)) return false;
      }
      else return false;
    }
    else if (msg._regionIdentifier != null) return false;
    if (_businessDayConvention != null) {
      if (msg._businessDayConvention != null) {
        if (!_businessDayConvention.equals (msg._businessDayConvention)) return false;
      }
      else return false;
    }
    else if (msg._businessDayConvention != null) return false;
    if (_notional != null) {
      if (msg._notional != null) {
        if (!_notional.equals (msg._notional)) return false;
      }
      else return false;
    }
    else if (msg._notional != null) return false;
    return true;
  }
  public int hashCode () {
    int hc = 1;
    hc *= 31;
    if (_dayCount != null) hc += _dayCount.hashCode ();
    hc *= 31;
    if (_frequency != null) hc += _frequency.hashCode ();
    hc *= 31;
    if (_regionIdentifier != null) hc += _regionIdentifier.hashCode ();
    hc *= 31;
    if (_businessDayConvention != null) hc += _businessDayConvention.hashCode ();
    hc *= 31;
    if (_notional != null) hc += _notional.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
