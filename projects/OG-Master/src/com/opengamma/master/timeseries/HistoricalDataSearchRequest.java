/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.time.calendar.LocalDate;

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

import com.google.common.collect.Lists;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierBundleWithDates;
import com.opengamma.id.IdentifierWithDates;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.RegexUtils;
import com.opengamma.util.db.PagingRequest;

/**
 * Request for searching for time-series.
 * <p>
 * Documents will be returned that match the search criteria.
 * This class provides the ability to page the results.
 * <p>
 * This class is mutable and not thread-safe.
 */
@PublicSPI
@BeanDefinition
public class HistoricalDataSearchRequest extends DirectBean {

  /**
   * The request for paging.
   * By default all matching items will be returned.
   */
  @PropertyDefinition
  private PagingRequest _pagingRequest = PagingRequest.ALL;
  /**
   * The identifiers to match, empty to not match on identifiers.
   * This will return time-series where at least one complete identifier in the series matches
   * at least one complete identifier in this bundle. Note that an empty bundle will not match
   * anything, whereas a null bundle places no restrictions on the result.
   * This method is suitable for exact machine searching, whereas the {@code identifierValue}
   * search is useful for human searching.
   */
  @PropertyDefinition(set = "manual")
  private List<Identifier> _identifiers;
  /**
   * The identifier value, matching against the <b>value</b> of the identifiers,
   * null to not match by identifier value.
   * This matches against the {@link Identifier#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code identifiers}
   * search is useful for exact machine searching.
   */
  @PropertyDefinition
  private String _identifierValue;
  /**
   * The date on which identifiers must be valid.
   */
  @PropertyDefinition
  private LocalDate _identifierValidityDate;
  /**
   * The data source, wildcards allowed, null to not match on data source.
   */
  @PropertyDefinition
  private String _dataSource;
  /**
   * The data provider, wildcards allowed, null to not match on data provider.
   */
  @PropertyDefinition
  private String _dataProvider; 
  /**
   * The data field, wildcards allowed, null to not match on data field.
   */
  @PropertyDefinition
  private String _dataField;
  /**
   * The observation time, wildcards allowed, null to not match on observation time.
   */
  @PropertyDefinition
  private String _observationTime;
  /**
   * Set to true to load data points, otherwise return just info.
   */
  @PropertyDefinition
  private boolean _loadTimeSeries;
  /**
   * Set to true to load the earliest and latest date for time-series.
   */
  @PropertyDefinition
  private boolean _loadEarliestLatest;
  /**
   * The start date, inclusive, null returns data from the earliest valid date.
   */
  @PropertyDefinition
  private LocalDate _start; 
  /**
   * The end date, inclusive, null returns data up to the latest valid date.
   */
  @PropertyDefinition
  private LocalDate _end;

  /**
   * Creates an instance.
   */
  public HistoricalDataSearchRequest() {
  }

  /**
   * Creates an instance using a single search identifier.
   * 
   * @param identifier  the identifier to search for, not null
   */
  public HistoricalDataSearchRequest(Identifier identifier) {
    addIdentifier(identifier);
  }

  /**
   * Creates an instance using a bundle of identifiers.
   * 
   * @param identifiers  the identifiers to search for, not null
   */
  public HistoricalDataSearchRequest(Iterable<Identifier> identifiers) {
    setIdentifiers(identifiers);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single security key identifier to the collection to search for.
   * 
   * @param identifier  the identifier to add, not null
   */
  public void addIdentifier(Identifier identifier) {
    ArgumentChecker.notNull(identifier, "identifier");
    if (_identifiers == null) {
      _identifiers = new ArrayList<Identifier>();
    }
    _identifiers.add(identifier);
  }

  /**
   * Adds a collection of identifiers to the collection to search for.
   * 
   * @param identifiers  the identifiers to add, not null
   */
  public void setIdentifiers(Identifier... identifiers) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    if (identifiers == null) {
      _identifiers = null;
    } else {
      _identifiers = new ArrayList<Identifier>(Arrays.asList(identifiers));
    }
  }

  /**
   * Adds a collection of identifiers to the collection to search for.
   * 
   * @param identifiers  the identifiers to add, not null
   */
  public void setIdentifiers(List<Identifier> identifiers) {  // signature for bean API
    setIdentifiers((Iterable<Identifier>) identifiers);
  }

