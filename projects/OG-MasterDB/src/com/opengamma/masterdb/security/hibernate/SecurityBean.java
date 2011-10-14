/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

import java.util.Map;

/**
 * Hibernate bean for a security.
 */
public abstract class SecurityBean {

  /**
   * The detail id.
   */
  private Long _id;
  /**
   * The security id.
   */
  private Long _securityId;

  /**
   * The security _attributes.
   */
  private Map<String, String> _attributes;

  //-------------------------------------------------------------------------
  /**
   * Gets the id.
   * @return the id
   */
  public Long getId() {
    return _id;
  }

  /**
   * Sets the id.
   * @param id  the id to set
   */
  public void setId(Long id) {
    _id = id;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security id.
   * @return the id
   */
  public Long getSecurityId() {
    return _securityId;
  }

  /**
   * Sets the security id.
   * @param securityId  the id to set
   */
  public void setSecurityId(Long securityId) {
    _securityId = securityId;
  }

  /**
   * Gets the _attributes to use for security aggregation.
   *
   * @return the _attributes, not null
   */
  public Map<String, String> getAttributes() {
    return _attributes;
  }

  /**
   * Sets the _attributes to use for security aggregation.
   *
   * @param attributes to set
   */
  public void setAttributes(Map<String, String> attributes) {
    this._attributes = attributes;
  }
}
