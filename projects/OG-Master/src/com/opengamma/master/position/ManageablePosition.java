/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.id.UniqueIdentifier;
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
public class ManageablePosition extends DirectBean implements MutableUniqueIdentifiable {

  /**
   * The position unique identifier.
   * This must be null when adding to a master and not null when retrieved from a master.
   */
  @PropertyDefinition
  private UniqueIdentifier _uniqueId;
  /**
   * The quantity.
   * This field must not be null for the object to be valid.
   */
  @PropertyDefinition
  private BigDecimal _quantity;
  /**
   * The security key identifier bundle specifying the security.
   * This field must not be null for the object to be valid.
   */
  @PropertyDefinition
  private IdentifierBundle _securityKey;
  /**
   * The trades that the make up the position, not null.
   * An empty list usually means that trade data is unavailable.
   */
  @PropertyDefinition
  private final List<ManageableTrade> _trades = new ArrayList<ManageableTrade>();
  /**
   * The provider key identifier for the data.
   * This optional field can be used to capture the identifier used by the data provider.
   * This can be useful when receiving updates from the same provider.
   */
  @PropertyDefinition
  private Identifier _providerKey;
  
  /**
   * Position attributes used for aggregation
   */
  @PropertyDefinition
  private final Map<String, String> _attributes = new HashMap<String, String>();

  /**
   * Creates an instance.
   */
  public ManageablePosition() {
  }

  /**
   * Creates a position from an amount of a security identified by key.
   * @param quantity  the amount of the position, not null
   * @param securityKey  the security identifier, not null
   */
  public ManageablePosition(final BigDecimal quantity, final Identifier securityKey) {
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(securityKey, "securityKey");
    _quantity = quantity;
    _securityKey = IdentifierBundle.of(securityKey);
  }

