/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaldata.hibernate;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Database bean for storing a name and description.
 */
public class NamedDescriptionBean {

  private Long _id;
  private String _name;
  private String _description;

  public NamedDescriptionBean() {
    super();
  }

  public NamedDescriptionBean(String name, String description) {
    _name = name;
    _description = description;
  }

  //-------------------------------------------------------------------------
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

  public String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof NamedDescriptionBean)) {
      return false;
    }
    NamedDescriptionBean other = (NamedDescriptionBean) obj;
    if (getId() != null && other.getId() != null && getId() != -1 && other.getId() != -1) {
      return getId().longValue() == other.getId().longValue();
    }
    return new EqualsBuilder().append(getName(), other.getName()).append(getDescription(), other.getDescription()).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getName()).append(getDescription()).toHashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
