/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

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
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple mutable implementation of {@code Position}.
 */
@BeanDefinition
public class SimplePosition extends DirectBean
    implements Position, MutableUniqueIdentifiable, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The unique identifier of the position.
   */
  @PropertyDefinition(validate = "notNull")
  private UniqueId _uniqueId;
  /**
   * The unique identifier of the parent node, null if no parent.
   */
  @PropertyDefinition
  private UniqueId _parentNodeId;
  /**
   * The number of units in the position.
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
   * The trades that the make up the position, not null.
   * An empty list usually means that trade data is unavailable.
   */
  @PropertyDefinition(validate = "notNull")
  private final Collection<Trade> _trades = new ArrayList<Trade>();
  /**
   * The general purpose position attributes.
   * These can be used to add arbitrary additional information to the object
   * and for aggregating in portfolios.
   */
  @PropertyDefinition(validate = "notNull")
  private final Map<String, String> _attributes = Maps.newHashMap();

  /**
   * Construct an empty instance that must be populated via setters.
   */
  public SimplePosition() {
    _securityLink = new SimpleSecurityLink();
  }

  /**
   * Creates a position from an amount of a security identified by key.
   * 
   * @param quantity  the amount of the position, not null
   * @param securityKey  the security identifier, not null
   */
  public SimplePosition(BigDecimal quantity, ExternalId securityKey) {
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(securityKey, "security key");
    _quantity = quantity;
    _securityLink = new SimpleSecurityLink(securityKey);
  }

  /**
   * Creates a position from an amount of a security identified by key.
   * 
   * @param quantity  the amount of the position, not null
   * @param securityKey  the security identifier, not null
   */
  public SimplePosition(BigDecimal quantity, ExternalIdBundle securityKey) {
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(securityKey, "security key");
    _quantity = quantity;
    _securityLink = new SimpleSecurityLink(securityKey);
  }

  /**
   * Creates a position from an amount of a security identified by key.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param quantity  the amount of the position, not null
   * @param securityKey  the security identifier, not null
   */
  public SimplePosition(UniqueId uniqueId, BigDecimal quantity, ExternalId securityKey) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(securityKey, "securityKey");
    _uniqueId = uniqueId;
    _quantity = quantity;
    _securityLink = new SimpleSecurityLink(securityKey);
  }

  /**
   * Creates a position from an amount of a security identified by key.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param quantity  the amount of the position, not null
   * @param securityKey  the security identifier, not null
   */
  public SimplePosition(UniqueId uniqueId, BigDecimal quantity, ExternalIdBundle securityKey) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(securityKey, "securityKey");
    _uniqueId = uniqueId;
    _quantity = quantity;
    _securityLink = new SimpleSecurityLink(securityKey);
  }

  /**
   * Creates a position from an amount of a security.
   * 
   * @param uniqueId  the unique identifier, not null
   * @param quantity  the amount of the position, not null
   * @param security  the security, not null
   */
  public SimplePosition(UniqueId uniqueId, BigDecimal quantity, Security security) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(security, "security");
    _uniqueId = uniqueId;
    _quantity = quantity;
    _securityLink = SimpleSecurityLink.of(security);
  }

  /**
   * Creates a deep copy of the specified position.
   * 
   * @param copyFrom  the instance to copy fields from, not null
   */
  public SimplePosition(final Position copyFrom) {
    ArgumentChecker.notNull(copyFrom, "copyFrom");
    _uniqueId = copyFrom.getUniqueId();
    _parentNodeId = copyFrom.getParentNodeId();
    _quantity = copyFrom.getQuantity();
    _securityLink = new SimpleSecurityLink(copyFrom.getSecurityLink());
    for (Trade trade : copyFrom.getTrades()) {
      SimpleTrade clonedTrade = new SimpleTrade(trade);
      clonedTrade.setParentPositionId(_uniqueId);
      _trades.add(clonedTrade);
    }
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
  /**
   * Add a trade to set of trades.
   * 
   * @param trade  the trade to add, not null
   */
  public void addTrade(Trade trade) {
    ArgumentChecker.notNull(trade, "trade");
    _trades.add(trade);
  }

  /**
   * Removes a given trade from the set of trades.
   * 
   * @param trade  the trade to remove, null ignored
   * @return true if the set of trades contained the specified trade
   */
  public boolean removeTrade(Trade trade) {
    return _trades.remove(trade);
  }

  //-------------------------------------------------------------------------
  /**
   * Add an attribute.
   * 
   * @param key  the attribute key, not null
   * @param value  the attribute value, not null
   */
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
    return new StrBuilder(128)
        .append("Position[")
        .append(getUniqueId())
        .append(", ")
        .append(getQuantity())
        .append(' ')
        .append(LinkUtils.best(getSecurityLink()))
        .append(']')
        .toString();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SimplePosition}.
   * @return the meta-bean, not null
   */
  public static SimplePosition.Meta meta() {
    return SimplePosition.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(SimplePosition.Meta.INSTANCE);
  }

  @Override
  public SimplePosition.Meta metaBean() {
    return SimplePosition.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        return getUniqueId();
      case 915246087:  // parentNodeId
        return getParentNodeId();
      case -1285004149:  // quantity
        return getQuantity();
      case 807992154:  // securityLink
        return getSecurityLink();
      case -865715313:  // trades
        return getTrades();
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
      case 915246087:  // parentNodeId
        setParentNodeId((UniqueId) newValue);
        return;
      case -1285004149:  // quantity
        setQuantity((BigDecimal) newValue);
        return;
      case 807992154:  // securityLink
        setSecurityLink((SecurityLink) newValue);
        return;
      case -865715313:  // trades
        setTrades((Collection<Trade>) newValue);
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
    JodaBeanUtils.notNull(_trades, "trades");
    JodaBeanUtils.notNull(_attributes, "attributes");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SimplePosition other = (SimplePosition) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(getParentNodeId(), other.getParentNodeId()) &&
          JodaBeanUtils.equal(getQuantity(), other.getQuantity()) &&
          JodaBeanUtils.equal(getSecurityLink(), other.getSecurityLink()) &&
          JodaBeanUtils.equal(getTrades(), other.getTrades()) &&
          JodaBeanUtils.equal(getAttributes(), other.getAttributes());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getParentNodeId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getQuantity());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecurityLink());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTrades());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAttributes());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unique identifier of the position.
   * @return the value of the property, not null
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier of the position.
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
   * Gets the unique identifier of the parent node, null if no parent.
   * @return the value of the property
   */
  public UniqueId getParentNodeId() {
    return _parentNodeId;
  }

  /**
   * Sets the unique identifier of the parent node, null if no parent.
   * @param parentNodeId  the new value of the property
   */
  public void setParentNodeId(UniqueId parentNodeId) {
    this._parentNodeId = parentNodeId;
  }

  /**
   * Gets the the {@code parentNodeId} property.
   * @return the property, not null
   */
  public final Property<UniqueId> parentNodeId() {
    return metaBean().parentNodeId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the number of units in the position.
   * @return the value of the property, not null
   */
  public BigDecimal getQuantity() {
    return _quantity;
  }

  /**
   * Sets the number of units in the position.
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
   * Gets the trades that the make up the position, not null.
   * An empty list usually means that trade data is unavailable.
   * @return the value of the property, not null
   */
  public Collection<Trade> getTrades() {
    return _trades;
  }

  /**
   * Sets the trades that the make up the position, not null.
   * An empty list usually means that trade data is unavailable.
   * @param trades  the new value of the property
   */
  public void setTrades(Collection<Trade> trades) {
    this._trades.clear();
    this._trades.addAll(trades);
  }

  /**
   * Gets the the {@code trades} property.
   * An empty list usually means that trade data is unavailable.
   * @return the property, not null
   */
  public final Property<Collection<Trade>> trades() {
    return metaBean().trades().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the general purpose position attributes.
   * These can be used to add arbitrary additional information to the object
   * and for aggregating in portfolios.
   * @return the value of the property, not null
   */
  public Map<String, String> getAttributes() {
    return _attributes;
  }

  /**
   * Sets the general purpose position attributes.
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
   * The meta-bean for {@code SimplePosition}.
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
        this, "uniqueId", SimplePosition.class, UniqueId.class);
    /**
     * The meta-property for the {@code parentNodeId} property.
     */
    private final MetaProperty<UniqueId> _parentNodeId = DirectMetaProperty.ofReadWrite(
        this, "parentNodeId", SimplePosition.class, UniqueId.class);
    /**
     * The meta-property for the {@code quantity} property.
     */
    private final MetaProperty<BigDecimal> _quantity = DirectMetaProperty.ofReadWrite(
        this, "quantity", SimplePosition.class, BigDecimal.class);
    /**
     * The meta-property for the {@code securityLink} property.
     */
    private final MetaProperty<SecurityLink> _securityLink = DirectMetaProperty.ofReadWrite(
        this, "securityLink", SimplePosition.class, SecurityLink.class);
    /**
     * The meta-property for the {@code trades} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Collection<Trade>> _trades = DirectMetaProperty.ofReadWrite(
        this, "trades", SimplePosition.class, (Class) Collection.class);
    /**
     * The meta-property for the {@code attributes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<String, String>> _attributes = DirectMetaProperty.ofReadWrite(
        this, "attributes", SimplePosition.class, (Class) Map.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "parentNodeId",
        "quantity",
        "securityLink",
        "trades",
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
        case 915246087:  // parentNodeId
          return _parentNodeId;
        case -1285004149:  // quantity
          return _quantity;
        case 807992154:  // securityLink
          return _securityLink;
        case -865715313:  // trades
          return _trades;
        case 405645655:  // attributes
          return _attributes;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SimplePosition> builder() {
      return new DirectBeanBuilder<SimplePosition>(new SimplePosition());
    }

    @Override
    public Class<? extends SimplePosition> beanType() {
      return SimplePosition.class;
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
     * The meta-property for the {@code parentNodeId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> parentNodeId() {
      return _parentNodeId;
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
     * The meta-property for the {@code trades} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Collection<Trade>> trades() {
      return _trades;
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
