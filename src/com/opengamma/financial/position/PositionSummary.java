/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * A summary of a position.
 */
public final class PositionSummary {

  /**
   * The parent node unique identifier.
   */
  private UniqueIdentifier _parentNodeUid;
  /**
   * The unique identifier.
   */
  private UniqueIdentifier _uid;
  /**
   * The amount of the position.
   */
  private BigDecimal _quantity;
  /**
   * The identifiers specifying the security.
   */
  private IdentifierBundle _securityKey;
  /**
   * The status, true if active, false if deleted.
   */
  private boolean _active = true;

  /**
   * Creates an instance.
   */
  public PositionSummary() {
  }

  /**
   * Creates an instance.
   * @param uid  the unique identifier, not null
   */
  public PositionSummary(UniqueIdentifier uid) {
    setUniqueIdentifier(uid);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the parent node unique identifier of the position.
   * @return the parent node unique identifier
   */
  public UniqueIdentifier getParentNode() {
    return _parentNodeUid;
  }

  /**
   * Sets the parent node unique identifier of the position.
   * @param uid  the parent node unique identifier
   */
  public void setParentNode(UniqueIdentifier uid) {
    _parentNodeUid = uid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the unique identifier of the position.
   * @return the unique identifier
   */
  public UniqueIdentifier getUniqueIdentifier() {
    return _uid;
  }

  /**
   * Sets the unique identifier of the position.
   * @param uid  the unique identifier
   */
  public void setUniqueIdentifier(UniqueIdentifier uid) {
    _uid = uid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the amount of the position held in terms of the security.
   * @return the amount of the position
   */
  public BigDecimal getQuantity() {
    return _quantity;
  }

  /**
   * Sets the amount of the position held in terms of the security.
   * @param quantity  the amount of the position
   */
  public void setQuantity(BigDecimal quantity) {
    _quantity = quantity;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a key to the security being held.
   * <p>
   * This allows the security to be referenced without actually loading the security itself.
   * @return the security key
   */
  public IdentifierBundle getSecurityKey() {
    return _securityKey;
  }

  /**
   * Sets the key to the security being held.
   * @param securityKey  the security key
   */
  public void setSecurityKey(IdentifierBundle securityKey) {
    _securityKey = securityKey;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the status.
   * @return the status, true if active, false if deleted
   */
  public boolean isActive() {
    return _active;
  }

  /**
   * Sets the status.
   * @param active  the status, true if active, false if deleted
   */
  public void setActive(boolean active) {
    _active = active;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
