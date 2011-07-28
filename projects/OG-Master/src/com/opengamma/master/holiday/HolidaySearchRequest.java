/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday;

import java.io.Serializable;
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

import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierSearch;
import com.opengamma.id.IdentifierSearchType;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.RegexUtils;
import com.opengamma.util.money.Currency;

/**
 * Request for searching for holidays.
 * <p>
 * Documents will be returned that match the search criteria.
 * This class provides the ability to page the results and to search
 * as at a specific version and correction instant.
 * See {@link HolidayHistoryRequest} for more details on how history works.
 */
@PublicSPI
@BeanDefinition
public class HolidaySearchRequest extends AbstractSearchRequest implements Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The set of holiday object identifiers, null to not limit by holiday object identifiers.
   * Note that an empty set will return no holidays.
   */
  @PropertyDefinition(set = "manual")
  private List<ObjectIdentifier> _holidayIds;
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
  public void addHolidayId(ObjectIdentifiable holidayId) {
    ArgumentChecker.notNull(holidayId, "holidayId");
    if (_holidayIds == null) {
      _holidayIds = new ArrayList<ObjectIdentifier>();
    }
    _holidayIds.add(holidayId.getObjectId());
  }

  /**
   * Sets the set of holiday object identifiers, null to not limit by holiday object identifiers.
   * Note that an empty set will return no holidays.
   * 
   * @param holidayIds  the new holiday identifiers, null clears the holiday id search
   */
  public void setHolidayIds(Iterable<? extends ObjectIdentifiable> holidayIds) {
    if (holidayIds == null) {
      _holidayIds = null;
    } else {
      _holidayIds = new ArrayList<ObjectIdentifier>();
      for (ObjectIdentifiable holidayId : holidayIds) {
        _holidayIds.add(holidayId.getObjectId());
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

  //-------------------------------------------------------------------------
  @Override
  public boolean matches(final AbstractDocument obj) {
    if (obj instanceof HolidayDocument == false) {
      return false;
    }
    final HolidayDocument document = (HolidayDocument) obj;
    final ManageableHoliday holiday = document.getHoliday();
    if (getHolidayIds() != null && getHolidayIds().contains(document.getObjectId()) == false) {
      return false;
    }
    if (getType() != null && getType().equals(holiday.getType()) == false) {
      return false;
    }
    if (getProviderKey() != null && getProviderKey().equals(document.getProviderKey()) == false) {
      return false;
    }
    if (getDateToCheck() != null && holiday.getHolidayDates().contains(getDateToCheck()) == false) {
      return false;
    }
    if (getCurrency() != null && getCurrency().equals(holiday.getCurrency()) == false) {
      return false;
    }
    if (getRegionKeys() != null && getRegionKeys().matches(holiday.getRegionKey()) == false) {
      return false;
    }
    if (getExchangeKeys() != null && getExchangeKeys().matches(holiday.getExchangeKey()) == false) {
      return false;
    }
    if (getName() != null && RegexUtils.wildcardMatch(getName(), document.getName()) == false) {
      return false;
    }
    return true;
  }

//  public static void main(String[] args) {
//    HolidaySearchRequest a = new HolidaySearchRequest();
//    a.setCurrency(Currency.of("GBP"));
//    a.setDateToCheck(LocalDate.of(2011, 6, 23));
//    a.setExchangeKeys(new IdentifierSearch(Identifier.of("BLOOMBERG_UUID", "hkjhkjk")));
//    a.setName("Foo Bar files");
//    a.setPagingRequest(PagingRequest.ALL);
//    a.setType(HolidayType.CURRENCY);
//
//    HolidaySearchRequest b = new HolidaySearchRequest();
//    b.setCurrency(Currency.of("GBP"));
//    b.setDateToCheck(LocalDate.of(2011, 6, 23));
//    b.setExchangeKeys(new IdentifierSearch(Identifier.of("BLOOMBERG_UUID", "hkjhkjk")));
//    b.setName("Foo Bar files");
//    b.setPagingRequest(PagingRequest.ALL);
//    b.setType(HolidayType.CURRENCY);
//
//    HolidayType[] values = HolidayType.values();
//    for (int j = 0; j < 15; j++) {
//      int total = 0;
//      long start = System.nanoTime();
//      for (int i = 0; i < 100000; i++) {
////        if (a.hashCode() > 0) {
////          total += a.getType().ordinal();
////        }
//        if (a.equals(b)) {
//          total += a.getType().ordinal();
//        }
//        a.setType(values[total % 4]);
//        b.setType(values[total % 4]);
//      }
//      long end = System.nanoTime();
//      System.out.println(total);
//      System.out.println(((end - start) / 1000) + "us");
//      a.setType(HolidayType.CURRENCY);
//      b.setType(HolidayType.CURRENCY);
//    }
//  }

//  @Override
//  public int hashCode() {
//      // override to gain better performance using propertyGet(String)
//      int hash = getClass().hashCode();
//      Set<String> names = propertyNames();
//      for (String name : names) {
//          Object value = propertyGet(name);
//          hash += BeanUtils.hashCode(value);
//      }
//      return hash;
//  }

//  @Override
//  public boolean equals(Object obj) {
//    if (obj == this) {
//      return true;
//    }
//    if (obj != null && obj.getClass() == getClass()) {
//      DirectBean other = (DirectBean) obj;
//      for (MetaProperty<?> mp : other.metaBean().metaPropertyIterable()) {
//        if (Objects.equal(mp.get(this), mp.get(other)) == false) {
//          return false;
//        }
//      }
//      return true;
//    }
//    return false;
//  }
//
//  @Override
//  public boolean equals(Object obj) {
//    if (obj == this) {
//      return true;
//    }
//    if (obj != null && obj.getClass() == getClass()) {
//      HolidaySearchRequest other = (HolidaySearchRequest) obj;
//      for (int i = 0; i < 8; i++) {
//        if (Objects.equal(this.propertyGet(i), other.propertyGet(i)) == false) {
//          return false;
//        }
//      }
//      return true;
//    }
//    return super.equals(obj);
//  }
//
//  protected Object propertyGet(int propertyIndex) {
//    switch (propertyIndex) {
//      case 0: // holidayIds
//        return getHolidayIds();
//      case 1: // name
//        return getName();
//      case 2: // type
//        return getType();
//      case 3: // providerKey
//        return getProviderKey();
//      case 4: // dateToCheck
//        return getDateToCheck();
//      case 5: // currency
//        return getCurrency();
//      case 6: // regionKeys
//        return getRegionKeys();
//      case 7: // exchangeKeys
//        return getExchangeKeys();
//    }
//    return super.propertyGet("");
//  }

//  @Override
//  public boolean equals(Object obj) {
//    // override as currently a performance hotspot
//    if (obj == this) {
//      return true;
//    }
//    if (obj != null && getClass() == obj.getClass()) {
//      HolidaySearchRequest other = (HolidaySearchRequest) obj;
//      return BeanUtils.equal(_holidayIds, other._holidayIds) &&
//          BeanUtils.equal(_name, other._name) &&
//          BeanUtils.equal(_type, other._type) &&
//          BeanUtils.equal(_providerKey, other._providerKey) &&
//          BeanUtils.equal(_dateToCheck, other._dateToCheck) &&
//          BeanUtils.equal(_currency, other._currency) &&
//          BeanUtils.equal(_regionKeys, other._regionKeys) &&
//          BeanUtils.equal(_exchangeKeys, other._exchangeKeys);
//    }
//    return super.equals(obj);
//  }
//
//  @Override
//  public int hashCode() {
//    // override as currently a performance hotspot
//    int hash = 7;
//    hash = hash * 31 + JodaBeanUtils.hashCode(getHolidayIds());
//    hash = hash * 31 + JodaBeanUtils.hashCode(getName());
//    hash = hash * 31 + JodaBeanUtils.hashCode(getType());
//    hash = hash * 31 + JodaBeanUtils.hashCode(getProviderKey());
//    hash = hash * 31 + JodaBeanUtils.hashCode(getDateToCheck());
//    hash = hash * 31 + JodaBeanUtils.hashCode(getCurrency());
//    hash = hash * 31 + JodaBeanUtils.hashCode(getRegionKeys());
//    hash = hash * 31 + JodaBeanUtils.hashCode(getExchangeKeys());
//    return hash;
//  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code HolidaySearchRequest}.
   * @return the meta-bean, not null
   */
  public static HolidaySearchRequest.Meta meta() {
    return HolidaySearchRequest.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(HolidaySearchRequest.Meta.INSTANCE);
  }

  @Override
  public HolidaySearchRequest.Meta metaBean() {
    return HolidaySearchRequest.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
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
    return super.propertyGet(propertyName, quiet);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -1121781952:  // holidayIds
        setHolidayIds((List<ObjectIdentifier>) newValue);
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
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      HolidaySearchRequest other = (HolidaySearchRequest) obj;
      return JodaBeanUtils.equal(getHolidayIds(), other.getHolidayIds()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getType(), other.getType()) &&
          JodaBeanUtils.equal(getProviderKey(), other.getProviderKey()) &&
          JodaBeanUtils.equal(getDateToCheck(), other.getDateToCheck()) &&
          JodaBeanUtils.equal(getCurrency(), other.getCurrency()) &&
          JodaBeanUtils.equal(getRegionKeys(), other.getRegionKeys()) &&
          JodaBeanUtils.equal(getExchangeKeys(), other.getExchangeKeys()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getHolidayIds());
    hash += hash * 31 + JodaBeanUtils.hashCode(getName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getType());
    hash += hash * 31 + JodaBeanUtils.hashCode(getProviderKey());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDateToCheck());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCurrency());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRegionKeys());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExchangeKeys());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of holiday object identifiers, null to not limit by holiday object identifiers.
   * Note that an empty set will return no holidays.
   * @return the value of the property
   */
  public List<ObjectIdentifier> getHolidayIds() {
    return _holidayIds;
  }

  /**
   * Gets the the {@code holidayIds} property.
   * Note that an empty set will return no holidays.
   * @return the property, not null
   */
  public final Property<List<ObjectIdentifier>> holidayIds() {
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
    private final MetaProperty<List<ObjectIdentifier>> _holidayIds = DirectMetaProperty.ofReadWrite(
        this, "holidayIds", HolidaySearchRequest.class, (Class) List.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", HolidaySearchRequest.class, String.class);
    /**
     * The meta-property for the {@code type} property.
     */
    private final MetaProperty<HolidayType> _type = DirectMetaProperty.ofReadWrite(
        this, "type", HolidaySearchRequest.class, HolidayType.class);
    /**
     * The meta-property for the {@code providerKey} property.
     */
    private final MetaProperty<Identifier> _providerKey = DirectMetaProperty.ofReadWrite(
        this, "providerKey", HolidaySearchRequest.class, Identifier.class);
    /**
     * The meta-property for the {@code dateToCheck} property.
     */
    private final MetaProperty<LocalDate> _dateToCheck = DirectMetaProperty.ofReadWrite(
        this, "dateToCheck", HolidaySearchRequest.class, LocalDate.class);
    /**
     * The meta-property for the {@code currency} property.
     */
    private final MetaProperty<Currency> _currency = DirectMetaProperty.ofReadWrite(
        this, "currency", HolidaySearchRequest.class, Currency.class);
    /**
     * The meta-property for the {@code regionKeys} property.
     */
    private final MetaProperty<IdentifierSearch> _regionKeys = DirectMetaProperty.ofReadWrite(
        this, "regionKeys", HolidaySearchRequest.class, IdentifierSearch.class);
    /**
     * The meta-property for the {@code exchangeKeys} property.
     */
    private final MetaProperty<IdentifierSearch> _exchangeKeys = DirectMetaProperty.ofReadWrite(
        this, "exchangeKeys", HolidaySearchRequest.class, IdentifierSearch.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "holidayIds",
        "name",
        "type",
        "providerKey",
        "dateToCheck",
        "currency",
        "regionKeys",
        "exchangeKeys");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1121781952:  // holidayIds
          return _holidayIds;
        case 3373707:  // name
          return _name;
        case 3575610:  // type
          return _type;
        case 2064682670:  // providerKey
          return _providerKey;
        case 14222271:  // dateToCheck
          return _dateToCheck;
        case 575402001:  // currency
          return _currency;
        case -1990775032:  // regionKeys
          return _regionKeys;
        case 1429431991:  // exchangeKeys
          return _exchangeKeys;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends HolidaySearchRequest> builder() {
      return new DirectBeanBuilder<HolidaySearchRequest>(new HolidaySearchRequest());
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
    public final MetaProperty<List<ObjectIdentifier>> holidayIds() {
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
