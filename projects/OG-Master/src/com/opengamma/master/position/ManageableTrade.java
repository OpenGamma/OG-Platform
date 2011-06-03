/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position;

import java.math.BigDecimal;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.BasicBeanBuilder;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.core.position.Trade;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.money.Currency;

/**
 * A trade forming part of a position.
 * <p>
 * A position is formed of one or more trades.
 * For example a trade of 200 shares of OpenGamma might be combined with another trade
 * of 450 shares of OpenGamma to create a combined position of 650 shares.
 */
@PublicSPI
@BeanDefinition
public class ManageableTrade extends DirectBean implements MutableUniqueIdentifiable {

  /**
   * The trade unique identifier.
   * This field should be null until added to the master.
   */
  @PropertyDefinition
  private UniqueIdentifier _uniqueId;
  /**
   * The parent position unique identifier.
   * This field is managed by the master.
   */
  @PropertyDefinition
  private UniqueIdentifier _positionId;
  /**
   * The quantity.
   * This field must not be null for the object to be valid.
   */
  @PropertyDefinition
  private BigDecimal _quantity;
  /**
   * The identifiers specifying the security.
   * This field must not be null for the object to be valid.
   */
  @PropertyDefinition
  private IdentifierBundle _securityKey;
  /**
   * The trade date.
   * This field must not be null for the object to be valid.
   */
  @PropertyDefinition
  private LocalDate _tradeDate;
  /**
   * The trade time with offset, null if not known.
   */
  @PropertyDefinition
  private OffsetTime _tradeTime;
  /**
   * The counterparty key identifier, null if not known.
   */
  @PropertyDefinition
  private Identifier _counterpartyKey;
  /**
   * The provider key identifier for the data.
   * This optional field can be used to capture the identifier used by the data provider.
   * This can be useful when receiving updates from the same provider.
   */
  @PropertyDefinition
  private Identifier _providerKey;
  /**
   * Amount paid for trade at time of purchase
   */
  @PropertyDefinition
  private Double _premium;
  /**
   * Currency of payment at time of purchase
   */
  @PropertyDefinition
  private Currency _premiumCurrency;
  /**
   * Date of premium payment
   */
  @PropertyDefinition
  private LocalDate _premiumDate;
  /**
   * Time of premium payment
   */
  @PropertyDefinition
  private OffsetTime _premiumTime;

  /**
   * Creates an instance.
   */
  public ManageableTrade() {
  }

  /**
   * Creates an instance, copying the values from another {@link Trade} object.
   * 
   * @param trade the object to copy values from
   */
  public ManageableTrade(final Trade trade) {
    ArgumentChecker.notNull(trade, "trade");
    setUniqueId(trade.getUniqueId());
    setPositionId(trade.getParentPositionId());
    setQuantity(trade.getQuantity());
    setSecurityKey(trade.getSecurityKey());
    setTradeDate(trade.getTradeDate());
    setTradeTime(trade.getTradeTime());
    setCounterpartyKey(trade.getCounterparty().getIdentifier());
    setPremium(trade.getPremium());
    setPremiumCurrency(trade.getPremiumCurrency());
    setPremiumDate(trade.getPremiumDate());
    setPremiumTime(trade.getPremiumTime());
  }

  /**
   * Creates a trade from trade quantity, instant and counterparty identifier.
   * 
   * @param quantity  the amount of the trade, not null
   * @param securityKey  the security identifier, not null
   * @param tradeDate  the trade date, not null
   * @param tradeTime the trade time with timezone, may be null
   * @param counterpartyId the counterparty identifier, not null
   */
  public ManageableTrade(final BigDecimal quantity, final Identifier securityKey, final LocalDate tradeDate, final OffsetTime tradeTime, final Identifier counterpartyId) {
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    ArgumentChecker.notNull(counterpartyId, "counterpartyId");
    ArgumentChecker.notNull(securityKey, "securityKey");
    _quantity = quantity;
    _tradeDate = tradeDate;
    _tradeTime = tradeTime;
    _counterpartyKey = counterpartyId;
    _securityKey = IdentifierBundle.of(securityKey);
  }

