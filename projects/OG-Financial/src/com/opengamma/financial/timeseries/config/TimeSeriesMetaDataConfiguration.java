package com.opengamma.financial.timeseries.config;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.util.ArgumentChecker;

/**
 * TimeSeriesMetaDataConfiguration representation
 */
public class TimeSeriesMetaDataConfiguration implements java.io.Serializable {
  private final String _securityType;
  private final String _defaultDataSource;
  private Set<String> _dataSources = new TreeSet<String>();
  private final String _defaultDataField;
  private Set<String> _dataFields = new TreeSet<String>();
  private final String _defaultDataProvider;
  private Set<String> _dataProviders = new TreeSet<String>();
  
  public TimeSeriesMetaDataConfiguration(String securityType, String defaultDataSource, String defaultDataField, String defaultDataProvider) {
    ArgumentChecker.notNull(securityType, "securityType");
    ArgumentChecker.notNull(defaultDataSource, "defaultDataSource");
    ArgumentChecker.notNull(defaultDataField, "defaultDataField");
    ArgumentChecker.notNull(defaultDataProvider, "defaultDataProvider");
    _securityType = securityType;
    _defaultDataSource = defaultDataSource;
    _dataSources.add(defaultDataSource);
    _defaultDataField = defaultDataField;
    _dataFields.add(defaultDataField);
    _defaultDataProvider = defaultDataProvider;
    _dataProviders.add(defaultDataProvider);
  }
  
  public String getSecurityType() {
    return _securityType;
  }
  
  public String getDefaultDataSource() {
    return _defaultDataSource;
  }
  
  public Set<String> getDataSources() {
    return Collections.unmodifiableSet(_dataSources);
  }
  
  public void addDataSource(String dataSource) {
    ArgumentChecker.notNull(dataSource, "dataSource");
    _dataSources.add(dataSource);
  }
  
  public String getDefaultDataField() {
    return _defaultDataField;
  }
  
  public Set<String> getDataFields() {
    return Collections.unmodifiableSet(_dataFields);
  }
  
  public void addDataField(String dataField) {
    ArgumentChecker.notNull(dataField, "dataField");
    _dataFields.add(dataField);
  }
  
  public String getDefaultDataProvider() {
    return _defaultDataProvider;
  }
  
  public Set<String> getDataProviders() {
    return Collections.unmodifiableSet(_dataProviders);
  }
  
  public void addDataProvider(String dataProvider) {
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    _dataProviders.add(dataProvider);
  }
  
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof TimeSeriesMetaDataConfiguration) {
      TimeSeriesMetaDataConfiguration other = (TimeSeriesMetaDataConfiguration) obj;
      return ObjectUtils.equals(getSecurityType(), other.getSecurityType()) 
        && ObjectUtils.equals(getDataFields(), other.getDataFields()) 
        && ObjectUtils.equals(getDataProviders(), other.getDataProviders())
        && ObjectUtils.equals(getDataSources(), other.getDataSources());
    }
    return false;
  }
  
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ObjectUtils.hashCode(getSecurityType());
    result = prime * result + ObjectUtils.hashCode(getDataFields());
    result = prime * result + ObjectUtils.hashCode(getDataProviders());
    result = prime * result + ObjectUtils.hashCode(getDataSources());
    return result;
  }
  
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
