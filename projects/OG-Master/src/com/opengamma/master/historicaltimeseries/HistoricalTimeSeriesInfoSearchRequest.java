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
import com.opengamma.id.ObjectId;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.RegexUtils;

/**
 * Request for searching for historical time-series information.
 * <p>
 * Documents will be returned that match the search criteria.
 * This class provides the ability to page the results.
 * <p>
 * This class is mutable and not thread-safe.
 */
@PublicSPI
@BeanDefinition
public class HistoricalTimeSeriesInfoSearchRequest extends AbstractSearchRequest {

  /**
   * The set of time-series object identifiers, null to not limit by exchange object identifiers.
   * Note that an empty set will return no time-series.
   */
  @PropertyDefinition(set = "manual")
  private List<ObjectId> _infoIds;
  /**
   * The time-series keys to match, null to not match on time-series keys.
   */
  @PropertyDefinition
  private IdentifierSearch _identifierKeys;
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
   * Creates an instance.
   */
  public HistoricalTimeSeriesInfoSearchRequest() {
  }

  /**
   * Creates an instance using a single search identifier.
   * 
   * @param historicalTimeSeriesKey  the historical time-series key identifier to search for, not null
   */
  public HistoricalTimeSeriesInfoSearchRequest(Identifier historicalTimeSeriesKey) {
    addIdentifierKey(historicalTimeSeriesKey);
  }

