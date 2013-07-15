/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class Attribute {

  @XmlAttribute(name = "name")
  private String _name;

  @XmlValue
  private String _value;

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public String getValue() {
    return _value;
  }

  public void setValue(String value) {
    _value = value;
  }
}
