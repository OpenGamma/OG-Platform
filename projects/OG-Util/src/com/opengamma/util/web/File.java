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
 * Model representation of a file element in uiResourceConfig XML document
 */
public class File {

  @XmlAttribute(name = "id")
  private String _id;
  @XmlAttribute(name = "suffix")
  private String _suffix;
  @XmlElement(name = "bundle")
  private List<String> _bundle;
  
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
   * Gets the bundle field.
   * @return the bundle
   */
  public List<String> getBundle() {
    return _bundle;
  }
  
  /**
   * Sets the bundle field.
   * @param bundle  the bundle
   */
  public void setBundle(List<String> bundle) {
    _bundle = bundle;
  }
  
  /**
   * Gets the suffix field.
   * @return the suffix
   */
  public String getSuffix() {
    return _suffix;
  }

  /**
   * Sets the suffix field.
   * @param suffix  the suffix
   */
  public void setSuffix(String suffix) {
    _suffix = suffix;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_bundle == null) ? 0 : _bundle.hashCode());
    result = prime * result + ((_id == null) ? 0 : _id.hashCode());
    result = prime * result + ((_suffix == null) ? 0 : _suffix.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof File) {
      File other = (File) obj;
      return ObjectUtils.equals(_id, other._id) &&
              ObjectUtils.equals(_suffix, other._suffix);
    }
    return false;
  }

  @Override
  public String toString() {
    return "File [_id=" + _id + ", _suffix=" + _suffix + ", _bundle=" + _bundle + "]";
  }

}
