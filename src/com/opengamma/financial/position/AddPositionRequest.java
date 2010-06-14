/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import java.math.BigDecimal;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A request to add a position.
 */
public final class AddPositionRequest {

  /**
   * The parent node unique identifier.
   */
  private UniqueIdentifier _parentUid;
  /**
   * The quantity of the position.
   */
  private BigDecimal _quantity;
  /**
   * The security.
   */
  private IdentifierBundle _securityKey;

  /**
   * Creates an instance.
   */
  public AddPositionRequest() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the parent node unique identifier.
   * @return the unique identifier, not null
   */
  public UniqueIdentifier getParentNode() {
    return _parentUid;
  }

  /**
   * Sets the parent node unique identifier.
   * @param parentUid  the unique identifier, not null
   */
  public void setParentNode(UniqueIdentifier parentUid) {
    ArgumentChecker.notNull(parentUid, "UniqueIdentifier");
    _parentUid = parentUid;
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
   * Validates this request throwing an exception if not.
   */
  public void checkValid() {
    Validate.notNull(getParentNode(), "Parent must not be null");
    Validate.notNull(getQuantity(), "Quantity must not be null");
    Validate.notNull(getSecurityKey(), "Security key must not be null");
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
