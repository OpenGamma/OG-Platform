/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.provider.historicaltimeseries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.collect.Iterables;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.time.LocalDateRange;

/**
 * Request to get one or more historical time-series.
 * <p>
 * This class is mutable and not thread-safe.
 */
@PublicSPI
@BeanDefinition
public class HistoricalTimeSeriesProviderGetRequest extends DirectBean {

  /**
   * The set of time-series external identifiers to get.
   */
  @PropertyDefinition
  private final Set<ExternalIdBundle> _externalIdBundles = new HashSet<ExternalIdBundle>();
  /**
   * The data source to use.
   */
  @PropertyDefinition(validate = "notNull")
  private String _dataSource; 
  /**
   * The data provider to use.
   */
  @PropertyDefinition(validate = "notNull")
  private String _dataProvider; 
  /**
   * The data field to use.
   */
  @PropertyDefinition(validate = "notNull")
  private String _dataField;
  /**
   * The date range of the time-series.
   * Default value is a range over all time.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDateRange _dateRange = LocalDateRange.ALL;
  /**
   * The latest value only flag.
   * Default value is false.
   */
  @PropertyDefinition
  private boolean _latestValueOnly;

  //-------------------------------------------------------------------------
  /**
   * Obtains an instance to get a single time-series.
   * 
   * @param externalIdBundle  the identifier bundle, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @param dateRange  the date range to obtain, not null
   * @return the historical time-series, null if not found
   */
  public static HistoricalTimeSeriesProviderGetRequest createGet(
      ExternalIdBundle externalIdBundle,
      String dataSource, String dataProvider, String dataField, LocalDateRange dateRange) {
    HistoricalTimeSeriesProviderGetRequest request = new HistoricalTimeSeriesProviderGetRequest();
    request.addExternalIds(externalIdBundle);
    request.setDataSource(dataSource);
    request.setDataProvider(dataProvider);
    request.setDataField(dataField);
    request.setDateRange(dateRange);
    return request;
  }

  /**
   * Obtains an instance to get a single time-series.
   * 
   * @param externalIdBundle  the identifier bundle, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @param dateRange  the date range to obtain, not null
   * @return the historical time-series, null if not found
   */
  public static HistoricalTimeSeriesProviderGetRequest createGetLatest(
      ExternalIdBundle externalIdBundle,
      String dataSource, String dataProvider, String dataField, LocalDateRange dateRange) {
    HistoricalTimeSeriesProviderGetRequest request = new HistoricalTimeSeriesProviderGetRequest();
    request.addExternalIds(externalIdBundle);
    request.setDataSource(dataSource);
    request.setDataProvider(dataProvider);
    request.setDataField(dataField);
    request.setDateRange(dateRange);
    request.setLatestValueOnly(true);
    return request;
  }

