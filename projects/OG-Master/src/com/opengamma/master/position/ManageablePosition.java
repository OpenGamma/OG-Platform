/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.beans.BeanDefinition;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.BasicMetaBean;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaProperty;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
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
   * The position name, not null.
   */
  @PropertyDefinition(get = "manual", set = "")
  @SuppressWarnings("unused")
  private String _name = "";
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
  public String getName() {
    if (getQuantity() != null && getSecurityKey() != null && getSecurityKey().size() > 0) {
      if (getSecurityKey().getIdentifier(SecurityUtils.BLOOMBERG_TICKER) != null) {
        return getQuantity() + " " + getSecurityKey().getIdentifier(SecurityUtils.BLOOMBERG_TICKER);
      } else if (getSecurityKey().getIdentifier(SecurityUtils.RIC) != null) {
        return getQuantity() + " " + getSecurityKey().getIdentifier(SecurityUtils.RIC);
      } else if (getSecurityKey().getIdentifier(SecurityUtils.ACTIVFEED_TICKER) != null) {
        return getQuantity() + " " + getSecurityKey().getIdentifier(SecurityUtils.ACTIVFEED_TICKER);
      } else {
        return getQuantity() + " " + getSecurityKey().getIdentifiers().iterator().next().getValue();
      }
    }
    return getUniqueId() != null ? getUniqueId().toLatest().toString() : "";
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if any trade object identifier matches one in the specified list.
   * 
   * @param objectIds  the object identifiers to match against, not null
   * @return true if at least one identifier matches
   */
  public boolean matchesAny(List<ObjectIdentifier> objectIds) {
    ArgumentChecker.notNull(objectIds, "objectIds");
    for (ManageableTrade trade : getTrades()) {
      if (objectIds.contains(trade.getUniqueId().getObjectId())) {
        return true;
      }
    }
    return false;
  }

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
      case 3373707:  // name
        return getName();
      case -1285004149:  // quantity
        return getQuantity();
      case 1550083839:  // securityKey
        return getSecurityKey();
      case -865715313:  // trades
        return getTrades();
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
      case 3373707:  // name
        throw new UnsupportedOperationException("Property cannot be written: name");
      case -1285004149:  // quantity
        setQuantity((BigDecimal) newValue);
        return;
      case 1550083839:  // securityKey
        setSecurityKey((IdentifierBundle) newValue);
        return;
      case -865715313:  // trades
        setTrades((List<ManageableTrade>) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
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
   * Gets the the {@code name} property.
   * @return the property, not null
   */
  public final Property<String> name() {
    return metaBean().name().createProperty(this);
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
   * The meta-bean for {@code ManageablePosition}.
   */
  public static class Meta extends BasicMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueIdentifier> _uniqueId = DirectMetaProperty.ofReadWrite(this, "uniqueId", UniqueIdentifier.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadOnly(this, "name", String.class);
    /**
     * The meta-property for the {@code quantity} property.
     */
    private final MetaProperty<BigDecimal> _quantity = DirectMetaProperty.ofReadWrite(this, "quantity", BigDecimal.class);
    /**
     * The meta-property for the {@code securityKey} property.
     */
    private final MetaProperty<IdentifierBundle> _securityKey = DirectMetaProperty.ofReadWrite(this, "securityKey", IdentifierBundle.class);
    /**
     * The meta-property for the {@code trades} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ManageableTrade>> _trades = DirectMetaProperty.ofReadWrite(this, "trades", (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map;

    @SuppressWarnings({"unchecked", "rawtypes" })
    protected Meta() {
      LinkedHashMap temp = new LinkedHashMap();
      temp.put("uniqueId", _uniqueId);
      temp.put("name", _name);
      temp.put("quantity", _quantity);
      temp.put("securityKey", _securityKey);
      temp.put("trades", _trades);
      _map = Collections.unmodifiableMap(temp);
    }

    @Override
    public ManageablePosition createBean() {
      return new ManageablePosition();
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
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
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

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
