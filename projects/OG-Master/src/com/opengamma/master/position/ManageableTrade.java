/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

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

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurityLink;
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
public class ManageableTrade extends DirectBean implements Trade, MutableUniqueIdentifiable, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;
  /**
   * The trade unique identifier.
   * This field should be null until added to the master.
   */
  @PropertyDefinition
  private UniqueId _uniqueId;
  /**
   * The parent position unique identifier.
   * This field is managed by the master.
   */
  @PropertyDefinition
  private UniqueId _parentPositionId;
  /**
   * The quantity.
   * This field must not be null for the object to be valid.
   */
  @PropertyDefinition
  private BigDecimal _quantity;
  /**
   * The link referencing the security, not null.
   * This may also hold the resolved security.
   */
  @PropertyDefinition(validate = "notNull")
  private ManageableSecurityLink _securityLink;
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
   * The counterparty external identifier, null if not known.
   */
  @PropertyDefinition
  private ExternalId _counterpartyExternalId;
  /**
   * The provider external identifier for the data, null if not applicable.
   * This optional field can be used to capture the identifier used by the data provider.
   * This can be useful when receiving updates from the same provider.
   */
  @PropertyDefinition
  private ExternalId _providerId;
  /**
   * Amount paid for trade at time of purchase, null if not known.
   */
  @PropertyDefinition
  private Double _premium;
  /**
   * Currency of payment at time of purchase, null if not known.
   */
  @PropertyDefinition
  private Currency _premiumCurrency;
  /**
   * Date of premium payment, null if not known.
   */
  @PropertyDefinition
  private LocalDate _premiumDate;
  /**
   * Time of premium payment, null if not known.
   */
  @PropertyDefinition
  private OffsetTime _premiumTime;
  /**
   * The details of the deal, null if not known.
   * The OpenGamma trade is intended to model a trade from the perspective of risk analytics.
   * The deal interface provides a hook to add more detail about the trade from another
   * perspective, such as trade booking.
   */
  @PropertyDefinition
  private Deal _deal;
  /**
   * The general purpose trade attributes, which can be used for aggregating in portfolios.
   */
  @PropertyDefinition
  private final Map<String, String> _attributes = new HashMap<String, String>();

  /**
   * Creates an instance.
   */
  public ManageableTrade() {
    _securityLink = new ManageableSecurityLink();
  }

  /**
   * Creates an instance, copying the values from another {@link Trade} object.
   * 
   * @param trade the object to copy values from
   */
  public ManageableTrade(final Trade trade) {
    ArgumentChecker.notNull(trade, "trade");
    ArgumentChecker.notNull(trade.getAttributes(), "trade.attributes");
    _parentPositionId = trade.getParentPositionId();
    _quantity = trade.getQuantity();
    _securityLink = new ManageableSecurityLink(trade.getSecurityLink());
    _tradeDate = trade.getTradeDate();
    _tradeTime = trade.getTradeTime();
    _counterpartyExternalId = (trade.getCounterparty() != null ? trade.getCounterparty().getExternalId() : null);
    _premium = trade.getPremium();
    _premiumCurrency = trade.getPremiumCurrency();
    _premiumDate = trade.getPremiumDate();
    _premiumTime = trade.getPremiumTime();
    if (trade.getAttributes() != null) {
      for (Entry<String, String> entry : trade.getAttributes().entrySet()) {
        addAttribute(entry.getKey(), entry.getValue());
      }
    }
  }

  /**
   * Creates a trade from trade quantity, instant and counterparty identifier.
   * 
   * @param quantity  the amount of the trade, not null
   * @param securityKey  the security identifier, not null
   * @param tradeDate  the trade date, not null
   * @param tradeTime  the trade time with offset, may be null
   * @param counterpartyId  the counterparty identifier, not null
   */
  public ManageableTrade(final BigDecimal quantity, final ExternalId securityKey, final LocalDate tradeDate, final OffsetTime tradeTime, final ExternalId counterpartyId) {
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    ArgumentChecker.notNull(counterpartyId, "counterpartyId");
    ArgumentChecker.notNull(securityKey, "securityKey");
    _quantity = quantity;
    _tradeDate = tradeDate;
    _tradeTime = tradeTime;
    _counterpartyExternalId = counterpartyId;
    _securityLink = new ManageableSecurityLink(securityKey);
  }

  /**
   * Creates a trade from trade quantity, instant and counterparty identifier.
   * 
   * @param quantity  the amount of the trade, not null
   * @param securityKey  the security identifier, not null
   * @param tradeDate  the trade date, not null
   * @param tradeTime  the trade time with offset, may be null
   * @param counterpartyId  the counterparty identifier, not null
   */
  public ManageableTrade(final BigDecimal quantity, final ExternalIdBundle securityKey, final LocalDate tradeDate, final OffsetTime tradeTime, final ExternalId counterpartyId) {
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(securityKey, "securityKey");
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    ArgumentChecker.notNull(counterpartyId, "counterpartyId");
    _quantity = quantity;
    _tradeDate = tradeDate;
    _tradeTime = tradeTime;
    _counterpartyExternalId = counterpartyId;
    _securityLink = new ManageableSecurityLink(securityKey);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a key value pair to attributes
   * 
   * @param key  the key to add, not null
   * @param value  the value to add, not null
   */
  public void addAttribute(String key, String value) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(value, "value");
    _attributes.put(key, value);
  }

  //-------------------------------------------------------------------------
  @Override
  public Counterparty getCounterparty() {
    return new SimpleCounterparty(getCounterpartyExternalId());
  }

  @Override
  public Security getSecurity() {
    return _securityLink.getTarget();
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
  static {
    JodaBeanUtils.registerMetaBean(ManageableTrade.Meta.INSTANCE);
  }

  @Override
  public ManageableTrade.Meta metaBean() {
    return ManageableTrade.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        return getUniqueId();
      case -108882834:  // parentPositionId
        return getParentPositionId();
      case -1285004149:  // quantity
        return getQuantity();
      case 807992154:  // securityLink
        return getSecurityLink();
      case 752419634:  // tradeDate
        return getTradeDate();
      case 752903761:  // tradeTime
        return getTradeTime();
      case 432285776:  // counterpartyExternalId
        return getCounterpartyExternalId();
      case 205149932:  // providerId
        return getProviderId();
      case -318452137:  // premium
        return getPremium();
      case 1136581512:  // premiumCurrency
        return getPremiumCurrency();
      case 651701925:  // premiumDate
        return getPremiumDate();
      case 652186052:  // premiumTime
        return getPremiumTime();
      case 3079276:  // deal
        return getDeal();
      case 405645655:  // attributes
        return getAttributes();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        setUniqueId((UniqueId) newValue);
        return;
      case -108882834:  // parentPositionId
        setParentPositionId((UniqueId) newValue);
        return;
      case -1285004149:  // quantity
        setQuantity((BigDecimal) newValue);
        return;
      case 807992154:  // securityLink
        setSecurityLink((ManageableSecurityLink) newValue);
        return;
      case 752419634:  // tradeDate
        setTradeDate((LocalDate) newValue);
        return;
      case 752903761:  // tradeTime
        setTradeTime((OffsetTime) newValue);
        return;
      case 432285776:  // counterpartyExternalId
        setCounterpartyExternalId((ExternalId) newValue);
        return;
      case 205149932:  // providerId
        setProviderId((ExternalId) newValue);
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
      case 3079276:  // deal
        setDeal((Deal) newValue);
        return;
      case 405645655:  // attributes
        setAttributes((Map<String, String>) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_securityLink, "securityLink");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ManageableTrade other = (ManageableTrade) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(getParentPositionId(), other.getParentPositionId()) &&
          JodaBeanUtils.equal(getQuantity(), other.getQuantity()) &&
          JodaBeanUtils.equal(getSecurityLink(), other.getSecurityLink()) &&
          JodaBeanUtils.equal(getTradeDate(), other.getTradeDate()) &&
          JodaBeanUtils.equal(getTradeTime(), other.getTradeTime()) &&
          JodaBeanUtils.equal(getCounterpartyExternalId(), other.getCounterpartyExternalId()) &&
          JodaBeanUtils.equal(getProviderId(), other.getProviderId()) &&
          JodaBeanUtils.equal(getPremium(), other.getPremium()) &&
          JodaBeanUtils.equal(getPremiumCurrency(), other.getPremiumCurrency()) &&
          JodaBeanUtils.equal(getPremiumDate(), other.getPremiumDate()) &&
          JodaBeanUtils.equal(getPremiumTime(), other.getPremiumTime()) &&
          JodaBeanUtils.equal(getDeal(), other.getDeal()) &&
          JodaBeanUtils.equal(getAttributes(), other.getAttributes());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getParentPositionId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getQuantity());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecurityLink());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTradeDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTradeTime());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCounterpartyExternalId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getProviderId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPremium());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPremiumCurrency());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPremiumDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPremiumTime());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDeal());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAttributes());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the trade unique identifier.
   * This field should be null until added to the master.
   * @return the value of the property
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the trade unique identifier.
   * This field should be null until added to the master.
   * @param uniqueId  the new value of the property
   */
  public void setUniqueId(UniqueId uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * This field should be null until added to the master.
   * @return the property, not null
   */
  public final Property<UniqueId> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the parent position unique identifier.
   * This field is managed by the master.
   * @return the value of the property
   */
  public UniqueId getParentPositionId() {
    return _parentPositionId;
  }

  /**
   * Sets the parent position unique identifier.
   * This field is managed by the master.
   * @param parentPositionId  the new value of the property
   */
  public void setParentPositionId(UniqueId parentPositionId) {
    this._parentPositionId = parentPositionId;
  }

  /**
   * Gets the the {@code parentPositionId} property.
   * This field is managed by the master.
   * @return the property, not null
   */
  public final Property<UniqueId> parentPositionId() {
    return metaBean().parentPositionId().createProperty(this);
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
   * Gets the link referencing the security, not null.
   * This may also hold the resolved security.
   * @return the value of the property, not null
   */
  public ManageableSecurityLink getSecurityLink() {
    return _securityLink;
  }

  /**
   * Sets the link referencing the security, not null.
   * This may also hold the resolved security.
   * @param securityLink  the new value of the property, not null
   */
  public void setSecurityLink(ManageableSecurityLink securityLink) {
    JodaBeanUtils.notNull(securityLink, "securityLink");
    this._securityLink = securityLink;
  }

  /**
   * Gets the the {@code securityLink} property.
   * This may also hold the resolved security.
   * @return the property, not null
   */
  public final Property<ManageableSecurityLink> securityLink() {
    return metaBean().securityLink().createProperty(this);
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
   * Gets the counterparty external identifier, null if not known.
   * @return the value of the property
   */
  public ExternalId getCounterpartyExternalId() {
    return _counterpartyExternalId;
  }

  /**
   * Sets the counterparty external identifier, null if not known.
   * @param counterpartyExternalId  the new value of the property
   */
  public void setCounterpartyExternalId(ExternalId counterpartyExternalId) {
    this._counterpartyExternalId = counterpartyExternalId;
  }

  /**
   * Gets the the {@code counterpartyExternalId} property.
   * @return the property, not null
   */
  public final Property<ExternalId> counterpartyExternalId() {
    return metaBean().counterpartyExternalId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the provider external identifier for the data, null if not applicable.
   * This optional field can be used to capture the identifier used by the data provider.
   * This can be useful when receiving updates from the same provider.
   * @return the value of the property
   */
  public ExternalId getProviderId() {
    return _providerId;
  }

  /**
   * Sets the provider external identifier for the data, null if not applicable.
   * This optional field can be used to capture the identifier used by the data provider.
   * This can be useful when receiving updates from the same provider.
   * @param providerId  the new value of the property
   */
  public void setProviderId(ExternalId providerId) {
    this._providerId = providerId;
  }

  /**
   * Gets the the {@code providerId} property.
   * This optional field can be used to capture the identifier used by the data provider.
   * This can be useful when receiving updates from the same provider.
   * @return the property, not null
   */
  public final Property<ExternalId> providerId() {
    return metaBean().providerId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets amount paid for trade at time of purchase, null if not known.
   * @return the value of the property
   */
  public Double getPremium() {
    return _premium;
  }

  /**
   * Sets amount paid for trade at time of purchase, null if not known.
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
   * Gets currency of payment at time of purchase, null if not known.
   * @return the value of the property
   */
  public Currency getPremiumCurrency() {
    return _premiumCurrency;
  }

  /**
   * Sets currency of payment at time of purchase, null if not known.
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
   * Gets date of premium payment, null if not known.
   * @return the value of the property
   */
  public LocalDate getPremiumDate() {
    return _premiumDate;
  }

  /**
   * Sets date of premium payment, null if not known.
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
   * Gets time of premium payment, null if not known.
   * @return the value of the property
   */
  public OffsetTime getPremiumTime() {
    return _premiumTime;
  }

  /**
   * Sets time of premium payment, null if not known.
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
   * Gets the details of the deal, null if not known.
   * The OpenGamma trade is intended to model a trade from the perspective of risk analytics.
   * The deal interface provides a hook to add more detail about the trade from another
   * perspective, such as trade booking.
   * @return the value of the property
   */
  public Deal getDeal() {
    return _deal;
  }

  /**
   * Sets the details of the deal, null if not known.
   * The OpenGamma trade is intended to model a trade from the perspective of risk analytics.
   * The deal interface provides a hook to add more detail about the trade from another
   * perspective, such as trade booking.
   * @param deal  the new value of the property
   */
  public void setDeal(Deal deal) {
    this._deal = deal;
  }

  /**
   * Gets the the {@code deal} property.
   * The OpenGamma trade is intended to model a trade from the perspective of risk analytics.
   * The deal interface provides a hook to add more detail about the trade from another
   * perspective, such as trade booking.
   * @return the property, not null
   */
  public final Property<Deal> deal() {
    return metaBean().deal().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the general purpose trade attributes, which can be used for aggregating in portfolios.
   * @return the value of the property
   */
  public Map<String, String> getAttributes() {
    return _attributes;
  }

  /**
   * Sets the general purpose trade attributes, which can be used for aggregating in portfolios.
   * @param attributes  the new value of the property
   */
  public void setAttributes(Map<String, String> attributes) {
    this._attributes.clear();
    this._attributes.putAll(attributes);
  }

  /**
   * Gets the the {@code attributes} property.
   * @return the property, not null
   */
  public final Property<Map<String, String>> attributes() {
    return metaBean().attributes().createProperty(this);
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
    private final MetaProperty<UniqueId> _uniqueId = DirectMetaProperty.ofReadWrite(
        this, "uniqueId", ManageableTrade.class, UniqueId.class);
    /**
     * The meta-property for the {@code parentPositionId} property.
     */
    private final MetaProperty<UniqueId> _parentPositionId = DirectMetaProperty.ofReadWrite(
        this, "parentPositionId", ManageableTrade.class, UniqueId.class);
    /**
     * The meta-property for the {@code quantity} property.
     */
    private final MetaProperty<BigDecimal> _quantity = DirectMetaProperty.ofReadWrite(
        this, "quantity", ManageableTrade.class, BigDecimal.class);
    /**
     * The meta-property for the {@code securityLink} property.
     */
    private final MetaProperty<ManageableSecurityLink> _securityLink = DirectMetaProperty.ofReadWrite(
        this, "securityLink", ManageableTrade.class, ManageableSecurityLink.class);
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
     * The meta-property for the {@code counterpartyExternalId} property.
     */
    private final MetaProperty<ExternalId> _counterpartyExternalId = DirectMetaProperty.ofReadWrite(
        this, "counterpartyExternalId", ManageableTrade.class, ExternalId.class);
    /**
     * The meta-property for the {@code providerId} property.
     */
    private final MetaProperty<ExternalId> _providerId = DirectMetaProperty.ofReadWrite(
        this, "providerId", ManageableTrade.class, ExternalId.class);
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
     * The meta-property for the {@code deal} property.
     */
    private final MetaProperty<Deal> _deal = DirectMetaProperty.ofReadWrite(
        this, "deal", ManageableTrade.class, Deal.class);
    /**
     * The meta-property for the {@code attributes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<String, String>> _attributes = DirectMetaProperty.ofReadWrite(
        this, "attributes", ManageableTrade.class, (Class) Map.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "parentPositionId",
        "quantity",
        "securityLink",
        "tradeDate",
        "tradeTime",
        "counterpartyExternalId",
        "providerId",
        "premium",
        "premiumCurrency",
        "premiumDate",
        "premiumTime",
        "deal",
        "attributes");

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
        case -108882834:  // parentPositionId
          return _parentPositionId;
        case -1285004149:  // quantity
          return _quantity;
        case 807992154:  // securityLink
          return _securityLink;
        case 752419634:  // tradeDate
          return _tradeDate;
        case 752903761:  // tradeTime
          return _tradeTime;
        case 432285776:  // counterpartyExternalId
          return _counterpartyExternalId;
        case 205149932:  // providerId
          return _providerId;
        case -318452137:  // premium
          return _premium;
        case 1136581512:  // premiumCurrency
          return _premiumCurrency;
        case 651701925:  // premiumDate
          return _premiumDate;
        case 652186052:  // premiumTime
          return _premiumTime;
        case 3079276:  // deal
          return _deal;
        case 405645655:  // attributes
          return _attributes;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ManageableTrade> builder() {
      return new DirectBeanBuilder<ManageableTrade>(new ManageableTrade());
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
    public final MetaProperty<UniqueId> uniqueId() {
      return _uniqueId;
    }

    /**
     * The meta-property for the {@code parentPositionId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> parentPositionId() {
      return _parentPositionId;
    }

    /**
     * The meta-property for the {@code quantity} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BigDecimal> quantity() {
      return _quantity;
    }

    /**
     * The meta-property for the {@code securityLink} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ManageableSecurityLink> securityLink() {
      return _securityLink;
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
     * The meta-property for the {@code counterpartyExternalId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> counterpartyExternalId() {
      return _counterpartyExternalId;
    }

    /**
     * The meta-property for the {@code providerId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> providerId() {
      return _providerId;
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

    /**
     * The meta-property for the {@code deal} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Deal> deal() {
      return _deal;
    }

    /**
     * The meta-property for the {@code attributes} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<String, String>> attributes() {
      return _attributes;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
