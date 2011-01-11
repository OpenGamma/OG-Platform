/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.time.calendar.TimeZone;

import org.joda.beans.BeanDefinition;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaProperty;

import com.google.common.collect.Iterables;
import com.opengamma.core.common.Currency;
import com.opengamma.core.region.RegionClassification;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierSearch;
import com.opengamma.id.IdentifierSearchType;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.util.ArgumentChecker;

/**
 * Request for searching for regions.
 * <p>
 * Documents will be returned that match the search criteria.
 * This class provides the ability to page the results and to search
 * as at a specific version and correction instant.
 * See {@link RegionHistoryRequest} for more details on how history works.
 */
@BeanDefinition
public class RegionSearchRequest extends AbstractSearchRequest implements Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The set of region object identifiers, null to not limit by region object identifiers.
   * Note that an empty set will return no regions.
   */
  @PropertyDefinition(set = "manual")
  private List<UniqueIdentifier> _regionIds;
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
  private UniqueIdentifier _childrenOfId;

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
   * Adds a single region id to the set.
   * 
   * @param regionId  the region id to add, not null
   */
  public void addRegionId(UniqueIdentifier regionId) {
    ArgumentChecker.notNull(regionId, "regionId");
    if (_regionIds == null) {
      _regionIds = new ArrayList<UniqueIdentifier>();
    }
    _regionIds.add(regionId);
  }

  /**
   * Sets the set of region object identifiers, null to not limit by region object identifiers.
   * Note that an empty set will return no regions.
   * 
   * @param regionIds  the new region identifiers, null clears the region id search
   */
  public void setRegionIds(Iterable<UniqueIdentifier> regionIds) {
    if (regionIds == null) {
      _regionIds = null;
    } else {
      _regionIds = new ArrayList<UniqueIdentifier>();
      Iterables.addAll(_regionIds, regionIds);
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
   * @param countryISO  the country ISO code to search for, not null
   */
  public void addCountryISO(String countryISO) {
    addRegionKey(RegionUtils.countryRegionId(countryISO));
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

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code RegionSearchRequest}.
   * @return the meta-bean, not null
   */
  public static RegionSearchRequest.Meta meta() {
    return RegionSearchRequest.Meta.INSTANCE;
  }

  @Override
  public RegionSearchRequest.Meta metaBean() {
    return RegionSearchRequest.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
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
    return super.propertyGet(propertyName);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case 74326820:  // regionIds
        setRegionIds((List<UniqueIdentifier>) newValue);
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
        setChildrenOfId((UniqueIdentifier) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of region object identifiers, null to not limit by region object identifiers.
   * Note that an empty set will return no regions.
   * @return the value of the property
   */
  public List<UniqueIdentifier> getRegionIds() {
    return _regionIds;
  }

  /**
   * Gets the the {@code regionIds} property.
   * Note that an empty set will return no regions.
   * @return the property, not null
   */
  public final Property<List<UniqueIdentifier>> regionIds() {
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
  public UniqueIdentifier getChildrenOfId() {
    return _childrenOfId;
  }

  /**
   * Sets the unique identifier to get children of, null to not retrieve based on children.
   * Only the immediate children of the identifier will be matched.
   * @param childrenOfId  the new value of the property
   */
  public void setChildrenOfId(UniqueIdentifier childrenOfId) {
    this._childrenOfId = childrenOfId;
  }

  /**
   * Gets the the {@code childrenOfId} property.
   * Only the immediate children of the identifier will be matched.
   * @return the property, not null
   */
  public final Property<UniqueIdentifier> childrenOfId() {
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
    private final MetaProperty<List<UniqueIdentifier>> _regionIds = DirectMetaProperty.ofReadWrite(this, "regionIds", (Class) List.class);
    /**
     * The meta-property for the {@code regionKeys} property.
     */
    private final MetaProperty<IdentifierSearch> _regionKeys = DirectMetaProperty.ofReadWrite(this, "regionKeys", IdentifierSearch.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(this, "name", String.class);
    /**
     * The meta-property for the {@code classification} property.
     */
    private final MetaProperty<RegionClassification> _classification = DirectMetaProperty.ofReadWrite(this, "classification", RegionClassification.class);
    /**
     * The meta-property for the {@code providerKey} property.
     */
    private final MetaProperty<Identifier> _providerKey = DirectMetaProperty.ofReadWrite(this, "providerKey", Identifier.class);
    /**
     * The meta-property for the {@code childrenOfId} property.
     */
    private final MetaProperty<UniqueIdentifier> _childrenOfId = DirectMetaProperty.ofReadWrite(this, "childrenOfId", UniqueIdentifier.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map;

    @SuppressWarnings({"unchecked", "rawtypes" })
    protected Meta() {
      LinkedHashMap temp = new LinkedHashMap(super.metaPropertyMap());
      temp.put("regionIds", _regionIds);
      temp.put("regionKeys", _regionKeys);
      temp.put("name", _name);
      temp.put("classification", _classification);
      temp.put("providerKey", _providerKey);
      temp.put("childrenOfId", _childrenOfId);
      _map = Collections.unmodifiableMap(temp);
    }

    @Override
    public RegionSearchRequest createBean() {
      return new RegionSearchRequest();
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
    public final MetaProperty<List<UniqueIdentifier>> regionIds() {
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
    public final MetaProperty<UniqueIdentifier> childrenOfId() {
      return _childrenOfId;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
