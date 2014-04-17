/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;

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
import org.threeten.bp.OffsetTime;

import com.google.common.collect.Maps;
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
public class ManageableTrade extends DirectBean
    implements Trade, MutableUniqueIdentifiable, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The unique identifier of the trade.
   * This field should be null until added to the master.
   */
  @PropertyDefinition
  private UniqueId _uniqueId;
  /**
   * The unique identifier of the parent position. This field is managed by the master.
   */
  @PropertyDefinition
  private UniqueId _parentPositionId;
  /**
   * The quantity. This field must not be null for the object to be valid.
   */
  @PropertyDefinition(validate = "notNull")
  private BigDecimal _quantity;
  /**
   * The link referencing the security, not null.
   * This may also hold the resolved security.
   */
  @PropertyDefinition(validate = "notNull")
  private ManageableSecurityLink _securityLink;
  /**
   * The counterparty external identifier, not null.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _counterpartyExternalId;
  /**
   * The trade date.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalDate _tradeDate;
  /**
   * The trade time with offset, null if not known.
   */
  @PropertyDefinition
  private OffsetTime _tradeTime;
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
   * The general purpose trade attributes.
   * These can be used to add arbitrary additional information to the object
   * and for aggregating in portfolios.
   */
  @PropertyDefinition(validate = "notNull")
  private final Map<String, String> _attributes = Maps.newHashMap();
  /**
   * The details of the deal, null if not known.
   * The OpenGamma trade is intended to model a trade from the perspective of risk analytics.
   * The deal interface provides a hook to add more detail about the trade from another
   * perspective, such as trade booking.
   */
  @PropertyDefinition
  private Deal _deal;
  /**
   * The provider external identifier for the data, null if not applicable.
   * This optional field can be used to capture the identifier used by the data provider.
   * This can be useful when receiving updates from the same provider.
   */
  @PropertyDefinition
  private ExternalId _providerId;

  /**
   * Construct an empty instance that must be populated via setters.
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
    _quantity = trade.getQuantity();
    _securityLink = new ManageableSecurityLink(trade.getSecurityLink());
    _tradeDate = trade.getTradeDate();
    _tradeTime = trade.getTradeTime();
    // this is a bug - PLAT-3117 - counterparty ID isn't nullable. use a default or throw an exception?
    _counterpartyExternalId = (trade.getCounterparty() != null ? trade.getCounterparty().getExternalId() : null);
    _premium = trade.getPremium();
    _premiumCurrency = trade.getPremiumCurrency();
    _premiumDate = trade.getPremiumDate();
    _premiumTime = trade.getPremiumTime();
    if (trade.getAttributes() != null) {
      for (final Entry<String, String> entry : trade.getAttributes().entrySet()) {
        addAttribute(entry.getKey(), entry.getValue());
      }
    }
  }

  /**
   * Creates a trade from trade quantity, instant and counterparty identifier.
   *
   * @param quantity  the amount of the trade, not null
   * @param securityId  the security identifier, not null
   * @param tradeDate  the trade date, not null
   * @param tradeTime  the trade time with offset, may be null
   * @param counterpartyId  the counterparty identifier, not null
   */
  public ManageableTrade(final BigDecimal quantity, final ExternalId securityId, final LocalDate tradeDate, final OffsetTime tradeTime, final ExternalId counterpartyId) {
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    ArgumentChecker.notNull(counterpartyId, "counterpartyId");
    ArgumentChecker.notNull(securityId, "securityId");
    _quantity = quantity;
    _tradeDate = tradeDate;
    _tradeTime = tradeTime;
    _counterpartyExternalId = counterpartyId;
    _securityLink = new ManageableSecurityLink(securityId);
  }

  /**
   * Creates a trade from trade quantity, instant and counterparty identifier.
   *
   * @param quantity  the amount of the trade, not null
   * @param securityId  the security identifier, not null
   * @param tradeDate  the trade date, not null
   * @param tradeTime  the trade time with offset, may be null
   * @param counterpartyId  the counterparty identifier, not null
   */
  public ManageableTrade(final BigDecimal quantity, final ExternalIdBundle securityId, final LocalDate tradeDate, final OffsetTime tradeTime, final ExternalId counterpartyId) {
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(securityId, "securityId");
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    ArgumentChecker.notNull(counterpartyId, "counterpartyId");
    _quantity = quantity;
    _tradeDate = tradeDate;
    _tradeTime = tradeTime;
    _counterpartyExternalId = counterpartyId;
    _securityLink = new ManageableSecurityLink(securityId);
  }

  //-------------------------------------------------------------------------
  @Override
  public void addAttribute(final String key, final String value) {
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

  //-----------------------------------------------------------------------
  /**
   * Gets the unique identifier of the trade.
   * This field should be null until added to the master.
   * @return the value of the property
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier of the trade.
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
   * Gets the unique identifier of the parent position. This field is managed by the master.
   * @return the value of the property
   */
  public UniqueId getParentPositionId() {
    return _parentPositionId;
  }

  /**
   * Sets the unique identifier of the parent position. This field is managed by the master.
   * @param parentPositionId  the new value of the property
   */
  public void setParentPositionId(UniqueId parentPositionId) {
    this._parentPositionId = parentPositionId;
  }

  /**
   * Gets the the {@code parentPositionId} property.
   * @return the property, not null
   */
  public final Property<UniqueId> parentPositionId() {
    return metaBean().parentPositionId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the quantity. This field must not be null for the object to be valid.
   * @return the value of the property, not null
   */
  public BigDecimal getQuantity() {
    return _quantity;
  }

  /**
   * Sets the quantity. This field must not be null for the object to be valid.
   * @param quantity  the new value of the property, not null
   */
  public void setQuantity(BigDecimal quantity) {
    JodaBeanUtils.notNull(quantity, "quantity");
    this._quantity = quantity;
  }

  /**
   * Gets the the {@code quantity} property.
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
   * Gets the counterparty external identifier, not null.
   * @return the value of the property, not null
   */
  public ExternalId getCounterpartyExternalId() {
    return _counterpartyExternalId;
  }

  /**
   * Sets the counterparty external identifier, not null.
   * @param counterpartyExternalId  the new value of the property, not null
   */
  public void setCounterpartyExternalId(ExternalId counterpartyExternalId) {
    JodaBeanUtils.notNull(counterpartyExternalId, "counterpartyExternalId");
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
   * Gets the trade date.
   * @return the value of the property, not null
   */
  public LocalDate getTradeDate() {
    return _tradeDate;
  }

  /**
   * Sets the trade date.
   * @param tradeDate  the new value of the property, not null
   */
  public void setTradeDate(LocalDate tradeDate) {
    JodaBeanUtils.notNull(tradeDate, "tradeDate");
    this._tradeDate = tradeDate;
  }

  /**
   * Gets the the {@code tradeDate} property.
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
   * Gets the general purpose trade attributes.
   * These can be used to add arbitrary additional information to the object
   * and for aggregating in portfolios.
   * @return the value of the property, not null
   */
  public Map<String, String> getAttributes() {
    return _attributes;
  }

  /**
   * Sets the general purpose trade attributes.
   * These can be used to add arbitrary additional information to the object
   * and for aggregating in portfolios.
   * @param attributes  the new value of the property, not null
   */
  public void setAttributes(Map<String, String> attributes) {
    JodaBeanUtils.notNull(attributes, "attributes");
    this._attributes.clear();
    this._attributes.putAll(attributes);
  }

  /**
   * Gets the the {@code attributes} property.
   * These can be used to add arbitrary additional information to the object
   * and for aggregating in portfolios.
   * @return the property, not null
   */
  public final Property<Map<String, String>> attributes() {
    return metaBean().attributes().createProperty(this);
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
  @Override
  public ManageableTrade clone() {
    return JodaBeanUtils.cloneAlways(this);
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
          JodaBeanUtils.equal(getCounterpartyExternalId(), other.getCounterpartyExternalId()) &&
          JodaBeanUtils.equal(getTradeDate(), other.getTradeDate()) &&
          JodaBeanUtils.equal(getTradeTime(), other.getTradeTime()) &&
          JodaBeanUtils.equal(getPremium(), other.getPremium()) &&
          JodaBeanUtils.equal(getPremiumCurrency(), other.getPremiumCurrency()) &&
          JodaBeanUtils.equal(getPremiumDate(), other.getPremiumDate()) &&
          JodaBeanUtils.equal(getPremiumTime(), other.getPremiumTime()) &&
          JodaBeanUtils.equal(getAttributes(), other.getAttributes()) &&
          JodaBeanUtils.equal(getDeal(), other.getDeal()) &&
          JodaBeanUtils.equal(getProviderId(), other.getProviderId());
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
    hash += hash * 31 + JodaBeanUtils.hashCode(getCounterpartyExternalId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTradeDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTradeTime());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPremium());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPremiumCurrency());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPremiumDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPremiumTime());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAttributes());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDeal());
    hash += hash * 31 + JodaBeanUtils.hashCode(getProviderId());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(480);
    buf.append("ManageableTrade{");
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
    buf.append("parentPositionId").append('=').append(JodaBeanUtils.toString(getParentPositionId())).append(',').append(' ');
    buf.append("quantity").append('=').append(JodaBeanUtils.toString(getQuantity())).append(',').append(' ');
    buf.append("securityLink").append('=').append(JodaBeanUtils.toString(getSecurityLink())).append(',').append(' ');
    buf.append("counterpartyExternalId").append('=').append(JodaBeanUtils.toString(getCounterpartyExternalId())).append(',').append(' ');
    buf.append("tradeDate").append('=').append(JodaBeanUtils.toString(getTradeDate())).append(',').append(' ');
    buf.append("tradeTime").append('=').append(JodaBeanUtils.toString(getTradeTime())).append(',').append(' ');
    buf.append("premium").append('=').append(JodaBeanUtils.toString(getPremium())).append(',').append(' ');
    buf.append("premiumCurrency").append('=').append(JodaBeanUtils.toString(getPremiumCurrency())).append(',').append(' ');
    buf.append("premiumDate").append('=').append(JodaBeanUtils.toString(getPremiumDate())).append(',').append(' ');
    buf.append("premiumTime").append('=').append(JodaBeanUtils.toString(getPremiumTime())).append(',').append(' ');
    buf.append("attributes").append('=').append(JodaBeanUtils.toString(getAttributes())).append(',').append(' ');
    buf.append("deal").append('=').append(JodaBeanUtils.toString(getDeal())).append(',').append(' ');
    buf.append("providerId").append('=').append(JodaBeanUtils.toString(getProviderId())).append(',').append(' ');
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
     * The meta-property for the {@code counterpartyExternalId} property.
     */
    private final MetaProperty<ExternalId> _counterpartyExternalId = DirectMetaProperty.ofReadWrite(
        this, "counterpartyExternalId", ManageableTrade.class, ExternalId.class);
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
     * The meta-property for the {@code attributes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<String, String>> _attributes = DirectMetaProperty.ofReadWrite(
        this, "attributes", ManageableTrade.class, (Class) Map.class);
    /**
     * The meta-property for the {@code deal} property.
     */
    private final MetaProperty<Deal> _deal = DirectMetaProperty.ofReadWrite(
        this, "deal", ManageableTrade.class, Deal.class);
    /**
     * The meta-property for the {@code providerId} property.
     */
    private final MetaProperty<ExternalId> _providerId = DirectMetaProperty.ofReadWrite(
        this, "providerId", ManageableTrade.class, ExternalId.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "parentPositionId",
        "quantity",
        "securityLink",
        "counterpartyExternalId",
        "tradeDate",
        "tradeTime",
        "premium",
        "premiumCurrency",
        "premiumDate",
        "premiumTime",
        "attributes",
        "deal",
        "providerId");

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
        case 432285776:  // counterpartyExternalId
          return _counterpartyExternalId;
        case 752419634:  // tradeDate
          return _tradeDate;
        case 752903761:  // tradeTime
          return _tradeTime;
        case -318452137:  // premium
          return _premium;
        case 1136581512:  // premiumCurrency
          return _premiumCurrency;
        case 651701925:  // premiumDate
          return _premiumDate;
        case 652186052:  // premiumTime
          return _premiumTime;
        case 405645655:  // attributes
          return _attributes;
        case 3079276:  // deal
          return _deal;
        case 205149932:  // providerId
          return _providerId;
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
     * The meta-property for the {@code counterpartyExternalId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> counterpartyExternalId() {
      return _counterpartyExternalId;
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
     * The meta-property for the {@code attributes} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<String, String>> attributes() {
      return _attributes;
    }

    /**
     * The meta-property for the {@code deal} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Deal> deal() {
      return _deal;
    }

    /**
     * The meta-property for the {@code providerId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> providerId() {
      return _providerId;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return ((ManageableTrade) bean).getUniqueId();
        case -108882834:  // parentPositionId
          return ((ManageableTrade) bean).getParentPositionId();
        case -1285004149:  // quantity
          return ((ManageableTrade) bean).getQuantity();
        case 807992154:  // securityLink
          return ((ManageableTrade) bean).getSecurityLink();
        case 432285776:  // counterpartyExternalId
          return ((ManageableTrade) bean).getCounterpartyExternalId();
        case 752419634:  // tradeDate
          return ((ManageableTrade) bean).getTradeDate();
        case 752903761:  // tradeTime
          return ((ManageableTrade) bean).getTradeTime();
        case -318452137:  // premium
          return ((ManageableTrade) bean).getPremium();
        case 1136581512:  // premiumCurrency
          return ((ManageableTrade) bean).getPremiumCurrency();
        case 651701925:  // premiumDate
          return ((ManageableTrade) bean).getPremiumDate();
        case 652186052:  // premiumTime
          return ((ManageableTrade) bean).getPremiumTime();
        case 405645655:  // attributes
          return ((ManageableTrade) bean).getAttributes();
        case 3079276:  // deal
          return ((ManageableTrade) bean).getDeal();
        case 205149932:  // providerId
          return ((ManageableTrade) bean).getProviderId();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          ((ManageableTrade) bean).setUniqueId((UniqueId) newValue);
          return;
        case -108882834:  // parentPositionId
          ((ManageableTrade) bean).setParentPositionId((UniqueId) newValue);
          return;
        case -1285004149:  // quantity
          ((ManageableTrade) bean).setQuantity((BigDecimal) newValue);
          return;
        case 807992154:  // securityLink
          ((ManageableTrade) bean).setSecurityLink((ManageableSecurityLink) newValue);
          return;
        case 432285776:  // counterpartyExternalId
          ((ManageableTrade) bean).setCounterpartyExternalId((ExternalId) newValue);
          return;
        case 752419634:  // tradeDate
          ((ManageableTrade) bean).setTradeDate((LocalDate) newValue);
          return;
        case 752903761:  // tradeTime
          ((ManageableTrade) bean).setTradeTime((OffsetTime) newValue);
          return;
        case -318452137:  // premium
          ((ManageableTrade) bean).setPremium((Double) newValue);
          return;
        case 1136581512:  // premiumCurrency
          ((ManageableTrade) bean).setPremiumCurrency((Currency) newValue);
          return;
        case 651701925:  // premiumDate
          ((ManageableTrade) bean).setPremiumDate((LocalDate) newValue);
          return;
        case 652186052:  // premiumTime
          ((ManageableTrade) bean).setPremiumTime((OffsetTime) newValue);
          return;
        case 405645655:  // attributes
          ((ManageableTrade) bean).setAttributes((Map<String, String>) newValue);
          return;
        case 3079276:  // deal
          ((ManageableTrade) bean).setDeal((Deal) newValue);
          return;
        case 205149932:  // providerId
          ((ManageableTrade) bean).setProviderId((ExternalId) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ManageableTrade) bean)._quantity, "quantity");
      JodaBeanUtils.notNull(((ManageableTrade) bean)._securityLink, "securityLink");
      JodaBeanUtils.notNull(((ManageableTrade) bean)._counterpartyExternalId, "counterpartyExternalId");
      JodaBeanUtils.notNull(((ManageableTrade) bean)._tradeDate, "tradeDate");
      JodaBeanUtils.notNull(((ManageableTrade) bean)._attributes, "attributes");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
