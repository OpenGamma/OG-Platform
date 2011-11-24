/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import com.opengamma.id.UniqueId;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Hibernate bean.
 */
public class ViewDefinition {

  private int _id;
  private String _uid;

  public int getId() {
    return _id;
  }

  public void setId(int id) {
    _id = id;
  }

  public String getUid() {
    return _uid;
  }

  public void setUid(String uid) {
    _uid = uid;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public String toString() {
    return "ViewDefinition : " + _uid;
  }

}
