/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region;

import java.io.Serializable;
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
import org.threeten.bp.ZoneId;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.RegionClassification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.RegexUtils;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * Request for searching for regions.
 * <p>
 * Documents will be returned that match the search criteria.
 * This class provides the ability to page the results and to search
 * as at a specific version and correction instant.
 * See {@link RegionHistoryRequest} for more details on how history works.
 */
@PublicSPI
@BeanDefinition
public class RegionSearchRequest extends AbstractSearchRequest implements Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The set of region object identifiers, null to not limit by region object identifiers.
   * Note that an empty set will return no regions.
   */
  @PropertyDefinition(set = "manual")
  private List<ObjectId> _objectIds;
  /**
   * The region external identifiers to match, null to not match on region identifiers.
   */
  @PropertyDefinition
  private ExternalIdSearch _externalIdSearch;
  /**
   * The region name, wildcards allowed, null to not match on name.
   */
  @PropertyDefinition
  private String _name;
  /**
   * The region classification, null to not match on classification.
   */
  @PropertyDefinition
  private RegionClassification _classification;
  /**
   * The data provider identifier to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   */
  @PropertyDefinition
  private ExternalId _providerId;
  /**
   * The unique identifier to get children of, null to not retrieve based on children.
   * Only the immediate children of the identifier will be matched.
   */
  @PropertyDefinition
  private UniqueId _childrenOfId;

  /**
   * Creates an instance.
   */
  public RegionSearchRequest() {
  }

  /**
   * Creates an instance using a single search identifier.
   * 
   * @param regionId  the region external identifier to search for, not null
   */
  public RegionSearchRequest(ExternalId regionId) {
    addExternalId(regionId);
  }

  /**
   * Creates an instance using a bundle of identifiers.
   * 
   * @param regionBundle  the region bundle to search for, not null
   */
  public RegionSearchRequest(ExternalIdBundle regionBundle) {
    addExternalIds(regionBundle);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single region object identifier to the set.
   * 
   * @param regionId  the region object identifier to add, not null
   */
  public void addObjectId(ObjectIdentifiable regionId) {
    ArgumentChecker.notNull(regionId, "regionId");
    if (_objectIds == null) {
      _objectIds = new ArrayList<ObjectId>();
    }
    _objectIds.add(regionId.getObjectId());
  }

  /**
   * Sets the set of region object identifiers, null to not limit by region object identifiers.
   * Note that an empty set will return no regions.
   * 
   * @param regionIds  the new region identifiers, null clears the region id search
   */
  public void setObjectIds(Iterable<? extends ObjectIdentifiable> regionIds) {
    if (regionIds == null) {
      _objectIds = null;
    } else {
      _objectIds = new ArrayList<ObjectId>();
      for (ObjectIdentifiable regionId : regionIds) {
        _objectIds.add(regionId.getObjectId());
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single region external identifier to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param regionId  the region key identifier to add, not null
   */
  public void addExternalId(ExternalId regionId) {
    ArgumentChecker.notNull(regionId, "regionId");
    addExternalIds(Arrays.asList(regionId));
  }

  /**
   * Adds a collection of region external identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param regionIds  the region key identifiers to add, not null
   */
  public void addExternalIds(ExternalId... regionIds) {
    ArgumentChecker.notNull(regionIds, "regionIds");
    if (getExternalIdSearch() == null) {
      setExternalIdSearch(ExternalIdSearch.of(regionIds));
    } else {
      setExternalIdSearch(getExternalIdSearch().withExternalIdsAdded(regionIds));
    }
  }

  /**
   * Adds a collection of region external identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param regionIds  the region key identifiers to add, not null
   */
  public void addExternalIds(Iterable<ExternalId> regionIds) {
    ArgumentChecker.notNull(regionIds, "regionIds");
    if (getExternalIdSearch() == null) {
      setExternalIdSearch(ExternalIdSearch.of(regionIds));
    } else {
      setExternalIdSearch(getExternalIdSearch().withExternalIdsAdded(regionIds));
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
  /**
   * Adds a search for a currency by adding the matching bundle.
   * 
   * @param country  the country to search for, not null
   */
  public void addCountry(Country country) {
    ArgumentChecker.notNull(country, "country");
    addExternalId(ExternalSchemes.countryRegionId(country));
  }

  /**
   * Adds a search for a currency by adding the matching bundle.
   * 
   * @param currency  the currency to search for, not null
   */
  public void addCurrency(Currency currency) {
    ArgumentChecker.notNull(currency, "currency");
    addExternalId(ExternalSchemes.currencyRegionId(currency));
  }

  /**
   * Adds a search for a time-zone by adding the matching bundle.
   * 
   * @param timeZone  the time-zone to search for, not null
   */
  public void addTimeZone(ZoneId timeZone) {
    ArgumentChecker.notNull(timeZone, "timeZone");
    addExternalId(ExternalSchemes.timeZoneRegionId(timeZone));
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean matches(final AbstractDocument obj) {
    if (obj instanceof RegionDocument == false) {
      return false;
    }
    final RegionDocument document = (RegionDocument) obj;
    final ManageableRegion region = document.getRegion();
    if (getObjectIds() != null && getObjectIds().contains(document.getObjectId()) == false) {
      return false;
    }
    if (getExternalIdSearch() != null && getExternalIdSearch().matches(region.getExternalIdBundle()) == false) {
      return false;
    }
    if (getName() != null && RegexUtils.wildcardMatch(getName(), region.getName()) == false) {
      return false;
    }
    if (getClassification() != null && getClassification() != region.getClassification()) {
      return false;
    }
    if (getProviderId() != null && getProviderId().equals(document.getProviderId()) == false) {
      return false;
    }
    if (getChildrenOfId() != null && region.getParentRegionIds().contains(getChildrenOfId()) == false) {
      return false;
    }
    return true;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code RegionSearchRequest}.
   * @return the meta-bean, not null
   */
  public static RegionSearchRequest.Meta meta() {
    return RegionSearchRequest.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(RegionSearchRequest.Meta.INSTANCE);
  }

  @Override
  public RegionSearchRequest.Meta metaBean() {
    return RegionSearchRequest.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of region object identifiers, null to not limit by region object identifiers.
   * Note that an empty set will return no regions.
   * @return the value of the property
   */
  public List<ObjectId> getObjectIds() {
    return _objectIds;
  }

  /**
   * Gets the the {@code objectIds} property.
   * Note that an empty set will return no regions.
   * @return the property, not null
   */
  public final Property<List<ObjectId>> objectIds() {
    return metaBean().objectIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the region external identifiers to match, null to not match on region identifiers.
   * @return the value of the property
   */
  public ExternalIdSearch getExternalIdSearch() {
    return _externalIdSearch;
  }

  /**
   * Sets the region external identifiers to match, null to not match on region identifiers.
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
   * Gets the region name, wildcards allowed, null to not match on name.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the region name, wildcards allowed, null to not match on name.
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
   * Gets the region classification, null to not match on classification.
   * @return the value of the property
   */
  public RegionClassification getClassification() {
    return _classification;
  }

  /**
   * Sets the region classification, null to not match on classification.
   * @param classification  the new value of the property
   */
  public void setClassification(RegionClassification classification) {
    this._classification = classification;
  }

  /**
   * Gets the the {@code classification} property.
   * @return the property, not null
   */
  public final Property<RegionClassification> classification() {
    return metaBean().classification().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the data provider identifier to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   * @return the value of the property
   */
  public ExternalId getProviderId() {
    return _providerId;
  }

  /**
   * Sets the data provider identifier to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   * @param providerId  the new value of the property
   */
  public void setProviderId(ExternalId providerId) {
    this._providerId = providerId;
  }

  /**
   * Gets the the {@code providerId} property.
   * This field is useful when receiving updates from the same provider.
   * @return the property, not null
   */
  public final Property<ExternalId> providerId() {
    return metaBean().providerId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unique identifier to get children of, null to not retrieve based on children.
   * Only the immediate children of the identifier will be matched.
   * @return the value of the property
   */
  public UniqueId getChildrenOfId() {
    return _childrenOfId;
  }

  /**
   * Sets the unique identifier to get children of, null to not retrieve based on children.
   * Only the immediate children of the identifier will be matched.
   * @param childrenOfId  the new value of the property
   */
  public void setChildrenOfId(UniqueId childrenOfId) {
    this._childrenOfId = childrenOfId;
  }

  /**
   * Gets the the {@code childrenOfId} property.
   * Only the immediate children of the identifier will be matched.
   * @return the property, not null
   */
  public final Property<UniqueId> childrenOfId() {
    return metaBean().childrenOfId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public RegionSearchRequest clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      RegionSearchRequest other = (RegionSearchRequest) obj;
      return JodaBeanUtils.equal(getObjectIds(), other.getObjectIds()) &&
          JodaBeanUtils.equal(getExternalIdSearch(), other.getExternalIdSearch()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getClassification(), other.getClassification()) &&
          JodaBeanUtils.equal(getProviderId(), other.getProviderId()) &&
          JodaBeanUtils.equal(getChildrenOfId(), other.getChildrenOfId()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getObjectIds());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExternalIdSearch());
    hash += hash * 31 + JodaBeanUtils.hashCode(getName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassification());
    hash += hash * 31 + JodaBeanUtils.hashCode(getProviderId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getChildrenOfId());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(224);
    buf.append("RegionSearchRequest{");
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
    buf.append("name").append('=').append(JodaBeanUtils.toString(getName())).append(',').append(' ');
    buf.append("classification").append('=').append(JodaBeanUtils.toString(getClassification())).append(',').append(' ');
    buf.append("providerId").append('=').append(JodaBeanUtils.toString(getProviderId())).append(',').append(' ');
    buf.append("childrenOfId").append('=').append(JodaBeanUtils.toString(getChildrenOfId())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code RegionSearchRequest}.
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
        this, "objectIds", RegionSearchRequest.class, (Class) List.class);
    /**
     * The meta-property for the {@code externalIdSearch} property.
     */
    private final MetaProperty<ExternalIdSearch> _externalIdSearch = DirectMetaProperty.ofReadWrite(
        this, "externalIdSearch", RegionSearchRequest.class, ExternalIdSearch.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", RegionSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code classification} property.
     */
    private final MetaProperty<RegionClassification> _classification = DirectMetaProperty.ofReadWrite(
        this, "classification", RegionSearchRequest.class, RegionClassification.class);
    /**
     * The meta-property for the {@code providerId} property.
     */
    private final MetaProperty<ExternalId> _providerId = DirectMetaProperty.ofReadWrite(
        this, "providerId", RegionSearchRequest.class, ExternalId.class);
    /**
     * The meta-property for the {@code childrenOfId} property.
     */
    private final MetaProperty<UniqueId> _childrenOfId = DirectMetaProperty.ofReadWrite(
        this, "childrenOfId", RegionSearchRequest.class, UniqueId.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "objectIds",
        "externalIdSearch",
        "name",
        "classification",
        "providerId",
        "childrenOfId");

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
        case 3373707:  // name
          return _name;
        case 382350310:  // classification
          return _classification;
        case 205149932:  // providerId
          return _providerId;
        case 178436081:  // childrenOfId
          return _childrenOfId;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends RegionSearchRequest> builder() {
      return new DirectBeanBuilder<RegionSearchRequest>(new RegionSearchRequest());
    }

    @Override
    public Class<? extends RegionSearchRequest> beanType() {
      return RegionSearchRequest.class;
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
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code classification} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<RegionClassification> classification() {
      return _classification;
    }

    /**
     * The meta-property for the {@code providerId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> providerId() {
      return _providerId;
    }

    /**
     * The meta-property for the {@code childrenOfId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> childrenOfId() {
      return _childrenOfId;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1489617159:  // objectIds
          return ((RegionSearchRequest) bean).getObjectIds();
        case -265376882:  // externalIdSearch
          return ((RegionSearchRequest) bean).getExternalIdSearch();
        case 3373707:  // name
          return ((RegionSearchRequest) bean).getName();
        case 382350310:  // classification
          return ((RegionSearchRequest) bean).getClassification();
        case 205149932:  // providerId
          return ((RegionSearchRequest) bean).getProviderId();
        case 178436081:  // childrenOfId
          return ((RegionSearchRequest) bean).getChildrenOfId();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1489617159:  // objectIds
          ((RegionSearchRequest) bean).setObjectIds((List<ObjectId>) newValue);
          return;
        case -265376882:  // externalIdSearch
          ((RegionSearchRequest) bean).setExternalIdSearch((ExternalIdSearch) newValue);
          return;
        case 3373707:  // name
          ((RegionSearchRequest) bean).setName((String) newValue);
          return;
        case 382350310:  // classification
          ((RegionSearchRequest) bean).setClassification((RegionClassification) newValue);
          return;
        case 205149932:  // providerId
          ((RegionSearchRequest) bean).setProviderId((ExternalId) newValue);
          return;
        case 178436081:  // childrenOfId
          ((RegionSearchRequest) bean).setChildrenOfId((UniqueId) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
