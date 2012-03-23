/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import org.apache.commons.lang.text.StrBuilder;
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

import com.google.common.collect.Maps;
import com.opengamma.core.LinkUtils;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A simple mutable implementation of {@code Trade}.
 */
@BeanDefinition
public class SimpleTrade extends DirectBean
    implements Trade, MutableUniqueIdentifiable, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The unique identifier of the trade.
   */
  @PropertyDefinition(validate = "notNull")
  private UniqueId _uniqueId;
  /**
   * The unique identifier of the parent position, null if no parent.
   */
  @PropertyDefinition
  private UniqueId _parentPositionId;
  /**
   * The number of units in the trade.
   */
  @PropertyDefinition(validate = "notNull")
  private BigDecimal _quantity;
  /**
   * The link referencing the security, not null.
   * This may also hold the resolved security.
   */
  @PropertyDefinition(validate = "notNull")
  private SecurityLink _securityLink;
  /**
   * The counterparty.
   */
  @PropertyDefinition
  private Counterparty _counterparty;
  /**
   * The trade date.
   */
  @PropertyDefinition
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
   * Construct an empty instance that must be populated via setters.
   */
  public SimpleTrade() {
    _securityLink = new SimpleSecurityLink();
  }

  /**
   * Creates a trade from a positionId, an amount of a security identified by key, counterparty and tradeinstant.
   * 
   * @param parentPositionId  the parent position id, not null
   * @param securityLink  the security identifier, not null
   * @param quantity  the amount of the trade, not null
   * @param counterparty  the counterparty, not null
   * @param tradeDate  the trade date, not null
   * @param tradeTime  the trade time with offset, may be null
   */
  public SimpleTrade(UniqueId parentPositionId, SecurityLink securityLink, BigDecimal quantity, Counterparty counterparty, LocalDate tradeDate, OffsetTime tradeTime) {
    ArgumentChecker.notNull(parentPositionId, "parentPositionId");
    ArgumentChecker.notNull(securityLink, "securityLink");
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(counterparty, "counterparty");
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    _quantity = quantity;
    _counterparty = counterparty;
    _tradeDate = tradeDate;
    _tradeTime = tradeTime;
    _parentPositionId = parentPositionId;
    _securityLink = securityLink;
  }

  /**
   * Creates a trade from a positionId, an amount of a security, counterparty and trade instant.
   * 
   * @param parentPositionId  the parent position id, not null
   * @param security  the security, not null
   * @param quantity  the amount of the trade, not null
   * @param counterparty  the counterparty, not null
   * @param tradeDate  the trade date, not null
   * @param tradeTime  the trade time with offset, may be null
   */
  public SimpleTrade(UniqueId parentPositionId, Security security, BigDecimal quantity, Counterparty counterparty, LocalDate tradeDate, OffsetTime tradeTime) {
    ArgumentChecker.notNull(parentPositionId, "parentPositionId");
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(counterparty, "counterparty");
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    _quantity = quantity;
    _counterparty = counterparty;
    _tradeDate = tradeDate;
    _tradeTime = tradeTime;
    _parentPositionId = parentPositionId;
    _securityLink = SimpleSecurityLink.of(security);
  }

  /**
   * Creates a deep copy of the specified position.
   * 
   * @param copyFrom instance to copy fields from, not null
   */
  public SimpleTrade(final Trade copyFrom) {
    ArgumentChecker.notNull(copyFrom, "copyFrom");
    _uniqueId = copyFrom.getUniqueId();
    _quantity = copyFrom.getQuantity();
    _counterparty = copyFrom.getCounterparty();
    _tradeDate = copyFrom.getTradeDate();
    _tradeTime = copyFrom.getTradeTime();
    _parentPositionId = copyFrom.getParentPositionId();
    _premium = copyFrom.getPremium();
    _premiumCurrency = copyFrom.getPremiumCurrency();
    _premiumDate = copyFrom.getPremiumDate();
    _premiumTime = copyFrom.getPremiumTime();
    _securityLink = new SimpleSecurityLink(copyFrom.getSecurityLink());
    setAttributes(copyFrom.getAttributes());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the target security from the link.
   * <p>
   * This convenience method gets the target security from the link.
   * This is guaranteed to return a security within an analytic function.
   * 
   * @return the security link, null if target not resolved in the link
   */
  @Override
  public Security getSecurity() {
    return _securityLink.getTarget();
  }

  //-------------------------------------------------------------------------
  @Override
  public void addAttribute(String key, String value) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(value, "value");
    _attributes.put(key, value);
  }

  /**
   * Removes the attribute with specified key.
   * 
   * @param key  the attribute key to remove, not null
   */
  public void removeAttribute(final String key) {
    ArgumentChecker.notNull(key, "key");
    _attributes.remove(key);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return new StrBuilder(256)
        .append("Trade[")
        .append(getUniqueId())
        .append(", ")
        .append(getQuantity())
        .append(' ')
        .append(LinkUtils.best(getSecurityLink()))
        .append(" PositionID:")
        .append(getParentPositionId())
        .append(" ")
        .append(getCounterparty())
        .append(" ")
        .append(getTradeDate())
        .append(" ")
        .append(getTradeTime())
        .append(']')
        .toString();
  }
  
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SimpleTrade}.
   * @return the meta-bean, not null
   */
  public static SimpleTrade.Meta meta() {
    return SimpleTrade.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(SimpleTrade.Meta.INSTANCE);
  }

  @Override
  public SimpleTrade.Meta metaBean() {
    return SimpleTrade.Meta.INSTANCE;
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
      case -1651301782:  // counterparty
        return getCounterparty();
      case 752419634:  // tradeDate
        return getTradeDate();
      case 752903761:  // tradeTime
        return getTradeTime();
      case -318452137:  // premium
        return getPremium();
      case 1136581512:  // premiumCurrency
        return getPremiumCurrency();
      case 651701925:  // premiumDate
        return getPremiumDate();
      case 652186052:  // premiumTime
        return getPremiumTime();
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
        setSecurityLink((SecurityLink) newValue);
        return;
      case -1651301782:  // counterparty
        setCounterparty((Counterparty) newValue);
        return;
      case 752419634:  // tradeDate
        setTradeDate((LocalDate) newValue);
        return;
      case 752903761:  // tradeTime
        setTradeTime((OffsetTime) newValue);
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
      case 405645655:  // attributes
        setAttributes((Map<String, String>) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_uniqueId, "uniqueId");
    JodaBeanUtils.notNull(_quantity, "quantity");
    JodaBeanUtils.notNull(_securityLink, "securityLink");
    JodaBeanUtils.notNull(_attributes, "attributes");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SimpleTrade other = (SimpleTrade) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(getParentPositionId(), other.getParentPositionId()) &&
          JodaBeanUtils.equal(getQuantity(), other.getQuantity()) &&
          JodaBeanUtils.equal(getSecurityLink(), other.getSecurityLink()) &&
          JodaBeanUtils.equal(getCounterparty(), other.getCounterparty()) &&
          JodaBeanUtils.equal(getTradeDate(), other.getTradeDate()) &&
          JodaBeanUtils.equal(getTradeTime(), other.getTradeTime()) &&
          JodaBeanUtils.equal(getPremium(), other.getPremium()) &&
          JodaBeanUtils.equal(getPremiumCurrency(), other.getPremiumCurrency()) &&
          JodaBeanUtils.equal(getPremiumDate(), other.getPremiumDate()) &&
          JodaBeanUtils.equal(getPremiumTime(), other.getPremiumTime()) &&
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
    hash += hash * 31 + JodaBeanUtils.hashCode(getCounterparty());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTradeDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTradeTime());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPremium());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPremiumCurrency());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPremiumDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPremiumTime());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAttributes());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unique identifier of the trade.
   * @return the value of the property, not null
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier of the trade.
   * @param uniqueId  the new value of the property, not null
   */
  public void setUniqueId(UniqueId uniqueId) {
    JodaBeanUtils.notNull(uniqueId, "uniqueId");
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * @return the property, not null
   */
  public final Property<UniqueId> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unique identifier of the parent position, null if no parent.
   * @return the value of the property
   */
  public UniqueId getParentPositionId() {
    return _parentPositionId;
  }

  /**
   * Sets the unique identifier of the parent position, null if no parent.
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
   * Gets the number of units in the trade.
   * @return the value of the property, not null
   */
  public BigDecimal getQuantity() {
    return _quantity;
  }

  /**
   * Sets the number of units in the trade.
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
  public SecurityLink getSecurityLink() {
    return _securityLink;
  }

  /**
   * Sets the link referencing the security, not null.
   * This may also hold the resolved security.
   * @param securityLink  the new value of the property, not null
   */
  public void setSecurityLink(SecurityLink securityLink) {
    JodaBeanUtils.notNull(securityLink, "securityLink");
    this._securityLink = securityLink;
  }

  /**
   * Gets the the {@code securityLink} property.
   * This may also hold the resolved security.
   * @return the property, not null
   */
  public final Property<SecurityLink> securityLink() {
    return metaBean().securityLink().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the counterparty.
   * @return the value of the property
   */
  public Counterparty getCounterparty() {
    return _counterparty;
  }

  /**
   * Sets the counterparty.
   * @param counterparty  the new value of the property
   */
  public void setCounterparty(Counterparty counterparty) {
    this._counterparty = counterparty;
  }

  /**
   * Gets the the {@code counterparty} property.
   * @return the property, not null
   */
  public final Property<Counterparty> counterparty() {
    return metaBean().counterparty().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the trade date.
   * @return the value of the property
   */
  public LocalDate getTradeDate() {
    return _tradeDate;
  }

  /**
   * Sets the trade date.
   * @param tradeDate  the new value of the property
   */
  public void setTradeDate(LocalDate tradeDate) {
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
   * @param attributes  the new value of the property
   */
  public void setAttributes(Map<String, String> attributes) {
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
   * The meta-bean for {@code SimpleTrade}.
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
        this, "uniqueId", SimpleTrade.class, UniqueId.class);
    /**
     * The meta-property for the {@code parentPositionId} property.
     */
    private final MetaProperty<UniqueId> _parentPositionId = DirectMetaProperty.ofReadWrite(
        this, "parentPositionId", SimpleTrade.class, UniqueId.class);
    /**
     * The meta-property for the {@code quantity} property.
     */
    private final MetaProperty<BigDecimal> _quantity = DirectMetaProperty.ofReadWrite(
        this, "quantity", SimpleTrade.class, BigDecimal.class);
    /**
     * The meta-property for the {@code securityLink} property.
     */
    private final MetaProperty<SecurityLink> _securityLink = DirectMetaProperty.ofReadWrite(
        this, "securityLink", SimpleTrade.class, SecurityLink.class);
    /**
     * The meta-property for the {@code counterparty} property.
     */
    private final MetaProperty<Counterparty> _counterparty = DirectMetaProperty.ofReadWrite(
        this, "counterparty", SimpleTrade.class, Counterparty.class);
    /**
     * The meta-property for the {@code tradeDate} property.
     */
    private final MetaProperty<LocalDate> _tradeDate = DirectMetaProperty.ofReadWrite(
        this, "tradeDate", SimpleTrade.class, LocalDate.class);
    /**
     * The meta-property for the {@code tradeTime} property.
     */
    private final MetaProperty<OffsetTime> _tradeTime = DirectMetaProperty.ofReadWrite(
        this, "tradeTime", SimpleTrade.class, OffsetTime.class);
    /**
     * The meta-property for the {@code premium} property.
     */
    private final MetaProperty<Double> _premium = DirectMetaProperty.ofReadWrite(
        this, "premium", SimpleTrade.class, Double.class);
    /**
     * The meta-property for the {@code premiumCurrency} property.
     */
    private final MetaProperty<Currency> _premiumCurrency = DirectMetaProperty.ofReadWrite(
        this, "premiumCurrency", SimpleTrade.class, Currency.class);
    /**
     * The meta-property for the {@code premiumDate} property.
     */
    private final MetaProperty<LocalDate> _premiumDate = DirectMetaProperty.ofReadWrite(
        this, "premiumDate", SimpleTrade.class, LocalDate.class);
    /**
     * The meta-property for the {@code premiumTime} property.
     */
    private final MetaProperty<OffsetTime> _premiumTime = DirectMetaProperty.ofReadWrite(
        this, "premiumTime", SimpleTrade.class, OffsetTime.class);
    /**
     * The meta-property for the {@code attributes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<String, String>> _attributes = DirectMetaProperty.ofReadWrite(
        this, "attributes", SimpleTrade.class, (Class) Map.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "parentPositionId",
        "quantity",
        "securityLink",
        "counterparty",
        "tradeDate",
        "tradeTime",
        "premium",
        "premiumCurrency",
        "premiumDate",
        "premiumTime",
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
        case -1651301782:  // counterparty
          return _counterparty;
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
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SimpleTrade> builder() {
      return new DirectBeanBuilder<SimpleTrade>(new SimpleTrade());
    }

    @Override
    public Class<? extends SimpleTrade> beanType() {
      return SimpleTrade.class;
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
    public final MetaProperty<SecurityLink> securityLink() {
      return _securityLink;
    }

    /**
     * The meta-property for the {@code counterparty} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Counterparty> counterparty() {
      return _counterparty;
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

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
