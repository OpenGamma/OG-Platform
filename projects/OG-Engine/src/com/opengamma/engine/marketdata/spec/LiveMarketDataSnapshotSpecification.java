// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.marketdata.spec;
public class LiveMarketDataSnapshotSpecification extends com.opengamma.engine.marketdata.spec.MarketDataSnapshotSpecification implements java.io.Serializable {
  private static final long serialVersionUID = 37638471725l;
  private String _dataSource;
  public static final String DATA_SOURCE_KEY = "dataSource";
  public LiveMarketDataSnapshotSpecification () {
  }
  protected LiveMarketDataSnapshotSpecification (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeContext, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (DATA_SOURCE_KEY);
    if (fudgeField != null)  {
      try {
        setDataSource ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a LiveMarketDataSnapshotSpecification - field 'dataSource' is not string", e);
      }
    }
  }
  public LiveMarketDataSnapshotSpecification (String dataSource) {
    _dataSource = dataSource;
  }
  protected LiveMarketDataSnapshotSpecification (final LiveMarketDataSnapshotSpecification source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _dataSource = source._dataSource;
  }
  public LiveMarketDataSnapshotSpecification clone () {
    return new LiveMarketDataSnapshotSpecification (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_dataSource != null)  {
      msg.add (DATA_SOURCE_KEY, null, _dataSource);
    }
  }
  public static LiveMarketDataSnapshotSpecification fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.engine.marketdata.spec.LiveMarketDataSnapshotSpecification".equals (className)) break;
      try {
        return (com.opengamma.engine.marketdata.spec.LiveMarketDataSnapshotSpecification)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeMsg.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new LiveMarketDataSnapshotSpecification (fudgeContext, fudgeMsg);
  }
  public String getDataSource () {
    return _dataSource;
  }
  public void setDataSource (String dataSource) {
    _dataSource = dataSource;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
