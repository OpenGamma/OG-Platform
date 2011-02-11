/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.web;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.google.common.base.Objects;

/**
 * Model class to represent the bundle element in uiResourceConfig XML document.
 */
public class Bundle {

  @XmlAttribute(name = "id")
  private String _id;
  @XmlElement(name = "fragment")
  private List<String> _fragment;

  /**
   * Gets the id field.
   * 
   * @return the id
   */
  public String getId() {
    return _id;
  }

  /**
   * Sets the id field.
   * 
   * @param id  the id
   */
  public void setId(String id) {
    _id = id;
  }

  /**
   * Gets the fragment field.
   * 
   * @return the fragment
   */
  public List<String> getFragment() {
    return _fragment;
  }

  /**
   * Sets the fragment field.
   * 
   * @param fragment  the fragment
   */
  public void setFragment(List<String> fragment) {
    _fragment = fragment;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof Bundle) {
      Bundle other = (Bundle) obj;
      return Objects.equal(_fragment, other._fragment) &&
          Objects.equal(_id, other._id);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_fragment, _id);
  }

  @Override
  public String toString() {
    return "Bundle [_id=" + _id + ", _fragment=" + _fragment + "]";
  }

}
