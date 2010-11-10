/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;
import javax.time.calendar.TimeZone;

import org.joda.beans.BeanDefinition;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.BasicMetaBean;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaProperty;

import com.opengamma.core.common.Currency;
import com.opengamma.core.region.RegionClassification;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.PagingRequest;

/**
 * Request for searching for regions.
 */
@BeanDefinition
public class RegionSearchRequest extends DirectBean implements Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The request for paging.
   * By default all matching items will be returned.
   */
  @PropertyDefinition
  private PagingRequest _pagingRequest = PagingRequest.ALL;
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
   * The identifier of the data provider, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   */
  @PropertyDefinition
  private Identifier _providerId;
  /**
   * The unique identifier to get children of, null to not retrieve based on children.
   * Only the immediate children of the identifier will be matched.
   */
  @PropertyDefinition
  private UniqueIdentifier _childrenOfId;
  /**
   * The region identifier bundles to match, empty to not match on this field, not null.
   * A region matches if one of the bundles matches.
   * Note that an empty set places no restrictions on the result.
   */
  @PropertyDefinition(set = "setClearAddAll")
  private final Set<IdentifierBundle> _identifiers = new HashSet<IdentifierBundle>();
  /** 
   * The instant to search for a version at, null treated as the latest version.
   */
  @PropertyDefinition
  private Instant _versionAsOfInstant;
  /**
   * The instant to search for corrections for, null treated as the latest correction.
   */
  @PropertyDefinition
  private Instant _correctedToInstant;

  /**
   * Creates an instance.
   */
  public RegionSearchRequest() {
  }

  /**
   * Creates an instance using a single search identifier.
   * 
   * @param identifier  the identifier to look up, not null
   */
  public RegionSearchRequest(Identifier identifier) {
    addIdentifierBundle(IdentifierBundle.of(identifier));
  }

  /**
   * Creates an instance using a bundle of identifiers.
   * 
   * @param bundle  the bundle of identifiers to look up, not null
   */
  public RegionSearchRequest(IdentifierBundle bundle) {
    ArgumentChecker.notNull(bundle, "identifiers");
    addIdentifierBundle(bundle);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a bundle representing this identifier to the collection to search for.
   * 
   * @param identifier  the identifier to add as a bundle, not null
   */
  public void addIdentifierBundle(Identifier identifier) {
    addIdentifierBundle(IdentifierBundle.of(identifier));
  }

  /**
   * Adds a bundle to the collection to search for.
   * 
   * @param bundle  the bundle to add, not null
   */
  public void addIdentifierBundle(IdentifierBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    getIdentifiers().add(bundle);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a search for a currency by adding the matching bundle.
   * 
   * @param countryISO  the country ISO code to search for, not null
   */
  public void addCountryISO(String countryISO) {
    addIdentifierBundle(IdentifierBundle.of(RegionUtils.countryRegionId(countryISO)));
  }

  /**
   * Adds a search for a currency by adding the matching bundle.
   * 
   * @param currency  the currency to search for, not null
   */
  public void addCurrency(Currency currency) {
    ArgumentChecker.notNull(currency, "currency");
    addIdentifierBundle(IdentifierBundle.of(RegionUtils.currencyRegionId(currency)));
  }

  /**
   * Adds a search for a time-zone by adding the matching bundle.
   * 
   * @param timeZone  the time-zone to search for, not null
   */
  public void addCurrency(TimeZone timeZone) {
    ArgumentChecker.notNull(timeZone, "timeZone");
    addIdentifierBundle(IdentifierBundle.of(RegionUtils.timeZoneRegionId(timeZone)));
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
      case -2092032669:  // pagingRequest
        return getPagingRequest();
      case 3373707:  // name
        return getName();
      case 382350310:  // classification
        return getClassification();
      case 205149932:  // providerId
        return getProviderId();
      case 178436081:  // childrenOfId
        return getChildrenOfId();
      case 1368189162:  // identifiers
        return getIdentifiers();
      case 598802432:  // versionAsOfInstant
        return getVersionAsOfInstant();
      case -28367267:  // correctedToInstant
        return getCorrectedToInstant();
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
      case 3373707:  // name
        setName((String) newValue);
        return;
      case 382350310:  // classification
        setClassification((RegionClassification) newValue);
        return;
      case 205149932:  // providerId
        setProviderId((Identifier) newValue);
        return;
      case 178436081:  // childrenOfId
        setChildrenOfId((UniqueIdentifier) newValue);
        return;
      case 1368189162:  // identifiers
        setIdentifiers((Set<IdentifierBundle>) newValue);
        return;
      case 598802432:  // versionAsOfInstant
        setVersionAsOfInstant((Instant) newValue);
        return;
      case -28367267:  // correctedToInstant
        setCorrectedToInstant((Instant) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
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
   * Gets the identifier of the data provider, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   * @return the value of the property
   */
  public Identifier getProviderId() {
    return _providerId;
  }

  /**
   * Sets the identifier of the data provider, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   * @param providerId  the new value of the property
   */
  public void setProviderId(Identifier providerId) {
    this._providerId = providerId;
  }

  /**
   * Gets the the {@code providerId} property.
   * This field is useful when receiving updates from the same provider.
   * @return the property, not null
   */
  public final Property<Identifier> providerId() {
    return metaBean().providerId().createProperty(this);
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
   * Gets the region identifier bundles to match, empty to not match on this field, not null.
   * A region matches if one of the bundles matches.
   * Note that an empty set places no restrictions on the result.
   * @return the value of the property
   */
  public Set<IdentifierBundle> getIdentifiers() {
    return _identifiers;
  }

  /**
   * Sets the region identifier bundles to match, empty to not match on this field, not null.
   * A region matches if one of the bundles matches.
   * Note that an empty set places no restrictions on the result.
   * @param identifiers  the new value of the property
   */
  public void setIdentifiers(Set<IdentifierBundle> identifiers) {
    this._identifiers.clear();
    this._identifiers.addAll(identifiers);
  }

  /**
   * Gets the the {@code identifiers} property.
   * A region matches if one of the bundles matches.
   * Note that an empty set places no restrictions on the result.
   * @return the property, not null
   */
  public final Property<Set<IdentifierBundle>> identifiers() {
    return metaBean().identifiers().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the instant to search for a version at, null treated as the latest version.
   * @return the value of the property
   */
  public Instant getVersionAsOfInstant() {
    return _versionAsOfInstant;
  }

  /**
   * Sets the instant to search for a version at, null treated as the latest version.
   * @param versionAsOfInstant  the new value of the property
   */
  public void setVersionAsOfInstant(Instant versionAsOfInstant) {
    this._versionAsOfInstant = versionAsOfInstant;
  }

  /**
   * Gets the the {@code versionAsOfInstant} property.
   * @return the property, not null
   */
  public final Property<Instant> versionAsOfInstant() {
    return metaBean().versionAsOfInstant().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the instant to search for corrections for, null treated as the latest correction.
   * @return the value of the property
   */
  public Instant getCorrectedToInstant() {
    return _correctedToInstant;
  }

  /**
   * Sets the instant to search for corrections for, null treated as the latest correction.
   * @param correctedToInstant  the new value of the property
   */
  public void setCorrectedToInstant(Instant correctedToInstant) {
    this._correctedToInstant = correctedToInstant;
  }

  /**
   * Gets the the {@code correctedToInstant} property.
   * @return the property, not null
   */
  public final Property<Instant> correctedToInstant() {
    return metaBean().correctedToInstant().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code RegionSearchRequest}.
   */
  public static class Meta extends BasicMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code pagingRequest} property.
     */
    private final MetaProperty<PagingRequest> _pagingRequest = DirectMetaProperty.ofReadWrite(this, "pagingRequest", PagingRequest.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(this, "name", String.class);
    /**
     * The meta-property for the {@code classification} property.
     */
    private final MetaProperty<RegionClassification> _classification = DirectMetaProperty.ofReadWrite(this, "classification", RegionClassification.class);
    /**
     * The meta-property for the {@code providerId} property.
     */
    private final MetaProperty<Identifier> _providerId = DirectMetaProperty.ofReadWrite(this, "providerId", Identifier.class);
    /**
     * The meta-property for the {@code childrenOfId} property.
     */
    private final MetaProperty<UniqueIdentifier> _childrenOfId = DirectMetaProperty.ofReadWrite(this, "childrenOfId", UniqueIdentifier.class);
    /**
     * The meta-property for the {@code identifiers} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<IdentifierBundle>> _identifiers = DirectMetaProperty.ofReadWrite(this, "identifiers", (Class) Set.class);
    /**
     * The meta-property for the {@code versionAsOfInstant} property.
     */
    private final MetaProperty<Instant> _versionAsOfInstant = DirectMetaProperty.ofReadWrite(this, "versionAsOfInstant", Instant.class);
    /**
     * The meta-property for the {@code correctedToInstant} property.
     */
    private final MetaProperty<Instant> _correctedToInstant = DirectMetaProperty.ofReadWrite(this, "correctedToInstant", Instant.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map;

    @SuppressWarnings({"unchecked", "rawtypes" })
    protected Meta() {
      LinkedHashMap temp = new LinkedHashMap();
      temp.put("pagingRequest", _pagingRequest);
      temp.put("name", _name);
      temp.put("classification", _classification);
      temp.put("providerId", _providerId);
      temp.put("childrenOfId", _childrenOfId);
      temp.put("identifiers", _identifiers);
      temp.put("versionAsOfInstant", _versionAsOfInstant);
      temp.put("correctedToInstant", _correctedToInstant);
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
     * The meta-property for the {@code pagingRequest} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<PagingRequest> pagingRequest() {
      return _pagingRequest;
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
    public final MetaProperty<Identifier> providerId() {
      return _providerId;
    }

    /**
     * The meta-property for the {@code childrenOfId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueIdentifier> childrenOfId() {
      return _childrenOfId;
    }

    /**
     * The meta-property for the {@code identifiers} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<IdentifierBundle>> identifiers() {
      return _identifiers;
    }

    /**
     * The meta-property for the {@code versionAsOfInstant} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Instant> versionAsOfInstant() {
      return _versionAsOfInstant;
    }

    /**
     * The meta-property for the {@code correctedToInstant} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Instant> correctedToInstant() {
      return _correctedToInstant;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