  /**
   * Creates an instance using a bundle of identifiers.
   * 
   * @param historicalTimeSeriesKeys  the historical time-series key identifiers to search for, not null
   */
  public HistoricalTimeSeriesInfoSearchRequest(IdentifierBundle historicalTimeSeriesKeys) {
    addIdentifierKeys(historicalTimeSeriesKeys);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single historical time-series object identifier to the set.
   * 
   * @param infoId  the historical time-series object identifier to add, not null
   */
  public void addInfoId(ObjectIdentifiable infoId) {
    ArgumentChecker.notNull(infoId, "infoId");
    if (_infoIds == null) {
      _infoIds = new ArrayList<ObjectId>();
    }
    _infoIds.add(infoId.getObjectId());
  }

  /**
   * Sets the set of historical time-series object identifiers, null to not limit by historical time-series object identifiers.
   * Note that an empty set will return no historical time-series.
   * 
   * @param infoIds  the new historical time-series identifiers, null clears the historical time-series id search
   */
  public void setInfoIds(Iterable<? extends ObjectIdentifiable> infoIds) {
    if (infoIds == null) {
      _infoIds = null;
    } else {
      _infoIds = new ArrayList<ObjectId>();
      for (ObjectIdentifiable exchangeId : infoIds) {
        _infoIds.add(exchangeId.getObjectId());
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single historical time-series key identifier to the collection to search for.
   * Unless customized, the search will match 
   * {@link IdentifierSearchType#ANY any} of the identifiers.
   * 
   * @param identifierKey  the historical time-series key identifier to add, not null
   */
  public void addIdentifierKey(Identifier identifierKey) {
    ArgumentChecker.notNull(identifierKey, "identifierKey");
    addIdentifierKeys(Arrays.asList(identifierKey));
  }

  /**
   * Adds a collection of historical time-series key identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link IdentifierSearchType#ANY any} of the identifiers.
   * 
   * @param identifierKeys  the historical time-series key identifiers to add, not null
   */
  public void addIdentifierKeys(Identifier... identifierKeys) {
    ArgumentChecker.notNull(identifierKeys, "identifierKeys");
    if (getIdentifierKeys() == null) {
      setIdentifierKeys(new IdentifierSearch(identifierKeys));
    } else {
      getIdentifierKeys().addIdentifiers(identifierKeys);
    }
  }

  /**
   * Adds a collection of historical time-series key identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link IdentifierSearchType#ANY any} of the identifiers.
   * 
   * @param identifierKeys  the historical time-series key identifiers to add, not null
   */
  public void addIdentifierKeys(Iterable<Identifier> identifierKeys) {
    ArgumentChecker.notNull(identifierKeys, "identifierKeys");
    if (getIdentifierKeys() == null) {
      setIdentifierKeys(new IdentifierSearch(identifierKeys));
    } else {
      getIdentifierKeys().addIdentifiers(identifierKeys);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean matches(AbstractDocument obj) {
    if (obj instanceof HistoricalTimeSeriesInfoDocument == false) {
      return false;
    }
    final HistoricalTimeSeriesInfoDocument document = (HistoricalTimeSeriesInfoDocument) obj;
    ManageableHistoricalTimeSeriesInfo info = document.getInfo();
    if (getInfoIds() != null && getInfoIds().contains(document.getObjectId()) == false) {
      return false;
    }
    if (getIdentifierKeys() != null && getIdentifierKeys().matches(
        info.getIdentifiers().asIdentifierBundle(getIdentifierValidityDate())) == false) {
      return false;
    }
    if (getName() != null && RegexUtils.wildcardMatch(getName(), info.getName()) == false) {
      return false;
    }
    if (getDataSource() != null && getDataSource().equals(info.getDataSource()) == false) {
      return false;
    }    
    if (getDataProvider() != null && getDataProvider().equals(info.getDataProvider()) == false) {
      return false;
    }    
    if (getDataField() != null && getDataField().equals(info.getDataField()) == false) {
      return false;
    }    
    if (getObservationTime() != null && getObservationTime().equals(info.getObservationTime()) == false) {
      return false;
    }    
    if (getIdentifierValue() != null) {
      success: {  // label used with break statement, CSIGNORE
        IdentifierBundle docBundle = info.getIdentifiers().asIdentifierBundle();
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
   * The meta-bean for {@code HistoricalTimeSeriesInfoSearchRequest}.
   * @return the meta-bean, not null
   */
  public static HistoricalTimeSeriesInfoSearchRequest.Meta meta() {
    return HistoricalTimeSeriesInfoSearchRequest.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(HistoricalTimeSeriesInfoSearchRequest.Meta.INSTANCE);
  }

  @Override
  public HistoricalTimeSeriesInfoSearchRequest.Meta metaBean() {
    return HistoricalTimeSeriesInfoSearchRequest.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 1945391914:  // infoIds
        return getInfoIds();
      case 482595389:  // identifierKeys
        return getIdentifierKeys();
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
    }
    return super.propertyGet(propertyName, quiet);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 1945391914:  // infoIds
        setInfoIds((List<ObjectId>) newValue);
        return;
      case 482595389:  // identifierKeys
        setIdentifierKeys((IdentifierSearch) newValue);
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
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      HistoricalTimeSeriesInfoSearchRequest other = (HistoricalTimeSeriesInfoSearchRequest) obj;
      return JodaBeanUtils.equal(getInfoIds(), other.getInfoIds()) &&
          JodaBeanUtils.equal(getIdentifierKeys(), other.getIdentifierKeys()) &&
          JodaBeanUtils.equal(getIdentifierValue(), other.getIdentifierValue()) &&
          JodaBeanUtils.equal(getIdentifierValidityDate(), other.getIdentifierValidityDate()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getDataSource(), other.getDataSource()) &&
          JodaBeanUtils.equal(getDataProvider(), other.getDataProvider()) &&
          JodaBeanUtils.equal(getDataField(), other.getDataField()) &&
          JodaBeanUtils.equal(getObservationTime(), other.getObservationTime()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getInfoIds());
    hash += hash * 31 + JodaBeanUtils.hashCode(getIdentifierKeys());
    hash += hash * 31 + JodaBeanUtils.hashCode(getIdentifierValue());
    hash += hash * 31 + JodaBeanUtils.hashCode(getIdentifierValidityDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDataSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDataProvider());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDataField());
    hash += hash * 31 + JodaBeanUtils.hashCode(getObservationTime());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of time-series object identifiers, null to not limit by exchange object identifiers.
   * Note that an empty set will return no time-series.
   * @return the value of the property
   */
  public List<ObjectId> getInfoIds() {
    return _infoIds;
  }

  /**
   * Gets the the {@code infoIds} property.
   * Note that an empty set will return no time-series.
   * @return the property, not null
   */
  public final Property<List<ObjectId>> infoIds() {
    return metaBean().infoIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the time-series keys to match, null to not match on time-series keys.
   * @return the value of the property
   */
  public IdentifierSearch getIdentifierKeys() {
    return _identifierKeys;
  }

  /**
   * Sets the time-series keys to match, null to not match on time-series keys.
   * @param identifierKeys  the new value of the property
   */
  public void setIdentifierKeys(IdentifierSearch identifierKeys) {
    this._identifierKeys = identifierKeys;
  }

  /**
   * Gets the the {@code identifierKeys} property.
   * @return the property, not null
   */
  public final Property<IdentifierSearch> identifierKeys() {
    return metaBean().identifierKeys().createProperty(this);
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
   * The meta-bean for {@code HistoricalTimeSeriesInfoSearchRequest}.
   */
  public static class Meta extends AbstractSearchRequest.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code infoIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ObjectId>> _infoIds = DirectMetaProperty.ofReadWrite(
        this, "infoIds", HistoricalTimeSeriesInfoSearchRequest.class, (Class) List.class);
    /**
     * The meta-property for the {@code identifierKeys} property.
     */
    private final MetaProperty<IdentifierSearch> _identifierKeys = DirectMetaProperty.ofReadWrite(
        this, "identifierKeys", HistoricalTimeSeriesInfoSearchRequest.class, IdentifierSearch.class);
    /**
     * The meta-property for the {@code identifierValue} property.
     */
    private final MetaProperty<String> _identifierValue = DirectMetaProperty.ofReadWrite(
        this, "identifierValue", HistoricalTimeSeriesInfoSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code identifierValidityDate} property.
     */
    private final MetaProperty<LocalDate> _identifierValidityDate = DirectMetaProperty.ofReadWrite(
        this, "identifierValidityDate", HistoricalTimeSeriesInfoSearchRequest.class, LocalDate.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", HistoricalTimeSeriesInfoSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code dataSource} property.
     */
    private final MetaProperty<String> _dataSource = DirectMetaProperty.ofReadWrite(
        this, "dataSource", HistoricalTimeSeriesInfoSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code dataProvider} property.
     */
    private final MetaProperty<String> _dataProvider = DirectMetaProperty.ofReadWrite(
        this, "dataProvider", HistoricalTimeSeriesInfoSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code dataField} property.
     */
    private final MetaProperty<String> _dataField = DirectMetaProperty.ofReadWrite(
        this, "dataField", HistoricalTimeSeriesInfoSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code observationTime} property.
     */
    private final MetaProperty<String> _observationTime = DirectMetaProperty.ofReadWrite(
        this, "observationTime", HistoricalTimeSeriesInfoSearchRequest.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "infoIds",
        "identifierKeys",
        "identifierValue",
        "identifierValidityDate",
        "name",
        "dataSource",
        "dataProvider",
        "dataField",
        "observationTime");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1945391914:  // infoIds
          return _infoIds;
        case 482595389:  // identifierKeys
          return _identifierKeys;
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
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends HistoricalTimeSeriesInfoSearchRequest> builder() {
      return new DirectBeanBuilder<HistoricalTimeSeriesInfoSearchRequest>(new HistoricalTimeSeriesInfoSearchRequest());
    }

    @Override
    public Class<? extends HistoricalTimeSeriesInfoSearchRequest> beanType() {
      return HistoricalTimeSeriesInfoSearchRequest.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code infoIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ObjectId>> infoIds() {
      return _infoIds;
    }

    /**
     * The meta-property for the {@code identifierKeys} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<IdentifierSearch> identifierKeys() {
      return _identifierKeys;
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

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
