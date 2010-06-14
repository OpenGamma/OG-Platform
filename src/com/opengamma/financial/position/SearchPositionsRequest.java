/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import java.math.BigDecimal;

import javax.time.Instant;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.PagingRequest;

/**
 * A request to search for positions.
 */
public final class SearchPositionsRequest {

  /**
   * The paging request.
   */
  private PagingRequest _pagingRequest = PagingRequest.ALL;
  /**
   * The node to search for.
   */
  private UniqueIdentifier _parentNodeUid;
  /**
   * The instant to search at.
   */
  private Instant _instant;
  /**
   * The minimum quantity, inclusive.
   */
  private BigDecimal _minQuantity;
  /**
   * The maximum quantity, exclusive.
   */
  private BigDecimal _maxQuantity;
  /**
   * The security key to match.
   */
  private Identifier _securityKey;

  /**
   * Creates an instance.
   */
  public SearchPositionsRequest() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the paging request.
   * @return the paging request, not null
   */
  public PagingRequest getPagingRequest() {
    return _pagingRequest;
  }

  /**
   * Sets the paging request.
   * @param paging  the paging request, not null
   */
  public void setPagingRequest(PagingRequest paging) {
    ArgumentChecker.notNull(paging, "PagingRequest");
    _pagingRequest = paging;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the unique identifier of the parent node.
   * @return the parent node
   */
  public UniqueIdentifier getParentNode() {
    return _parentNodeUid;
  }

  /**
   * Sets the unique identifier of the parent node.
   * @param uid  the parent node
   */
  public void setParentNode(UniqueIdentifier uid) {
    _parentNodeUid = uid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the instant to search at.
   * @return the instant to search at
   */
  public Instant getInstant() {
    return _instant;
  }

  /**
   * Sets the instant to search at.
   * @param instant  the instant to search at
   */
  public void setInstant(Instant instant) {
    _instant = instant;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the minimum quantity to filter by.
   * @return the minimum quantity to filter by
   */
  public BigDecimal getMinQuantity() {
    return _minQuantity;
  }

  /**
   * Sets the minimum quantity to filter by.
   * @param minQuantity  the minimum quantity
   */
  public void setMinQuantity(BigDecimal minQuantity) {
    _minQuantity = minQuantity;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the maximum quantity to filter by.
   * @return the maximum quantity to filter by
   */
  public BigDecimal getMaxQuantity() {
    return _maxQuantity;
  }

  /**
   * Sets the maximum quantity to filter by.
   * @param maxQuantity  the maximum quantity
   */
  public void setMaxQuantity(BigDecimal maxQuantity) {
    _maxQuantity = maxQuantity;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security key to match exactly.
   * @return the security key to match exactly
   */
  public Identifier getSecurityKey() {
    return _securityKey;
  }

  /**
   * Sets the security key to match exactly.
   * @param securityKey  the security key to match exactly
   */
  public void setSecurityKey(Identifier securityKey) {
    _securityKey = securityKey;
  }

  //-------------------------------------------------------------------------
  /**
   * Validates this request throwing an exception if not.
   */
  public void checkValid() {
    Validate.isTrue(getMinQuantity() == null || getMinQuantity().compareTo(BigDecimal.ZERO) < 0, "Minimum quantity must be zero or greater");
    Validate.isTrue(getMaxQuantity() == null || getMaxQuantity().compareTo(BigDecimal.ZERO) < 0, "Maximum quantity must be zero or greater");
    Validate.isTrue(getMinQuantity() == null || getMaxQuantity() == null || getMinQuantity().compareTo(getMaxQuantity()) < 0, "Minimum quantity must be less than maximum quantity");
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
