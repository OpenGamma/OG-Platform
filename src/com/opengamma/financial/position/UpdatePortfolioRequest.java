/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.id.UniqueIdentifier;

/**
 * A request to update the details of a portfolio.
 */
public final class UpdatePortfolioRequest {

  /**
   * The portfolio uid.
   */
  private UniqueIdentifier _uid;
  /**
   * The portfolio name.
   */
  private String _name;

  /**
   * Creates an instance.
   */
  public UpdatePortfolioRequest() {
  }

  /**
   * Creates an instance.
   * @param portfolio  the portfolio to copy, not null
   */
  public UpdatePortfolioRequest(Portfolio portfolio) {
    setUniqueIdentifier(portfolio.getUniqueIdentifier());
    setName(portfolio.getName());
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the unique identifier.
   * @return the unique identifier, not null
   */
  public UniqueIdentifier getUniqueIdentifier() {
    return _uid;
  }

  /**
   * Sets the portfolio unique identifier.
   * @param uid  the unique identifier, not null
   */
  public void setUniqueIdentifier(UniqueIdentifier uid) {
    _uid = uid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the name to change to.
   * @return the name
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name to change to.
   * @param name  the name
   */
  public void setName(String name) {
    _name = StringUtils.trim(name);
  }

  //-------------------------------------------------------------------------
  /**
   * Validates this request throwing an exception if not.
   */
  public void checkValid() {
    Validate.notNull(getUniqueIdentifier(), "UniqueIdentifier must not be null");
    Validate.isTrue(getUniqueIdentifier().isVersioned(), "UniqueIdentifier must be versioned");
    Validate.notEmpty(getName(), "Name must not be empty");
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
