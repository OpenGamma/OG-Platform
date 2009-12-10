/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;


/**
 * 
 *
 * @author jim
 */
public abstract class SecurityBean {
  private Long _id;
  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    _id = id;
  }
  /**
   * @return the id
   */
  public Long getId() {
    return _id;
  }

  public abstract <T> T accept(SecurityBeanVisitor<T> visitor);
}
