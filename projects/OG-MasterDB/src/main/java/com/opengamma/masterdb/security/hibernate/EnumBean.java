/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

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

  public EnumBean(final String name) {
    _name = name;
  }

  public Long getId() {
    return _id;
  }

  public void setId(final Long id) {
    _id = id;
  }

  public String getName() {
    return _name;
  }

  public void setName(final String name) {
    _name = name;
  }

  /* subclasses will need to check class equivalence */
  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof EnumBean)) {
      return false;
    }
    final EnumBean other = (EnumBean) o;
    if (getId() != -1 && other.getId() != -1) {
      return getId().longValue() == other.getId().longValue();
    }
    return ObjectUtils.equals(other.getName(), getName());
  }

  @Override
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