  /**
   * Creates a position from an amount of a security identified by key.
   * @param quantity  the amount of the position, not null
   * @param securityKey  the security identifier, not null
   */
  public ManageablePosition(final BigDecimal quantity, final IdentifierBundle securityKey) {
    ArgumentChecker.notNull(quantity, "quantity");
    ArgumentChecker.notNull(securityKey, "securityKey");
    _quantity = quantity;
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

  /**
   * Adds a trade to the list.
   * @param trade  the trade to add, not null
   */
  public void addTrade(final ManageableTrade trade) {
    ArgumentChecker.notNull(trade, "trade");
    getTrades().add(trade);
  }

  /**
   * Gets a suitable name for the position.
   * @return the name, not null
   */
  @DerivedProperty
  public String getName() {
    if (getQuantity() != null && getSecurityKey() != null && getSecurityKey().size() > 0) {
      final String amount = JdkUtils.stripTrailingZeros(getQuantity()).toPlainString() + " x ";
      if (getSecurityKey().getIdentifierValue(SecurityUtils.BLOOMBERG_TICKER) != null) {
        return amount + getSecurityKey().getIdentifierValue(SecurityUtils.BLOOMBERG_TICKER);
      } else if (getSecurityKey().getIdentifierValue(SecurityUtils.RIC) != null) {
        return amount + getSecurityKey().getIdentifierValue(SecurityUtils.RIC);
      } else if (getSecurityKey().getIdentifierValue(SecurityUtils.ACTIVFEED_TICKER) != null) {
        return amount + getSecurityKey().getIdentifierValue(SecurityUtils.ACTIVFEED_TICKER);
      } else {
        return amount + getSecurityKey().getIdentifiers().iterator().next().getValue();
      }
    }
    return getUniqueId() != null ? getUniqueId().toLatest().toString() : "";
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
    ObjectIdentifier objectId = tradeObjectId.getObjectId();
    for (ManageableTrade trade : getTrades()) {
      if (getUniqueId().equalObjectIdentifier(objectId)) {
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
  public boolean matchesAnyTrade(List<ObjectIdentifier> objectIds) {
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
   * @param tradeProviderKey  the trade provider key to match against, not null
   * @return true if the key matches
   */
  public boolean matchesAnyTradeProviderKey(Identifier tradeProviderKey) {
    ArgumentChecker.notNull(tradeProviderKey, "tradeProviderKey");
    for (ManageableTrade trade : getTrades()) {
      if (tradeProviderKey.equals(trade.getProviderKey())) {
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

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ManageablePosition}.
   * @return the meta-bean, not null
   */
  public static ManageablePosition.Meta meta() {
    return ManageablePosition.Meta.INSTANCE;
  }

  @Override
  public ManageablePosition.Meta metaBean() {
    return ManageablePosition.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        return getUniqueId();
      case -1285004149:  // quantity
        return getQuantity();
      case 1550083839:  // securityKey
        return getSecurityKey();
      case -865715313:  // trades
        return getTrades();
      case 2064682670:  // providerKey
        return getProviderKey();
      case 405645655:  // attributes
        return getAttributes();
      case 3373707:  // name
        return getName();
    }
    return super.propertyGet(propertyName);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        setUniqueId((UniqueIdentifier) newValue);
        return;
      case -1285004149:  // quantity
        setQuantity((BigDecimal) newValue);
        return;
      case 1550083839:  // securityKey
        setSecurityKey((IdentifierBundle) newValue);
        return;
      case -865715313:  // trades
        setTrades((List<ManageableTrade>) newValue);
        return;
      case 2064682670:  // providerKey
        setProviderKey((Identifier) newValue);
        return;
      case 405645655:  // attributes
        setAttributes((Map<String, String>) newValue);
        return;
      case 3373707:  // name
        throw new UnsupportedOperationException("Property cannot be written: name");
    }
    super.propertySet(propertyName, newValue);
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
          JodaBeanUtils.equal(getSecurityKey(), other.getSecurityKey()) &&
          JodaBeanUtils.equal(getTrades(), other.getTrades()) &&
          JodaBeanUtils.equal(getProviderKey(), other.getProviderKey()) &&
          JodaBeanUtils.equal(getAttributes(), other.getAttributes()) &&
          JodaBeanUtils.equal(getName(), other.getName());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getQuantity());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSecurityKey());
    hash += hash * 31 + JodaBeanUtils.hashCode(getTrades());
    hash += hash * 31 + JodaBeanUtils.hashCode(getProviderKey());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAttributes());
    hash += hash * 31 + JodaBeanUtils.hashCode(getName());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the position unique identifier.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @return the value of the property
   */
  public UniqueIdentifier getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the position unique identifier.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @param uniqueId  the new value of the property
   */
  public void setUniqueId(UniqueIdentifier uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @return the property, not null
   */
  public final Property<UniqueIdentifier> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
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
   * Gets the security key identifier bundle specifying the security.
   * This field must not be null for the object to be valid.
   * @return the value of the property
   */
  public IdentifierBundle getSecurityKey() {
    return _securityKey;
  }

  /**
   * Sets the security key identifier bundle specifying the security.
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
   * Gets position attributes used for aggregation
   * @return the value of the property
   */
  public Map<String, String> getAttributes() {
    return _attributes;
  }

  /**
   * Sets position attributes used for aggregation
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
   * Gets the the {@code name} property.
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
    private final MetaProperty<UniqueIdentifier> _uniqueId = DirectMetaProperty.ofReadWrite(
        this, "uniqueId", ManageablePosition.class, UniqueIdentifier.class);
    /**
     * The meta-property for the {@code quantity} property.
     */
    private final MetaProperty<BigDecimal> _quantity = DirectMetaProperty.ofReadWrite(
        this, "quantity", ManageablePosition.class, BigDecimal.class);
    /**
     * The meta-property for the {@code securityKey} property.
     */
    private final MetaProperty<IdentifierBundle> _securityKey = DirectMetaProperty.ofReadWrite(
        this, "securityKey", ManageablePosition.class, IdentifierBundle.class);
    /**
     * The meta-property for the {@code trades} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ManageableTrade>> _trades = DirectMetaProperty.ofReadWrite(
        this, "trades", ManageablePosition.class, (Class) List.class);
    /**
     * The meta-property for the {@code providerKey} property.
     */
    private final MetaProperty<Identifier> _providerKey = DirectMetaProperty.ofReadWrite(
        this, "providerKey", ManageablePosition.class, Identifier.class);
    /**
     * The meta-property for the {@code attributes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<String, String>> _attributes = DirectMetaProperty.ofReadWrite(
        this, "attributes", ManageablePosition.class, (Class) Map.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadOnly(
        this, "name", ManageablePosition.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "quantity",
        "securityKey",
        "trades",
        "providerKey",
        "attributes",
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
        case 1550083839:  // securityKey
          return _securityKey;
        case -865715313:  // trades
          return _trades;
        case 2064682670:  // providerKey
          return _providerKey;
        case 405645655:  // attributes
          return _attributes;
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
     * The meta-property for the {@code trades} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ManageableTrade>> trades() {
      return _trades;
    }

    /**
     * The meta-property for the {@code providerKey} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Identifier> providerKey() {
      return _providerKey;
    }

    /**
     * The meta-property for the {@code attributes} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<String, String>> attributes() {
      return _attributes;
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
