// Automatically created - do not modify - CSOFF
///CLOVER:OFF
package com.opengamma.engine.marketdata.spec;
public class LiveMarketDataSpecification extends com.opengamma.engine.marketdata.spec.MarketDataSpecification implements java.io.Serializable {
  private static final long serialVersionUID = 37638471725l;
  private String _dataSource;
  public static final String DATA_SOURCE_KEY = "dataSource";
  public LiveMarketDataSpecification () {
  }
  protected LiveMarketDataSpecification (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (deserializer, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (DATA_SOURCE_KEY);
    if (fudgeField != null)  {
      try {
        setDataSource ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a LiveMarketDataSpecification - field 'dataSource' is not string", e);
      }
    }
  }
  public LiveMarketDataSpecification (String dataSource) {
    _dataSource = dataSource;
  }
  protected LiveMarketDataSpecification (final LiveMarketDataSpecification source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _dataSource = source._dataSource;
  }
  public LiveMarketDataSpecification clone () {
    return new LiveMarketDataSpecification (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer) {
    if (serializer == null) throw new NullPointerException ("serializer must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = serializer.newMessage ();
    toFudgeMsg (serializer, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializer serializer, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (serializer, msg);
    if (_dataSource != null)  {
      msg.add (DATA_SOURCE_KEY, null, _dataSource);
    }
  }
  public static LiveMarketDataSpecification fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializer deserializer, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification".equals (className)) break;
      try {
        return (com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializer.class, org.fudgemsg.FudgeMsg.class).invoke (null, deserializer, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new LiveMarketDataSpecification (deserializer, fudgeMsg);
  }
  public String getDataSource () {
    return _dataSource;
  }
  public void setDataSource (String dataSource) {
    _dataSource = dataSource;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof LiveMarketDataSpecification)) return false;
    LiveMarketDataSpecification msg = (LiveMarketDataSpecification)o;
    if (_dataSource != null) {
      if (msg._dataSource != null) {
        if (!_dataSource.equals (msg._dataSource)) return false;
      }
      else return false;
    }
    else if (msg._dataSource != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_dataSource != null) hc += _dataSource.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON - CSON
