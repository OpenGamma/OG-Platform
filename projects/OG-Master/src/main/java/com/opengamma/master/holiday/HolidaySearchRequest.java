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

import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
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
  private List<ObjectId> _holidayObjectIds;
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
   * The data provider external identifier to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   */
  @PropertyDefinition
  private ExternalId _providerId;
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
   * The region external identifiers to match, null to not match on region identifiers.
   * This will return holidays where the holiday region identifier matches one of the search identifiers.
   * Note that only the {@link ExternalIdSearchType#ANY any} search type is applicable.
   */
  @PropertyDefinition
  private ExternalIdSearch _regionExternalIdSearch;
  /**
   * The exchange external identifiers to match, null to not match on exchange identifiers.
   * This will return holidays where the holiday exchange identifier matches one of the search identifiers.
   * Note that only the {@link ExternalIdSearchType#ANY any} search type is applicable.
   */
  @PropertyDefinition
  private ExternalIdSearch _exchangeExternalIdSearch;
  /**
   * The custom external identifiers to match, null to not match on custom identifiers.
   * This will return holidays where the holiday exchange identifier matches one of the search identifiers.
   * Note that only the {@link ExternalIdSearchType#ANY any} search type is applicable.
   */
  @PropertyDefinition
  private ExternalIdSearch _customExternalIdSearch;
  /**
   * The sort order to use.
   */
  @PropertyDefinition(validate = "notNull")
  private HolidaySearchSortOrder _sortOrder = HolidaySearchSortOrder.NAME_ASC;

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
   * @param exchangeOrRegionOrCustomKeys  the region, exchange or custom identifiers to search for, not null
   */
  public HolidaySearchRequest(final HolidayType type, final Iterable<ExternalId> exchangeOrRegionOrCustomKeys) {
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(exchangeOrRegionOrCustomKeys, "exchangeOrRegionIds");
    setType(type);
    switch (type) {
      case BANK:
        setRegionExternalIdSearch(ExternalIdSearch.of(exchangeOrRegionOrCustomKeys));
        break;
      case SETTLEMENT:
      case TRADING:
        setExchangeExternalIdSearch(ExternalIdSearch.of(exchangeOrRegionOrCustomKeys));
        break;
      case CUSTOM:
        setCustomExternalIdSearch(ExternalIdSearch.of(exchangeOrRegionOrCustomKeys));
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
  public void addHolidayObjectId(ObjectIdentifiable holidayId) {
    ArgumentChecker.notNull(holidayId, "holidayId");
    if (_holidayObjectIds == null) {
      _holidayObjectIds = new ArrayList<ObjectId>();
    }
    _holidayObjectIds.add(holidayId.getObjectId());
  }

  /**
   * Sets the set of holiday object identifiers, null to not limit by holiday object identifiers.
   * Note that an empty set will return no holidays.
   * 
   * @param holidayIds  the new holiday identifiers, null clears the holiday id search
   */
  public void setHolidayObjectIds(Iterable<? extends ObjectIdentifiable> holidayIds) {
    if (holidayIds == null) {
      _holidayObjectIds = null;
    } else {
      _holidayObjectIds = new ArrayList<ObjectId>();
      for (ObjectIdentifiable holidayId : holidayIds) {
        _holidayObjectIds.add(holidayId.getObjectId());
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
  public void addRegionExternalId(ExternalId regionId) {
    ArgumentChecker.notNull(regionId, "regionId");
    addRegionExternalIds(Arrays.asList(regionId));
  }

  /**
   * Adds a collection of region external identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param regionIds  the region key identifiers to add, not null
   */
  public void addRegionExternalIds(ExternalId... regionIds) {
    ArgumentChecker.notNull(regionIds, "regionIds");
    if (getRegionExternalIdSearch() == null) {
      setRegionExternalIdSearch(ExternalIdSearch.of(regionIds));
    } else {
      setRegionExternalIdSearch(getRegionExternalIdSearch().withExternalIdsAdded(regionIds));
    }
  }

  /**
   * Adds a collection of region external identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param regionIds  the region key identifiers to add, not null
   */
  public void addRegionExternalIds(Iterable<ExternalId> regionIds) {
    ArgumentChecker.notNull(regionIds, "regionIds");
    if (getExchangeExternalIdSearch() == null) {
      setRegionExternalIdSearch(ExternalIdSearch.of(regionIds));
    } else {
      setRegionExternalIdSearch(getRegionExternalIdSearch().withExternalIdsAdded(regionIds));
    }
  }

  /**
   * Sets the search type to use in {@code ExternalIdSearch} for regions.
   * 
   * @param type  the type to set, not null
   */
  public void setRegionExternalIdSearchType(ExternalIdSearchType type) {
    if (getRegionExternalIdSearch() == null) {
      setRegionExternalIdSearch(ExternalIdSearch.of(type));
    } else {
      setRegionExternalIdSearch(getRegionExternalIdSearch().withSearchType(type));
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single exchange external identifier to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param exchangeId  the exchange key identifier to add, not null
   */
  public void addExchangeExternalId(ExternalId exchangeId) {
    ArgumentChecker.notNull(exchangeId, "exchangeId");
    addExchangeExternalIds(Arrays.asList(exchangeId));
  }

  /**
   * Adds a collection of exchange external identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param exchangeIds  the exchange key identifiers to add, not null
   */
  public void addExchangeExternalIds(ExternalId... exchangeIds) {
    ArgumentChecker.notNull(exchangeIds, "exchangeIds");
    if (getExchangeExternalIdSearch() == null) {
      setExchangeExternalIdSearch(ExternalIdSearch.of(exchangeIds));
    } else {
      setExchangeExternalIdSearch(getExchangeExternalIdSearch().withExternalIdsAdded(exchangeIds));
    }
  }

  /**
   * Adds a collection of exchange external identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param exchangeIds  the exchange key identifiers to add, not null
   */
  public void addExchangeExternalIds(Iterable<ExternalId> exchangeIds) {
    ArgumentChecker.notNull(exchangeIds, "exchangeIds");
    if (getExchangeExternalIdSearch() == null) {
      setExchangeExternalIdSearch(ExternalIdSearch.of(exchangeIds));
    } else {
      setExchangeExternalIdSearch(getExchangeExternalIdSearch().withExternalIdsAdded(exchangeIds));
    }
  }

  /**
   * Sets the search type to use in {@code ExternalIdSearch} for exchanges.
   * 
   * @param type  the type to set, not null
   */
  public void setExchangeExternalIdSearchType(ExternalIdSearchType type) {
    if (getExchangeExternalIdSearch() == null) {
      setExchangeExternalIdSearch(ExternalIdSearch.of(type));
    } else {
      setExchangeExternalIdSearch(getExchangeExternalIdSearch().withSearchType(type));
    }
  }
  
  //-------------------------------------------------------------------------
  /**
   * Adds a single custom external identifier to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param customId  the custom key identifier to add, not null
   */
  public void addCustomExternalId(ExternalId customId) {
    ArgumentChecker.notNull(customId, "customId");
    addCustomExternalIds(Arrays.asList(customId));
  }

  /**
   * Adds a collection of custom external identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param customIds  the custom key identifiers to add, not null
   */
  public void addCustomExternalIds(ExternalId... customIds) {
    ArgumentChecker.notNull(customIds, "customIds");
    if (getCustomExternalIdSearch() == null) {
      setCustomExternalIdSearch(ExternalIdSearch.of(customIds));
    } else {
      setCustomExternalIdSearch(getCustomExternalIdSearch().withExternalIdsAdded(customIds));
    }
  }

  /**
   * Adds a collection of custom external identifiers to the collection to search for.
   * Unless customized, the search will match 
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   * 
   * @param customIds  the exchange key identifiers to add, not null
   */
  public void addCustomExternalIds(Iterable<ExternalId> customIds) {
    ArgumentChecker.notNull(customIds, "customIds");
    if (getCustomExternalIdSearch() == null) {
      setCustomExternalIdSearch(ExternalIdSearch.of(customIds));
    } else {
      setCustomExternalIdSearch(getCustomExternalIdSearch().withExternalIdsAdded(customIds));
    }
  }

  /**
   * Sets the search type to use in {@code ExternalIdSearch} for custom ids.
   * 
   * @param type  the type to set, not null
   */
  public void setCustomExternalIdSearchType(ExternalIdSearchType type) {
    if (getCustomExternalIdSearch() == null) {
      setCustomExternalIdSearch(ExternalIdSearch.of(type));
    } else {
      setCustomExternalIdSearch(getCustomExternalIdSearch().withSearchType(type));
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
    if (getHolidayObjectIds() != null && getHolidayObjectIds().contains(document.getObjectId()) == false) {
      return false;
    }
    if (getType() != null && getType().equals(holiday.getType()) == false) {
      return false;
    }
    if (getProviderId() != null && getProviderId().equals(document.getProviderId()) == false) {
      return false;
    }
    if (getDateToCheck() != null && holiday.getHolidayDates().contains(getDateToCheck()) == false) {
      return false;
    }
    if (getCurrency() != null && getCurrency().equals(holiday.getCurrency()) == false) {
      return false;
    }
    if (getRegionExternalIdSearch() != null && getRegionExternalIdSearch().matches(holiday.getRegionExternalId()) == false) {
      return false;
    }
    if (getExchangeExternalIdSearch() != null && getExchangeExternalIdSearch().matches(holiday.getExchangeExternalId()) == false) {
      return false;
    }
    if (getCustomExternalIdSearch() != null && getCustomExternalIdSearch().matches(holiday.getCustomExternalId()) == false) {
      return false;
    }
    if (getName() != null && RegexUtils.wildcardMatch(getName(), document.getName()) == false) {
      return false;
    }
    return true;
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

  static {
    JodaBeanUtils.registerMetaBean(HolidaySearchRequest.Meta.INSTANCE);
  }

  @Override
  public HolidaySearchRequest.Meta metaBean() {
    return HolidaySearchRequest.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of holiday object identifiers, null to not limit by holiday object identifiers.
   * Note that an empty set will return no holidays.
   * @return the value of the property
   */
  public List<ObjectId> getHolidayObjectIds() {
    return _holidayObjectIds;
  }

  /**
   * Gets the the {@code holidayObjectIds} property.
   * Note that an empty set will return no holidays.
   * @return the property, not null
   */
  public final Property<List<ObjectId>> holidayObjectIds() {
    return metaBean().holidayObjectIds().createProperty(this);
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
   * Gets the data provider external identifier to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   * @return the value of the property
   */
  public ExternalId getProviderId() {
    return _providerId;
  }

  /**
   * Sets the data provider external identifier to match, null to not match on provider.
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
   * Gets the region external identifiers to match, null to not match on region identifiers.
   * This will return holidays where the holiday region identifier matches one of the search identifiers.
   * Note that only the {@link ExternalIdSearchType#ANY any} search type is applicable.
   * @return the value of the property
   */
  public ExternalIdSearch getRegionExternalIdSearch() {
    return _regionExternalIdSearch;
  }

  /**
   * Sets the region external identifiers to match, null to not match on region identifiers.
   * This will return holidays where the holiday region identifier matches one of the search identifiers.
   * Note that only the {@link ExternalIdSearchType#ANY any} search type is applicable.
   * @param regionExternalIdSearch  the new value of the property
   */
  public void setRegionExternalIdSearch(ExternalIdSearch regionExternalIdSearch) {
    this._regionExternalIdSearch = regionExternalIdSearch;
  }

  /**
   * Gets the the {@code regionExternalIdSearch} property.
   * This will return holidays where the holiday region identifier matches one of the search identifiers.
   * Note that only the {@link ExternalIdSearchType#ANY any} search type is applicable.
   * @return the property, not null
   */
  public final Property<ExternalIdSearch> regionExternalIdSearch() {
    return metaBean().regionExternalIdSearch().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the exchange external identifiers to match, null to not match on exchange identifiers.
   * This will return holidays where the holiday exchange identifier matches one of the search identifiers.
   * Note that only the {@link ExternalIdSearchType#ANY any} search type is applicable.
   * @return the value of the property
   */
  public ExternalIdSearch getExchangeExternalIdSearch() {
    return _exchangeExternalIdSearch;
  }

  /**
   * Sets the exchange external identifiers to match, null to not match on exchange identifiers.
   * This will return holidays where the holiday exchange identifier matches one of the search identifiers.
   * Note that only the {@link ExternalIdSearchType#ANY any} search type is applicable.
   * @param exchangeExternalIdSearch  the new value of the property
   */
  public void setExchangeExternalIdSearch(ExternalIdSearch exchangeExternalIdSearch) {
    this._exchangeExternalIdSearch = exchangeExternalIdSearch;
  }

  /**
   * Gets the the {@code exchangeExternalIdSearch} property.
   * This will return holidays where the holiday exchange identifier matches one of the search identifiers.
   * Note that only the {@link ExternalIdSearchType#ANY any} search type is applicable.
   * @return the property, not null
   */
  public final Property<ExternalIdSearch> exchangeExternalIdSearch() {
    return metaBean().exchangeExternalIdSearch().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the custom external identifiers to match, null to not match on custom identifiers.
   * This will return holidays where the holiday exchange identifier matches one of the search identifiers.
   * Note that only the {@link ExternalIdSearchType#ANY any} search type is applicable.
   * @return the value of the property
   */
  public ExternalIdSearch getCustomExternalIdSearch() {
    return _customExternalIdSearch;
  }

  /**
   * Sets the custom external identifiers to match, null to not match on custom identifiers.
   * This will return holidays where the holiday exchange identifier matches one of the search identifiers.
   * Note that only the {@link ExternalIdSearchType#ANY any} search type is applicable.
   * @param customExternalIdSearch  the new value of the property
   */
  public void setCustomExternalIdSearch(ExternalIdSearch customExternalIdSearch) {
    this._customExternalIdSearch = customExternalIdSearch;
  }

  /**
   * Gets the the {@code customExternalIdSearch} property.
   * This will return holidays where the holiday exchange identifier matches one of the search identifiers.
   * Note that only the {@link ExternalIdSearchType#ANY any} search type is applicable.
   * @return the property, not null
   */
  public final Property<ExternalIdSearch> customExternalIdSearch() {
    return metaBean().customExternalIdSearch().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the sort order to use.
   * @return the value of the property, not null
   */
  public HolidaySearchSortOrder getSortOrder() {
    return _sortOrder;
  }

  /**
   * Sets the sort order to use.
   * @param sortOrder  the new value of the property, not null
   */
  public void setSortOrder(HolidaySearchSortOrder sortOrder) {
    JodaBeanUtils.notNull(sortOrder, "sortOrder");
    this._sortOrder = sortOrder;
  }

  /**
   * Gets the the {@code sortOrder} property.
   * @return the property, not null
   */
  public final Property<HolidaySearchSortOrder> sortOrder() {
    return metaBean().sortOrder().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public HolidaySearchRequest clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      HolidaySearchRequest other = (HolidaySearchRequest) obj;
      return JodaBeanUtils.equal(getHolidayObjectIds(), other.getHolidayObjectIds()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getType(), other.getType()) &&
          JodaBeanUtils.equal(getProviderId(), other.getProviderId()) &&
          JodaBeanUtils.equal(getDateToCheck(), other.getDateToCheck()) &&
          JodaBeanUtils.equal(getCurrency(), other.getCurrency()) &&
          JodaBeanUtils.equal(getRegionExternalIdSearch(), other.getRegionExternalIdSearch()) &&
          JodaBeanUtils.equal(getExchangeExternalIdSearch(), other.getExchangeExternalIdSearch()) &&
          JodaBeanUtils.equal(getCustomExternalIdSearch(), other.getCustomExternalIdSearch()) &&
          JodaBeanUtils.equal(getSortOrder(), other.getSortOrder()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getHolidayObjectIds());
    hash += hash * 31 + JodaBeanUtils.hashCode(getName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getType());
    hash += hash * 31 + JodaBeanUtils.hashCode(getProviderId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDateToCheck());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCurrency());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRegionExternalIdSearch());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExchangeExternalIdSearch());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCustomExternalIdSearch());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSortOrder());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(352);
    buf.append("HolidaySearchRequest{");
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
    buf.append("holidayObjectIds").append('=').append(JodaBeanUtils.toString(getHolidayObjectIds())).append(',').append(' ');
    buf.append("name").append('=').append(JodaBeanUtils.toString(getName())).append(',').append(' ');
    buf.append("type").append('=').append(JodaBeanUtils.toString(getType())).append(',').append(' ');
    buf.append("providerId").append('=').append(JodaBeanUtils.toString(getProviderId())).append(',').append(' ');
    buf.append("dateToCheck").append('=').append(JodaBeanUtils.toString(getDateToCheck())).append(',').append(' ');
    buf.append("currency").append('=').append(JodaBeanUtils.toString(getCurrency())).append(',').append(' ');
    buf.append("regionExternalIdSearch").append('=').append(JodaBeanUtils.toString(getRegionExternalIdSearch())).append(',').append(' ');
    buf.append("exchangeExternalIdSearch").append('=').append(JodaBeanUtils.toString(getExchangeExternalIdSearch())).append(',').append(' ');
    buf.append("customExternalIdSearch").append('=').append(JodaBeanUtils.toString(getCustomExternalIdSearch())).append(',').append(' ');
    buf.append("sortOrder").append('=').append(JodaBeanUtils.toString(getSortOrder())).append(',').append(' ');
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
     * The meta-property for the {@code holidayObjectIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ObjectId>> _holidayObjectIds = DirectMetaProperty.ofReadWrite(
        this, "holidayObjectIds", HolidaySearchRequest.class, (Class) List.class);
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
     * The meta-property for the {@code providerId} property.
     */
    private final MetaProperty<ExternalId> _providerId = DirectMetaProperty.ofReadWrite(
        this, "providerId", HolidaySearchRequest.class, ExternalId.class);
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
     * The meta-property for the {@code regionExternalIdSearch} property.
     */
    private final MetaProperty<ExternalIdSearch> _regionExternalIdSearch = DirectMetaProperty.ofReadWrite(
        this, "regionExternalIdSearch", HolidaySearchRequest.class, ExternalIdSearch.class);
    /**
     * The meta-property for the {@code exchangeExternalIdSearch} property.
     */
    private final MetaProperty<ExternalIdSearch> _exchangeExternalIdSearch = DirectMetaProperty.ofReadWrite(
        this, "exchangeExternalIdSearch", HolidaySearchRequest.class, ExternalIdSearch.class);
    /**
     * The meta-property for the {@code customExternalIdSearch} property.
     */
    private final MetaProperty<ExternalIdSearch> _customExternalIdSearch = DirectMetaProperty.ofReadWrite(
        this, "customExternalIdSearch", HolidaySearchRequest.class, ExternalIdSearch.class);
    /**
     * The meta-property for the {@code sortOrder} property.
     */
    private final MetaProperty<HolidaySearchSortOrder> _sortOrder = DirectMetaProperty.ofReadWrite(
        this, "sortOrder", HolidaySearchRequest.class, HolidaySearchSortOrder.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "holidayObjectIds",
        "name",
        "type",
        "providerId",
        "dateToCheck",
        "currency",
        "regionExternalIdSearch",
        "exchangeExternalIdSearch",
        "customExternalIdSearch",
        "sortOrder");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1205921407:  // holidayObjectIds
          return _holidayObjectIds;
        case 3373707:  // name
          return _name;
        case 3575610:  // type
          return _type;
        case 205149932:  // providerId
          return _providerId;
        case 14222271:  // dateToCheck
          return _dateToCheck;
        case 575402001:  // currency
          return _currency;
        case 253144738:  // regionExternalIdSearch
          return _regionExternalIdSearch;
        case 585750481:  // exchangeExternalIdSearch
          return _exchangeExternalIdSearch;
        case 1477343519:  // customExternalIdSearch
          return _customExternalIdSearch;
        case -26774448:  // sortOrder
          return _sortOrder;
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
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code holidayObjectIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ObjectId>> holidayObjectIds() {
      return _holidayObjectIds;
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
     * The meta-property for the {@code providerId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> providerId() {
      return _providerId;
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
     * The meta-property for the {@code regionExternalIdSearch} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalIdSearch> regionExternalIdSearch() {
      return _regionExternalIdSearch;
    }

    /**
     * The meta-property for the {@code exchangeExternalIdSearch} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalIdSearch> exchangeExternalIdSearch() {
      return _exchangeExternalIdSearch;
    }

    /**
     * The meta-property for the {@code customExternalIdSearch} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalIdSearch> customExternalIdSearch() {
      return _customExternalIdSearch;
    }

    /**
     * The meta-property for the {@code sortOrder} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HolidaySearchSortOrder> sortOrder() {
      return _sortOrder;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1205921407:  // holidayObjectIds
          return ((HolidaySearchRequest) bean).getHolidayObjectIds();
        case 3373707:  // name
          return ((HolidaySearchRequest) bean).getName();
        case 3575610:  // type
          return ((HolidaySearchRequest) bean).getType();
        case 205149932:  // providerId
          return ((HolidaySearchRequest) bean).getProviderId();
        case 14222271:  // dateToCheck
          return ((HolidaySearchRequest) bean).getDateToCheck();
        case 575402001:  // currency
          return ((HolidaySearchRequest) bean).getCurrency();
        case 253144738:  // regionExternalIdSearch
          return ((HolidaySearchRequest) bean).getRegionExternalIdSearch();
        case 585750481:  // exchangeExternalIdSearch
          return ((HolidaySearchRequest) bean).getExchangeExternalIdSearch();
        case 1477343519:  // customExternalIdSearch
          return ((HolidaySearchRequest) bean).getCustomExternalIdSearch();
        case -26774448:  // sortOrder
          return ((HolidaySearchRequest) bean).getSortOrder();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1205921407:  // holidayObjectIds
          ((HolidaySearchRequest) bean).setHolidayObjectIds((List<ObjectId>) newValue);
          return;
        case 3373707:  // name
          ((HolidaySearchRequest) bean).setName((String) newValue);
          return;
        case 3575610:  // type
          ((HolidaySearchRequest) bean).setType((HolidayType) newValue);
          return;
        case 205149932:  // providerId
          ((HolidaySearchRequest) bean).setProviderId((ExternalId) newValue);
          return;
        case 14222271:  // dateToCheck
          ((HolidaySearchRequest) bean).setDateToCheck((LocalDate) newValue);
          return;
        case 575402001:  // currency
          ((HolidaySearchRequest) bean).setCurrency((Currency) newValue);
          return;
        case 253144738:  // regionExternalIdSearch
          ((HolidaySearchRequest) bean).setRegionExternalIdSearch((ExternalIdSearch) newValue);
          return;
        case 585750481:  // exchangeExternalIdSearch
          ((HolidaySearchRequest) bean).setExchangeExternalIdSearch((ExternalIdSearch) newValue);
          return;
        case 1477343519:  // customExternalIdSearch
          ((HolidaySearchRequest) bean).setCustomExternalIdSearch((ExternalIdSearch) newValue);
          return;
        case -26774448:  // sortOrder
          ((HolidaySearchRequest) bean).setSortOrder((HolidaySearchSortOrder) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((HolidaySearchRequest) bean)._sortOrder, "sortOrder");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