  /**
   * Creates a trade from trade quantity, instant and counterparty identifier.
   * 
   * @param quantity  the amount of the trade, not null
   * @param securityKey  the security identifier, not null
   * @param tradeDate  the trade date, not null
   * @param tradeTime the trade time with timezone, may be null
   * @param counterpartyId the counterparty identifier, not null
   */
  public ManageableTrade(final BigDecimal quantity, final IdentifierBundle securityKey, final LocalDate tradeDate, final OffsetTime tradeTime, final Identifier counterpartyId) {
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(securityKey, "securityKey");
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    ArgumentChecker.notNull(counterpartyId, "counterpartyId");
    _quantity = quantity;
    _tradeDate = tradeDate;
    _tradeTime = tradeTime;
    _counterpartyKey = counterpartyId;
    _securityKey = securityKey;
  }

  //-------------------------------------------------------------------------
  /**
   * Adds an identifier to the security key.
   * @param securityKeyIdentifier  the identifier to add, not null
   */
  public void addSecurityKey(final Identifier securityKeyIdentifier) {
    ArgumentChecker.notNull(securityKeyIdentifier, "securityKeyIdentifier");
    setSecurityKey(getSecurityKey().withIdentifier(securityKeyIdentifier));
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ManageableTrade}.
   * @return the meta-bean, not null
   */
  public static ManageableTrade.Meta meta() {
    return ManageableTrade.Meta.INSTANCE;
  }