  /**
   * Adds a collection of identifiers to the collection to search for.
   * 
   * @param identifiers  the identifiers to add, not null
   */
  public void setIdentifiers(Iterable<Identifier> identifiers) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    if (identifiers == null) {
      _identifiers = null;
    } else {
      _identifiers = Lists.newArrayList(identifiers);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this search matches the specified document.
   * 
   * @param document  the document to match, null or inappropriate document type returns false
   * @return true if matches
   */
  public boolean matches(HistoricalDataDocument document) {
    if (getDataSource() != null && getDataSource().equals(document.getDataSource()) == false) {
      return false;
    }    
    if (getDataProvider() != null && getDataProvider().equals(document.getDataProvider()) == false) {
      return false;
    }    
    if (getDataField() != null && getDataField().equals(document.getDataField()) == false) {
      return false;
    }    
    if (getObservationTime() != null && getObservationTime().equals(document.getObservationTime()) == false) {
      return false;
    }    
    if (getIdentifiers() != null) {
      success: {  // label used with break statement, CSIGNORE
        IdentifierBundleWithDates docBundle = document.getIdentifiers();
        for (IdentifierWithDates docId : docBundle) {
          if (getIdentifiers().contains(docId.asIdentifier()) && docId.isValidOn(getIdentifierValidityDate())) {
            break success;
          }
        }
        return false;
      }
    }
    if (getIdentifierValue() != null) {
      success: {  // label used with break statement, CSIGNORE
        IdentifierBundle docBundle = document.getIdentifiers().asIdentifierBundle();
        for (Identifier identifier : docBundle.getIdentifiers()) {
          if (RegexUtils.wildcardMatch(getIdentifierValue(), identifier.getValue())) {
            break success;
          }
        }
        return false;
      }
    }
    return true;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code TimeSeriesSearchRequest}.
   * @return the meta-bean, not null
   */
  public static HistoricalDataSearchRequest.Meta meta() {
    return HistoricalDataSearchRequest.Meta.INSTANCE;
  }

  @Override
  public HistoricalDataSearchRequest.Meta metaBean() {
    return HistoricalDataSearchRequest.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case -2092032669:  // pagingRequest
        return getPagingRequest();
      case 1368189162:  // identifiers
        return getIdentifiers();
      case 2085582408:  // identifierValue
        return getIdentifierValue();
      case 48758089:  // identifierValidityDate
        return getIdentifierValidityDate();
      case 1272470629:  // dataSource
        return getDataSource();
      case 339742651:  // dataProvider
        return getDataProvider();
      case -386794640:  // dataField
        return getDataField();
      case 951232793:  // observationTime
        return getObservationTime();
      case 1833789738:  // loadTimeSeries
        return isLoadTimeSeries();
      case -771242688:  // loadEarliestLatest
        return isLoadEarliestLatest();
      case 109757538:  // start
        return getStart();
      case 100571:  // end
        return getEnd();
    }
    return super.propertyGet(propertyName);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case -2092032669:  // pagingRequest
        setPagingRequest((PagingRequest) newValue);
        return;
      case 1368189162:  // identifiers
        setIdentifiers((List<Identifier>) newValue);
        return;
      case 2085582408:  // identifierValue
        setIdentifierValue((String) newValue);
        return;
      case 48758089:  // identifierValidityDate
        setIdentifierValidityDate((LocalDate) newValue);
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
      case 951232793:  // observationTime
        setObservationTime((String) newValue);
        return;
      case 1833789738:  // loadTimeSeries
        setLoadTimeSeries((Boolean) newValue);
        return;
      case -771242688:  // loadEarliestLatest
        setLoadEarliestLatest((Boolean) newValue);
        return;
      case 109757538:  // start
        setStart((LocalDate) newValue);
        return;
      case 100571:  // end
        setEnd((LocalDate) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      HistoricalDataSearchRequest other = (HistoricalDataSearchRequest) obj;
      return JodaBeanUtils.equal(getPagingRequest(), other.getPagingRequest()) &&
          JodaBeanUtils.equal(getIdentifiers(), other.getIdentifiers()) &&
          JodaBeanUtils.equal(getIdentifierValue(), other.getIdentifierValue()) &&
          JodaBeanUtils.equal(getIdentifierValidityDate(), other.getIdentifierValidityDate()) &&
          JodaBeanUtils.equal(getDataSource(), other.getDataSource()) &&
          JodaBeanUtils.equal(getDataProvider(), other.getDataProvider()) &&
          JodaBeanUtils.equal(getDataField(), other.getDataField()) &&
          JodaBeanUtils.equal(getObservationTime(), other.getObservationTime()) &&
          JodaBeanUtils.equal(isLoadTimeSeries(), other.isLoadTimeSeries()) &&
          JodaBeanUtils.equal(isLoadEarliestLatest(), other.isLoadEarliestLatest()) &&
          JodaBeanUtils.equal(getStart(), other.getStart()) &&
          JodaBeanUtils.equal(getEnd(), other.getEnd());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getPagingRequest());
    hash += hash * 31 + JodaBeanUtils.hashCode(getIdentifiers());
    hash += hash * 31 + JodaBeanUtils.hashCode(getIdentifierValue());
    hash += hash * 31 + JodaBeanUtils.hashCode(getIdentifierValidityDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDataSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDataProvider());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDataField());
    hash += hash * 31 + JodaBeanUtils.hashCode(getObservationTime());
    hash += hash * 31 + JodaBeanUtils.hashCode(isLoadTimeSeries());
    hash += hash * 31 + JodaBeanUtils.hashCode(isLoadEarliestLatest());
    hash += hash * 31 + JodaBeanUtils.hashCode(getStart());
    hash += hash * 31 + JodaBeanUtils.hashCode(getEnd());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the request for paging.
   * By default all matching items will be returned.
   * @return the value of the property
   */
  public PagingRequest getPagingRequest() {
    return _pagingRequest;
  }

