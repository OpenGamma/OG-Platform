/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.LocalDate;

import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.money.Currency;

/**
 * The manageable implementation of a set of holiday dates.
 * <p>
 * This implementation is used by the holiday master to store and manipulate the data.
 */
@PublicSPI
@BeanDefinition
public class ManageableHoliday extends DirectBean implements Holiday, Serializable, MutableUniqueIdentifiable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The unique identifier of the holiday.
   * This must be null when adding to a master and not null when retrieved from a master.
   */
  @PropertyDefinition
  private UniqueId _uniqueId;
  /**
   * The type of the holiday.
   * This field must not be null for the object to be valid.
   */
  @PropertyDefinition
  private HolidayType _type;
  /**
   * The region external identifier bundle, used when this is a holiday of type BANK.
   * This must be null if the type is not BANK.
   */
  @PropertyDefinition
  private ExternalId _regionExternalId;
  /**
   * The exchange external identifier bundle, used when this is a holiday of type SETTLEMENT or TRADING.
   * This must be null if the type is not SETTLEMENT or TRADING.
   */
  @PropertyDefinition
  private ExternalId _exchangeExternalId;
  /**
   * The custom external identifier, used when this is a holiday of type CUSTOM
   * This must be null if the type is not CUSTOM.
   */
  @PropertyDefinition
  private ExternalId _customExternalId;
  /**
   * The currency, used when this is a holiday of type CURRENCY.
   * This must be null if the type is not CURRENCY.
   */
  @PropertyDefinition
  private Currency _currency;
  /**
   * The list of dates that the target (currency/region/exchange) is on holiday, not null.
   */
  @PropertyDefinition
  private final List<LocalDate> _holidayDates = new ArrayList<LocalDate>();

  /**
   * Creates an instance.
   */
  public ManageableHoliday() {
  }

  /**
   * Create an instance from another holiday instance.
   * <p>
   * This copies the specified holiday creating an independent copy.
   * 
   * @param holiday  the holiday to copy, not null
   */
  public ManageableHoliday(final Holiday holiday) {
    ArgumentChecker.notNull(holiday, "holiday");
    setUniqueId(holiday.getUniqueId());
    setType(holiday.getType());
    setRegionExternalId(holiday.getRegionExternalId());
    setExchangeExternalId(holiday.getExchangeExternalId());
    setCustomExternalId(holiday.getCustomExternalId());
    setCurrency(holiday.getCurrency());
    setHolidayDates(holiday.getHolidayDates());
  }

  /**
   * Create a CURRENCY holiday from a collection of holiday dates.
   * <p>
   * The unique identifier is managed separately using {@link #setUniqueId}.
   * 
   * @param currency  the currency of this CURRENCY holiday schedule, not null
   * @param holidaySeries  the dates on which holidays fall, not null
   */
  public ManageableHoliday(Currency currency, Collection<LocalDate> holidaySeries) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(holidaySeries, "holidaySeries");
    setCurrency(currency);
    setType(HolidayType.CURRENCY);
    getHolidayDates().addAll(holidaySeries);
  }

  /**
   * Create a BANK, SETTLEMENT or TRADING holiday from a collection of holiday dates.
   * <p>
   * The unique identifier is managed separately using {@link #setUniqueId}.
   * 
   * @param holidayType  the type of the holiday, not null
   * @param regionOrExchangeId  the identifier for either a region (for a BANK holiday) or an exchange (for a SETTLEMENT or TRADING holiday), not null
   * @param holidaySeries  a collection of dates on which holidays fall, not null
   */
  public ManageableHoliday(HolidayType holidayType, ExternalId regionOrExchangeId, Collection<LocalDate> holidaySeries) {
    ArgumentChecker.notNull(holidayType, "holidayType");
    ArgumentChecker.notNull(regionOrExchangeId, "regionOrExchangeId");
    ArgumentChecker.notNull(holidaySeries, "holidaySeries");
    switch (holidayType) {
      case BANK:
        setRegionExternalId(regionOrExchangeId);
        break;
      case SETTLEMENT:
      case TRADING:
        setExchangeExternalId(regionOrExchangeId);
        break;
      case CURRENCY:
      default:
        throw new IllegalArgumentException("Use the Currency constructor for a currency related Holiday");
    }
    setType(holidayType);
    getHolidayDates().addAll(holidaySeries);
  }

  /**
   * Create a CUSTOM holiday from a collection of holiday dates.
   * <p>
   * The unique identifier is managed separately using {@link #setUniqueId}.
   * 
   * @param customExternalId  the external id of this holiday schedule, not null
   * @param holidaySeries  the dates on which holidays fall, not null
   */
  public ManageableHoliday(final ExternalId customExternalId, final Collection<LocalDate> holidaySeries) {
    ArgumentChecker.notNull(customExternalId, "customExternalId");
    ArgumentChecker.notNull(holidaySeries, "holidaySeries");
    setCustomExternalId(customExternalId);
    setType(HolidayType.CUSTOM);
    getHolidayDates().addAll(holidaySeries);
  }
  
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ManageableHoliday}.
   * @return the meta-bean, not null
   */
  public static ManageableHoliday.Meta meta() {
    return ManageableHoliday.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ManageableHoliday.Meta.INSTANCE);
  }

  @Override
  public ManageableHoliday.Meta metaBean() {
    return ManageableHoliday.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unique identifier of the holiday.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @return the value of the property
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier of the holiday.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @param uniqueId  the new value of the property
   */
  public void setUniqueId(UniqueId uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @return the property, not null
   */
  public final Property<UniqueId> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the type of the holiday.
   * This field must not be null for the object to be valid.
   * @return the value of the property
   */
  public HolidayType getType() {
    return _type;
  }

  /**
   * Sets the type of the holiday.
   * This field must not be null for the object to be valid.
   * @param type  the new value of the property
   */
  public void setType(HolidayType type) {
    this._type = type;
  }

  /**
   * Gets the the {@code type} property.
   * This field must not be null for the object to be valid.
   * @return the property, not null
   */
  public final Property<HolidayType> type() {
    return metaBean().type().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the region external identifier bundle, used when this is a holiday of type BANK.
   * This must be null if the type is not BANK.
   * @return the value of the property
   */
  public ExternalId getRegionExternalId() {
    return _regionExternalId;
  }

  /**
   * Sets the region external identifier bundle, used when this is a holiday of type BANK.
   * This must be null if the type is not BANK.
   * @param regionExternalId  the new value of the property
   */
  public void setRegionExternalId(ExternalId regionExternalId) {
    this._regionExternalId = regionExternalId;
  }

  /**
   * Gets the the {@code regionExternalId} property.
   * This must be null if the type is not BANK.
   * @return the property, not null
   */
  public final Property<ExternalId> regionExternalId() {
    return metaBean().regionExternalId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the exchange external identifier bundle, used when this is a holiday of type SETTLEMENT or TRADING.
   * This must be null if the type is not SETTLEMENT or TRADING.
   * @return the value of the property
   */
  public ExternalId getExchangeExternalId() {
    return _exchangeExternalId;
  }

  /**
   * Sets the exchange external identifier bundle, used when this is a holiday of type SETTLEMENT or TRADING.
   * This must be null if the type is not SETTLEMENT or TRADING.
   * @param exchangeExternalId  the new value of the property
   */
  public void setExchangeExternalId(ExternalId exchangeExternalId) {
    this._exchangeExternalId = exchangeExternalId;
  }

  /**
   * Gets the the {@code exchangeExternalId} property.
   * This must be null if the type is not SETTLEMENT or TRADING.
   * @return the property, not null
   */
  public final Property<ExternalId> exchangeExternalId() {
    return metaBean().exchangeExternalId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the custom external identifier, used when this is a holiday of type CUSTOM
   * This must be null if the type is not CUSTOM.
   * @return the value of the property
   */
  public ExternalId getCustomExternalId() {
    return _customExternalId;
  }

  /**
   * Sets the custom external identifier, used when this is a holiday of type CUSTOM
   * This must be null if the type is not CUSTOM.
   * @param customExternalId  the new value of the property
   */
  public void setCustomExternalId(ExternalId customExternalId) {
    this._customExternalId = customExternalId;
  }

  /**
   * Gets the the {@code customExternalId} property.
   * This must be null if the type is not CUSTOM.
   * @return the property, not null
   */
  public final Property<ExternalId> customExternalId() {
    return metaBean().customExternalId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the currency, used when this is a holiday of type CURRENCY.
   * This must be null if the type is not CURRENCY.
   * @return the value of the property
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Sets the currency, used when this is a holiday of type CURRENCY.
   * This must be null if the type is not CURRENCY.
   * @param currency  the new value of the property
   */
  public void setCurrency(Currency currency) {
    this._currency = currency;
  }

  /**
   * Gets the the {@code currency} property.
   * This must be null if the type is not CURRENCY.
   * @return the property, not null
   */
  public final Property<Currency> currency() {
    return metaBean().currency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the list of dates that the target (currency/region/exchange) is on holiday, not null.
   * @return the value of the property, not null
   */
  public List<LocalDate> getHolidayDates() {
    return _holidayDates;
  }

  /**
   * Sets the list of dates that the target (currency/region/exchange) is on holiday, not null.
   * @param holidayDates  the new value of the property, not null
   */
  public void setHolidayDates(List<LocalDate> holidayDates) {
    JodaBeanUtils.notNull(holidayDates, "holidayDates");
    this._holidayDates.clear();
    this._holidayDates.addAll(holidayDates);
  }

  /**
   * Gets the the {@code holidayDates} property.
   * @return the property, not null
   */
  public final Property<List<LocalDate>> holidayDates() {
    return metaBean().holidayDates().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ManageableHoliday clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ManageableHoliday other = (ManageableHoliday) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(getType(), other.getType()) &&
          JodaBeanUtils.equal(getRegionExternalId(), other.getRegionExternalId()) &&
          JodaBeanUtils.equal(getExchangeExternalId(), other.getExchangeExternalId()) &&
          JodaBeanUtils.equal(getCustomExternalId(), other.getCustomExternalId()) &&
          JodaBeanUtils.equal(getCurrency(), other.getCurrency()) &&
          JodaBeanUtils.equal(getHolidayDates(), other.getHolidayDates());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getType());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRegionExternalId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExchangeExternalId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCustomExternalId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCurrency());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHolidayDates());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(256);
    buf.append("ManageableHoliday{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("uniqueId").append('=').append(JodaBeanUtils.toString(getUniqueId())).append(',').append(' ');
    buf.append("type").append('=').append(JodaBeanUtils.toString(getType())).append(',').append(' ');
    buf.append("regionExternalId").append('=').append(JodaBeanUtils.toString(getRegionExternalId())).append(',').append(' ');
    buf.append("exchangeExternalId").append('=').append(JodaBeanUtils.toString(getExchangeExternalId())).append(',').append(' ');
    buf.append("customExternalId").append('=').append(JodaBeanUtils.toString(getCustomExternalId())).append(',').append(' ');
    buf.append("currency").append('=').append(JodaBeanUtils.toString(getCurrency())).append(',').append(' ');
    buf.append("holidayDates").append('=').append(JodaBeanUtils.toString(getHolidayDates())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ManageableHoliday}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueId> _uniqueId = DirectMetaProperty.ofReadWrite(
        this, "uniqueId", ManageableHoliday.class, UniqueId.class);
    /**
     * The meta-property for the {@code type} property.
     */
    private final MetaProperty<HolidayType> _type = DirectMetaProperty.ofReadWrite(
        this, "type", ManageableHoliday.class, HolidayType.class);
    /**
     * The meta-property for the {@code regionExternalId} property.
     */
    private final MetaProperty<ExternalId> _regionExternalId = DirectMetaProperty.ofReadWrite(
        this, "regionExternalId", ManageableHoliday.class, ExternalId.class);
    /**
     * The meta-property for the {@code exchangeExternalId} property.
     */
    private final MetaProperty<ExternalId> _exchangeExternalId = DirectMetaProperty.ofReadWrite(
        this, "exchangeExternalId", ManageableHoliday.class, ExternalId.class);
    /**
     * The meta-property for the {@code customExternalId} property.
     */
    private final MetaProperty<ExternalId> _customExternalId = DirectMetaProperty.ofReadWrite(
        this, "customExternalId", ManageableHoliday.class, ExternalId.class);
    /**
     * The meta-property for the {@code currency} property.
     */
    private final MetaProperty<Currency> _currency = DirectMetaProperty.ofReadWrite(
        this, "currency", ManageableHoliday.class, Currency.class);
    /**
     * The meta-property for the {@code holidayDates} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<LocalDate>> _holidayDates = DirectMetaProperty.ofReadWrite(
        this, "holidayDates", ManageableHoliday.class, (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "type",
        "regionExternalId",
        "exchangeExternalId",
        "customExternalId",
        "currency",
        "holidayDates");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return _uniqueId;
        case 3575610:  // type
          return _type;
        case -62093222:  // regionExternalId
          return _regionExternalId;
        case 323354825:  // exchangeExternalId
          return _exchangeExternalId;
        case -1550240617:  // customExternalId
          return _customExternalId;
        case 575402001:  // currency
          return _currency;
        case -367347:  // holidayDates
          return _holidayDates;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ManageableHoliday> builder() {
      return new DirectBeanBuilder<ManageableHoliday>(new ManageableHoliday());
    }

    @Override
    public Class<? extends ManageableHoliday> beanType() {
      return ManageableHoliday.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code uniqueId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> uniqueId() {
      return _uniqueId;
    }

    /**
     * The meta-property for the {@code type} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HolidayType> type() {
      return _type;
    }

    /**
     * The meta-property for the {@code regionExternalId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> regionExternalId() {
      return _regionExternalId;
    }

    /**
     * The meta-property for the {@code exchangeExternalId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> exchangeExternalId() {
      return _exchangeExternalId;
    }

    /**
     * The meta-property for the {@code customExternalId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> customExternalId() {
      return _customExternalId;
    }

    /**
     * The meta-property for the {@code currency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Currency> currency() {
      return _currency;
    }

    /**
     * The meta-property for the {@code holidayDates} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<LocalDate>> holidayDates() {
      return _holidayDates;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return ((ManageableHoliday) bean).getUniqueId();
        case 3575610:  // type
          return ((ManageableHoliday) bean).getType();
        case -62093222:  // regionExternalId
          return ((ManageableHoliday) bean).getRegionExternalId();
        case 323354825:  // exchangeExternalId
          return ((ManageableHoliday) bean).getExchangeExternalId();
        case -1550240617:  // customExternalId
          return ((ManageableHoliday) bean).getCustomExternalId();
        case 575402001:  // currency
          return ((ManageableHoliday) bean).getCurrency();
        case -367347:  // holidayDates
          return ((ManageableHoliday) bean).getHolidayDates();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          ((ManageableHoliday) bean).setUniqueId((UniqueId) newValue);
          return;
        case 3575610:  // type
          ((ManageableHoliday) bean).setType((HolidayType) newValue);
          return;
        case -62093222:  // regionExternalId
          ((ManageableHoliday) bean).setRegionExternalId((ExternalId) newValue);
          return;
        case 323354825:  // exchangeExternalId
          ((ManageableHoliday) bean).setExchangeExternalId((ExternalId) newValue);
          return;
        case -1550240617:  // customExternalId
          ((ManageableHoliday) bean).setCustomExternalId((ExternalId) newValue);
          return;
        case 575402001:  // currency
          ((ManageableHoliday) bean).setCurrency((Currency) newValue);
          return;
        case -367347:  // holidayDates
          ((ManageableHoliday) bean).setHolidayDates((List<LocalDate>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ManageableHoliday) bean)._holidayDates, "holidayDates");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
