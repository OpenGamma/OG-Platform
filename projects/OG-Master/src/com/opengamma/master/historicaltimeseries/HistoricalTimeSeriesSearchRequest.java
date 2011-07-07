/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries;


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
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierSearch;
import com.opengamma.id.IdentifierSearchType;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.RegexUtils;

/**
 * Request for searching for historical time-series.
 * <p>
 * Documents will be returned that match the search criteria.
 * This class provides the ability to page the results.
 * <p>
 * This class is mutable and not thread-safe.
 */
@PublicSPI
@BeanDefinition
public class HistoricalTimeSeriesSearchRequest extends AbstractSearchRequest {

  /**
   * The set of time-series object identifiers, null to not limit by exchange object identifiers.
   * Note that an empty set will return no time-series.
   */
  @PropertyDefinition(set = "manual")
  private List<ObjectIdentifier> _historicalTimeSeriesIds;
  /**
   * The time-series keys to match, null to not match on time-series keys.
   */
  @PropertyDefinition
  private IdentifierSearch _historicalTimeSeriesKeys;
  /**
   * The identifier value, matching against the <i>value</i> of the identifiers,
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
   * The time-series name, wildcards allowed, null to not match on name.
   */
  @PropertyDefinition
  private String _name;
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
   * Set to true to load the earliest and latest date for time-series.
   */
  @PropertyDefinition
  private boolean _loadEarliestLatest;
  /**
   * Set to true to load data points, otherwise return just info.
   */
  @PropertyDefinition
  private boolean _loadTimeSeries;
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
  public HistoricalTimeSeriesSearchRequest() {
  }

  /**
   * Creates an instance using a single search identifier.
   * 
   * @param historicalTimeSeriesKey  the historical time-series key identifier to search for, not null
   */
  public HistoricalTimeSeriesSearchRequest(Identifier historicalTimeSeriesKey) {
    addHistoricalTimeSeriesKey(historicalTimeSeriesKey);
  }

