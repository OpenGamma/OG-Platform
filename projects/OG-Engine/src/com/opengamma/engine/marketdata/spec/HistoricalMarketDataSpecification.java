// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.engine.marketdata.spec;
public class HistoricalMarketDataSpecification extends com.opengamma.engine.marketdata.spec.MarketDataSpecification implements java.io.Serializable {
  private static final long serialVersionUID = -7509181024321257288l;
  private javax.time.calendar.LocalDate _snapshotDate;
  public static final String SNAPSHOT_DATE_KEY = "snapshotDate";
  private String _dataSource;
  public static final String DATA_SOURCE_KEY = "dataSource";
  private String _dataProvider;
  public static final String DATA_PROVIDER_KEY = "dataProvider";
  private String _dataField;
  public static final String DATA_FIELD_KEY = "dataField";
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
    fudgeField = fudgeMsg.getByName (DATA_SOURCE_KEY);
    if (fudgeField != null)  {
      try {
        setDataSource ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a HistoricalMarketDataSpecification - field 'dataSource' is not string", e);
      }
    }
    fudgeField = fudgeMsg.getByName (DATA_PROVIDER_KEY);
    if (fudgeField != null)  {
      try {
        setDataProvider ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a HistoricalMarketDataSpecification - field 'dataProvider' is not string", e);
      }
    }
    fudgeField = fudgeMsg.getByName (DATA_FIELD_KEY);
    if (fudgeField != null)  {
      try {
        setDataField ((fudgeField.getValue () != null) ? fudgeField.getValue ().toString () : null);
      }
      catch (IllegalArgumentException e) {
        throw new IllegalArgumentException ("Fudge message is not a HistoricalMarketDataSpecification - field 'dataField' is not string", e);
      }
    }
  }
  public HistoricalMarketDataSpecification (javax.time.calendar.DateProvider snapshotDate, String dataSource, String dataProvider, String dataField) {
    if (snapshotDate == null) throw new NullPointerException ("'snapshotDate' cannot be null");
    else {
      _snapshotDate = snapshotDate.toLocalDate ();
    }
    _dataSource = dataSource;
    _dataProvider = dataProvider;
    _dataField = dataField;
  }
  protected HistoricalMarketDataSpecification (final HistoricalMarketDataSpecification source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._snapshotDate == null) _snapshotDate = null;
    else {
      _snapshotDate = source._snapshotDate.toLocalDate ();
    }
    _dataSource = source._dataSource;
    _dataProvider = source._dataProvider;
    _dataField = source._dataField;
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
    if (_dataSource != null)  {
      msg.add (DATA_SOURCE_KEY, null, _dataSource);
    }
    if (_dataProvider != null)  {
      msg.add (DATA_PROVIDER_KEY, null, _dataProvider);
    }
    if (_dataField != null)  {
      msg.add (DATA_FIELD_KEY, null, _dataField);
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
  public String getDataSource () {
    return _dataSource;
  }
  public void setDataSource (String dataSource) {
    _dataSource = dataSource;
  }
  public String getDataProvider () {
    return _dataProvider;
  }
  public void setDataProvider (String dataProvider) {
    _dataProvider = dataProvider;
  }
  public String getDataField () {
    return _dataField;
  }
  public void setDataField (String dataField) {
    _dataField = dataField;
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
    if (_dataSource != null) {
      if (msg._dataSource != null) {
        if (!_dataSource.equals (msg._dataSource)) return false;
      }
      else return false;
    }
    else if (msg._dataSource != null) return false;
    if (_dataProvider != null) {
      if (msg._dataProvider != null) {
        if (!_dataProvider.equals (msg._dataProvider)) return false;
      }
      else return false;
    }
    else if (msg._dataProvider != null) return false;
    if (_dataField != null) {
      if (msg._dataField != null) {
        if (!_dataField.equals (msg._dataField)) return false;
      }
      else return false;
    }
    else if (msg._dataField != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_snapshotDate != null) hc += _snapshotDate.hashCode ();
    hc *= 31;
    if (_dataSource != null) hc += _dataSource.hashCode ();
    hc *= 31;
    if (_dataProvider != null) hc += _dataProvider.hashCode ();
    hc *= 31;
    if (_dataField != null) hc += _dataField.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
