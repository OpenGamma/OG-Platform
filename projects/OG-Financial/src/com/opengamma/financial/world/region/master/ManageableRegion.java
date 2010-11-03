/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.region.master;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.TimeZone;

import org.joda.beans.BeanDefinition;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.BasicMetaBean;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.financial.Currency;
import com.opengamma.financial.world.region.Region;
import com.opengamma.financial.world.region.RegionClassification;
import com.opengamma.financial.world.region.RegionUtils;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * The manageable implementation of a region.
 * <p>
 * This implementation is used by the region master to store and manipulate the data.
 */
@BeanDefinition
public class ManageableRegion extends DirectBean implements Region {

  /**
   * The unique identifier of the region.
   */
  @PropertyDefinition
  private UniqueIdentifier _uniqueIdentifier;
  /**
   * The classification of the region.
   */
  @PropertyDefinition
  private RegionClassification _classification;
  /**
   * The unique identifiers of the parent regions.
   * For example, a country might be a member of the World, UN, European Union and NATO.
   */
  @PropertyDefinition(set = "setClearAddAll")
  private Set<UniqueIdentifier> _parentRegionIds = new HashSet<UniqueIdentifier>();
  /**
   * The short descriptive name for the region.
   */
  @PropertyDefinition
  private String _name;
  /**
   * The full descriptive name for the region.
   */
  @PropertyDefinition
  private String _fullName;
  /**
   * The identifiers defining the region.
   * This will include the country, currency and time-zone.
   */
  @PropertyDefinition
  private IdentifierBundle _identifiers = IdentifierBundle.EMPTY;
  /**
   * The extensible data store for additional information.
   * Applications may store additional region based information here.
   */
  @PropertyDefinition
  private final FlexiBean _data = new FlexiBean();

  /**
   * Creates an instance.
   */
  public ManageableRegion() {
  }

