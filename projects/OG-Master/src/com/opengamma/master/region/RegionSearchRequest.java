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

import javax.time.calendar.TimeZone;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.core.region.RegionClassification;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierSearch;
import com.opengamma.id.IdentifierSearchType;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.ObjectId;
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
  private List<ObjectId> _regionIds;
  /**
   * The region keys to match, null to not match on region keys.
   */
  @PropertyDefinition
  private IdentifierSearch _regionKeys;
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
   * The data provider key to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   */
  @PropertyDefinition
  private Identifier _providerKey;
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
   * @param regionKey  the region key identifier to search for, not null
   */
  public RegionSearchRequest(Identifier regionKey) {
    addRegionKey(regionKey);
  }

  /**
   * Creates an instance using a bundle of identifiers.
   * 
   * @param regionKeys  the region key identifiers to search for, not null
   */
  public RegionSearchRequest(IdentifierBundle regionKeys) {
    addRegionKeys(regionKeys);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single region object identifier to the set.
   * 
   * @param regionId  the region object identifier to add, not null
   */
  public void addRegionId(ObjectIdentifiable regionId) {
    ArgumentChecker.notNull(regionId, "regionId");
    if (_regionIds == null) {
      _regionIds = new ArrayList<ObjectId>();
    }
    _regionIds.add(regionId.getObjectId());
  }

  /**
   * Sets the set of region object identifiers, null to not limit by region object identifiers.
   * Note that an empty set will return no regions.
   * 
   * @param regionIds  the new region identifiers, null clears the region id search
   */
  public void setRegionIds(Iterable<? extends ObjectIdentifiable> regionIds) {
    if (regionIds == null) {
      _regionIds = null;
    } else {
      _regionIds = new ArrayList<ObjectId>();
      for (ObjectIdentifiable regionId : regionIds) {
        _regionIds.add(regionId.getObjectId());
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single region key identifier to the collection to search for.
   * Unless customized, the search will match 
   * {@link IdentifierSearchType#ANY any} of the identifiers.
   * 
   * @param regionKey  the region key identifier to add, not null
   */
  public void addRegionKey(Identifier regionKey) {
    ArgumentChecker.notNull(regionKey, "regionKey");
    addRegionKeys(Arrays.asList(regionKey));
  }

  /**
   * Adds a collection of region key identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link IdentifierSearchType#ANY any} of the identifiers.
   * 
   * @param regionKeys  the region key identifiers to add, not null
   */
  public void addRegionKeys(Identifier... regionKeys) {
    ArgumentChecker.notNull(regionKeys, "regionKeys");
    if (getRegionKeys() == null) {
      setRegionKeys(new IdentifierSearch(regionKeys));
    } else {
      getRegionKeys().addIdentifiers(regionKeys);
    }
  }

  /**
   * Adds a collection of region key identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link IdentifierSearchType#ANY any} of the identifiers.
   * 
   * @param regionKeys  the region key identifiers to add, not null
   */
  public void addRegionKeys(Iterable<Identifier> regionKeys) {
    ArgumentChecker.notNull(regionKeys, "regionKeys");
    if (getRegionKeys() == null) {
      setRegionKeys(new IdentifierSearch(regionKeys));
    } else {
      getRegionKeys().addIdentifiers(regionKeys);
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
    addRegionKey(RegionUtils.countryRegionId(country));
  }

  /**
   * Adds a search for a currency by adding the matching bundle.
   * 
   * @param currency  the currency to search for, not null
   */
  public void addCurrency(Currency currency) {
    ArgumentChecker.notNull(currency, "currency");
    addRegionKey(RegionUtils.currencyRegionId(currency));
  }

  /**
   * Adds a search for a time-zone by adding the matching bundle.
   * 
   * @param timeZone  the time-zone to search for, not null
   */
  public void addTimeZone(TimeZone timeZone) {
    ArgumentChecker.notNull(timeZone, "timeZone");
    addRegionKey(RegionUtils.timeZoneRegionId(timeZone));
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean matches(final AbstractDocument obj) {
    if (obj instanceof RegionDocument == false) {
      return false;
    }
    final RegionDocument document = (RegionDocument) obj;
    final ManageableRegion region = document.getRegion();
    if (getRegionIds() != null && getRegionIds().contains(document.getObjectId()) == false) {
      return false;
    }
    if (getRegionKeys() != null && getRegionKeys().matches(region.getIdentifiers()) == false) {
      return false;
    }
    if (getName() != null && RegexUtils.wildcardMatch(getName(), region.getName()) == false) {
      return false;
    }
    if (getClassification() != null && getClassification() != region.getClassification()) {
      return false;
    }
    if (getProviderKey() != null && getProviderKey().equals(document.getProviderKey()) == false) {
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

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 74326820:  // regionIds
        return getRegionIds();
      case -1990775032:  // regionKeys
        return getRegionKeys();
      case 3373707:  // name
        return getName();
      case 382350310:  // classification
        return getClassification();
      case 2064682670:  // providerKey
        return getProviderKey();
      case 178436081:  // childrenOfId
        return getChildrenOfId();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 74326820:  // regionIds
        setRegionIds((List<ObjectId>) newValue);
        return;
      case -1990775032:  // regionKeys
        setRegionKeys((IdentifierSearch) newValue);
        return;
      case 3373707:  // name
        setName((String) newValue);
        return;
      case 382350310:  // classification
        setClassification((RegionClassification) newValue);
        return;
      case 2064682670:  // providerKey
        setProviderKey((Identifier) newValue);
        return;
      case 178436081:  // childrenOfId
        setChildrenOfId((UniqueId) newValue);
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
      RegionSearchRequest other = (RegionSearchRequest) obj;
      return JodaBeanUtils.equal(getRegionIds(), other.getRegionIds()) &&
          JodaBeanUtils.equal(getRegionKeys(), other.getRegionKeys()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getClassification(), other.getClassification()) &&
          JodaBeanUtils.equal(getProviderKey(), other.getProviderKey()) &&
          JodaBeanUtils.equal(getChildrenOfId(), other.getChildrenOfId()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getRegionIds());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRegionKeys());
    hash += hash * 31 + JodaBeanUtils.hashCode(getName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassification());
    hash += hash * 31 + JodaBeanUtils.hashCode(getProviderKey());
    hash += hash * 31 + JodaBeanUtils.hashCode(getChildrenOfId());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of region object identifiers, null to not limit by region object identifiers.
   * Note that an empty set will return no regions.
   * @return the value of the property
   */
  public List<ObjectId> getRegionIds() {
    return _regionIds;
  }

  /**
   * Gets the the {@code regionIds} property.
   * Note that an empty set will return no regions.
   * @return the property, not null
   */
  public final Property<List<ObjectId>> regionIds() {
    return metaBean().regionIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the region keys to match, null to not match on region keys.
   * @return the value of the property
   */
  public IdentifierSearch getRegionKeys() {
    return _regionKeys;
  }

  /**
   * Sets the region keys to match, null to not match on region keys.
   * @param regionKeys  the new value of the property
   */
  public void setRegionKeys(IdentifierSearch regionKeys) {
    this._regionKeys = regionKeys;
  }

  /**
   * Gets the the {@code regionKeys} property.
   * @return the property, not null
   */
  public final Property<IdentifierSearch> regionKeys() {
    return metaBean().regionKeys().createProperty(this);
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
   * Gets the data provider key to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   * @return the value of the property
   */
  public Identifier getProviderKey() {
    return _providerKey;
  }

  /**
   * Sets the data provider key to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   * @param providerKey  the new value of the property
   */
  public void setProviderKey(Identifier providerKey) {
    this._providerKey = providerKey;
  }

  /**
   * Gets the the {@code providerKey} property.
   * This field is useful when receiving updates from the same provider.
   * @return the property, not null
   */
  public final Property<Identifier> providerKey() {
    return metaBean().providerKey().createProperty(this);
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
  /**
   * The meta-bean for {@code RegionSearchRequest}.
   */
  public static class Meta extends AbstractSearchRequest.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code regionIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ObjectId>> _regionIds = DirectMetaProperty.ofReadWrite(
        this, "regionIds", RegionSearchRequest.class, (Class) List.class);
    /**
     * The meta-property for the {@code regionKeys} property.
     */
    private final MetaProperty<IdentifierSearch> _regionKeys = DirectMetaProperty.ofReadWrite(
        this, "regionKeys", RegionSearchRequest.class, IdentifierSearch.class);
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
     * The meta-property for the {@code providerKey} property.
     */
    private final MetaProperty<Identifier> _providerKey = DirectMetaProperty.ofReadWrite(
        this, "providerKey", RegionSearchRequest.class, Identifier.class);
    /**
     * The meta-property for the {@code childrenOfId} property.
     */
    private final MetaProperty<UniqueId> _childrenOfId = DirectMetaProperty.ofReadWrite(
        this, "childrenOfId", RegionSearchRequest.class, UniqueId.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "regionIds",
        "regionKeys",
        "name",
        "classification",
        "providerKey",
        "childrenOfId");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 74326820:  // regionIds
          return _regionIds;
        case -1990775032:  // regionKeys
          return _regionKeys;
        case 3373707:  // name
          return _name;
        case 382350310:  // classification
          return _classification;
        case 2064682670:  // providerKey
          return _providerKey;
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
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code regionIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ObjectId>> regionIds() {
      return _regionIds;
    }

    /**
     * The meta-property for the {@code regionKeys} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<IdentifierSearch> regionKeys() {
      return _regionKeys;
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
     * The meta-property for the {@code providerKey} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Identifier> providerKey() {
      return _providerKey;
    }

    /**
     * The meta-property for the {@code childrenOfId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> childrenOfId() {
      return _childrenOfId;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
