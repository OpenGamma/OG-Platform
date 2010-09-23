/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db.hibernate;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Hibernate bean for storing an enum.
 */
public abstract class EnumBean {

  private Long _id;
  private String _name;
  
  public EnumBean() {
  }

  public EnumBean(String name) {
    _name = name;
  }
  
  public Long getId() {
    return _id;
  }
  
  public void setId(Long id) {
    _id = id;
  }
  
  public String getName() {
    return _name;
  }
  
  public void setName(String name) {
    _name = name;
  }
  
  /* subclasses will need to check class equivalence */
  public boolean equals(Object o) {
    if (!(o instanceof EnumBean)) {
      return false;
    }
    EnumBean other = (EnumBean) o;
    if (getId() != -1 && other.getId() != -1) {
      return getId().longValue() == other.getId().longValue();
    }
    return ObjectUtils.equals(other.getName(), getName());
  }
  
  public int hashCode() {
    if (_id != null) {
      return _name.hashCode();
    } else {
      return _id.intValue();
    }
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
