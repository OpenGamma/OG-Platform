// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
// Created from com/opengamma/timeseries/config/TimeSeriesMetaDataDefinition.proto:9(10)
package com.opengamma.timeseries.config;
public class TimeSeriesMetaDataDefinition implements java.io.Serializable {
  private static final long serialVersionUID = -9178702820571018106l;
  private String _securityType;
  public static final String SECURITY_TYPE_KEY = "securityType";
  private String _defaultDataSource;
  public static final String DEFAULT_DATA_SOURCE_KEY = "defaultDataSource";
  private java.util.List<String> _dataSources;
  public static final String DATA_SOURCES_KEY = "dataSources";
  private String _defaultDataField;
  public static final String DEFAULT_DATA_FIELD_KEY = "defaultDataField";
  private java.util.List<String> _dataFields;
  public static final String DATA_FIELDS_KEY = "dataFields";
  private String _defaultDataProvider;
  public static final String DEFAULT_DATA_PROVIDER_KEY = "defaultDataProvider";
  private java.util.List<String> _dataProviders;
  public static final String DATA_PROVIDERS_KEY = "dataProviders";
  public TimeSeriesMetaDataDefinition (String securityType, String defaultDataSource, String defaultDataField, String defaultDataProvider) {
    if (securityType == null) throw new NullPointerException ("securityType' cannot be null");
    _securityType = securityType;
    if (defaultDataSource == null) throw new NullPointerException ("defaultDataSource' cannot be null");
    _defaultDataSource = defaultDataSource;
    if (defaultDataField == null) throw new NullPointerException ("defaultDataField' cannot be null");
    _defaultDataField = defaultDataField;
    if (defaultDataProvider == null) throw new NullPointerException ("defaultDataProvider' cannot be null");
    _defaultDataProvider = defaultDataProvider;
  }
  protected TimeSeriesMetaDataDefinition (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    org.fudgemsg.FudgeField fudgeField;
    java.util.List<org.fudgemsg.FudgeField> fudgeFields;
    fudgeField = fudgeMsg.getByName (SECURITY_TYPE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a TimeSeriesMetaDataDefinition - field 'securityType' is not present");
    try {
      _securityType = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a TimeSeriesMetaDataDefinition - field 'securityType' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (DEFAULT_DATA_SOURCE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a TimeSeriesMetaDataDefinition - field 'defaultDataSource' is not present");
    try {
      _defaultDataSource = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a TimeSeriesMetaDataDefinition - field 'defaultDataSource' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (DEFAULT_DATA_FIELD_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a TimeSeriesMetaDataDefinition - field 'defaultDataField' is not present");
    try {
      _defaultDataField = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a TimeSeriesMetaDataDefinition - field 'defaultDataField' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (DEFAULT_DATA_PROVIDER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a TimeSeriesMetaDataDefinition - field 'defaultDataProvider' is not present");
    try {
      _defaultDataProvider = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a TimeSeriesMetaDataDefinition - field 'defaultDataProvider' is not string", e);
    }
    fudgeFields = fudgeMsg.getAllByName (DATA_SOURCES_KEY);
    if (fudgeFields.size () > 0)  {
      final java.util.List<String> fudge1;
      fudge1 = new java.util.ArrayList<String> (fudgeFields.size ());
      for (org.fudgemsg.FudgeField fudge2 : fudgeFields) {
        try {
          fudge1.add (fudge2.getValue ().toString ());
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a TimeSeriesMetaDataDefinition - field 'dataSources' is not string", e);
        }
      }
      setDataSources (fudge1);
    }
    fudgeFields = fudgeMsg.getAllByName (DATA_FIELDS_KEY);
    if (fudgeFields.size () > 0)  {
      final java.util.List<String> fudge1;
      fudge1 = new java.util.ArrayList<String> (fudgeFields.size ());
      for (org.fudgemsg.FudgeField fudge2 : fudgeFields) {
        try {
          fudge1.add (fudge2.getValue ().toString ());
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a TimeSeriesMetaDataDefinition - field 'dataFields' is not string", e);
        }
      }
      setDataFields (fudge1);
    }
    fudgeFields = fudgeMsg.getAllByName (DATA_PROVIDERS_KEY);
    if (fudgeFields.size () > 0)  {
      final java.util.List<String> fudge1;
      fudge1 = new java.util.ArrayList<String> (fudgeFields.size ());
      for (org.fudgemsg.FudgeField fudge2 : fudgeFields) {
        try {
          fudge1.add (fudge2.getValue ().toString ());
        }
        catch (IllegalArgumentException e) {
          throw new IllegalArgumentException ("Fudge message is not a TimeSeriesMetaDataDefinition - field 'dataProviders' is not string", e);
        }
      }
      setDataProviders (fudge1);
    }
  }
  public TimeSeriesMetaDataDefinition (String securityType, String defaultDataSource, java.util.Collection<? extends String> dataSources, String defaultDataField, java.util.Collection<? extends String> dataFields, String defaultDataProvider, java.util.Collection<? extends String> dataProviders) {
    if (securityType == null) throw new NullPointerException ("securityType' cannot be null");
    _securityType = securityType;
    if (defaultDataSource == null) throw new NullPointerException ("defaultDataSource' cannot be null");
    _defaultDataSource = defaultDataSource;
    if (dataSources == null) _dataSources = null;
    else {
      final java.util.List<String> fudge0 = new java.util.ArrayList<String> (dataSources);
      for (java.util.ListIterator<String> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        String fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'dataSources' cannot be null");
      }
      _dataSources = fudge0;
    }
    if (defaultDataField == null) throw new NullPointerException ("defaultDataField' cannot be null");
    _defaultDataField = defaultDataField;
    if (dataFields == null) _dataFields = null;
    else {
      final java.util.List<String> fudge0 = new java.util.ArrayList<String> (dataFields);
      for (java.util.ListIterator<String> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        String fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'dataFields' cannot be null");
      }
      _dataFields = fudge0;
    }
    if (defaultDataProvider == null) throw new NullPointerException ("defaultDataProvider' cannot be null");
    _defaultDataProvider = defaultDataProvider;
    if (dataProviders == null) _dataProviders = null;
    else {
      final java.util.List<String> fudge0 = new java.util.ArrayList<String> (dataProviders);
      for (java.util.ListIterator<String> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        String fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'dataProviders' cannot be null");
      }
      _dataProviders = fudge0;
    }
  }
  protected TimeSeriesMetaDataDefinition (final TimeSeriesMetaDataDefinition source) {
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _securityType = source._securityType;
    _defaultDataSource = source._defaultDataSource;
    if (source._dataSources == null) _dataSources = null;
    else {
      _dataSources = new java.util.ArrayList<String> (source._dataSources);
    }
    _defaultDataField = source._defaultDataField;
    if (source._dataFields == null) _dataFields = null;
    else {
      _dataFields = new java.util.ArrayList<String> (source._dataFields);
    }
    _defaultDataProvider = source._defaultDataProvider;
    if (source._dataProviders == null) _dataProviders = null;
    else {
      _dataProviders = new java.util.ArrayList<String> (source._dataProviders);
    }
  }
  public TimeSeriesMetaDataDefinition clone () {
    return new TimeSeriesMetaDataDefinition (this);
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMessageFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    if (_securityType != null)  {
      msg.add (SECURITY_TYPE_KEY, null, _securityType);
    }
    if (_defaultDataSource != null)  {
      msg.add (DEFAULT_DATA_SOURCE_KEY, null, _defaultDataSource);
    }
    if (_dataSources != null)  {
      for (String fudge1 : _dataSources) {
        msg.add (DATA_SOURCES_KEY, null, fudge1);
      }
    }
    if (_defaultDataField != null)  {
      msg.add (DEFAULT_DATA_FIELD_KEY, null, _defaultDataField);
    }
    if (_dataFields != null)  {
      for (String fudge1 : _dataFields) {
        msg.add (DATA_FIELDS_KEY, null, fudge1);
      }
    }
    if (_defaultDataProvider != null)  {
      msg.add (DEFAULT_DATA_PROVIDER_KEY, null, _defaultDataProvider);
    }
    if (_dataProviders != null)  {
      for (String fudge1 : _dataProviders) {
        msg.add (DATA_PROVIDERS_KEY, null, fudge1);
      }
    }
  }
  public static TimeSeriesMetaDataDefinition fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.timeseries.config.TimeSeriesMetaDataDefinition".equals (className)) break;
      try {
        return (com.opengamma.timeseries.config.TimeSeriesMetaDataDefinition)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new TimeSeriesMetaDataDefinition (fudgeMsg);
  }
  public String getSecurityType () {
    return _securityType;
  }
  public void setSecurityType (String securityType) {
    if (securityType == null) throw new NullPointerException ("securityType' cannot be null");
    _securityType = securityType;
  }
  public String getDefaultDataSource () {
    return _defaultDataSource;
  }
  public void setDefaultDataSource (String defaultDataSource) {
    if (defaultDataSource == null) throw new NullPointerException ("defaultDataSource' cannot be null");
    _defaultDataSource = defaultDataSource;
  }
  public java.util.List<String> getDataSources () {
    if (_dataSources != null) {
      return java.util.Collections.unmodifiableList (_dataSources);
    }
    else return null;
  }
  public void setDataSources (String dataSources) {
    if (dataSources == null) _dataSources = null;
    else {
      _dataSources = new java.util.ArrayList<String> (1);
      addDataSources (dataSources);
    }
  }
  public void setDataSources (java.util.Collection<? extends String> dataSources) {
    if (dataSources == null) _dataSources = null;
    else {
      final java.util.List<String> fudge0 = new java.util.ArrayList<String> (dataSources);
      for (java.util.ListIterator<String> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        String fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'dataSources' cannot be null");
      }
      _dataSources = fudge0;
    }
  }
  public void addDataSources (String dataSources) {
    if (dataSources == null) throw new NullPointerException ("'dataSources' cannot be null");
    if (_dataSources == null) _dataSources = new java.util.ArrayList<String> ();
    _dataSources.add (dataSources);
  }
  public String getDefaultDataField () {
    return _defaultDataField;
  }
  public void setDefaultDataField (String defaultDataField) {
    if (defaultDataField == null) throw new NullPointerException ("defaultDataField' cannot be null");
    _defaultDataField = defaultDataField;
  }
  public java.util.List<String> getDataFields () {
    if (_dataFields != null) {
      return java.util.Collections.unmodifiableList (_dataFields);
    }
    else return null;
  }
  public void setDataFields (String dataFields) {
    if (dataFields == null) _dataFields = null;
    else {
      _dataFields = new java.util.ArrayList<String> (1);
      addDataFields (dataFields);
    }
  }
  public void setDataFields (java.util.Collection<? extends String> dataFields) {
    if (dataFields == null) _dataFields = null;
    else {
      final java.util.List<String> fudge0 = new java.util.ArrayList<String> (dataFields);
      for (java.util.ListIterator<String> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        String fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'dataFields' cannot be null");
      }
      _dataFields = fudge0;
    }
  }
  public void addDataFields (String dataFields) {
    if (dataFields == null) throw new NullPointerException ("'dataFields' cannot be null");
    if (_dataFields == null) _dataFields = new java.util.ArrayList<String> ();
    _dataFields.add (dataFields);
  }
  public String getDefaultDataProvider () {
    return _defaultDataProvider;
  }
  public void setDefaultDataProvider (String defaultDataProvider) {
    if (defaultDataProvider == null) throw new NullPointerException ("defaultDataProvider' cannot be null");
    _defaultDataProvider = defaultDataProvider;
  }
  public java.util.List<String> getDataProviders () {
    if (_dataProviders != null) {
      return java.util.Collections.unmodifiableList (_dataProviders);
    }
    else return null;
  }
  public void setDataProviders (String dataProviders) {
    if (dataProviders == null) _dataProviders = null;
    else {
      _dataProviders = new java.util.ArrayList<String> (1);
      addDataProviders (dataProviders);
    }
  }
  public void setDataProviders (java.util.Collection<? extends String> dataProviders) {
    if (dataProviders == null) _dataProviders = null;
    else {
      final java.util.List<String> fudge0 = new java.util.ArrayList<String> (dataProviders);
      for (java.util.ListIterator<String> fudge1 = fudge0.listIterator (); fudge1.hasNext (); ) {
        String fudge2 = fudge1.next ();
        if (fudge2 == null) throw new NullPointerException ("List element of 'dataProviders' cannot be null");
      }
      _dataProviders = fudge0;
    }
  }
  public void addDataProviders (String dataProviders) {
    if (dataProviders == null) throw new NullPointerException ("'dataProviders' cannot be null");
    if (_dataProviders == null) _dataProviders = new java.util.ArrayList<String> ();
    _dataProviders.add (dataProviders);
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof TimeSeriesMetaDataDefinition)) return false;
    TimeSeriesMetaDataDefinition msg = (TimeSeriesMetaDataDefinition)o;
    if (_securityType != null) {
      if (msg._securityType != null) {
        if (!_securityType.equals (msg._securityType)) return false;
      }
      else return false;
    }
    else if (msg._securityType != null) return false;
    if (_defaultDataSource != null) {
      if (msg._defaultDataSource != null) {
        if (!_defaultDataSource.equals (msg._defaultDataSource)) return false;
      }
      else return false;
    }
    else if (msg._defaultDataSource != null) return false;
    if (_dataSources != null) {
      if (msg._dataSources != null) {
        if (!_dataSources.equals (msg._dataSources)) return false;
      }
      else return false;
    }
    else if (msg._dataSources != null) return false;
    if (_defaultDataField != null) {
      if (msg._defaultDataField != null) {
        if (!_defaultDataField.equals (msg._defaultDataField)) return false;
      }
      else return false;
    }
    else if (msg._defaultDataField != null) return false;
    if (_dataFields != null) {
      if (msg._dataFields != null) {
        if (!_dataFields.equals (msg._dataFields)) return false;
      }
      else return false;
    }
    else if (msg._dataFields != null) return false;
    if (_defaultDataProvider != null) {
      if (msg._defaultDataProvider != null) {
        if (!_defaultDataProvider.equals (msg._defaultDataProvider)) return false;
      }
      else return false;
    }
    else if (msg._defaultDataProvider != null) return false;
    if (_dataProviders != null) {
      if (msg._dataProviders != null) {
        if (!_dataProviders.equals (msg._dataProviders)) return false;
      }
      else return false;
    }
    else if (msg._dataProviders != null) return false;
    return true;
  }
  public int hashCode () {
    int hc = 1;
    hc *= 31;
    if (_securityType != null) hc += _securityType.hashCode ();
    hc *= 31;
    if (_defaultDataSource != null) hc += _defaultDataSource.hashCode ();
    hc *= 31;
    if (_dataSources != null) hc += _dataSources.hashCode ();
    hc *= 31;
    if (_defaultDataField != null) hc += _defaultDataField.hashCode ();
    hc *= 31;
    if (_dataFields != null) hc += _dataFields.hashCode ();
    hc *= 31;
    if (_defaultDataProvider != null) hc += _defaultDataProvider.hashCode ();
    hc *= 31;
    if (_dataProviders != null) hc += _dataProviders.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
