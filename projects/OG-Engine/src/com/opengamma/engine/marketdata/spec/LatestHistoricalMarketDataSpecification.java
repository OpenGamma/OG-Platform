// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.marketdata.spec;
public class LatestHistoricalMarketDataSpecification extends com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification implements java.io.Serializable {
  private static final long serialVersionUID = 1l;
  public LatestHistoricalMarketDataSpecification () {
  }
  protected LatestHistoricalMarketDataSpecification (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
  }
  public LatestHistoricalMarketDataSpecification (String timeSeriesResolverKey, String timeSeriesFieldResolverKey) {
    super (timeSeriesResolverKey, timeSeriesFieldResolverKey);
  }
  protected LatestHistoricalMarketDataSpecification (final LatestHistoricalMarketDataSpecification source) {
    super (source);
  }
  public LatestHistoricalMarketDataSpecification clone () {
    return new LatestHistoricalMarketDataSpecification (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
  }
  public static LatestHistoricalMarketDataSpecification fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification".equals (className)) break;
      try {
        return (com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new LatestHistoricalMarketDataSpecification (deserializer, fudgeMsg);
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof LatestHistoricalMarketDataSpecification)) return false;
    LatestHistoricalMarketDataSpecification msg = (LatestHistoricalMarketDataSpecification)o;
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
