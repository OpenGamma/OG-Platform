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
 * Model representation of a file element in uiResourceConfig XML document.
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
   * Gets the bundle field.
   * 
   * @return the bundle
   */
  public List<String> getBundle() {
    return _bundle;
  }

  /**
   * Sets the bundle field.
   * 
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

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof File) {
      File other = (File) obj;
      return Objects.equal(_suffix, other._suffix) &&
          Objects.equal(_id, other._id) &&
          Objects.equal(_bundle, other._bundle);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_bundle, _id, _suffix);
  }

  @Override
  public String toString() {
    return "File [_id=" + _id + ", _suffix=" + _suffix + ", _bundle=" + _bundle + "]";
  }

}
