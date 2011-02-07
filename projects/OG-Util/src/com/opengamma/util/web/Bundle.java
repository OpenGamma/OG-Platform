/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.web;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang.ObjectUtils;

/**
 * Model class to represent the bundle element in uiResourceConfig XML document
 */
public class Bundle {

  @XmlAttribute(name = "id")
  private String _id;
  @XmlElement(name = "fragment")
  private List<String> _fragment;
  
  /**
   * Gets the id field.
   * @return the id
   */
  
  public String getId() {
    return _id;
  }
  /**
   * Sets the id field.
   * @param id  the id
   */
  public void setId(String id) {
    _id = id;
  }
  /**
   * Gets the fragment field.
   * @return the fragment
   */
  
  public List<String> getFragment() {
    return _fragment;
  }
  /**
   * Sets the fragment field.
   * @param fragment  the fragment
   */
  public void setFragment(List<String> fragment) {
    _fragment = fragment;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_fragment == null) ? 0 : _fragment.hashCode());
    result = prime * result + ((_id == null) ? 0 : _id.hashCode());
    return result;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof Bundle) {
      Bundle other = (Bundle) obj;
      return ObjectUtils.equals(_id, other._id) &&
      ObjectUtils.equals(_fragment, other._fragment);
    }
    return false;
  }
  
  @Override
  public String toString() {
    return "Bundle [_id=" + _id + ", _fragment=" + _fragment + "]";
  }
 
}
