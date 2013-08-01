// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.engine.marketdata.spec;
public class HistoricalMarketDataSpecification extends com.opengamma.engine.marketdata.spec.MarketDataSpecification implements java.io.Serializable {
  private static final long serialVersionUID = 3103680605l;
  private String _timeSeriesResolverKey;
  public static final String TIME_SERIES_RESOLVER_KEY_KEY = "timeSeriesResolverKey";
  public HistoricalMarketDataSpecification () {
  }
  protected HistoricalMarketDataSpecification (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (TIME_SERIES_RESOLVER_KEY_KEY);
    if (fudgeField != null)  {
      try {
        setTimeSeriesResolverKey ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a HistoricalMarketDataSpecification - field 'timeSeriesResolverKey' is not string", e);
      }
    }
  }
  public HistoricalMarketDataSpecification (String timeSeriesResolverKey) {
    _timeSeriesResolverKey = timeSeriesResolverKey;
  }
  protected HistoricalMarketDataSpecification (final HistoricalMarketDataSpecification source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _timeSeriesResolverKey = source._timeSeriesResolverKey;
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
    if (_timeSeriesResolverKey != null)  {
      msg.add (TIME_SERIES_RESOLVER_KEY_KEY, null, _timeSeriesResolverKey);
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
  public String getTimeSeriesResolverKey () {
    return _timeSeriesResolverKey;
  }
  public void setTimeSeriesResolverKey (String timeSeriesResolverKey) {
    _timeSeriesResolverKey = timeSeriesResolverKey;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof HistoricalMarketDataSpecification)) return false;
    HistoricalMarketDataSpecification msg = (HistoricalMarketDataSpecification)o;
    if (_timeSeriesResolverKey != null) {
      if (msg._timeSeriesResolverKey != null) {
        if (!_timeSeriesResolverKey.equals (msg._timeSeriesResolverKey)) return false;
      }
      else return false;
    }
    else if (msg._timeSeriesResolverKey != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_timeSeriesResolverKey != null) hc += _timeSeriesResolverKey.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