  /**
   * Create an instance from another region instance.
   * <p>
   * This copies the specified region creating an independent copy.
   * 
   * @param region  the region to copy, not null
   */
  public ManageableRegion(final Region region) {
    ArgumentChecker.notNull(region, "region");
    setUniqueIdentifier(region.getUniqueIdentifier());
    setClassification(region.getClassification());
    setParentRegionIds(region.getParentRegionIds());
    setName(region.getName());
    setFullName(region.getFullName());
    setIdentifiers(region.getIdentifiers());
    setData(region.getData());
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a bundle representing this identifier to the collection to search for.
   * 
   * @param identifier  the identifier to add as a bundle, not null
   */
  public void addIdentifier(Identifier identifier) {
    setIdentifiers(getIdentifiers().withIdentifier(identifier));
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the country ISO code.
   * This is the 2 letter country code.
   * @return the value of the property
   */
  public String getCountryISO() {
    return _identifiers.getIdentifier(RegionUtils.ISO_COUNTRY_ALPHA2);
  }

  /**
   * Sets the country, stored in the identifier set.
   * 
   * @param countryISO  the country to set, null to remove any country
   */
  public void setCountryISO(String countryISO) {
    setIdentifiers(getIdentifiers().withoutScheme(RegionUtils.ISO_COUNTRY_ALPHA2));
    if (countryISO != null) {
      addIdentifier(RegionUtils.countryRegionId(countryISO));
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the currency.
   * @return the value of the property
   */
  public Currency getCurrency() {
    String code = _identifiers.getIdentifier(RegionUtils.ISO_CURRENCY_ALPHA3);
    return (code != null ? Currency.getInstance(code) : null);
  }

  /**
   * Sets the currency, stored in the identifier set.
   * 
   * @param currency  the currency to set, null to remove any currency
   */
  public void setCurrency(Currency currency) {
    setIdentifiers(getIdentifiers().withoutScheme(RegionUtils.ISO_CURRENCY_ALPHA3));
    if (currency != null) {
      addIdentifier(RegionUtils.currencyRegionId(currency));
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the time-zone.
   * For larger regions, there can be multiple time-zones, so this is only reliable
   * for municipalities.
   * @return the value of the property
   */
  public TimeZone getTimeZone() {
    String id = _identifiers.getIdentifier(RegionUtils.TZDB_TIME_ZONE);
    return (id != null ? TimeZone.of(id) : null);
  }

  /**
   * Sets the time-zone, stored in the identifier set.
   * 
   * @param timeZone  the time-zone to set, null to remove any time-zone
   */
  public void setTimeZone(TimeZone timeZone) {
    setIdentifiers(getIdentifiers().withoutScheme(RegionUtils.TZDB_TIME_ZONE));
    if (timeZone != null) {
      addIdentifier(RegionUtils.timeZoneRegionId(timeZone));
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ManageableRegion}.
   * @return the meta-bean, not null
   */
  public static ManageableRegion.Meta meta() {
    return ManageableRegion.Meta.INSTANCE;
  }

  @Override
  public ManageableRegion.Meta metaBean() {
    return ManageableRegion.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case -125484198:  // uniqueIdentifier
        return getUniqueIdentifier();
      case 382350310:  // classification
        return getClassification();
      case 1273190810:  // parentRegionIds
        return getParentRegionIds();
      case 3373707:  // name
        return getName();
      case 1330852282:  // fullName
        return getFullName();
      case 1368189162:  // identifiers
        return getIdentifiers();
      case 3076010:  // data
        return getData();
    }
    return super.propertyGet(propertyName);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case -125484198:  // uniqueIdentifier
        setUniqueIdentifier((UniqueIdentifier) newValue);
        return;
      case 382350310:  // classification
        setClassification((RegionClassification) newValue);
        return;
      case 1273190810:  // parentRegionIds
        setParentRegionIds((Set<UniqueIdentifier>) newValue);
        return;
      case 3373707:  // name
        setName((String) newValue);
        return;
      case 1330852282:  // fullName
        setFullName((String) newValue);
        return;
      case 1368189162:  // identifiers
        setIdentifiers((IdentifierBundle) newValue);
        return;
      case 3076010:  // data
        setData((FlexiBean) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unique identifier of the region.
   * @return the value of the property
   */
  public UniqueIdentifier getUniqueIdentifier() {
    return _uniqueIdentifier;
  }

  /**
   * Sets the unique identifier of the region.
   * @param uniqueIdentifier  the new value of the property
   */
  public void setUniqueIdentifier(UniqueIdentifier uniqueIdentifier) {
    this._uniqueIdentifier = uniqueIdentifier;
  }

  /**
   * Gets the the {@code uniqueIdentifier} property.
   * @return the property, not null
   */
  public final Property<UniqueIdentifier> uniqueIdentifier() {
    return metaBean().uniqueIdentifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classification of the region.
   * @return the value of the property
   */
  public RegionClassification getClassification() {
    return _classification;
  }

  /**
   * Sets the classification of the region.
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
   * Gets the unique identifiers of the parent regions.
   * For example, a country might be a member of the World, UN, European Union and NATO.
   * @return the value of the property
   */
  public Set<UniqueIdentifier> getParentRegionIds() {
    return _parentRegionIds;
  }

  /**
   * Sets the unique identifiers of the parent regions.
   * For example, a country might be a member of the World, UN, European Union and NATO.
   * @param parentRegionIds  the new value of the property
   */
  public void setParentRegionIds(Set<UniqueIdentifier> parentRegionIds) {
    this._parentRegionIds.clear();
    this._parentRegionIds.addAll(parentRegionIds);
  }

  /**
   * Gets the the {@code parentRegionIds} property.
   * For example, a country might be a member of the World, UN, European Union and NATO.
   * @return the property, not null
   */
  public final Property<Set<UniqueIdentifier>> parentRegionIds() {
    return metaBean().parentRegionIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the short descriptive name for the region.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the short descriptive name for the region.
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
   * Gets the full descriptive name for the region.
   * @return the value of the property
   */
  public String getFullName() {
    return _fullName;
  }

  /**
   * Sets the full descriptive name for the region.
   * @param fullName  the new value of the property
   */
  public void setFullName(String fullName) {
    this._fullName = fullName;
  }

  /**
   * Gets the the {@code fullName} property.
   * @return the property, not null
   */
  public final Property<String> fullName() {
    return metaBean().fullName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the identifiers defining the region.
   * This will include the country, currency and time-zone.
   * @return the value of the property
   */
  public IdentifierBundle getIdentifiers() {
    return _identifiers;
  }

  /**
   * Sets the identifiers defining the region.
   * This will include the country, currency and time-zone.
   * @param identifiers  the new value of the property
   */
  public void setIdentifiers(IdentifierBundle identifiers) {
    this._identifiers = identifiers;
  }

  /**
   * Gets the the {@code identifiers} property.
   * This will include the country, currency and time-zone.
   * @return the property, not null
   */
  public final Property<IdentifierBundle> identifiers() {
    return metaBean().identifiers().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the extensible data store for additional information.
   * Applications may store additional region based information here.
   * @return the value of the property
   */
  public FlexiBean getData() {
    return _data;
  }

  /**
   * Sets the extensible data store for additional information.
   * Applications may store additional region based information here.
   * @param data  the new value of the property
   */
  public void setData(FlexiBean data) {
    this._data.clear();
    this._data.putAll(data);
  }

  /**
   * Gets the the {@code data} property.
   * Applications may store additional region based information here.
   * @return the property, not null
   */
  public final Property<FlexiBean> data() {
    return metaBean().data().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ManageableRegion}.
   */
  public static class Meta extends BasicMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueIdentifier} property.
     */
    private final MetaProperty<UniqueIdentifier> _uniqueIdentifier = DirectMetaProperty.ofReadWrite(this, "uniqueIdentifier", UniqueIdentifier.class);
    /**
     * The meta-property for the {@code classification} property.
     */
    private final MetaProperty<RegionClassification> _classification = DirectMetaProperty.ofReadWrite(this, "classification", RegionClassification.class);
    /**
     * The meta-property for the {@code parentRegionIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<UniqueIdentifier>> _parentRegionIds = DirectMetaProperty.ofReadWrite(this, "parentRegionIds", (Class) Set.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(this, "name", String.class);
    /**
     * The meta-property for the {@code fullName} property.
     */
    private final MetaProperty<String> _fullName = DirectMetaProperty.ofReadWrite(this, "fullName", String.class);
    /**
     * The meta-property for the {@code identifiers} property.
     */
    private final MetaProperty<IdentifierBundle> _identifiers = DirectMetaProperty.ofReadWrite(this, "identifiers", IdentifierBundle.class);
    /**
     * The meta-property for the {@code data} property.
     */
    private final MetaProperty<FlexiBean> _data = DirectMetaProperty.ofReadWrite(this, "data", FlexiBean.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map;

    @SuppressWarnings({"unchecked", "rawtypes" })
    protected Meta() {
      LinkedHashMap temp = new LinkedHashMap();
      temp.put("uniqueIdentifier", _uniqueIdentifier);
      temp.put("classification", _classification);
      temp.put("parentRegionIds", _parentRegionIds);
      temp.put("name", _name);
      temp.put("fullName", _fullName);
      temp.put("identifiers", _identifiers);
      temp.put("data", _data);
      _map = Collections.unmodifiableMap(temp);
    }

    @Override
    public ManageableRegion createBean() {
      return new ManageableRegion();
    }

    @Override
    public Class<? extends ManageableRegion> beanType() {
      return ManageableRegion.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code uniqueIdentifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueIdentifier> uniqueIdentifier() {
      return _uniqueIdentifier;
    }

    /**
     * The meta-property for the {@code classification} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<RegionClassification> classification() {
      return _classification;
    }

    /**
     * The meta-property for the {@code parentRegionIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<UniqueIdentifier>> parentRegionIds() {
      return _parentRegionIds;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code fullName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> fullName() {
      return _fullName;
    }

    /**
     * The meta-property for the {@code identifiers} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<IdentifierBundle> identifiers() {
      return _identifiers;
    }

    /**
     * The meta-property for the {@code data} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<FlexiBean> data() {
      return _data;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
