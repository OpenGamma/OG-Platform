/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.text.StrBuilder;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.core.security.SecurityLinkUtils;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple mutable implementation of {@code Position}.
 */
public class PositionImpl implements Position, MutableUniqueIdentifiable, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The unique identifier of the position.
   */
  private UniqueId _uniqueId;
  /**
   * The unique identifier of the parent node.
   */
  private UniqueId _parentNodeId;
  /**
   * The amount of the position.
   */
  private BigDecimal _quantity;
  /**
   * The link to the security.
   */
  private SecurityLink _securityLink;
  /**
   * The sets of trades that make up the position.
   */
  private final Set<Trade> _trades = Sets.newHashSet();
  /**
   * The trade attributes
   */
  private Map<String, String> _attributes = Maps.newHashMap();

  /**
   * Construct an empty instance that must be populated via setters.
   */
  public PositionImpl() {
    _securityLink = new SimpleSecurityLink();
  }

  /**
   * Creates a position from an amount of a security identified by key.
   * 
   * @param quantity  the amount of the position, not null
   * @param securityKey  the security identifier, not null
   */
  public PositionImpl(BigDecimal quantity, ExternalId securityKey) {
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
  public PositionImpl(BigDecimal quantity, ExternalIdBundle securityKey) {
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
  public PositionImpl(UniqueId uniqueId, BigDecimal quantity, ExternalId securityKey) {
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
  public PositionImpl(UniqueId uniqueId, BigDecimal quantity, ExternalIdBundle securityKey) {
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
  public PositionImpl(UniqueId uniqueId, BigDecimal quantity, Security security) {
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
  public PositionImpl(final Position copyFrom) {
    ArgumentChecker.notNull(copyFrom, "copyFrom");
    _uniqueId = copyFrom.getUniqueId();
    _parentNodeId = copyFrom.getParentNodeId();
    _quantity = copyFrom.getQuantity();
    _securityLink = new SimpleSecurityLink(copyFrom.getSecurityLink());
    for (Trade trade : copyFrom.getTrades()) {
      TradeImpl clonedTrade = new TradeImpl(trade);
      clonedTrade.setParentPositionId(_uniqueId);
      _trades.add(clonedTrade);
    }
    setAttributes(copyFrom.getAttributes());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the unique identifier of the position.
   * 
   * @return the position unique identifier, not null
   */
  @Override
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier of the position.
   * 
   * @param uniqueId  the new identifier, not null
   */
  public void setUniqueId(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    _uniqueId = uniqueId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the unique identifier of the parent node.
   * 
   * @return the parent node unique identifier, null if not connected to a node
   */
  @Override
  public UniqueId getParentNodeId() {
    return _parentNodeId;
  }

  /**
   * Sets the unique identifier of the parent node.
   * 
   * @param parentNodeId  the parent node unique identifier, null if not connected to a node
   */
  public void setParentNodeId(final UniqueId parentNodeId) {
    _parentNodeId = parentNodeId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the amount of the position held in terms of the security.
   * 
   * @return the amount of the position, not null
   */
  @Override
  public BigDecimal getQuantity() {
    return _quantity;
  }

  /**
   * Sets the amount of the position held in terms of the security.
   * 
   * @param quantity  the amount of the position, not null
   */
  public void setQuantity(BigDecimal quantity) {
    ArgumentChecker.notNull(quantity, "quantity");
    _quantity = quantity;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the link to the security being held.
   * <p>
   * This allows the security to be referenced without actually loading the security itself.
   * 
   * @return the security link, not null
   */
  @Override
  public SecurityLink getSecurityLink() {
    return _securityLink;
  }

  /**
   * Sets the link to the security being held.
   * 
   * @param securityLink  the security link, not null
   */
  public void setSecurityLink(SecurityLink securityLink) {
    ArgumentChecker.notNull(securityLink, "securityLink");
    _securityLink = securityLink;
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
   * Gets the set of trades the makes up this position if available.
   * 
   * @return the trades, not null
   */
  @Override
  public Set<Trade> getTrades() {
    return Collections.unmodifiableSet(_trades);
  }

  /**
   * Sets the trades, replacing the current set of trades.
   * 
   * @param trades  the trades to set, replacing any previous trades, not null
   */
  public void setTrades(Set<Trade> trades) {
    ArgumentChecker.notNull(trades, "trades");
    _trades.clear();
    _trades.addAll(trades);
  }

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
  @Override
  public Map<String, String> getAttributes() {
    return Collections.unmodifiableMap(_attributes);
  }
  
  /**
   * Add a position attribute
   * 
   * @param key attribute key, not null
   * @param value attribute value, not null
   */
  public void addAttribute(String key, String value) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(value, "value");
    _attributes.put(key, value);
  }

  /**
   * Add all attributes from a given Map
   *  
   * @param attributes the attributes map, not null
   */
  public void setAttributes(Map<String, String> attributes) {
    ArgumentChecker.notNull(attributes, "attributes");
    clearAttributes();
    for (Entry<String, String> entry : attributes.entrySet()) {
      addAttribute(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Remove all attributes
   */
  public void clearAttributes() {
    _attributes.clear();
  }

  /**
   * Removes an attribute with given key
   * 
   * @param key the attribute key to remove, not null
   */
  public void removeAttribute(final String key) {
    ArgumentChecker.notNull(key, "key");
    _attributes.remove(key);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof PositionImpl) {
      PositionImpl other = (PositionImpl) obj;
      return new EqualsBuilder().append(getQuantity(), other.getQuantity())
          .append(getSecurityLink(), other.getSecurityLink())
          .append(getTrades(), other.getTrades())
          .append(getParentNodeId(), other.getParentNodeId())
          .append(getAttributes(), other.getAttributes())
          .isEquals();
    }
    return false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(getQuantity())
        .append(getSecurityLink())
        .append(getTrades())
        .append(getParentNodeId())
        .append(getAttributes())
        .toHashCode();
  }

  @Override
  public String toString() {
    return new StrBuilder(128)
        .append("Position[")
        .append(getUniqueId())
        .append(", ")
        .append(getQuantity())
        .append(' ')
        .append(SecurityLinkUtils.best(getSecurityLink()))
        .append(']')
        .toString();
  }

}