  /**
   * Obtains an instance to get a single time-series.
   * 
   * @param externalIdBundleSet  the identifier bundle set, not null
   * @param dataSource  the data source, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the dataField, not null
   * @param dateRange  the date range to obtain, not null
   * @return the historical time-series, null if not found
   */
  public static HistoricalTimeSeriesProviderGetRequest createGetBulk(
      Set<ExternalIdBundle> externalIdBundleSet,
      String dataSource, String dataProvider, String dataField, LocalDateRange dateRange) {
    HistoricalTimeSeriesProviderGetRequest request = new HistoricalTimeSeriesProviderGetRequest();
    request.addExternalIds(externalIdBundleSet);
    request.setDataSource(dataSource);
    request.setDataProvider(dataProvider);
    request.setDataField(dataField);
    request.setDateRange(dateRange);
    return request;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   */
  protected HistoricalTimeSeriesProviderGetRequest() {
  }

  //-------------------------------------------------------------------------
  /**
   * Adds an array of historical time-series external identifiers to the collection to load.
   * 
   * @param externalIds  the historical time-series identifiers to load, not null
   */
  public void addExternalIds(ExternalId... externalIds) {
    ArgumentChecker.notNull(externalIds, "externalIds");
    List<ExternalIdBundle> list = new ArrayList<ExternalIdBundle>();
    for (ExternalId externalId : externalIds) {
      list.add(ExternalIdBundle.of(externalId));
    }
    getExternalIdBundles().addAll(list);
  }

  /**
   * Adds an array of historical time-series external identifiers to the collection to load.
   * 
   * @param externalIdBundles  the historical time-series identifiers to load, not null
   */
  public void addExternalIds(ExternalIdBundle... externalIdBundles) {
    ArgumentChecker.notNull(externalIdBundles, "externalIdBundles");
    getExternalIdBundles().addAll(Arrays.asList(externalIdBundles));
  }

  /**
   * Adds a collection of historical time-series external identifiers to the collection to load.
   * 
   * @param externalIdBundles  the historical time-series identifiers to load, not null
   */
  public void addExternalIds(Iterable<ExternalIdBundle> externalIdBundles) {
    ArgumentChecker.notNull(externalIdBundles, "externalIdBundles");
    Iterables.addAll(getExternalIdBundles(), externalIdBundles);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code HistoricalTimeSeriesProviderGetRequest}.
   * @return the meta-bean, not null
   */
  public static HistoricalTimeSeriesProviderGetRequest.Meta meta() {
    return HistoricalTimeSeriesProviderGetRequest.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(HistoricalTimeSeriesProviderGetRequest.Meta.INSTANCE);
  }

  @Override
  public HistoricalTimeSeriesProviderGetRequest.Meta metaBean() {
    return HistoricalTimeSeriesProviderGetRequest.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -1369745653:  // externalIdBundles
        return getExternalIdBundles();
      case 1272470629:  // dataSource
        return getDataSource();
      case 339742651:  // dataProvider
        return getDataProvider();
      case -386794640:  // dataField
        return getDataField();
      case -261425617:  // dateRange
        return getDateRange();
      case 2011110646:  // latestValueOnly
        return isLatestValueOnly();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -1369745653:  // externalIdBundles
        setExternalIdBundles((Set<ExternalIdBundle>) newValue);
        return;
      case 1272470629:  // dataSource
        setDataSource((String) newValue);
        return;
      case 339742651:  // dataProvider
        setDataProvider((String) newValue);
        return;
      case -386794640:  // dataField
        setDataField((String) newValue);
        return;
      case -261425617:  // dateRange
        setDateRange((LocalDateRange) newValue);
        return;
      case 2011110646:  // latestValueOnly
        setLatestValueOnly((Boolean) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_dataSource, "dataSource");
    JodaBeanUtils.notNull(_dataProvider, "dataProvider");
    JodaBeanUtils.notNull(_dataField, "dataField");
    JodaBeanUtils.notNull(_dateRange, "dateRange");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      HistoricalTimeSeriesProviderGetRequest other = (HistoricalTimeSeriesProviderGetRequest) obj;
      return JodaBeanUtils.equal(getExternalIdBundles(), other.getExternalIdBundles()) &&
          JodaBeanUtils.equal(getDataSource(), other.getDataSource()) &&
          JodaBeanUtils.equal(getDataProvider(), other.getDataProvider()) &&
          JodaBeanUtils.equal(getDataField(), other.getDataField()) &&
          JodaBeanUtils.equal(getDateRange(), other.getDateRange()) &&
          JodaBeanUtils.equal(isLatestValueOnly(), other.isLatestValueOnly());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getExternalIdBundles());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDataSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDataProvider());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDataField());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDateRange());
    hash += hash * 31 + JodaBeanUtils.hashCode(isLatestValueOnly());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of time-series external identifiers to get.
   * @return the value of the property
   */
  public Set<ExternalIdBundle> getExternalIdBundles() {
    return _externalIdBundles;
  }

  /**
   * Sets the set of time-series external identifiers to get.
   * @param externalIdBundles  the new value of the property
   */
  public void setExternalIdBundles(Set<ExternalIdBundle> externalIdBundles) {
    this._externalIdBundles.clear();
    this._externalIdBundles.addAll(externalIdBundles);
  }

  /**
   * Gets the the {@code externalIdBundles} property.
   * @return the property, not null
   */
  public final Property<Set<ExternalIdBundle>> externalIdBundles() {
    return metaBean().externalIdBundles().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the data source to use.
   * @return the value of the property, not null
   */
  public String getDataSource() {
    return _dataSource;
  }

  /**
   * Sets the data source to use.
   * @param dataSource  the new value of the property, not null
   */
  public void setDataSource(String dataSource) {
    JodaBeanUtils.notNull(dataSource, "dataSource");
    this._dataSource = dataSource;
  }

  /**
   * Gets the the {@code dataSource} property.
   * @return the property, not null
   */
  public final Property<String> dataSource() {
    return metaBean().dataSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the data provider to use.
   * @return the value of the property, not null
   */
  public String getDataProvider() {
    return _dataProvider;
  }

  /**
   * Sets the data provider to use.
   * @param dataProvider  the new value of the property, not null
   */
  public void setDataProvider(String dataProvider) {
    JodaBeanUtils.notNull(dataProvider, "dataProvider");
    this._dataProvider = dataProvider;
  }

  /**
   * Gets the the {@code dataProvider} property.
   * @return the property, not null
   */
  public final Property<String> dataProvider() {
    return metaBean().dataProvider().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the data field to use.
   * @return the value of the property, not null
   */
  public String getDataField() {
    return _dataField;
  }

  /**
   * Sets the data field to use.
   * @param dataField  the new value of the property, not null
   */
  public void setDataField(String dataField) {
    JodaBeanUtils.notNull(dataField, "dataField");
    this._dataField = dataField;
  }

  /**
   * Gets the the {@code dataField} property.
   * @return the property, not null
   */
  public final Property<String> dataField() {
    return metaBean().dataField().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the date range of the time-series.
   * Default value is a range over all time.
   * @return the value of the property, not null
   */
  public LocalDateRange getDateRange() {
    return _dateRange;
  }

  /**
   * Sets the date range of the time-series.
   * Default value is a range over all time.
   * @param dateRange  the new value of the property, not null
   */
  public void setDateRange(LocalDateRange dateRange) {
    JodaBeanUtils.notNull(dateRange, "dateRange");
    this._dateRange = dateRange;
  }

  /**
   * Gets the the {@code dateRange} property.
   * Default value is a range over all time.
   * @return the property, not null
   */
  public final Property<LocalDateRange> dateRange() {
    return metaBean().dateRange().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the latest value only flag.
   * Default value is false.
   * @return the value of the property
   */
  public boolean isLatestValueOnly() {
    return _latestValueOnly;
  }

  /**
   * Sets the latest value only flag.
   * Default value is false.
   * @param latestValueOnly  the new value of the property
   */
  public void setLatestValueOnly(boolean latestValueOnly) {
    this._latestValueOnly = latestValueOnly;
  }

  /**
   * Gets the the {@code latestValueOnly} property.
   * Default value is false.
   * @return the property, not null
   */
  public final Property<Boolean> latestValueOnly() {
    return metaBean().latestValueOnly().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code HistoricalTimeSeriesProviderGetRequest}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code externalIdBundles} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<ExternalIdBundle>> _externalIdBundles = DirectMetaProperty.ofReadWrite(
        this, "externalIdBundles", HistoricalTimeSeriesProviderGetRequest.class, (Class) Set.class);
    /**
     * The meta-property for the {@code dataSource} property.
     */
    private final MetaProperty<String> _dataSource = DirectMetaProperty.ofReadWrite(
        this, "dataSource", HistoricalTimeSeriesProviderGetRequest.class, String.class);
    /**
     * The meta-property for the {@code dataProvider} property.
     */
    private final MetaProperty<String> _dataProvider = DirectMetaProperty.ofReadWrite(
        this, "dataProvider", HistoricalTimeSeriesProviderGetRequest.class, String.class);
    /**
     * The meta-property for the {@code dataField} property.
     */
    private final MetaProperty<String> _dataField = DirectMetaProperty.ofReadWrite(
        this, "dataField", HistoricalTimeSeriesProviderGetRequest.class, String.class);
    /**
     * The meta-property for the {@code dateRange} property.
     */
    private final MetaProperty<LocalDateRange> _dateRange = DirectMetaProperty.ofReadWrite(
        this, "dateRange", HistoricalTimeSeriesProviderGetRequest.class, LocalDateRange.class);
    /**
     * The meta-property for the {@code latestValueOnly} property.
     */
    private final MetaProperty<Boolean> _latestValueOnly = DirectMetaProperty.ofReadWrite(
        this, "latestValueOnly", HistoricalTimeSeriesProviderGetRequest.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "externalIdBundles",
        "dataSource",
        "dataProvider",
        "dataField",
        "dateRange",
        "latestValueOnly");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1369745653:  // externalIdBundles
          return _externalIdBundles;
        case 1272470629:  // dataSource
          return _dataSource;
        case 339742651:  // dataProvider
          return _dataProvider;
        case -386794640:  // dataField
          return _dataField;
        case -261425617:  // dateRange
          return _dateRange;
        case 2011110646:  // latestValueOnly
          return _latestValueOnly;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends HistoricalTimeSeriesProviderGetRequest> builder() {
      return new DirectBeanBuilder<HistoricalTimeSeriesProviderGetRequest>(new HistoricalTimeSeriesProviderGetRequest());
    }

    @Override
    public Class<? extends HistoricalTimeSeriesProviderGetRequest> beanType() {
      return HistoricalTimeSeriesProviderGetRequest.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code externalIdBundles} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<ExternalIdBundle>> externalIdBundles() {
      return _externalIdBundles;
    }

    /**
     * The meta-property for the {@code dataSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> dataSource() {
      return _dataSource;
    }

    /**
     * The meta-property for the {@code dataProvider} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> dataProvider() {
      return _dataProvider;
    }

    /**
     * The meta-property for the {@code dataField} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> dataField() {
      return _dataField;
    }

    /**
     * The meta-property for the {@code dateRange} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDateRange> dateRange() {
      return _dateRange;
    }

    /**
     * The meta-property for the {@code latestValueOnly} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> latestValueOnly() {
      return _latestValueOnly;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
