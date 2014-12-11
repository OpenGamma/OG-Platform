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

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
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
  private List<ObjectId> _objectIds;
  /**
   * The external identifiers to match, null to not match on external identifiers.
   */
  @PropertyDefinition
  private ExternalIdSearch _externalIdSearch;
  /**
   * The external identifier value, matching against the <i>value</i> of the identifiers,
   * null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code externalIdSearch}
   * search is useful for exact machine searching.
   */
  @PropertyDefinition
  private String _externalIdValue;
  /**
   * The date on which external identifiers must be valid, null to ignore identifier date limitations.
   */
  @PropertyDefinition
  private LocalDate _validityDate;
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
   * @param historicalTimeSeriesId  the historical time-series external identifier to search for, not null
   */
  public HistoricalTimeSeriesInfoSearchRequest(ExternalId historicalTimeSeriesId) {
    addExternalId(historicalTimeSeriesId);
  }

  /**
   * Creates an instance using a bundle of identifiers.
   * 
   * @param historicalTimeSeriesBundle  the historical time-series external identifiers to search for, not null
   */
  public HistoricalTimeSeriesInfoSearchRequest(ExternalIdBundle historicalTimeSeriesBundle) {
    addExternalIds(historicalTimeSeriesBundle);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single historical time-series object identifier to the set.
   * 
   * @param infoId  the historical time-series object identifier to add, not null
   */
  public void addObjectId(ObjectIdentifiable infoId) {
    ArgumentChecker.notNull(infoId, "infoId");
    if (_objectIds == null) {
      _objectIds = new ArrayList<ObjectId>();
    }
    _objectIds.add(infoId.getObjectId());
  }

  /**
   * Sets the set of historical time-series object identifiers, null to not limit by historical time-series object identifiers.
   * Note that an empty set will return no historical time-series.
   * 
   * @param infoIds  the new historical time-series identifiers, null clears the historical time-series id search
   */
  public void setObjectIds(Iterable<? extends ObjectIdentifiable> infoIds) {
    if (infoIds == null) {
      _objectIds = null;
    } else {
      _objectIds = new ArrayList<ObjectId>();
      for (ObjectIdentifiable exchangeId : infoIds) {
        _objectIds.add(exchangeId.getObjectId());
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single historical time-series external identifier to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param externalId  the historical time-series key identifier to add, not null
   */
  public void addExternalId(ExternalId externalId) {
    ArgumentChecker.notNull(externalId, "externalId");
    addExternalIds(Arrays.asList(externalId));
  }

  /**
   * Adds a collection of historical time-series external identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param externalIds  the historical time-series key identifiers to add, not null
   */
  public void addExternalIds(ExternalId... externalIds) {
    ArgumentChecker.notNull(externalIds, "externalIds");
    if (getExternalIdSearch() == null) {
      setExternalIdSearch(ExternalIdSearch.of(externalIds));
    } else {
      setExternalIdSearch(getExternalIdSearch().withExternalIdsAdded(externalIds));
    }
  }

  /**
   * Adds a collection of historical time-series external identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param externalIds  the historical time-series key identifiers to add, not null
   */
  public void addExternalIds(Iterable<ExternalId> externalIds) {
    ArgumentChecker.notNull(externalIds, "externalIds");
    if (getExternalIdSearch() == null) {
      setExternalIdSearch(ExternalIdSearch.of(externalIds));
    } else {
      setExternalIdSearch(getExternalIdSearch().withExternalIdsAdded(externalIds));
    }
  }

  /**
   * Sets the search type to use in {@code ExternalIdSearch}.
   * 
   * @param type  the type to set, not null
   */
  public void setExternalIdSearchType(ExternalIdSearchType type) {
    if (getExternalIdSearch() == null) {
      setExternalIdSearch(ExternalIdSearch.of(type));
    } else {
      setExternalIdSearch(getExternalIdSearch().withSearchType(type));
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
    if (getObjectIds() != null && getObjectIds().contains(document.getObjectId()) == false) {
      return false;
    }
    if (getExternalIdSearch() != null && getExternalIdSearch().matches(
        info.getExternalIdBundle().toBundle(getValidityDate())) == false) {
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
    if (getExternalIdValue() != null) {
      success: {  // label used with break statement, CSIGNORE
        ExternalIdBundle docBundle = info.getExternalIdBundle().toBundle();
        for (ExternalId identifier : docBundle.getExternalIds()) {
          if (RegexUtils.wildcardMatch(getExternalIdValue(), identifier.getValue())) {
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

  //-----------------------------------------------------------------------
  /**
   * Gets the set of time-series object identifiers, null to not limit by exchange object identifiers.
   * Note that an empty set will return no time-series.
   * @return the value of the property
   */
  public List<ObjectId> getObjectIds() {
    return _objectIds;
  }

  /**
   * Gets the the {@code objectIds} property.
   * Note that an empty set will return no time-series.
   * @return the property, not null
   */
  public final Property<List<ObjectId>> objectIds() {
    return metaBean().objectIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the external identifiers to match, null to not match on external identifiers.
   * @return the value of the property
   */
  public ExternalIdSearch getExternalIdSearch() {
    return _externalIdSearch;
  }

  /**
   * Sets the external identifiers to match, null to not match on external identifiers.
   * @param externalIdSearch  the new value of the property
   */
  public void setExternalIdSearch(ExternalIdSearch externalIdSearch) {
    this._externalIdSearch = externalIdSearch;
  }

  /**
   * Gets the the {@code externalIdSearch} property.
   * @return the property, not null
   */
  public final Property<ExternalIdSearch> externalIdSearch() {
    return metaBean().externalIdSearch().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the external identifier value, matching against the <i>value</i> of the identifiers,
   * null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code externalIdSearch}
   * search is useful for exact machine searching.
   * @return the value of the property
   */
  public String getExternalIdValue() {
    return _externalIdValue;
  }

  /**
   * Sets the external identifier value, matching against the <i>value</i> of the identifiers,
   * null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code externalIdSearch}
   * search is useful for exact machine searching.
   * @param externalIdValue  the new value of the property
   */
  public void setExternalIdValue(String externalIdValue) {
    this._externalIdValue = externalIdValue;
  }

  /**
   * Gets the the {@code externalIdValue} property.
   * null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code externalIdSearch}
   * search is useful for exact machine searching.
   * @return the property, not null
   */
  public final Property<String> externalIdValue() {
    return metaBean().externalIdValue().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the date on which external identifiers must be valid, null to ignore identifier date limitations.
   * @return the value of the property
   */
  public LocalDate getValidityDate() {
    return _validityDate;
  }

  /**
   * Sets the date on which external identifiers must be valid, null to ignore identifier date limitations.
   * @param validityDate  the new value of the property
   */
  public void setValidityDate(LocalDate validityDate) {
    this._validityDate = validityDate;
  }

  /**
   * Gets the the {@code validityDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> validityDate() {
    return metaBean().validityDate().createProperty(this);
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
  @Override
  public HistoricalTimeSeriesInfoSearchRequest clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      HistoricalTimeSeriesInfoSearchRequest other = (HistoricalTimeSeriesInfoSearchRequest) obj;
      return JodaBeanUtils.equal(getObjectIds(), other.getObjectIds()) &&
          JodaBeanUtils.equal(getExternalIdSearch(), other.getExternalIdSearch()) &&
          JodaBeanUtils.equal(getExternalIdValue(), other.getExternalIdValue()) &&
          JodaBeanUtils.equal(getValidityDate(), other.getValidityDate()) &&
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
    hash = hash * 31 + JodaBeanUtils.hashCode(getObjectIds());
    hash = hash * 31 + JodaBeanUtils.hashCode(getExternalIdSearch());
    hash = hash * 31 + JodaBeanUtils.hashCode(getExternalIdValue());
    hash = hash * 31 + JodaBeanUtils.hashCode(getValidityDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getDataSource());
    hash = hash * 31 + JodaBeanUtils.hashCode(getDataProvider());
    hash = hash * 31 + JodaBeanUtils.hashCode(getDataField());
    hash = hash * 31 + JodaBeanUtils.hashCode(getObservationTime());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(320);
    buf.append("HistoricalTimeSeriesInfoSearchRequest{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  @Override
  protected void toString(StringBuilder buf) {
    super.toString(buf);
    buf.append("objectIds").append('=').append(JodaBeanUtils.toString(getObjectIds())).append(',').append(' ');
    buf.append("externalIdSearch").append('=').append(JodaBeanUtils.toString(getExternalIdSearch())).append(',').append(' ');
    buf.append("externalIdValue").append('=').append(JodaBeanUtils.toString(getExternalIdValue())).append(',').append(' ');
    buf.append("validityDate").append('=').append(JodaBeanUtils.toString(getValidityDate())).append(',').append(' ');
    buf.append("name").append('=').append(JodaBeanUtils.toString(getName())).append(',').append(' ');
    buf.append("dataSource").append('=').append(JodaBeanUtils.toString(getDataSource())).append(',').append(' ');
    buf.append("dataProvider").append('=').append(JodaBeanUtils.toString(getDataProvider())).append(',').append(' ');
    buf.append("dataField").append('=').append(JodaBeanUtils.toString(getDataField())).append(',').append(' ');
    buf.append("observationTime").append('=').append(JodaBeanUtils.toString(getObservationTime())).append(',').append(' ');
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
     * The meta-property for the {@code objectIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ObjectId>> _objectIds = DirectMetaProperty.ofReadWrite(
        this, "objectIds", HistoricalTimeSeriesInfoSearchRequest.class, (Class) List.class);
    /**
     * The meta-property for the {@code externalIdSearch} property.
     */
    private final MetaProperty<ExternalIdSearch> _externalIdSearch = DirectMetaProperty.ofReadWrite(
        this, "externalIdSearch", HistoricalTimeSeriesInfoSearchRequest.class, ExternalIdSearch.class);
    /**
     * The meta-property for the {@code externalIdValue} property.
     */
    private final MetaProperty<String> _externalIdValue = DirectMetaProperty.ofReadWrite(
        this, "externalIdValue", HistoricalTimeSeriesInfoSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code validityDate} property.
     */
    private final MetaProperty<LocalDate> _validityDate = DirectMetaProperty.ofReadWrite(
        this, "validityDate", HistoricalTimeSeriesInfoSearchRequest.class, LocalDate.class);
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
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "objectIds",
        "externalIdSearch",
        "externalIdValue",
        "validityDate",
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
        case -1489617159:  // objectIds
          return _objectIds;
        case -265376882:  // externalIdSearch
          return _externalIdSearch;
        case 2072311499:  // externalIdValue
          return _externalIdValue;
        case -390680064:  // validityDate
          return _validityDate;
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
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code objectIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ObjectId>> objectIds() {
      return _objectIds;
    }

    /**
     * The meta-property for the {@code externalIdSearch} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalIdSearch> externalIdSearch() {
      return _externalIdSearch;
    }

    /**
     * The meta-property for the {@code externalIdValue} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> externalIdValue() {
      return _externalIdValue;
    }

    /**
     * The meta-property for the {@code validityDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> validityDate() {
      return _validityDate;
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

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1489617159:  // objectIds
          return ((HistoricalTimeSeriesInfoSearchRequest) bean).getObjectIds();
        case -265376882:  // externalIdSearch
          return ((HistoricalTimeSeriesInfoSearchRequest) bean).getExternalIdSearch();
        case 2072311499:  // externalIdValue
          return ((HistoricalTimeSeriesInfoSearchRequest) bean).getExternalIdValue();
        case -390680064:  // validityDate
          return ((HistoricalTimeSeriesInfoSearchRequest) bean).getValidityDate();
        case 3373707:  // name
          return ((HistoricalTimeSeriesInfoSearchRequest) bean).getName();
        case 1272470629:  // dataSource
          return ((HistoricalTimeSeriesInfoSearchRequest) bean).getDataSource();
        case 339742651:  // dataProvider
          return ((HistoricalTimeSeriesInfoSearchRequest) bean).getDataProvider();
        case -386794640:  // dataField
          return ((HistoricalTimeSeriesInfoSearchRequest) bean).getDataField();
        case 951232793:  // observationTime
          return ((HistoricalTimeSeriesInfoSearchRequest) bean).getObservationTime();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1489617159:  // objectIds
          ((HistoricalTimeSeriesInfoSearchRequest) bean).setObjectIds((List<ObjectId>) newValue);
          return;
        case -265376882:  // externalIdSearch
          ((HistoricalTimeSeriesInfoSearchRequest) bean).setExternalIdSearch((ExternalIdSearch) newValue);
          return;
        case 2072311499:  // externalIdValue
          ((HistoricalTimeSeriesInfoSearchRequest) bean).setExternalIdValue((String) newValue);
          return;
        case -390680064:  // validityDate
          ((HistoricalTimeSeriesInfoSearchRequest) bean).setValidityDate((LocalDate) newValue);
          return;
        case 3373707:  // name
          ((HistoricalTimeSeriesInfoSearchRequest) bean).setName((String) newValue);
          return;
        case 1272470629:  // dataSource
          ((HistoricalTimeSeriesInfoSearchRequest) bean).setDataSource((String) newValue);
          return;
        case 339742651:  // dataProvider
          ((HistoricalTimeSeriesInfoSearchRequest) bean).setDataProvider((String) newValue);
          return;
        case -386794640:  // dataField
          ((HistoricalTimeSeriesInfoSearchRequest) bean).setDataField((String) newValue);
          return;
        case 951232793:  // observationTime
          ((HistoricalTimeSeriesInfoSearchRequest) bean).setObservationTime((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
