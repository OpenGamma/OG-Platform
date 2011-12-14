/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

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

  public String getViewDefinitionUid() {
    return _uid;
  }

  public void setViewDefinitionUid(String uid) {
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