  /**
   * Sets the request for paging.
   * By default all matching items will be returned.
   * @param pagingRequest  the new value of the property
   */
  public void setPagingRequest(PagingRequest pagingRequest) {
    this._pagingRequest = pagingRequest;
  }

  /**
   * Gets the the {@code pagingRequest} property.
   * By default all matching items will be returned.
   * @return the property, not null
   */
  public final Property<PagingRequest> pagingRequest() {
    return metaBean().pagingRequest().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the identifiers to match, empty to not match on identifiers.
   * This will return time-series where at least one complete identifier in the series matches
   * at least one complete identifier in this bundle. Note that an empty bundle will not match
   * anything, whereas a null bundle places no restrictions on the result.
   * This method is suitable for exact machine searching, whereas the {@code identifierValue}
   * search is useful for human searching.
   * @return the value of the property
   */
  public List<Identifier> getIdentifiers() {
    return _identifiers;
  }

  /**
   * Gets the the {@code identifiers} property.
   * This will return time-series where at least one complete identifier in the series matches
   * at least one complete identifier in this bundle. Note that an empty bundle will not match
   * anything, whereas a null bundle places no restrictions on the result.
   * This method is suitable for exact machine searching, whereas the {@code identifierValue}
   * search is useful for human searching.
   * @return the property, not null
   */
  public final Property<List<Identifier>> identifiers() {
    return metaBean().identifiers().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the identifier value, matching against the <b>value</b> of the identifiers,
   * null to not match by identifier value.
   * This matches against the {@link Identifier#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code identifiers}
   * search is useful for exact machine searching.
   * @return the value of the property
   */
  public String getIdentifierValue() {
    return _identifierValue;
  }

  /**
   * Sets the identifier value, matching against the <b>value</b> of the identifiers,
   * null to not match by identifier value.
   * This matches against the {@link Identifier#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code identifiers}
   * search is useful for exact machine searching.
   * @param identifierValue  the new value of the property
   */
  public void setIdentifierValue(String identifierValue) {
    this._identifierValue = identifierValue;
  }

  /**
   * Gets the the {@code identifierValue} property.
   * null to not match by identifier value.
   * This matches against the {@link Identifier#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code identifiers}
   * search is useful for exact machine searching.
   * @return the property, not null
   */
  public final Property<String> identifierValue() {
    return metaBean().identifierValue().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the date on which identifiers must be valid.
   * @return the value of the property
   */
  public LocalDate getIdentifierValidityDate() {
    return _identifierValidityDate;
  }

  /**
   * Sets the date on which identifiers must be valid.
   * @param identifierValidityDate  the new value of the property
   */
  public void setIdentifierValidityDate(LocalDate identifierValidityDate) {
    this._identifierValidityDate = identifierValidityDate;
  }

  /**
   * Gets the the {@code identifierValidityDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> identifierValidityDate() {
    return metaBean().identifierValidityDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the data source, wildcards allowed, null to not match on data source.
   * @return the value of the property
   */
  public String getDataSource() {
    return _dataSource;
  }

  /**
   * Sets the data source, wildcards allowed, null to not match on data source.
   * @param dataSource  the new value of the property
   */
  public void setDataSource(String dataSource) {
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
   * Gets the data provider, wildcards allowed, null to not match on data provider.
   * @return the value of the property
   */
  public String getDataProvider() {
    return _dataProvider;
  }

  /**
   * Sets the data provider, wildcards allowed, null to not match on data provider.
   * @param dataProvider  the new value of the property
   */
  public void setDataProvider(String dataProvider) {
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
   * Gets the data field, wildcards allowed, null to not match on data field.
   * @return the value of the property
   */
  public String getDataField() {
    return _dataField;
  }

  /**
   * Sets the data field, wildcards allowed, null to not match on data field.
   * @param dataField  the new value of the property
   */
  public void setDataField(String dataField) {
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
   * Gets the observation time, wildcards allowed, null to not match on observation time.
   * @return the value of the property
   */
  public String getObservationTime() {
    return _observationTime;
  }

  /**
   * Sets the observation time, wildcards allowed, null to not match on observation time.
   * @param observationTime  the new value of the property
   */
  public void setObservationTime(String observationTime) {
    this._observationTime = observationTime;
  }

  /**
   * Gets the the {@code observationTime} property.
   * @return the property, not null
   */
  public final Property<String> observationTime() {
    return metaBean().observationTime().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets set to true to load data points, otherwise return just info.
   * @return the value of the property
   */
  public boolean isLoadTimeSeries() {
    return _loadTimeSeries;
  }

  /**
   * Sets set to true to load data points, otherwise return just info.
   * @param loadTimeSeries  the new value of the property
   */
  public void setLoadTimeSeries(boolean loadTimeSeries) {
    this._loadTimeSeries = loadTimeSeries;
  }

  /**
   * Gets the the {@code loadTimeSeries} property.
   * @return the property, not null
   */
  public final Property<Boolean> loadTimeSeries() {
    return metaBean().loadTimeSeries().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets set to true to load the earliest and latest date for time-series.
   * @return the value of the property
   */
  public boolean isLoadEarliestLatest() {
    return _loadEarliestLatest;
  }

  /**
   * Sets set to true to load the earliest and latest date for time-series.
   * @param loadEarliestLatest  the new value of the property
   */
  public void setLoadEarliestLatest(boolean loadEarliestLatest) {
    this._loadEarliestLatest = loadEarliestLatest;
  }

  /**
   * Gets the the {@code loadEarliestLatest} property.
   * @return the property, not null
   */
  public final Property<Boolean> loadEarliestLatest() {
    return metaBean().loadEarliestLatest().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the start date, inclusive, null returns data from the earliest valid date.
   * @return the value of the property
   */
  public LocalDate getStart() {
    return _start;
  }

  /**
   * Sets the start date, inclusive, null returns data from the earliest valid date.
   * @param start  the new value of the property
   */
  public void setStart(LocalDate start) {
    this._start = start;
  }

  /**
   * Gets the the {@code start} property.
   * @return the property, not null
   */
  public final Property<LocalDate> start() {
    return metaBean().start().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the end date, inclusive, null returns data up to the latest valid date.
   * @return the value of the property
   */
  public LocalDate getEnd() {
    return _end;
  }

  /**
   * Sets the end date, inclusive, null returns data up to the latest valid date.
   * @param end  the new value of the property
   */
  public void setEnd(LocalDate end) {
    this._end = end;
  }

  /**
   * Gets the the {@code end} property.
   * @return the property, not null
   */
  public final Property<LocalDate> end() {
    return metaBean().end().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code TimeSeriesSearchRequest}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code pagingRequest} property.
     */
    private final MetaProperty<PagingRequest> _pagingRequest = DirectMetaProperty.ofReadWrite(
        this, "pagingRequest", HistoricalDataSearchRequest.class, PagingRequest.class);
    /**
     * The meta-property for the {@code identifiers} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<Identifier>> _identifiers = DirectMetaProperty.ofReadWrite(
        this, "identifiers", HistoricalDataSearchRequest.class, (Class) List.class);
    /**
     * The meta-property for the {@code identifierValue} property.
     */
    private final MetaProperty<String> _identifierValue = DirectMetaProperty.ofReadWrite(
        this, "identifierValue", HistoricalDataSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code identifierValidityDate} property.
     */
    private final MetaProperty<LocalDate> _identifierValidityDate = DirectMetaProperty.ofReadWrite(
        this, "identifierValidityDate", HistoricalDataSearchRequest.class, LocalDate.class);
    /**
     * The meta-property for the {@code dataSource} property.
     */
    private final MetaProperty<String> _dataSource = DirectMetaProperty.ofReadWrite(
        this, "dataSource", HistoricalDataSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code dataProvider} property.
     */
    private final MetaProperty<String> _dataProvider = DirectMetaProperty.ofReadWrite(
        this, "dataProvider", HistoricalDataSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code dataField} property.
     */
    private final MetaProperty<String> _dataField = DirectMetaProperty.ofReadWrite(
        this, "dataField", HistoricalDataSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code observationTime} property.
     */
    private final MetaProperty<String> _observationTime = DirectMetaProperty.ofReadWrite(
        this, "observationTime", HistoricalDataSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code loadTimeSeries} property.
     */
    private final MetaProperty<Boolean> _loadTimeSeries = DirectMetaProperty.ofReadWrite(
        this, "loadTimeSeries", HistoricalDataSearchRequest.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code loadEarliestLatest} property.
     */
    private final MetaProperty<Boolean> _loadEarliestLatest = DirectMetaProperty.ofReadWrite(
        this, "loadEarliestLatest", HistoricalDataSearchRequest.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code start} property.
     */
    private final MetaProperty<LocalDate> _start = DirectMetaProperty.ofReadWrite(
        this, "start", HistoricalDataSearchRequest.class, LocalDate.class);
    /**
     * The meta-property for the {@code end} property.
     */
    private final MetaProperty<LocalDate> _end = DirectMetaProperty.ofReadWrite(
        this, "end", HistoricalDataSearchRequest.class, LocalDate.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
        this, null,
        "pagingRequest",
        "identifiers",
        "identifierValue",
        "identifierValidityDate",
        "dataSource",
        "dataProvider",
        "dataField",
        "observationTime",
        "loadTimeSeries",
        "loadEarliestLatest",
        "start",
        "end");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -2092032669:  // pagingRequest
          return _pagingRequest;
        case 1368189162:  // identifiers
          return _identifiers;
        case 2085582408:  // identifierValue
          return _identifierValue;
        case 48758089:  // identifierValidityDate
          return _identifierValidityDate;
        case 1272470629:  // dataSource
          return _dataSource;
        case 339742651:  // dataProvider
          return _dataProvider;
        case -386794640:  // dataField
          return _dataField;
        case 951232793:  // observationTime
          return _observationTime;
        case 1833789738:  // loadTimeSeries
          return _loadTimeSeries;
        case -771242688:  // loadEarliestLatest
          return _loadEarliestLatest;
        case 109757538:  // start
          return _start;
        case 100571:  // end
          return _end;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends HistoricalDataSearchRequest> builder() {
      return new DirectBeanBuilder<HistoricalDataSearchRequest>(new HistoricalDataSearchRequest());
    }

    @Override
    public Class<? extends HistoricalDataSearchRequest> beanType() {
      return HistoricalDataSearchRequest.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code pagingRequest} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PagingRequest> pagingRequest() {
      return _pagingRequest;
    }

    /**
     * The meta-property for the {@code identifiers} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<Identifier>> identifiers() {
      return _identifiers;
    }

    /**
     * The meta-property for the {@code identifierValue} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> identifierValue() {
      return _identifierValue;
    }

    /**
     * The meta-property for the {@code identifierValidityDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> identifierValidityDate() {
      return _identifierValidityDate;
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
     * The meta-property for the {@code observationTime} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> observationTime() {
      return _observationTime;
    }

    /**
     * The meta-property for the {@code loadTimeSeries} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> loadTimeSeries() {
      return _loadTimeSeries;
    }

    /**
     * The meta-property for the {@code loadEarliestLatest} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> loadEarliestLatest() {
      return _loadEarliestLatest;
    }

    /**
     * The meta-property for the {@code start} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> start() {
      return _start;
    }

    /**
     * The meta-property for the {@code end} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> end() {
      return _end;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