  /**
   * Creates an instance using a bundle of identifiers.
   * 
   * @param historicalTimeSeriesKeys  the historical time-series key identifiers to search for, not null
   */
  public HistoricalTimeSeriesSearchRequest(IdentifierBundle historicalTimeSeriesKeys) {
    addHistoricalTimeSeriesKeys(historicalTimeSeriesKeys);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single historical time-series object identifier to the set.
   * 
   * @param historicalTimeSeriesId  the historical time-series object identifier to add, not null
   */
  public void addHistoricalTimeSeriesId(ObjectIdentifiable historicalTimeSeriesId) {
    ArgumentChecker.notNull(historicalTimeSeriesId, "historicalTimeSeriesId");
    if (_historicalTimeSeriesIds == null) {
      _historicalTimeSeriesIds = new ArrayList<ObjectIdentifier>();
    }
    _historicalTimeSeriesIds.add(historicalTimeSeriesId.getObjectId());
  }

  /**
   * Sets the set of historical time-series object identifiers, null to not limit by historical time-series object identifiers.
   * Note that an empty set will return no historical time-series.
   * 
   * @param historicalTimeSeriesIds  the new historical time-series identifiers, null clears the historical time-series id search
   */
  public void setHistoricalTimeSeriesIds(Iterable<? extends ObjectIdentifiable> historicalTimeSeriesIds) {
    if (historicalTimeSeriesIds == null) {
      _historicalTimeSeriesIds = null;
    } else {
      _historicalTimeSeriesIds = new ArrayList<ObjectIdentifier>();
      for (ObjectIdentifiable exchangeId : historicalTimeSeriesIds) {
        _historicalTimeSeriesIds.add(exchangeId.getObjectId());
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single historical time-series key identifier to the collection to search for.
   * Unless customized, the search will match 
   * {@link IdentifierSearchType#ANY any} of the identifiers.
   * 
   * @param historicalTimeSeriesKey  the historical time-series key identifier to add, not null
   */
  public void addHistoricalTimeSeriesKey(Identifier historicalTimeSeriesKey) {
    ArgumentChecker.notNull(historicalTimeSeriesKey, "historicalTimeSeriesKey");
    addHistoricalTimeSeriesKeys(Arrays.asList(historicalTimeSeriesKey));
  }

  /**
   * Adds a collection of historical time-series key identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link IdentifierSearchType#ANY any} of the identifiers.
   * 
   * @param historicalTimeSeriesKeys  the historical time-series key identifiers to add, not null
   */
  public void addHistoricalTimeSeriesKeys(Identifier... historicalTimeSeriesKeys) {
    ArgumentChecker.notNull(historicalTimeSeriesKeys, "historicalTimeSeriesKeys");
    if (getHistoricalTimeSeriesKeys() == null) {
      setHistoricalTimeSeriesKeys(new IdentifierSearch(historicalTimeSeriesKeys));
    } else {
      getHistoricalTimeSeriesKeys().addIdentifiers(historicalTimeSeriesKeys);
    }
  }

  /**
   * Adds a collection of historical time-series key identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link IdentifierSearchType#ANY any} of the identifiers.
   * 
   * @param historicalTimeSeriesKeys  the historical time-series key identifiers to add, not null
   */
  public void addHistoricalTimeSeriesKeys(Iterable<Identifier> historicalTimeSeriesKeys) {
    ArgumentChecker.notNull(historicalTimeSeriesKeys, "historicalTimeSeriesKeys");
    if (getHistoricalTimeSeriesKeys() == null) {
      setHistoricalTimeSeriesKeys(new IdentifierSearch(historicalTimeSeriesKeys));
    } else {
      getHistoricalTimeSeriesKeys().addIdentifiers(historicalTimeSeriesKeys);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean matches(AbstractDocument obj) {
    if (obj instanceof HistoricalTimeSeriesDocument == false) {
      return false;
    }
    final HistoricalTimeSeriesDocument document = (HistoricalTimeSeriesDocument) obj;
    ManageableHistoricalTimeSeries series = document.getSeries();
    if (getHistoricalTimeSeriesIds() != null && getHistoricalTimeSeriesIds().contains(document.getObjectId()) == false) {
      return false;
    }
    if (getHistoricalTimeSeriesKeys() != null && getHistoricalTimeSeriesKeys().matches(
        series.getIdentifiers().asIdentifierBundle(getIdentifierValidityDate())) == false) {
      return false;
    }
    if (getName() != null && RegexUtils.wildcardMatch(getName(), series.getName()) == false) {
      return false;
    }
    if (getDataSource() != null && getDataSource().equals(series.getDataSource()) == false) {
      return false;
    }    
    if (getDataProvider() != null && getDataProvider().equals(series.getDataProvider()) == false) {
      return false;
    }    
    if (getDataField() != null && getDataField().equals(series.getDataField()) == false) {
      return false;
    }    
    if (getObservationTime() != null && getObservationTime().equals(series.getObservationTime()) == false) {
      return false;
    }    
    if (getIdentifierValue() != null) {
      success: {  // label used with break statement, CSIGNORE
        IdentifierBundle docBundle = series.getIdentifiers().asIdentifierBundle();
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
   * The meta-bean for {@code HistoricalTimeSeriesSearchRequest}.
   * @return the meta-bean, not null
   */
  public static HistoricalTimeSeriesSearchRequest.Meta meta() {
    return HistoricalTimeSeriesSearchRequest.Meta.INSTANCE;
  }

  @Override
  public HistoricalTimeSeriesSearchRequest.Meta metaBean() {
    return HistoricalTimeSeriesSearchRequest.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case -765540406:  // historicalTimeSeriesIds
        return getHistoricalTimeSeriesIds();
      case 2038112034:  // historicalTimeSeriesKeys
        return getHistoricalTimeSeriesKeys();
      case 2085582408:  // identifierValue
        return getIdentifierValue();
      case 48758089:  // identifierValidityDate
        return getIdentifierValidityDate();
      case 3373707:  // name
        return getName();
      case 1272470629:  // dataSource
        return getDataSource();
      case 339742651:  // dataProvider
        return getDataProvider();
      case -386794640:  // dataField
        return getDataField();
      case 951232793:  // observationTime
        return getObservationTime();
      case -771242688:  // loadEarliestLatest
        return isLoadEarliestLatest();
      case 1833789738:  // loadTimeSeries
        return isLoadTimeSeries();
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
      case -765540406:  // historicalTimeSeriesIds
        setHistoricalTimeSeriesIds((List<ObjectIdentifier>) newValue);
        return;
      case 2038112034:  // historicalTimeSeriesKeys
        setHistoricalTimeSeriesKeys((IdentifierSearch) newValue);
        return;
      case 2085582408:  // identifierValue
        setIdentifierValue((String) newValue);
        return;
      case 48758089:  // identifierValidityDate
        setIdentifierValidityDate((LocalDate) newValue);
        return;
      case 3373707:  // name
        setName((String) newValue);
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
      case -771242688:  // loadEarliestLatest
        setLoadEarliestLatest((Boolean) newValue);
        return;
      case 1833789738:  // loadTimeSeries
        setLoadTimeSeries((Boolean) newValue);
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
      HistoricalTimeSeriesSearchRequest other = (HistoricalTimeSeriesSearchRequest) obj;
      return JodaBeanUtils.equal(getHistoricalTimeSeriesIds(), other.getHistoricalTimeSeriesIds()) &&
          JodaBeanUtils.equal(getHistoricalTimeSeriesKeys(), other.getHistoricalTimeSeriesKeys()) &&
          JodaBeanUtils.equal(getIdentifierValue(), other.getIdentifierValue()) &&
          JodaBeanUtils.equal(getIdentifierValidityDate(), other.getIdentifierValidityDate()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getDataSource(), other.getDataSource()) &&
          JodaBeanUtils.equal(getDataProvider(), other.getDataProvider()) &&
          JodaBeanUtils.equal(getDataField(), other.getDataField()) &&
          JodaBeanUtils.equal(getObservationTime(), other.getObservationTime()) &&
          JodaBeanUtils.equal(isLoadEarliestLatest(), other.isLoadEarliestLatest()) &&
          JodaBeanUtils.equal(isLoadTimeSeries(), other.isLoadTimeSeries()) &&
          JodaBeanUtils.equal(getStart(), other.getStart()) &&
          JodaBeanUtils.equal(getEnd(), other.getEnd()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesIds());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesKeys());
    hash += hash * 31 + JodaBeanUtils.hashCode(getIdentifierValue());
    hash += hash * 31 + JodaBeanUtils.hashCode(getIdentifierValidityDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDataSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDataProvider());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDataField());
    hash += hash * 31 + JodaBeanUtils.hashCode(getObservationTime());
    hash += hash * 31 + JodaBeanUtils.hashCode(isLoadEarliestLatest());
    hash += hash * 31 + JodaBeanUtils.hashCode(isLoadTimeSeries());
    hash += hash * 31 + JodaBeanUtils.hashCode(getStart());
    hash += hash * 31 + JodaBeanUtils.hashCode(getEnd());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of time-series object identifiers, null to not limit by exchange object identifiers.
   * Note that an empty set will return no time-series.
   * @return the value of the property
   */
  public List<ObjectIdentifier> getHistoricalTimeSeriesIds() {
    return _historicalTimeSeriesIds;
  }

  /**
   * Gets the the {@code historicalTimeSeriesIds} property.
   * Note that an empty set will return no time-series.
   * @return the property, not null
   */
  public final Property<List<ObjectIdentifier>> historicalTimeSeriesIds() {
    return metaBean().historicalTimeSeriesIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the time-series keys to match, null to not match on time-series keys.
   * @return the value of the property
   */
  public IdentifierSearch getHistoricalTimeSeriesKeys() {
    return _historicalTimeSeriesKeys;
  }

  /**
   * Sets the time-series keys to match, null to not match on time-series keys.
   * @param historicalTimeSeriesKeys  the new value of the property
   */
  public void setHistoricalTimeSeriesKeys(IdentifierSearch historicalTimeSeriesKeys) {
    this._historicalTimeSeriesKeys = historicalTimeSeriesKeys;
  }

  /**
   * Gets the the {@code historicalTimeSeriesKeys} property.
   * @return the property, not null
   */
  public final Property<IdentifierSearch> historicalTimeSeriesKeys() {
    return metaBean().historicalTimeSeriesKeys().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the identifier value, matching against the <i>value</i> of the identifiers,
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
   * Sets the identifier value, matching against the <i>value</i> of the identifiers,
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
   * Gets the time-series name, wildcards allowed, null to not match on name.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the time-series name, wildcards allowed, null to not match on name.
   * @param name  the new value of the property
   */
  public void setName(String name) {
    this._name = name;
  }

  /**
   * Gets the the {@code name} property.
   * @return the property, not null
   */
  public final Property<String> name() {
    return metaBean().name().createProperty(this);
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
   * The meta-bean for {@code HistoricalTimeSeriesSearchRequest}.
   */
  public static class Meta extends AbstractSearchRequest.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code historicalTimeSeriesIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ObjectIdentifier>> _historicalTimeSeriesIds = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesIds", HistoricalTimeSeriesSearchRequest.class, (Class) List.class);
    /**
     * The meta-property for the {@code historicalTimeSeriesKeys} property.
     */
    private final MetaProperty<IdentifierSearch> _historicalTimeSeriesKeys = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesKeys", HistoricalTimeSeriesSearchRequest.class, IdentifierSearch.class);
    /**
     * The meta-property for the {@code identifierValue} property.
     */
    private final MetaProperty<String> _identifierValue = DirectMetaProperty.ofReadWrite(
        this, "identifierValue", HistoricalTimeSeriesSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code identifierValidityDate} property.
     */
    private final MetaProperty<LocalDate> _identifierValidityDate = DirectMetaProperty.ofReadWrite(
        this, "identifierValidityDate", HistoricalTimeSeriesSearchRequest.class, LocalDate.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", HistoricalTimeSeriesSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code dataSource} property.
     */
    private final MetaProperty<String> _dataSource = DirectMetaProperty.ofReadWrite(
        this, "dataSource", HistoricalTimeSeriesSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code dataProvider} property.
     */
    private final MetaProperty<String> _dataProvider = DirectMetaProperty.ofReadWrite(
        this, "dataProvider", HistoricalTimeSeriesSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code dataField} property.
     */
    private final MetaProperty<String> _dataField = DirectMetaProperty.ofReadWrite(
        this, "dataField", HistoricalTimeSeriesSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code observationTime} property.
     */
    private final MetaProperty<String> _observationTime = DirectMetaProperty.ofReadWrite(
        this, "observationTime", HistoricalTimeSeriesSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code loadEarliestLatest} property.
     */
    private final MetaProperty<Boolean> _loadEarliestLatest = DirectMetaProperty.ofReadWrite(
        this, "loadEarliestLatest", HistoricalTimeSeriesSearchRequest.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code loadTimeSeries} property.
     */
    private final MetaProperty<Boolean> _loadTimeSeries = DirectMetaProperty.ofReadWrite(
        this, "loadTimeSeries", HistoricalTimeSeriesSearchRequest.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code start} property.
     */
    private final MetaProperty<LocalDate> _start = DirectMetaProperty.ofReadWrite(
        this, "start", HistoricalTimeSeriesSearchRequest.class, LocalDate.class);
    /**
     * The meta-property for the {@code end} property.
     */
    private final MetaProperty<LocalDate> _end = DirectMetaProperty.ofReadWrite(
        this, "end", HistoricalTimeSeriesSearchRequest.class, LocalDate.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "historicalTimeSeriesIds",
        "historicalTimeSeriesKeys",
        "identifierValue",
        "identifierValidityDate",
        "name",
        "dataSource",
        "dataProvider",
        "dataField",
        "observationTime",
        "loadEarliestLatest",
        "loadTimeSeries",
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
        case -765540406:  // historicalTimeSeriesIds
          return _historicalTimeSeriesIds;
        case 2038112034:  // historicalTimeSeriesKeys
          return _historicalTimeSeriesKeys;
        case 2085582408:  // identifierValue
          return _identifierValue;
        case 48758089:  // identifierValidityDate
          return _identifierValidityDate;
        case 3373707:  // name
          return _name;
        case 1272470629:  // dataSource
          return _dataSource;
        case 339742651:  // dataProvider
          return _dataProvider;
        case -386794640:  // dataField
          return _dataField;
        case 951232793:  // observationTime
          return _observationTime;
        case -771242688:  // loadEarliestLatest
          return _loadEarliestLatest;
        case 1833789738:  // loadTimeSeries
          return _loadTimeSeries;
        case 109757538:  // start
          return _start;
        case 100571:  // end
          return _end;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends HistoricalTimeSeriesSearchRequest> builder() {
      return new DirectBeanBuilder<HistoricalTimeSeriesSearchRequest>(new HistoricalTimeSeriesSearchRequest());
    }

    @Override
    public Class<? extends HistoricalTimeSeriesSearchRequest> beanType() {
      return HistoricalTimeSeriesSearchRequest.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code historicalTimeSeriesIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ObjectIdentifier>> historicalTimeSeriesIds() {
      return _historicalTimeSeriesIds;
    }

    /**
     * The meta-property for the {@code historicalTimeSeriesKeys} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<IdentifierSearch> historicalTimeSeriesKeys() {
      return _historicalTimeSeriesKeys;
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
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
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
     * The meta-property for the {@code loadEarliestLatest} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> loadEarliestLatest() {
      return _loadEarliestLatest;
    }

    /**
     * The meta-property for the {@code loadTimeSeries} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> loadTimeSeries() {
      return _loadTimeSeries;
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
