// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.marketdata.spec;
public class FixedHistoricalMarketDataSpecification extends com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification implements java.io.Serializable {
  private static final long serialVersionUID = -29285166859l;
  private javax.time.calendar.LocalDate _snapshotDate;
  public static final String SNAPSHOT_DATE_KEY = "snapshotDate";
  public FixedHistoricalMarketDataSpecification (javax.time.calendar.DateProvider snapshotDate) {
    if (snapshotDate == null) throw new NullPointerException ("'snapshotDate' cannot be null");
    else {
      _snapshotDate = snapshotDate.toLocalDate ();
    }
  }
  protected FixedHistoricalMarketDataSpecification (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (SNAPSHOT_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FixedHistoricalMarketDataSpecification - field 'snapshotDate' is not present");
    try {
      _snapshotDate = fudgeMsg.getFieldValue (javax.time.calendar.DateProvider.class, fudgeField).toLocalDate ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FixedHistoricalMarketDataSpecification - field 'snapshotDate' is not date", e);
    }
  }
  public FixedHistoricalMarketDataSpecification (String timeSeriesResolverKey, String timeSeriesFieldResolverKey, javax.time.calendar.DateProvider snapshotDate) {
    super (timeSeriesResolverKey, timeSeriesFieldResolverKey);
    if (snapshotDate == null) throw new NullPointerException ("'snapshotDate' cannot be null");
    else {
      _snapshotDate = snapshotDate.toLocalDate ();
    }
  }
  protected FixedHistoricalMarketDataSpecification (final FixedHistoricalMarketDataSpecification source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._snapshotDate == null) _snapshotDate = null;
    else {
      _snapshotDate = source._snapshotDate.toLocalDate ();
    }
  }
  public FixedHistoricalMarketDataSpecification clone () {
    return new FixedHistoricalMarketDataSpecification (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_snapshotDate != null)  {
      msg.add (SNAPSHOT_DATE_KEY, null, _snapshotDate);
    }
  }
  public static FixedHistoricalMarketDataSpecification fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification".equals (className)) break;
      try {
        return (com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new FixedHistoricalMarketDataSpecification (deserializer, fudgeMsg);
  }
  public javax.time.calendar.LocalDate getSnapshotDate () {
    return _snapshotDate;
  }
  public void setSnapshotDate (javax.time.calendar.DateProvider snapshotDate) {
    if (snapshotDate == null) throw new NullPointerException ("'snapshotDate' cannot be null");
    else {
      _snapshotDate = snapshotDate.toLocalDate ();
    }
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof FixedHistoricalMarketDataSpecification)) return false;
    FixedHistoricalMarketDataSpecification msg = (FixedHistoricalMarketDataSpecification)o;
    if (_snapshotDate != null) {
      if (msg._snapshotDate != null) {
        if (!_snapshotDate.equals (msg._snapshotDate)) return false;
      }
      else return false;
    }
    else if (msg._snapshotDate != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_snapshotDate != null) hc += _snapshotDate.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