  @Override
  public ManageableTrade.Meta metaBean() {
    return ManageableTrade.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        return getUniqueId();
      case 1381039140:  // positionId
        return getPositionId();
      case -1285004149:  // quantity
        return getQuantity();
      case 1550083839:  // securityKey
        return getSecurityKey();
      case 752419634:  // tradeDate
        return getTradeDate();
      case 752903761:  // tradeTime
        return getTradeTime();
      case 624096149:  // counterpartyKey
        return getCounterpartyKey();
      case 2064682670:  // providerKey
        return getProviderKey();
      case -318452137:  // premium
        return getPremium();
      case 1136581512:  // premiumCurrency
        return getPremiumCurrency();
      case 651701925:  // premiumDate
        return getPremiumDate();
      case 652186052:  // premiumTime
        return getPremiumTime();
    }
    return super.propertyGet(propertyName);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        setUniqueId((UniqueIdentifier) newValue);
        return;
      case 1381039140:  // positionId
        setPositionId((UniqueIdentifier) newValue);
        return;
      case -1285004149:  // quantity
        setQuantity((BigDecimal) newValue);
        return;
      case 1550083839:  // securityKey
        setSecurityKey((IdentifierBundle) newValue);
        return;
      case 752419634:  // tradeDate
        setTradeDate((LocalDate) newValue);
        return;
      case 752903761:  // tradeTime
        setTradeTime((OffsetTime) newValue);
        return;
      case 624096149:  // counterpartyKey
        setCounterpartyKey((Identifier) newValue);
        return;
      case 2064682670:  // providerKey
        setProviderKey((Identifier) newValue);
        return;
      case -318452137:  // premium
        setPremium((Double) newValue);
        return;
      case 1136581512:  // premiumCurrency
        setPremiumCurrency((Currency) newValue);
        return;
      case 651701925:  // premiumDate
        setPremiumDate((LocalDate) newValue);
        return;
      case 652186052:  // premiumTime
        setPremiumTime((OffsetTime) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ManageableTrade other = (ManageableTrade) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(getPositionId(), other.getPositionId()) &&
          JodaBeanUtils.equal(getQuantity(), other.getQuantity()) &&
          JodaBeanUtils.equal(getSecurityKey(), other.getSecurityKey()) &&
          JodaBeanUtils.equal(getTradeDate(), other.getTradeDate()) &&
          JodaBeanUtils.equal(getTradeTime(), other.getTradeTime()) &&
          JodaBeanUtils.equal(getCounterpartyKey(), other.getCounterpartyKey()) &&
          JodaBeanUtils.equal(getProviderKey(), other.getProviderKey()) &&
          JodaBeanUtils.equal(getPremium(), other.getPremium()) &&
          JodaBeanUtils.equal(getPremiumCurrency(), other.getPremiumCurrency()) &&
          JodaBeanUtils.equal(getPremiumDate(), other.getPremiumDate()) &&
          JodaBeanUtils.equal(getPremiumTime(), other.getPremiumTime());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPositionId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getQuantity());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecurityKey());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTradeDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTradeTime());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCounterpartyKey());
    hash += hash * 31 + JodaBeanUtils.hashCode(getProviderKey());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPremium());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPremiumCurrency());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPremiumDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPremiumTime());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the trade unique identifier.
   * This field should be null until added to the master.
   * @return the value of the property
   */
  public UniqueIdentifier getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the trade unique identifier.
   * This field should be null until added to the master.
   * @param uniqueId  the new value of the property
   */
  public void setUniqueId(UniqueIdentifier uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * This field should be null until added to the master.
   * @return the property, not null
   */
  public final Property<UniqueIdentifier> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the parent position unique identifier.
   * This field is managed by the master.
   * @return the value of the property
   */
  public UniqueIdentifier getPositionId() {
    return _positionId;
  }

  /**
   * Sets the parent position unique identifier.
   * This field is managed by the master.
   * @param positionId  the new value of the property
   */
  public void setPositionId(UniqueIdentifier positionId) {
    this._positionId = positionId;
  }

  /**
   * Gets the the {@code positionId} property.
   * This field is managed by the master.
   * @return the property, not null
   */
  public final Property<UniqueIdentifier> positionId() {
    return metaBean().positionId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the quantity.
   * This field must not be null for the object to be valid.
   * @return the value of the property
   */
  public BigDecimal getQuantity() {
    return _quantity;
  }

  /**
   * Sets the quantity.
   * This field must not be null for the object to be valid.
   * @param quantity  the new value of the property
   */
  public void setQuantity(BigDecimal quantity) {
    this._quantity = quantity;
  }

  /**
   * Gets the the {@code quantity} property.
   * This field must not be null for the object to be valid.
   * @return the property, not null
   */
  public final Property<BigDecimal> quantity() {
    return metaBean().quantity().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the identifiers specifying the security.
   * This field must not be null for the object to be valid.
   * @return the value of the property
   */
  public IdentifierBundle getSecurityKey() {
    return _securityKey;
  }

  /**
   * Sets the identifiers specifying the security.
   * This field must not be null for the object to be valid.
   * @param securityKey  the new value of the property
   */
  public void setSecurityKey(IdentifierBundle securityKey) {
    this._securityKey = securityKey;
  }

  /**
   * Gets the the {@code securityKey} property.
   * This field must not be null for the object to be valid.
   * @return the property, not null
   */
  public final Property<IdentifierBundle> securityKey() {
    return metaBean().securityKey().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the trade date.
   * This field must not be null for the object to be valid.
   * @return the value of the property
   */
  public LocalDate getTradeDate() {
    return _tradeDate;
  }

  /**
   * Sets the trade date.
   * This field must not be null for the object to be valid.
   * @param tradeDate  the new value of the property
   */
  public void setTradeDate(LocalDate tradeDate) {
    this._tradeDate = tradeDate;
  }

  /**
   * Gets the the {@code tradeDate} property.
   * This field must not be null for the object to be valid.
   * @return the property, not null
   */
  public final Property<LocalDate> tradeDate() {
    return metaBean().tradeDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the trade time with offset, null if not known.
   * @return the value of the property
   */
  public OffsetTime getTradeTime() {
    return _tradeTime;
  }

  /**
   * Sets the trade time with offset, null if not known.
   * @param tradeTime  the new value of the property
   */
  public void setTradeTime(OffsetTime tradeTime) {
    this._tradeTime = tradeTime;
  }

  /**
   * Gets the the {@code tradeTime} property.
   * @return the property, not null
   */
  public final Property<OffsetTime> tradeTime() {
    return metaBean().tradeTime().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the counterparty key identifier, null if not known.
   * @return the value of the property
   */
  public Identifier getCounterpartyKey() {
    return _counterpartyKey;
  }

  /**
   * Sets the counterparty key identifier, null if not known.
   * @param counterpartyKey  the new value of the property
   */
  public void setCounterpartyKey(Identifier counterpartyKey) {
    this._counterpartyKey = counterpartyKey;
  }

  /**
   * Gets the the {@code counterpartyKey} property.
   * @return the property, not null
   */
  public final Property<Identifier> counterpartyKey() {
    return metaBean().counterpartyKey().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the provider key identifier for the data.
   * This optional field can be used to capture the identifier used by the data provider.
   * This can be useful when receiving updates from the same provider.
   * @return the value of the property
   */
  public Identifier getProviderKey() {
    return _providerKey;
  }

  /**
   * Sets the provider key identifier for the data.
   * This optional field can be used to capture the identifier used by the data provider.
   * This can be useful when receiving updates from the same provider.
   * @param providerKey  the new value of the property
   */
  public void setProviderKey(Identifier providerKey) {
    this._providerKey = providerKey;
  }

  /**
   * Gets the the {@code providerKey} property.
   * This optional field can be used to capture the identifier used by the data provider.
   * This can be useful when receiving updates from the same provider.
   * @return the property, not null
   */
  public final Property<Identifier> providerKey() {
    return metaBean().providerKey().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets amount paid for trade at time of purchase
   * @return the value of the property
   */
  public Double getPremium() {
    return _premium;
  }

  /**
   * Sets amount paid for trade at time of purchase
   * @param premium  the new value of the property
   */
  public void setPremium(Double premium) {
    this._premium = premium;
  }

  /**
   * Gets the the {@code premium} property.
   * @return the property, not null
   */
  public final Property<Double> premium() {
    return metaBean().premium().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets currency of payment at time of purchase
   * @return the value of the property
   */
  public Currency getPremiumCurrency() {
    return _premiumCurrency;
  }

  /**
   * Sets currency of payment at time of purchase
   * @param premiumCurrency  the new value of the property
   */
  public void setPremiumCurrency(Currency premiumCurrency) {
    this._premiumCurrency = premiumCurrency;
  }

  /**
   * Gets the the {@code premiumCurrency} property.
   * @return the property, not null
   */
  public final Property<Currency> premiumCurrency() {
    return metaBean().premiumCurrency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets date of premium payment
   * @return the value of the property
   */
  public LocalDate getPremiumDate() {
    return _premiumDate;
  }

  /**
   * Sets date of premium payment
   * @param premiumDate  the new value of the property
   */
  public void setPremiumDate(LocalDate premiumDate) {
    this._premiumDate = premiumDate;
  }

  /**
   * Gets the the {@code premiumDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> premiumDate() {
    return metaBean().premiumDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets time of premium payment
   * @return the value of the property
   */
  public OffsetTime getPremiumTime() {
    return _premiumTime;
  }

  /**
   * Sets time of premium payment
   * @param premiumTime  the new value of the property
   */
  public void setPremiumTime(OffsetTime premiumTime) {
    this._premiumTime = premiumTime;
  }

  /**
   * Gets the the {@code premiumTime} property.
   * @return the property, not null
   */
  public final Property<OffsetTime> premiumTime() {
    return metaBean().premiumTime().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ManageableTrade}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueIdentifier> _uniqueId = DirectMetaProperty.ofReadWrite(
        this, "uniqueId", ManageableTrade.class, UniqueIdentifier.class);
    /**
     * The meta-property for the {@code positionId} property.
     */
    private final MetaProperty<UniqueIdentifier> _positionId = DirectMetaProperty.ofReadWrite(
        this, "positionId", ManageableTrade.class, UniqueIdentifier.class);
    /**
     * The meta-property for the {@code quantity} property.
     */
    private final MetaProperty<BigDecimal> _quantity = DirectMetaProperty.ofReadWrite(
        this, "quantity", ManageableTrade.class, BigDecimal.class);
    /**
     * The meta-property for the {@code securityKey} property.
     */
    private final MetaProperty<IdentifierBundle> _securityKey = DirectMetaProperty.ofReadWrite(
        this, "securityKey", ManageableTrade.class, IdentifierBundle.class);
    /**
     * The meta-property for the {@code tradeDate} property.
     */
    private final MetaProperty<LocalDate> _tradeDate = DirectMetaProperty.ofReadWrite(
        this, "tradeDate", ManageableTrade.class, LocalDate.class);
    /**
     * The meta-property for the {@code tradeTime} property.
     */
    private final MetaProperty<OffsetTime> _tradeTime = DirectMetaProperty.ofReadWrite(
        this, "tradeTime", ManageableTrade.class, OffsetTime.class);
    /**
     * The meta-property for the {@code counterpartyKey} property.
     */
    private final MetaProperty<Identifier> _counterpartyKey = DirectMetaProperty.ofReadWrite(
        this, "counterpartyKey", ManageableTrade.class, Identifier.class);
    /**
     * The meta-property for the {@code providerKey} property.
     */
    private final MetaProperty<Identifier> _providerKey = DirectMetaProperty.ofReadWrite(
        this, "providerKey", ManageableTrade.class, Identifier.class);
    /**
     * The meta-property for the {@code premium} property.
     */
    private final MetaProperty<Double> _premium = DirectMetaProperty.ofReadWrite(
        this, "premium", ManageableTrade.class, Double.class);
    /**
     * The meta-property for the {@code premiumCurrency} property.
     */
    private final MetaProperty<Currency> _premiumCurrency = DirectMetaProperty.ofReadWrite(
        this, "premiumCurrency", ManageableTrade.class, Currency.class);
    /**
     * The meta-property for the {@code premiumDate} property.
     */
    private final MetaProperty<LocalDate> _premiumDate = DirectMetaProperty.ofReadWrite(
        this, "premiumDate", ManageableTrade.class, LocalDate.class);
    /**
     * The meta-property for the {@code premiumTime} property.
     */
    private final MetaProperty<OffsetTime> _premiumTime = DirectMetaProperty.ofReadWrite(
        this, "premiumTime", ManageableTrade.class, OffsetTime.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "positionId",
        "quantity",
        "securityKey",
        "tradeDate",
        "tradeTime",
        "counterpartyKey",
        "providerKey",
        "premium",
        "premiumCurrency",
        "premiumDate",
        "premiumTime");

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
        case 1381039140:  // positionId
          return _positionId;
        case -1285004149:  // quantity
          return _quantity;
        case 1550083839:  // securityKey
          return _securityKey;
        case 752419634:  // tradeDate
          return _tradeDate;
        case 752903761:  // tradeTime
          return _tradeTime;
        case 624096149:  // counterpartyKey
          return _counterpartyKey;
        case 2064682670:  // providerKey
          return _providerKey;
        case -318452137:  // premium
          return _premium;
        case 1136581512:  // premiumCurrency
          return _premiumCurrency;
        case 651701925:  // premiumDate
          return _premiumDate;
        case 652186052:  // premiumTime
          return _premiumTime;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ManageableTrade> builder() {
      return new BasicBeanBuilder<ManageableTrade>(new ManageableTrade());
    }

    @Override
    public Class<? extends ManageableTrade> beanType() {
      return ManageableTrade.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code uniqueId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueIdentifier> uniqueId() {
      return _uniqueId;
    }

    /**
     * The meta-property for the {@code positionId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueIdentifier> positionId() {
      return _positionId;
    }

    /**
     * The meta-property for the {@code quantity} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BigDecimal> quantity() {
      return _quantity;
    }

    /**
     * The meta-property for the {@code securityKey} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<IdentifierBundle> securityKey() {
      return _securityKey;
    }

    /**
     * The meta-property for the {@code tradeDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> tradeDate() {
      return _tradeDate;
    }

    /**
     * The meta-property for the {@code tradeTime} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<OffsetTime> tradeTime() {
      return _tradeTime;
    }

    /**
     * The meta-property for the {@code counterpartyKey} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Identifier> counterpartyKey() {
      return _counterpartyKey;
    }

    /**
     * The meta-property for the {@code providerKey} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Identifier> providerKey() {
      return _providerKey;
    }

    /**
     * The meta-property for the {@code premium} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> premium() {
      return _premium;
    }

    /**
     * The meta-property for the {@code premiumCurrency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Currency> premiumCurrency() {
      return _premiumCurrency;
    }

    /**
     * The meta-property for the {@code premiumDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> premiumDate() {
      return _premiumDate;
    }

    /**
     * The meta-property for the {@code premiumTime} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<OffsetTime> premiumTime() {
      return _premiumTime;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
