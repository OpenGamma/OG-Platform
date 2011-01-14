/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.time.calendar.LocalDate;

import org.joda.beans.BeanDefinition;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaProperty;

import com.google.common.collect.Iterables;
import com.opengamma.core.common.Currency;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierSearch;
import com.opengamma.id.IdentifierSearchType;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.util.ArgumentChecker;

/**
 * Request for searching for holidays.
 * <p>
 * Documents will be returned that match the search criteria.
 * This class provides the ability to page the results and to search
 * as at a specific version and correction instant.
 * See {@link HolidayHistoryRequest} for more details on how history works.
 */
@BeanDefinition
public class HolidaySearchRequest extends AbstractSearchRequest implements Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The set of holiday object identifiers, null to not limit by holiday object identifiers.
   * Note that an empty set will return no holidays.
   */
  @PropertyDefinition(set = "manual")
  private List<UniqueIdentifier> _holidayIds;
  /**
   * The holiday name, wildcards allowed, null to not match on name.
   */
  @PropertyDefinition
  private String _name;
  /**
   * The holiday type, null to not match on type.
   */
  @PropertyDefinition
  private HolidayType _type;
  /**
   * The data provider key to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   */
  @PropertyDefinition
  private Identifier _providerKey;
  /**
   * A date to check to determine if it is a holiday, null to not match on type.
   */
  @PropertyDefinition
  private LocalDate _dateToCheck;
  /**
   * The currency to search for, null to not match on currency.
   */
  @PropertyDefinition
  private Currency _currency;
  /**
   * The region keys to match, null to not match on region keys.
   * This will return holidays where the holiday region identifier matches one of the search identifiers.
   * Note that only the {@link IdentifierSearchType#ANY any} search type is applicable.
   */
  @PropertyDefinition
  private IdentifierSearch _regionKeys;
  /**
   * The exchange keys to match, null to not match on exchange keys.
   * This will return holidays where the holiday exchange identifier matches one of the search identifiers.
   * Note that only the {@link IdentifierSearchType#ANY any} search type is applicable.
   */
  @PropertyDefinition
  private IdentifierSearch _exchangeKeys;

  /**
   * Creates an instance.
   */
  public HolidaySearchRequest() {
  }

  /**
   * Creates an instance.
   * 
   * @param type  the type of the holiday, not null
   */
  public HolidaySearchRequest(final HolidayType type) {
    ArgumentChecker.notNull(type, "type");
    setType(type);
  }

  /**
   * Creates an instance to search for the specified currency.
   * <p>
   * The type will be set to be CURRENCY.
   * 
   * @param currency  the currency to search for, not null
   */
  public HolidaySearchRequest(Currency currency) {
    ArgumentChecker.notNull(currency, "currency");
    setCurrency(currency);
    setType(HolidayType.CURRENCY);
  }

  /**
   * Creates an instance to search for the specified identifier.
   * <p>
   * The type will be used to determine if the identifiers are regions or exchanges.
   * 
   * @param type  the type of the holiday, not null
   * @param exchangeOrRegionKeys  the region or exchange identifiers to search for, not null
   */
  public HolidaySearchRequest(final HolidayType type, final Iterable<Identifier> exchangeOrRegionKeys) {
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(exchangeOrRegionKeys, "exchangeOrRegionIds");
    setType(type);
    switch (type) {
      case BANK:
        setRegionKeys(new IdentifierSearch(exchangeOrRegionKeys));
        break;
      case SETTLEMENT:
      case TRADING:
        setExchangeKeys(new IdentifierSearch(exchangeOrRegionKeys));
        break;
      case CURRENCY:
      default:
        throw new IllegalArgumentException("Use currency constructor to request a currency holiday");
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single holiday id to the set.
   * 
   * @param holidayId  the holiday id to add, not null
   */
  public void addHolidayId(UniqueIdentifier holidayId) {
    ArgumentChecker.notNull(holidayId, "holidayId");
    if (_holidayIds == null) {
      _holidayIds = new ArrayList<UniqueIdentifier>();
    }
    _holidayIds.add(holidayId);
  }

  /**
   * Sets the set of holiday object identifiers, null to not limit by holiday object identifiers.
   * Note that an empty set will return no holidays.
   * 
   * @param holidayIds  the new holiday identifiers, null clears the holiday id search
   */
  public void setHolidayIds(Iterable<UniqueIdentifier> holidayIds) {
    if (holidayIds == null) {
      _holidayIds = null;
    } else {
      _holidayIds = new ArrayList<UniqueIdentifier>();
      Iterables.addAll(_holidayIds, holidayIds);
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
    addExchangeKeys(Arrays.asList(regionKey));
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
    if (getExchangeKeys() == null) {
      setExchangeKeys(new IdentifierSearch(regionKeys));
    } else {
      getExchangeKeys().addIdentifiers(regionKeys);
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
    if (getExchangeKeys() == null) {
      setExchangeKeys(new IdentifierSearch(regionKeys));
    } else {
      getExchangeKeys().addIdentifiers(regionKeys);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single exchange key identifier to the collection to search for.
   * Unless customized, the search will match 
   * {@link IdentifierSearchType#ANY any} of the identifiers.
   * 
   * @param exchangeKey  the exchange key identifier to add, not null
   */
  public void addExchangeKey(Identifier exchangeKey) {
    ArgumentChecker.notNull(exchangeKey, "exchangeKey");
    addExchangeKeys(Arrays.asList(exchangeKey));
  }

  /**
   * Adds a collection of exchange key identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link IdentifierSearchType#ANY any} of the identifiers.
   * 
   * @param exchangeKeys  the exchange key identifiers to add, not null
   */
  public void addExchangeKeys(Identifier... exchangeKeys) {
    ArgumentChecker.notNull(exchangeKeys, "exchangeKeys");
    if (getExchangeKeys() == null) {
      setExchangeKeys(new IdentifierSearch(exchangeKeys));
    } else {
      getExchangeKeys().addIdentifiers(exchangeKeys);
    }
  }

  /**
   * Adds a collection of exchange key identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link IdentifierSearchType#ANY any} of the identifiers.
   * 
   * @param exchangeKeys  the exchange key identifiers to add, not null
   */
  public void addExchangeKeys(Iterable<Identifier> exchangeKeys) {
    ArgumentChecker.notNull(exchangeKeys, "exchangeKeys");
    if (getExchangeKeys() == null) {
      setExchangeKeys(new IdentifierSearch(exchangeKeys));
    } else {
      getExchangeKeys().addIdentifiers(exchangeKeys);
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code HolidaySearchRequest}.
   * @return the meta-bean, not null
   */
  public static HolidaySearchRequest.Meta meta() {
    return HolidaySearchRequest.Meta.INSTANCE;
  }

  @Override
  public HolidaySearchRequest.Meta metaBean() {
    return HolidaySearchRequest.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case -1121781952:  // holidayIds
        return getHolidayIds();
      case 3373707:  // name
        return getName();
      case 3575610:  // type
        return getType();
      case 2064682670:  // providerKey
        return getProviderKey();
      case 14222271:  // dateToCheck
        return getDateToCheck();
      case 575402001:  // currency
        return getCurrency();
      case -1990775032:  // regionKeys
        return getRegionKeys();
      case 1429431991:  // exchangeKeys
        return getExchangeKeys();
    }
    return super.propertyGet(propertyName);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case -1121781952:  // holidayIds
        setHolidayIds((List<UniqueIdentifier>) newValue);
        return;
      case 3373707:  // name
        setName((String) newValue);
        return;
      case 3575610:  // type
        setType((HolidayType) newValue);
        return;
      case 2064682670:  // providerKey
        setProviderKey((Identifier) newValue);
        return;
      case 14222271:  // dateToCheck
        setDateToCheck((LocalDate) newValue);
        return;
      case 575402001:  // currency
        setCurrency((Currency) newValue);
        return;
      case -1990775032:  // regionKeys
        setRegionKeys((IdentifierSearch) newValue);
        return;
      case 1429431991:  // exchangeKeys
        setExchangeKeys((IdentifierSearch) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of holiday object identifiers, null to not limit by holiday object identifiers.
   * Note that an empty set will return no holidays.
   * @return the value of the property
   */
  public List<UniqueIdentifier> getHolidayIds() {
    return _holidayIds;
  }

  /**
   * Gets the the {@code holidayIds} property.
   * Note that an empty set will return no holidays.
   * @return the property, not null
   */
  public final Property<List<UniqueIdentifier>> holidayIds() {
    return metaBean().holidayIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the holiday name, wildcards allowed, null to not match on name.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the holiday name, wildcards allowed, null to not match on name.
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
   * Gets the holiday type, null to not match on type.
   * @return the value of the property
   */
  public HolidayType getType() {
    return _type;
  }

  /**
   * Sets the holiday type, null to not match on type.
   * @param type  the new value of the property
   */
  public void setType(HolidayType type) {
    this._type = type;
  }

  /**
   * Gets the the {@code type} property.
   * @return the property, not null
   */
  public final Property<HolidayType> type() {
    return metaBean().type().createProperty(this);
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
   * Gets a date to check to determine if it is a holiday, null to not match on type.
   * @return the value of the property
   */
  public LocalDate getDateToCheck() {
    return _dateToCheck;
  }

  /**
   * Sets a date to check to determine if it is a holiday, null to not match on type.
   * @param dateToCheck  the new value of the property
   */
  public void setDateToCheck(LocalDate dateToCheck) {
    this._dateToCheck = dateToCheck;
  }

  /**
   * Gets the the {@code dateToCheck} property.
   * @return the property, not null
   */
  public final Property<LocalDate> dateToCheck() {
    return metaBean().dateToCheck().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the currency to search for, null to not match on currency.
   * @return the value of the property
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Sets the currency to search for, null to not match on currency.
   * @param currency  the new value of the property
   */
  public void setCurrency(Currency currency) {
    this._currency = currency;
  }

  /**
   * Gets the the {@code currency} property.
   * @return the property, not null
   */
  public final Property<Currency> currency() {
    return metaBean().currency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the region keys to match, null to not match on region keys.
   * This will return holidays where the holiday region identifier matches one of the search identifiers.
   * Note that only the {@link IdentifierSearchType#ANY any} search type is applicable.
   * @return the value of the property
   */
  public IdentifierSearch getRegionKeys() {
    return _regionKeys;
  }

  /**
   * Sets the region keys to match, null to not match on region keys.
   * This will return holidays where the holiday region identifier matches one of the search identifiers.
   * Note that only the {@link IdentifierSearchType#ANY any} search type is applicable.
   * @param regionKeys  the new value of the property
   */
  public void setRegionKeys(IdentifierSearch regionKeys) {
    this._regionKeys = regionKeys;
  }

  /**
   * Gets the the {@code regionKeys} property.
   * This will return holidays where the holiday region identifier matches one of the search identifiers.
   * Note that only the {@link IdentifierSearchType#ANY any} search type is applicable.
   * @return the property, not null
   */
  public final Property<IdentifierSearch> regionKeys() {
    return metaBean().regionKeys().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the exchange keys to match, null to not match on exchange keys.
   * This will return holidays where the holiday exchange identifier matches one of the search identifiers.
   * Note that only the {@link IdentifierSearchType#ANY any} search type is applicable.
   * @return the value of the property
   */
  public IdentifierSearch getExchangeKeys() {
    return _exchangeKeys;
  }

  /**
   * Sets the exchange keys to match, null to not match on exchange keys.
   * This will return holidays where the holiday exchange identifier matches one of the search identifiers.
   * Note that only the {@link IdentifierSearchType#ANY any} search type is applicable.
   * @param exchangeKeys  the new value of the property
   */
  public void setExchangeKeys(IdentifierSearch exchangeKeys) {
    this._exchangeKeys = exchangeKeys;
  }

  /**
   * Gets the the {@code exchangeKeys} property.
   * This will return holidays where the holiday exchange identifier matches one of the search identifiers.
   * Note that only the {@link IdentifierSearchType#ANY any} search type is applicable.
   * @return the property, not null
   */
  public final Property<IdentifierSearch> exchangeKeys() {
    return metaBean().exchangeKeys().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code HolidaySearchRequest}.
   */
  public static class Meta extends AbstractSearchRequest.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code holidayIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<UniqueIdentifier>> _holidayIds = DirectMetaProperty.ofReadWrite(this, "holidayIds", (Class) List.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(this, "name", String.class);
    /**
     * The meta-property for the {@code type} property.
     */
    private final MetaProperty<HolidayType> _type = DirectMetaProperty.ofReadWrite(this, "type", HolidayType.class);
    /**
     * The meta-property for the {@code providerKey} property.
     */
    private final MetaProperty<Identifier> _providerKey = DirectMetaProperty.ofReadWrite(this, "providerKey", Identifier.class);
    /**
     * The meta-property for the {@code dateToCheck} property.
     */
    private final MetaProperty<LocalDate> _dateToCheck = DirectMetaProperty.ofReadWrite(this, "dateToCheck", LocalDate.class);
    /**
     * The meta-property for the {@code currency} property.
     */
    private final MetaProperty<Currency> _currency = DirectMetaProperty.ofReadWrite(this, "currency", Currency.class);
    /**
     * The meta-property for the {@code regionKeys} property.
     */
    private final MetaProperty<IdentifierSearch> _regionKeys = DirectMetaProperty.ofReadWrite(this, "regionKeys", IdentifierSearch.class);
    /**
     * The meta-property for the {@code exchangeKeys} property.
     */
    private final MetaProperty<IdentifierSearch> _exchangeKeys = DirectMetaProperty.ofReadWrite(this, "exchangeKeys", IdentifierSearch.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map;

    @SuppressWarnings({"unchecked", "rawtypes" })
    protected Meta() {
      LinkedHashMap temp = new LinkedHashMap(super.metaPropertyMap());
      temp.put("holidayIds", _holidayIds);
      temp.put("name", _name);
      temp.put("type", _type);
      temp.put("providerKey", _providerKey);
      temp.put("dateToCheck", _dateToCheck);
      temp.put("currency", _currency);
      temp.put("regionKeys", _regionKeys);
      temp.put("exchangeKeys", _exchangeKeys);
      _map = Collections.unmodifiableMap(temp);
    }

    @Override
    public HolidaySearchRequest createBean() {
      return new HolidaySearchRequest();
    }

    @Override
    public Class<? extends HolidaySearchRequest> beanType() {
      return HolidaySearchRequest.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code holidayIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<UniqueIdentifier>> holidayIds() {
      return _holidayIds;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code type} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HolidayType> type() {
      return _type;
    }

    /**
     * The meta-property for the {@code providerKey} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Identifier> providerKey() {
      return _providerKey;
    }

    /**
     * The meta-property for the {@code dateToCheck} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> dateToCheck() {
      return _dateToCheck;
    }

    /**
     * The meta-property for the {@code currency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Currency> currency() {
      return _currency;
    }

    /**
     * The meta-property for the {@code regionKeys} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<IdentifierSearch> regionKeys() {
      return _regionKeys;
    }

    /**
     * The meta-property for the {@code exchangeKeys} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<IdentifierSearch> exchangeKeys() {
      return _exchangeKeys;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
