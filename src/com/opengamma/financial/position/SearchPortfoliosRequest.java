/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import javax.time.Instant;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.PagingRequest;

/**
 * A request to search for portfolios.
 */
public final class SearchPortfoliosRequest {

  /**
   * The paging request.
   */
  private PagingRequest _pagingRequest = PagingRequest.ALL;
  /**
   * The name to search for.
   */
  private String _name;
  /**
   * The instant to search at.
   */
  private Instant _instant;
  /**
   * Whether to include deleted portfolios.
   */
  private boolean _includeDeleted;

  /**
   * Creates an instance.
   */
  public SearchPortfoliosRequest() {
  }

  /**
   * Creates an instance.
   * @param paging  the paging request, not null
   */
  public SearchPortfoliosRequest(PagingRequest paging) {
    setPagingRequest(paging);
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
   * Gets the name to search for, wildcard allowed.
   * @return the name
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name to search for, wildcard allowed.
   * @param name  the name
   */
  public void setName(String name) {
    _name = StringUtils.trim(name);
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
   * Gets whether to include deleted portfolios.
   * @return true to include deleted
   */
  public boolean isIncludeDeleted() {
    return _includeDeleted;
  }

  /**
   * Sets whether to include deleted portfolios.
   * @param includeDeleted  whether to include deleted
   */
  public void setIncludeDeleted(boolean includeDeleted) {
    _includeDeleted = includeDeleted;
  }

  //-------------------------------------------------------------------------
  /**
   * Validates this request throwing an exception if not.
   */
  public void checkValid() {
    Validate.notEmpty(getName(), "Name must not be empty");
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
