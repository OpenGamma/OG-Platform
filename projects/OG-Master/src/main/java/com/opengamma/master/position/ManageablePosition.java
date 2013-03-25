/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.DerivedProperty;
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
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.JdkUtils;
import com.opengamma.util.PublicSPI;

/**
 * A position held in a position master.
 * <p>
 * A position is fundamentally a quantity of a security.
 * For example, a position might be 50 shares of OpenGamma.
 * <p>
 * Positions are formed from a set of trades, however trade data may not always be available or complete.
 * Even if trade data is available, the position details cannot necessarily be derived from the trades.
 * Therefore the position holds the quantity and security reference directly, separately
 * from the underlying trades.
 * <p>
 * Positions are logically attached to nodes in the portfolio tree, however they are
 * stored and returned separately from the position master.
 */
@PublicSPI
@BeanDefinition
public class ManageablePosition extends DirectBean
    implements MutableUniqueIdentifiable, UniqueIdentifiable, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The unique identifier of the position.
   * This must be null when adding to a master and not null when retrieved from a master.
   */
  @PropertyDefinition
  private UniqueId _uniqueId;
  /**
   * The number of units in the position.
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
   * The trades that the make up the position, not null.
   * An empty list usually means that trade data is unavailable.
   */
  @PropertyDefinition
  private final List<ManageableTrade> _trades = new ArrayList<ManageableTrade>();
  /**
   * The general purpose position attributes.
   * These can be used to add arbitrary additional information to the object
   * and for aggregating in portfolios.
   */
  @PropertyDefinition(validate = "notNull")
  private final Map<String, String> _attributes = Maps.newHashMap();
  /**
   * The provider external identifier for the data.
   * This optional field can be used to capture the identifier used by the data provider.
   * This can be useful when receiving updates from the same provider.
   */
  @PropertyDefinition
  private ExternalId _providerId;

  /**
   * Construct an empty instance that must be populated via setters.
   */
  public ManageablePosition() {
    _securityLink = new ManageableSecurityLink();
  }

  /**
   * Creates a position from an amount of a security.
   * 
   * @param quantity  the amount of the position, not null
   * @param securityId  the security identifier, not null
   */
  public ManageablePosition(final BigDecimal quantity, final ExternalId securityId) {
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(securityId, "securityId");
    _quantity = quantity;
    _securityLink = new ManageableSecurityLink(securityId);
  }

  /**
   * Creates a position from an amount of a security.
   * 
   * @param quantity  the amount of the position, not null
   * @param securityId  the security bundle, not null
   */
  public ManageablePosition(final BigDecimal quantity, final ExternalIdBundle securityId) {
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(securityId, "securityId");
    _quantity = quantity;
    _securityLink = new ManageableSecurityLink(securityId);
  }
  
  /**
   * Creates a deep copy of the specified position.
   * 
   * @param copyFrom  the position to copy from, not null
   */
  public ManageablePosition(final ManageablePosition copyFrom) {
    ArgumentChecker.notNull(copyFrom, "position");
    _uniqueId = copyFrom.getUniqueId();
    _quantity = copyFrom.getQuantity();
    _providerId = copyFrom.getProviderId();
    _securityLink = copyFrom.getSecurityLink();
    if (copyFrom.getAttributes() != null) {
      for (Entry<String, String> entry : copyFrom.getAttributes().entrySet()) {
        addAttribute(entry.getKey(), entry.getValue());
      }
    }
    if (copyFrom.getTrades() != null) {
      for (ManageableTrade trade : copyFrom.getTrades()) {
        addTrade(JodaBeanUtils.clone(trade));
      }
    }
  }

  /**
   * Creates a populated instance (no trades or attributes).
   *
   * @param uniqueId    the position unique identifier, may be null
   * @param quantity    the amount of the position, not null
   * @param securityId  the security identifier, not null
   */
  public ManageablePosition(UniqueId uniqueId, BigDecimal quantity, ExternalIdBundle securityId) {
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(securityId, "securityId");
    setUniqueId(uniqueId);
    setQuantity(quantity);
    _securityLink = new ManageableSecurityLink(securityId);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a trade to the list.
   * 
   * @param trade  the trade to add, not null
   */
  public void addTrade(final ManageableTrade trade) {
    ArgumentChecker.notNull(trade, "trade");
    getTrades().add(trade);
  }

  /**
   * Gets a suitable name for the position.
   * 
   * @return the name, not null
   */
  @DerivedProperty
  public String getName() {
    String bestName = getSecurityLink().getBestName();
    if (getQuantity() != null && bestName.length() > 0) {
      return JdkUtils.stripTrailingZeros(getQuantity()).toPlainString() + " x " + bestName;
    }
    return getUniqueId() != null ? getUniqueId().getObjectId().toString() : "";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a trade from the list by object identifier.
   * 
   * @param tradeObjectId  the trade object identifier, not null
   * @return the trade with the identifier, null if not found
   */
  public ManageableTrade getTrade(final ObjectIdentifiable tradeObjectId) {
    ArgumentChecker.notNull(tradeObjectId, "tradeObjectId");
    ObjectId objectId = tradeObjectId.getObjectId();
    for (ManageableTrade trade : getTrades()) {
      if (trade.getUniqueId().equalObjectId(objectId)) {
        return trade;
      }
    }
    return null;
  }
  /**
   * Checks if any trade object identifier matches one in the specified list.
   * 
   * @param objectIds  the object identifiers to match against, not null
   * @return true if at least one identifier matches
   */
  public boolean matchesAnyTrade(List<ObjectId> objectIds) {
    ArgumentChecker.notNull(objectIds, "objectIds");
    for (ManageableTrade trade : getTrades()) {
      if (objectIds.contains(trade.getUniqueId().getObjectId())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if any trade provider key matches.
   * 
   * @param tradeProviderId  the trade provider key to match against, not null
   * @return true if the key matches
   */
  public boolean matchesAnyTradeProviderId(ExternalId tradeProviderId) {
    ArgumentChecker.notNull(tradeProviderId, "tradeProviderId");
    for (ManageableTrade trade : getTrades()) {
      if (tradeProviderId.equals(trade.getProviderId())) {
        return true;
      }
    }
    return false;
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
  /**
   * Gets the resolved security from the link.
   * 
   * @return the security from the link, null if not resolve
   */
  public Security getSecurity() {
    return _securityLink.getTarget();
  }

  //-------------------------------------------------------------------------
  /**
   * Converts this position to an object implementing the Position interface.
   * <p>
   * The interface contains different data to this class due to database design.
   * 
   * @return the security from the link, null if not resolve
   */
  public SimplePosition toPosition() {
    SimplePosition sp = new SimplePosition();
    sp.setUniqueId(this.getUniqueId());
    sp.setQuantity(this.getQuantity());
    sp.setSecurityLink(this.getSecurityLink());
    sp.getTrades().addAll(getTrades());
    sp.setAttributes(this.getAttributes());
    
    // Workaround for PLAT-2371 until PLAT-2286
    if (this.getProviderId() != null) {
      sp.addAttribute(this.providerId().name(), this.getProviderId().toString());
    }
    
    return sp;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ManageablePosition}.
   * @return the meta-bean, not null
   */
  public static ManageablePosition.Meta meta() {
    return ManageablePosition.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(ManageablePosition.Meta.INSTANCE);
  }

  @Override
  public ManageablePosition.Meta metaBean() {
    return ManageablePosition.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        return getUniqueId();
      case -1285004149:  // quantity
        return getQuantity();
      case 807992154:  // securityLink
        return getSecurityLink();
      case -865715313:  // trades
        return getTrades();
      case 405645655:  // attributes
        return getAttributes();
      case 205149932:  // providerId
        return getProviderId();
      case 3373707:  // name
        return getName();
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
      case -1285004149:  // quantity
        setQuantity((BigDecimal) newValue);
        return;
      case 807992154:  // securityLink
        setSecurityLink((ManageableSecurityLink) newValue);
        return;
      case -865715313:  // trades
        setTrades((List<ManageableTrade>) newValue);
        return;
      case 405645655:  // attributes
        setAttributes((Map<String, String>) newValue);
        return;
      case 205149932:  // providerId
        setProviderId((ExternalId) newValue);
        return;
      case 3373707:  // name
        if (quiet) {
          return;
        }
        throw new UnsupportedOperationException("Property cannot be written: name");
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
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
      ManageablePosition other = (ManageablePosition) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(getQuantity(), other.getQuantity()) &&
          JodaBeanUtils.equal(getSecurityLink(), other.getSecurityLink()) &&
          JodaBeanUtils.equal(getTrades(), other.getTrades()) &&
          JodaBeanUtils.equal(getAttributes(), other.getAttributes()) &&
          JodaBeanUtils.equal(getProviderId(), other.getProviderId()) &&
          JodaBeanUtils.equal(getName(), other.getName());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getQuantity());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecurityLink());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTrades());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAttributes());
    hash += hash * 31 + JodaBeanUtils.hashCode(getProviderId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getName());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unique identifier of the position.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @return the value of the property
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier of the position.
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
   * Gets the number of units in the position.
   * This field must not be null for the object to be valid.
   * @return the value of the property
   */
  public BigDecimal getQuantity() {
    return _quantity;
  }

  /**
   * Sets the number of units in the position.
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
   * Gets the trades that the make up the position, not null.
   * An empty list usually means that trade data is unavailable.
   * @return the value of the property
   */
  public List<ManageableTrade> getTrades() {
    return _trades;
  }

  /**
   * Sets the trades that the make up the position, not null.
   * An empty list usually means that trade data is unavailable.
   * @param trades  the new value of the property
   */
  public void setTrades(List<ManageableTrade> trades) {
    this._trades.clear();
    this._trades.addAll(trades);
  }

  /**
   * Gets the the {@code trades} property.
   * An empty list usually means that trade data is unavailable.
   * @return the property, not null
   */
  public final Property<List<ManageableTrade>> trades() {
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
   * Gets the provider external identifier for the data.
   * This optional field can be used to capture the identifier used by the data provider.
   * This can be useful when receiving updates from the same provider.
   * @return the value of the property
   */
  public ExternalId getProviderId() {
    return _providerId;
  }

  /**
   * Sets the provider external identifier for the data.
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
   * Gets the the {@code name} property.
   * 
   * @return the property, not null
   */
  public final Property<String> name() {
    return metaBean().name().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ManageablePosition}.
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
        this, "uniqueId", ManageablePosition.class, UniqueId.class);
    /**
     * The meta-property for the {@code quantity} property.
     */
    private final MetaProperty<BigDecimal> _quantity = DirectMetaProperty.ofReadWrite(
        this, "quantity", ManageablePosition.class, BigDecimal.class);
    /**
     * The meta-property for the {@code securityLink} property.
     */
    private final MetaProperty<ManageableSecurityLink> _securityLink = DirectMetaProperty.ofReadWrite(
        this, "securityLink", ManageablePosition.class, ManageableSecurityLink.class);
    /**
     * The meta-property for the {@code trades} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ManageableTrade>> _trades = DirectMetaProperty.ofReadWrite(
        this, "trades", ManageablePosition.class, (Class) List.class);
    /**
     * The meta-property for the {@code attributes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<String, String>> _attributes = DirectMetaProperty.ofReadWrite(
        this, "attributes", ManageablePosition.class, (Class) Map.class);
    /**
     * The meta-property for the {@code providerId} property.
     */
    private final MetaProperty<ExternalId> _providerId = DirectMetaProperty.ofReadWrite(
        this, "providerId", ManageablePosition.class, ExternalId.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadOnly(
        this, "name", ManageablePosition.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "quantity",
        "securityLink",
        "trades",
        "attributes",
        "providerId",
        "name");

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
        case -1285004149:  // quantity
          return _quantity;
        case 807992154:  // securityLink
          return _securityLink;
        case -865715313:  // trades
          return _trades;
        case 405645655:  // attributes
          return _attributes;
        case 205149932:  // providerId
          return _providerId;
        case 3373707:  // name
          return _name;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ManageablePosition> builder() {
      return new DirectBeanBuilder<ManageablePosition>(new ManageablePosition());
    }

    @Override
    public Class<? extends ManageablePosition> beanType() {
      return ManageablePosition.class;
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
     * The meta-property for the {@code trades} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ManageableTrade>> trades() {
      return _trades;
    }

    /**
     * The meta-property for the {@code attributes} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<String, String>> attributes() {
      return _attributes;
    }

    /**
     * The meta-property for the {@code providerId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> providerId() {
      return _providerId;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------

}
