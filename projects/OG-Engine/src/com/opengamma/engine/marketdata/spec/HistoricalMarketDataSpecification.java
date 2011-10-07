// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.marketdata.spec;
public class HistoricalMarketDataSpecification extends com.opengamma.engine.marketdata.spec.MarketDataSpecification implements java.io.Serializable {
  private static final long serialVersionUID = -27042536230597185l;
  private javax.time.calendar.LocalDate _snapshotDate;
  public static final String SNAPSHOT_DATE_KEY = "snapshotDate";
  private String _timeSeriesResolverKey;
  public static final String TIME_SERIES_RESOLVER_KEY_KEY = "timeSeriesResolverKey";
  private String _timeSeriesFieldResolverKey;
  public static final String TIME_SERIES_FIELD_RESOLVER_KEY_KEY = "timeSeriesFieldResolverKey";
  public HistoricalMarketDataSpecification (javax.time.calendar.DateProvider snapshotDate) {
    if (snapshotDate == null) throw new NullPointerException ("'snapshotDate' cannot be null");
    else {
      _snapshotDate = snapshotDate.toLocalDate ();
    }
  }
  protected HistoricalMarketDataSpecification (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (SNAPSHOT_DATE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a HistoricalMarketDataSpecification - field 'snapshotDate' is not present");
    try {
      _snapshotDate = fudgeMsg.getFieldValue (javax.time.calendar.DateProvider.class, fudgeField).toLocalDate ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a HistoricalMarketDataSpecification - field 'snapshotDate' is not date", e);
    }
    fudgeField = fudgeMsg.getByName (TIME_SERIES_RESOLVER_KEY_KEY);
    if (fudgeField != null)  {
      try {
        setTimeSeriesResolverKey ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a HistoricalMarketDataSpecification - field 'timeSeriesResolverKey' is not string", e);
      }
    }
    fudgeField = fudgeMsg.getByName (TIME_SERIES_FIELD_RESOLVER_KEY_KEY);
    if (fudgeField != null)  {
      try {
        setTimeSeriesFieldResolverKey ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a HistoricalMarketDataSpecification - field 'timeSeriesFieldResolverKey' is not string", e);
      }
    }
  }
  public HistoricalMarketDataSpecification (javax.time.calendar.DateProvider snapshotDate, String timeSeriesResolverKey, String timeSeriesFieldResolverKey) {
    if (snapshotDate == null) throw new NullPointerException ("'snapshotDate' cannot be null");
    else {
      _snapshotDate = snapshotDate.toLocalDate ();
    }
    _timeSeriesResolverKey = timeSeriesResolverKey;
    _timeSeriesFieldResolverKey = timeSeriesFieldResolverKey;
  }
  protected HistoricalMarketDataSpecification (final HistoricalMarketDataSpecification source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._snapshotDate == null) _snapshotDate = null;
    else {
      _snapshotDate = source._snapshotDate.toLocalDate ();
    }
    _timeSeriesResolverKey = source._timeSeriesResolverKey;
    _timeSeriesFieldResolverKey = source._timeSeriesFieldResolverKey;
  }
  public HistoricalMarketDataSpecification clone () {
    return new HistoricalMarketDataSpecification (this);
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
    if (_timeSeriesResolverKey != null)  {
      msg.add (TIME_SERIES_RESOLVER_KEY_KEY, null, _timeSeriesResolverKey);
    }
    if (_timeSeriesFieldResolverKey != null)  {
      msg.add (TIME_SERIES_FIELD_RESOLVER_KEY_KEY, null, _timeSeriesFieldResolverKey);
    }
  }
  public static HistoricalMarketDataSpecification fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification".equals (className)) break;
      try {
        return (com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new HistoricalMarketDataSpecification (deserializer, fudgeMsg);
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
  public String getTimeSeriesResolverKey () {
    return _timeSeriesResolverKey;
  }
  public void setTimeSeriesResolverKey (String timeSeriesResolverKey) {
    _timeSeriesResolverKey = timeSeriesResolverKey;
  }
  public String getTimeSeriesFieldResolverKey () {
    return _timeSeriesFieldResolverKey;
  }
  public void setTimeSeriesFieldResolverKey (String timeSeriesFieldResolverKey) {
    _timeSeriesFieldResolverKey = timeSeriesFieldResolverKey;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof HistoricalMarketDataSpecification)) return false;
    HistoricalMarketDataSpecification msg = (HistoricalMarketDataSpecification)o;
    if (_snapshotDate != null) {
      if (msg._snapshotDate != null) {
        if (!_snapshotDate.equals (msg._snapshotDate)) return false;
      }
      else return false;
    }
    else if (msg._snapshotDate != null) return false;
    if (_timeSeriesResolverKey != null) {
      if (msg._timeSeriesResolverKey != null) {
        if (!_timeSeriesResolverKey.equals (msg._timeSeriesResolverKey)) return false;
      }
      else return false;
    }
    else if (msg._timeSeriesResolverKey != null) return false;
    if (_timeSeriesFieldResolverKey != null) {
      if (msg._timeSeriesFieldResolverKey != null) {
        if (!_timeSeriesFieldResolverKey.equals (msg._timeSeriesFieldResolverKey)) return false;
      }
      else return false;
    }
    else if (msg._timeSeriesFieldResolverKey != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_snapshotDate != null) hc += _snapshotDate.hashCode ();
    hc *= 31;
    if (_timeSeriesResolverKey != null) hc += _timeSeriesResolverKey.hashCode ();
    hc *= 31;
    if (_timeSeriesFieldResolverKey != null) hc += _timeSeriesFieldResolverKey.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
